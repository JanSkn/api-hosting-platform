package com.hosting.deployer.service;

import com.hosting.common.aws.DeploymentService;
import com.hosting.common.aws.repositories.LambdaDeploymentRepository;
import com.hosting.common.config.EcrConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeploymentManagerService {

  private static final Logger LOGGER = LoggerFactory.getLogger(DeploymentManagerService.class);
  private final LambdaDeploymentRepository lambdaRepository;
  private final DeploymentService deploymentService;

  public DeploymentManagerService(
      LambdaDeploymentRepository lambdaRepository, DeploymentService deploymentService) {
    this.lambdaRepository = lambdaRepository;
    this.deploymentService = deploymentService;
  }

  public void deploy(String userId, String deploymentId, String imageTag, String accountId) {
    LOGGER.info("Starting API deployment");

    String repositoryUri = EcrConfig.REPOSITORY_URI;
    String imageUri = repositoryUri + ":" + imageTag;

    lambdaRepository.createFunction(deploymentId, imageUri, accountId);
    String functionUrl = lambdaRepository.setupFunctionUrl(deploymentId);

    deploymentService.setApiUri(userId, deploymentId, functionUrl);

    LOGGER.info("Successfully completed API deployment");
  }
}
