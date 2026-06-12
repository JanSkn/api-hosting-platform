package com.hosting.common.aws.repositories;

import com.hosting.common.config.UserLambdaConfig;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.CreateLogGroupRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DeleteLogGroupRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutRetentionPolicyRequest;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.*;

@ApplicationScoped
public class LambdaDeploymentRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(LambdaDeploymentRepository.class);

  // TODO: Retention for user-function logs.
  // Future feature: Provide users with the logs.
  private static final int LOG_RETENTION_DAYS = 1;

  private final LambdaClient lambdaClient;
  private final CloudWatchLogsClient logsClient;

  @Inject
  public LambdaDeploymentRepository(LambdaClient lambdaClient, CloudWatchLogsClient logsClient) {
    this.lambdaClient = lambdaClient;
    this.logsClient = logsClient;
  }

  private String getFunctionName(String deploymentId) {
    return "app-" + deploymentId;
  }

  private String getLogGroupName(String functionName) {
    // Lambda writes to this conventional log group name; pre-creating it lets us control retention.
    return "/aws/lambda/" + functionName;
  }

  public void createFunction(String deploymentId, String imageUri) {
    String roleArn = UserLambdaConfig.FUNCTION_URL_ROLE_ARN;
    String functionName = getFunctionName(deploymentId);

    // Pre-create the log group. Otherwise Lambda auto-creates it on first
    // invocation with "never expire", and the logs of untrusted user code accrue storage cost
    // forever.
    setupLogGroup(functionName);

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
      LOGGER.info("Created new lambda function: {}", functionName);
    } catch (Exception e) {
      LOGGER.error("Error creating Lambda function: {}", functionName, e);
      throw new RuntimeException("Error creating Lambda function: " + e.getMessage(), e);
    }
  }

  public String setupFunctionUrl(String deploymentId) {
    String functionName = getFunctionName(deploymentId);

    try {
      CreateFunctionUrlConfigResponse response =
          lambdaClient.createFunctionUrlConfig(
              CreateFunctionUrlConfigRequest.builder()
                  .functionName(functionName)
                  .authType(FunctionUrlAuthType.NONE)
                  .build());
      String url = response.functionUrl();

      LOGGER.info("Created new Function URL: {}", url);

      // see https://docs.aws.amazon.com/lambda/latest/dg/urls-auth.html; both permissions required
      lambdaClient.addPermission(
          AddPermissionRequest.builder()
              .functionName(functionName)
              .statementId("PublicFunctionUrlAccess")
              .action("lambda:InvokeFunctionUrl")
              .principal("*")
              .functionUrlAuthType(FunctionUrlAuthType.NONE)
              .build());
      LOGGER.info("Added public access permission to Function URL for {}", functionName);
      lambdaClient.addPermission(
          AddPermissionRequest.builder()
              .functionName(functionName)
              .statementId("PublicFunctionInvokeAccess")
              .action("lambda:InvokeFunction")
              .principal("*")
              .functionUrlAuthType(FunctionUrlAuthType.NONE)
              .build());
      LOGGER.info("Added public invoke permission to Function for {}", functionName);

      return url;
    } catch (Exception e) {
      LOGGER.error("Error setting up Function URL for: {}", functionName, e);
      throw new RuntimeException("Error setting up Function URL", e);
    }
  }

  public void deleteFunction(String deploymentId) {
    String functionName = getFunctionName(deploymentId);

    try {
      DeleteFunctionRequest deleteRequest =
          DeleteFunctionRequest.builder().functionName(functionName).build();
      lambdaClient.deleteFunction(deleteRequest);
      LOGGER.info("Successfully deleted lambda function: {}", functionName);
    } catch (ResourceNotFoundException e) {
      LOGGER.warn("Lambda function {} not found, skipping function deletion", functionName);
    } catch (Exception e) {
      LOGGER.error("Failed to delete lambda function: {}", functionName, e);
    }

    deleteLogGroup(functionName);
  }

  /**
   * Failures are logged but not rethrown — a missing retention policy must not block a deployment.
   */
  private void setupLogGroup(String functionName) {
    String logGroupName = getLogGroupName(functionName);

    try {
      logsClient.createLogGroup(CreateLogGroupRequest.builder().logGroupName(logGroupName).build());
      LOGGER.info("Created log group: {}", logGroupName);
    } catch (Exception e) {
      LOGGER.error("Failed to create log group: {}", logGroupName, e);
      return;
    }

    try {
      logsClient.putRetentionPolicy(
          PutRetentionPolicyRequest.builder()
              .logGroupName(logGroupName)
              .retentionInDays(LOG_RETENTION_DAYS)
              .build());
      LOGGER.info("Set {}-day retention on log group: {}", LOG_RETENTION_DAYS, logGroupName);
    } catch (Exception e) {
      LOGGER.error("Failed to set retention on log group: {}", logGroupName, e);
    }
  }

  private void deleteLogGroup(String functionName) {
    String logGroupName = getLogGroupName(functionName);

    try {
      logsClient.deleteLogGroup(DeleteLogGroupRequest.builder().logGroupName(logGroupName).build());
      LOGGER.info("Successfully deleted log group: {}", logGroupName);
    } catch (software.amazon.awssdk.services.cloudwatchlogs.model.ResourceNotFoundException e) {
      LOGGER.warn("Log group {} not found, skipping log group deletion", logGroupName);
    } catch (Exception e) {
      LOGGER.error("Failed to delete log group: {}", logGroupName, e);
    }
  }
}
