package com.hosting.deployer.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.hosting.common.aws.ClientProducer;
import com.hosting.common.aws.DeploymentService;
import com.hosting.common.aws.repositories.DeploymentMetadataRepository;
import com.hosting.common.aws.repositories.LambdaDeploymentRepository;
import com.hosting.common.logging.LoggingConfig;
import com.hosting.deployer.service.DeploymentManagerService;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FunctionDeployerHandler implements RequestHandler<Map<String, Object>, Void> {

  private static final Logger LOGGER = LoggerFactory.getLogger(FunctionDeployerHandler.class);
  private final DeploymentManagerService deploymentManagerService;

  public FunctionDeployerHandler() {
    ClientProducer clientProducer = new ClientProducer();
    LambdaDeploymentRepository lambdaRepository =
        new LambdaDeploymentRepository(
            clientProducer.lambdaClient(), clientProducer.cloudWatchLogsClient());
    DeploymentService deploymentService =
        new DeploymentService(new DeploymentMetadataRepository(clientProducer.dynamoDbClient()));

    this.deploymentManagerService =
        new DeploymentManagerService(lambdaRepository, deploymentService);
  }

  @Override
  public Void handleRequest(Map<String, Object> event, Context context) {
    try {
      @SuppressWarnings("unchecked")
      Map<String, Object> detail = (Map<String, Object>) event.get("detail");
      @SuppressWarnings("unchecked")
      Map<String, Object> additionalInfo =
          (Map<String, Object>) detail.get("additional-information");
      @SuppressWarnings("unchecked")
      Map<String, Object> environment = (Map<String, Object>) additionalInfo.get("environment");
      @SuppressWarnings("unchecked")
      List<Map<String, String>> envVars =
          (List<Map<String, String>>) environment.get("environment-variables");

      String userId = getEnvVar(envVars, "USER_ID");
      String deploymentId = getEnvVar(envVars, "DEPLOYMENT_ID");
      String correlationId = getEnvVar(envVars, "CORRELATION_ID");
      String imageTag = getEnvVar(envVars, "IMAGE_TAG");

      LoggingConfig.putAll(
          LoggingConfig.AWS_REQUEST_ID_MDC_KEY, context.getAwsRequestId(),
          LoggingConfig.USER_ID_MDC_KEY, userId,
          LoggingConfig.DEPLOYMENT_ID_MDC_KEY, deploymentId,
          LoggingConfig.CORRELATION_ID_MDC_KEY, correlationId);

      LOGGER.info("Processing CodeBuild SUCCEEDED event");

      deploymentManagerService.deploy(userId, deploymentId, imageTag);

    } catch (Exception e) {
      LOGGER.error("Failed to process deployment event", e);
      throw new RuntimeException("Deployment failed", e);
    }
    return null;
  }

  private String getEnvVar(List<Map<String, String>> envVars, String key) {
    return envVars.stream()
        .filter(v -> key.equals(v.get("name")))
        .map(v -> v.get("value"))
        .findFirst()
        .orElseThrow(
            () ->
                new RuntimeException(
                    "Required environment variable not found in CodeBuild event: " + key));
  }
}
