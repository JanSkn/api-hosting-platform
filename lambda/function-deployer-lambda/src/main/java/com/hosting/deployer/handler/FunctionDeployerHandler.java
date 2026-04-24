package com.hosting.deployer.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hosting.common.aws.ClientProducer;
import com.hosting.common.aws.DeploymentService;
import com.hosting.common.aws.eventbridge.models.BuildEventDetail;
import com.hosting.common.aws.repositories.DeploymentMetadataRepository;
import com.hosting.common.config.EcrConfig;
import com.hosting.common.config.UserLambdaConfig;
import com.hosting.common.logging.LoggingConfig;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.*;

public class FunctionDeployerHandler implements RequestHandler<Map<String, Object>, Void> {

  private static final Logger LOGGER = LoggerFactory.getLogger(FunctionDeployerHandler.class);
  private final ObjectMapper objectMapper = new ObjectMapper();

  private final LambdaClient lambdaClient;
  private final DeploymentService deploymentService;

  public FunctionDeployerHandler() {
    ClientProducer clientProducer = new ClientProducer();
    this.lambdaClient = clientProducer.lambdaClient();
    this.deploymentService =
        new DeploymentService(new DeploymentMetadataRepository(clientProducer.dynamoDbClient()));
  }

  @Override
  public Void handleRequest(Map<String, Object> event, Context context) {
    @SuppressWarnings("unchecked")
    Map<String, Object> detail = (Map<String, Object>) event.get("detail");

    BuildEventDetail buildEventDetail = objectMapper.convertValue(detail, BuildEventDetail.class);

    String userId = buildEventDetail.userId();
    String deploymentId = buildEventDetail.deploymentId();
    String correlationId = buildEventDetail.correlationId();

    LoggingConfig.putAll(
        LoggingConfig.AWS_REQUEST_ID_MDC_KEY,
        context.getAwsRequestId(),
        LoggingConfig.USER_ID_MDC_KEY,
        userId,
        LoggingConfig.DEPLOYMENT_ID_MDC_KEY,
        deploymentId,
        LoggingConfig.CORRELATION_ID_MDC_KEY,
        correlationId);

    try {
      LOGGER.info("Processing event for user API deployment");

      String imageTag = buildEventDetail.imageTag();

      String functionName = "app-" + deploymentId;
      String repositoryUri = EcrConfig.REPOSITORY_URI;
      String imageUri = repositoryUri + ":" + imageTag;

      deployFunction(functionName, imageUri, context);

      String functionUrl = setupFunctionUrl(functionName);
      deploymentService.setApiUri(userId, deploymentId, functionUrl);

      LOGGER.info("Successfully deployed user API", functionName, functionUrl);

    } catch (Exception e) {
      LOGGER.error("Failed to deploy user API", e);
      throw new RuntimeException("Deployment of user API failed", e);
    }
    return null;
  }

  private void deployFunction(String functionName, String imageUri, Context context) {
    String accountId = context.getInvokedFunctionArn().split(":")[4];
    String roleArn = String.format("arn:aws:iam::%s:role/UserFunctionRole", accountId);

    try {
    lambdaClient.createFunction(
        CreateFunctionRequest.builder()
            .functionName(functionName)
            .packageType(UserLambdaConfig.PACKAGE_TYPE)
            .code(FunctionCode.builder().imageUri(imageUri).build())
            .role(roleArn)
            .timeout(UserLambdaConfig.TIMEOUT_SECONDS)
            .memorySize(UserLambdaConfig.MEMORY_SIZE_MB)
            .architectures(UserLambdaConfig.ARCHITECTURE)
            .build());
    } catch (Exception e) {
      LOGGER.error("Error creating Lambda function", e);
      throw new RuntimeException("Error creating Lambda function: " + e.getMessage(), e);
    }
    LOGGER.info("Created new lambda function: {}", functionName);
  }

  private String setupFunctionUrl(String functionName) {
    CreateFunctionUrlConfigResponse response =
        lambdaClient.createFunctionUrlConfig(
            CreateFunctionUrlConfigRequest.builder()
                .functionName(functionName)
                .authType(FunctionUrlAuthType.NONE)
                .build());
    String url = response.functionUrl();

    LOGGER.info("Created new Function URL: {}", url);

    lambdaClient.addPermission(
        AddPermissionRequest.builder()
            .functionName(functionName)
            .statementId("PublicFunctionUrlAccess")
            .action("lambda:InvokeFunctionUrl")
            .principal("*")
            .functionUrlAuthType(FunctionUrlAuthType.NONE)
            .build());
    LOGGER.info("Added public access permission to Function URL");

    return url;
  }
}
