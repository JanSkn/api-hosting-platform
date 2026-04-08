package com.hosting.common.aws;

import com.hosting.common.aws.dynamo.models.Deployment;
import com.hosting.common.aws.repositories.BuildQueueRepository;
import com.hosting.common.aws.repositories.DeploymentMetadataRepository;
import com.hosting.common.aws.repositories.UserCodeRepository;
import com.hosting.common.config.ProjectConfig;
import com.hosting.common.dto.CreateDeploymentRequest;
import com.hosting.common.dto.UploadUrlResponse;
import com.hosting.common.enums.DeploymentEnums.Status;
import com.hosting.common.exceptions.GitHubDownloadException;
import com.hosting.common.exceptions.InvalidGitHubUrlException;
import com.hosting.common.exceptions.UserCodeNotUploadedException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
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

  public void setDeploymentStatus(String userId, String deploymentId, Status status) {
    Optional<Deployment> deploymentOpt = getDeployment(userId, deploymentId);
    if (deploymentOpt.isPresent()) {
      Deployment deployment = deploymentOpt.get();
      deployment.setStatus(status);
      deploymentMetadata.update(deployment);
    }
  }

  public UploadUrlResponse generateUploadUrl(String userId, String deploymentId) {
    String uploadUrl = userCode.generatePresignedUploadUrl(userId, deploymentId);
    return new UploadUrlResponse(uploadUrl, ProjectConfig.S3.PRESIGNED_EXPIRATION_SECONDS);
  }

  // we don't set createdAt here because we will set it after deployment completed
  public String initializeDeployment(String userId, CreateDeploymentRequest request) {
    String deploymentId = java.util.UUID.randomUUID().toString();
    Deployment deployment = new Deployment();
    deployment.setUserId(userId);
    deployment.setDeploymentId(deploymentId);
    deployment.setName(request.name());
    deployment.setRuntime(request.runtime());
    deployment.setGithubUrl(request.githubUrl());
    deployment.setS3ObjectKey(userCode.generateObjectKey(userId, deploymentId));
    deployment.setStatus(Status.INITIALIZED);
    deploymentMetadata.put(deployment); // will fail if more than allowed deployments per user

    return deploymentId;
  }

  public void triggerDeployment(String userId, String deploymentId) {
    Deployment deployment = deploymentMetadata.get(userId, deploymentId).orElseThrow();
    boolean isGithubDeployment =
        deployment.getGithubUrl() != null && !deployment.getGithubUrl().isEmpty();

    if (isGithubDeployment) {
      downloadAndUploadFromGithub(userId, deployment);
    }

    if (isGithubDeployment && !userCode.doesObjectExist(userId, deploymentId)) {
      throw new UserCodeNotUploadedException();
    }

    deployment.setStatus(Status.IN_PROGRESS);
    deploymentMetadata.update(deployment);
    buildQueue.pushToBuildQueue(deployment);
  }

  private void downloadAndUploadFromGithub(String userId, Deployment deployment) {
    String githubUrl = deployment.getGithubUrl();
    // URL parsing: https://github.com/owner/repo
    String owner = "";
    String repo = "";

    String path = githubUrl.replace("https://github.com/", "");
    String[] parts = path.split("/");
    if (parts.length >= 2) {
      owner = parts[0];
      repo = parts[1].replace(".git", "");
    }

    if (owner.isEmpty() || repo.isEmpty()) {
      throw new InvalidGitHubUrlException(githubUrl);
    }

    String zipUrl = String.format("https://api.github.com/repos/%s/%s/zipball", owner, repo);

    try (HttpClient client =
        HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .connectTimeout(Duration.ofSeconds(10))
            .build()) {

      HttpRequest request =
          HttpRequest.newBuilder().uri(URI.create(zipUrl)).timeout(Duration.ofSeconds(60)).build();

      HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());

      if (response.statusCode() != 200) {
        throw new GitHubDownloadException(
            "Failed to fetch ZIP from GitHub. Status: "
                + response.statusCode()
                + " Body: "
                + new String(response.body()));
      }

      userCode.uploadUserCode(userId, deployment.getDeploymentId(), response.body());
    } catch (InvalidGitHubUrlException e) {
      throw e;
    } catch (Exception e) {
      throw new GitHubDownloadException("Error downloading from GitHub: " + e.getMessage(), e);
    }
  }

  public void deleteDeployment(String userId, String deploymentId) {
    deploymentMetadata.delete(userId, deploymentId);
    userCode.deleteUserCode(userId, deploymentId);
  }

  public void deleteDeployments(String userId) {
    Optional<List<Deployment>> deploymentsOpt = getDeployments(userId);
    if (deploymentsOpt.isPresent()) {
      for (Deployment deployment : deploymentsOpt.get()) {
        deleteDeployment(userId, deployment.getDeploymentId());
      }
    }
  }
}
