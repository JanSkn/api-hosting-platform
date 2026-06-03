package com.hosting.common.aws.repositories;

import com.hosting.common.config.UserLambdaConfig;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.*;

@ApplicationScoped
public class LambdaDeploymentRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(LambdaDeploymentRepository.class);
  private final LambdaClient lambdaClient;

  @Inject
  public LambdaDeploymentRepository(LambdaClient lambdaClient) {
    this.lambdaClient = lambdaClient;
  }

  private String getFunctionName(String deploymentId) {
    return "app-" + deploymentId;
  }

  public void createFunction(String deploymentId, String imageUri, String accountId) {
    String roleArn = String.format("arn:aws:iam::%s:role/UserFunctionRole", accountId);
    String functionName = getFunctionName(deploymentId);

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

      lambdaClient.addPermission(
          AddPermissionRequest.builder()
              .functionName(functionName)
              .statementId("PublicFunctionUrlAccess")
              .action("lambda:InvokeFunctionUrl")
              .principal("*")
              .functionUrlAuthType(FunctionUrlAuthType.NONE)
              .build());
      LOGGER.info("Added public access permission to Function URL for {}", functionName);

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
  }
}
