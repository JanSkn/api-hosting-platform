package com.hosting.common.aws.repositories;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hosting.common.aws.dynamo.models.Deployment;
import com.hosting.common.aws.sqs.models.BuildMessage;
import com.hosting.common.config.ProjectConfig;
import com.hosting.common.exceptions.SQSBuildJobNotEnqueuedException;
import jakarta.inject.Inject;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

public class BuildQueueRepository {
  private final SqsClient sqsClient;
  private final ObjectMapper objectMapper;

  @Inject
  public BuildQueueRepository(SqsClient sqsClient, ObjectMapper objectMapper) {
    this.sqsClient = sqsClient;
    this.objectMapper = objectMapper;
  }

  public void pushToBuildQueue(Deployment deployment, String s3ObjectKey) {
    try {
      BuildMessage message =
          new BuildMessage(deployment.getDeploymentId(), deployment.getUserId(), s3ObjectKey);
      String jsonMessage = objectMapper.writeValueAsString(message);

      SendMessageRequest sendMsgRequest =
          SendMessageRequest.builder()
              .queueUrl(ProjectConfig.SQS.BUILD_QUEUE_URL.toString())
              .messageBody(jsonMessage)
              // for FIFO:
              .messageGroupId(deployment.getUserId())
              .messageDeduplicationId(deployment.getDeploymentId())
              .build();

      sqsClient.sendMessage(sendMsgRequest);
    } catch (Exception e) {
      throw new SQSBuildJobNotEnqueuedException();
    }
  }
}
