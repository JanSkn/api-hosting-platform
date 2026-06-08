package com.hosting.common.aws.repositories;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hosting.common.aws.dynamo.models.Deployment;
import com.hosting.common.aws.sqs.models.BuildMessage;
import com.hosting.common.config.SqsConfig;
import com.hosting.common.exceptions.SQSBuildJobNotEnqueuedException;
import com.hosting.common.logging.LoggingConfig;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@ApplicationScoped
public class BuildQueueRepository {
  private static final Logger LOGGER = LoggerFactory.getLogger(BuildQueueRepository.class);
  private SqsClient sqsClient;
  private ObjectMapper objectMapper;

  @Inject
  public BuildQueueRepository(SqsClient sqsClient, ObjectMapper objectMapper) {
    this.sqsClient = sqsClient;
    this.objectMapper = objectMapper;
  }

  public void pushToBuildQueue(Deployment deployment) {
    try {
      String correlationId = MDC.get(LoggingConfig.CORRELATION_ID_MDC_KEY);

      BuildMessage message =
          new BuildMessage(
              deployment.getDeploymentId(),
              deployment.getUserId(),
              deployment.getRuntime(),
              deployment.getS3ObjectKey(),
              correlationId);
      String jsonMessage = objectMapper.writeValueAsString(message);

      SendMessageRequest.Builder builder =
          SendMessageRequest.builder()
              .queueUrl(SqsConfig.BUILD_QUEUE_URL.toString())
              .messageBody(jsonMessage)
              // for FIFO:
              .messageGroupId(deployment.getUserId())
              .messageDeduplicationId(deployment.getDeploymentId());

      LOGGER.info("Enqueuing build job to SQS");

      sqsClient.sendMessage(builder.build());
    } catch (Exception e) {
      LOGGER.error("Failed to enqueue build job in SQS", e);
      throw new SQSBuildJobNotEnqueuedException("Failed to enqueue build job in SQS", e);
    }
  }
}
