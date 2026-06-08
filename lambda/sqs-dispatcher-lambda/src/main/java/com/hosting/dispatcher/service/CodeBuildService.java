package com.hosting.dispatcher.service;

import com.hosting.common.aws.DeploymentService;
import com.hosting.common.aws.sqs.models.BuildMessage;
import com.hosting.dispatcher.repository.CodeBuildRepository;
import software.amazon.awssdk.services.codebuild.CodeBuildClient;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;

public class CodeBuildService {

  private final CodeBuildRepository codeBuildRepository;
  private final DeploymentService deploymentService;

  public CodeBuildService(
      CodeBuildClient codeBuildClient,
      DeploymentService deploymentService,
      EventBridgeClient eventBridgeClient) {
    this.codeBuildRepository = new CodeBuildRepository(codeBuildClient, eventBridgeClient);
    this.deploymentService = deploymentService;
  }

  public void startBuild(BuildMessage buildMessage) {
    String imageTag =
        codeBuildRepository.generateImageTag(buildMessage.userId(), buildMessage.deploymentId());
    String buildId = codeBuildRepository.startBuildJob(buildMessage, imageTag);

    deploymentService.addBuildId(buildMessage.userId(), buildMessage.deploymentId(), buildId);
  }
}
