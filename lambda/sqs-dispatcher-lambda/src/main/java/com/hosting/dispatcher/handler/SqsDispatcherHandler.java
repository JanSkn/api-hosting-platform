package com.hosting.dispatcher.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hosting.common.aws.ClientProducer;
import com.hosting.common.aws.DeploymentService;
import com.hosting.common.aws.repositories.DeploymentMetadataRepository;
import com.hosting.common.aws.sqs.models.BuildMessage;
import com.hosting.common.logging.LoggingConfig;
import com.hosting.dispatcher.service.CodeBuildService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.services.codebuild.CodeBuildClient;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;

public class SqsDispatcherHandler implements RequestHandler<SQSEvent, Void> {

  private static final Logger LOGGER = LoggerFactory.getLogger(SqsDispatcherHandler.class);
  ClientProducer clientProducer = new ClientProducer();
  private final CodeBuildClient codeBuildClient = clientProducer.codeBuildClient();
  private final EventBridgeClient eventBridgeClient = clientProducer.eventBridgeClient();
  private final DynamoDbEnhancedClient dynamoDbClient = clientProducer.dynamoDbClient();
  private final DeploymentMetadataRepository deploymentMetadataRepository =
      new DeploymentMetadataRepository(dynamoDbClient);
  private final DeploymentService deploymentService =
      new DeploymentService(deploymentMetadataRepository);
  private final CodeBuildService codeBuildService =
      new CodeBuildService(codeBuildClient, eventBridgeClient, deploymentService);
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public Void handleRequest(SQSEvent sqsEvent, Context context) {
    for (SQSMessage message : sqsEvent.getRecords()) {
      try {
        String messageId = message.getMessageId();
        String messageBody = message.getBody();
        BuildMessage buildMessage = objectMapper.readValue(messageBody, BuildMessage.class);

        String correlationId = buildMessage.correlationId();

        LoggingConfig.putAll(
            LoggingConfig.SQS_MESSAGE_ID_MDC_KEY, messageId,
            LoggingConfig.CORRELATION_ID_MDC_KEY, correlationId,
            LoggingConfig.AWS_REQUEST_ID_MDC_KEY, context.getAwsRequestId(),
            LoggingConfig.USER_ID_MDC_KEY, buildMessage.userId(),
            LoggingConfig.DEPLOYMENT_ID_MDC_KEY, buildMessage.deploymentId());

        LOGGER.info("Processing SQS message");
        codeBuildService.startBuild(buildMessage);
        LOGGER.info("Successfully started CodeBuild process");
      } catch (Exception e) {
        LOGGER.error("Error processing SQS message: {}", e.getMessage(), e);
      } finally {
        LoggingConfig.clear();
      }
    }
    return null;
  }
}
