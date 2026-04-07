package com.hosting.common.aws;

import com.hosting.common.aws.dynamo.models.Deployment;
import com.hosting.common.aws.repositories.BuildQueueRepository;
import com.hosting.common.aws.repositories.DeploymentMetadataRepository;
import com.hosting.common.aws.repositories.UserCodeRepository;
import com.hosting.common.config.ProjectConfig;
import com.hosting.common.dto.UploadUrlResponse;
import com.hosting.common.enums.DeploymentEnums.Status;
import com.hosting.common.exceptions.UserCodeNotUploadedException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class DeploymentService {

  public DeploymentMetadataRepository deploymentMetadata;
  public UserCodeRepository userCode;
  public BuildQueueRepository buildQueue;

  @Inject
  public DeploymentService(
      DeploymentMetadataRepository deploymentRepository,
      UserCodeRepository userCodeRepository,
      BuildQueueRepository jobQueueRepository) {
    this.deploymentMetadata = deploymentRepository;
    this.userCode = userCodeRepository;
    this.buildQueue = jobQueueRepository;
  }

  public Optional<Deployment> getDeployment(String userId, String deploymentId) {
    return deploymentMetadata.get(userId, deploymentId);
  }

  public Optional<List<Deployment>> getDeployments(String userId) {
    return deploymentMetadata.getByUserId(userId);
  }

  public Optional<String> getDeploymentStatus(String userId, String deploymentId) {
    Optional<Deployment> deploymentOpt = getDeployment(userId, deploymentId);
    return deploymentOpt.map(Deployment::getStatus).map(Status::toString);
  }

  public UploadUrlResponse initializeDeployment(String userId) {
    String deploymentId = java.util.UUID.randomUUID().toString();
    Deployment deployment = new Deployment();
    deployment.setUserId(userId);
    deployment.setDeploymentId(deploymentId);
    deployment.setStatus(Status.UPLOADING);
    deploymentMetadata.put(deployment); // will fail if more than allowed deployments per user

    String uploadUrl = userCode.generatePresignedUploadUrl(userId, deploymentId);
    return new UploadUrlResponse(
        uploadUrl, deploymentId, ProjectConfig.S3.PRESIGNED_EXPIRATION_SECONDS);
  }

  public void triggerDeployment(String userId, String deploymentId) {
    if (!userCode.doesObjectExist(userId, deploymentId)) {
      throw new UserCodeNotUploadedException();
    }

    Deployment updatedDeployment = deploymentMetadata.get(userId, deploymentId).orElseThrow();
    updatedDeployment.setStatus(Status.IN_PROGRESS);
    deploymentMetadata.update(updatedDeployment);
    buildQueue.pushToBuildQueue(
        updatedDeployment, userCode.generateObjectKey(userId, deploymentId));
  }
}
