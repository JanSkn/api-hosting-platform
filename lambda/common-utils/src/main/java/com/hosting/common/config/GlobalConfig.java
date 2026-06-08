package com.hosting.common.config;

import java.net.URI;
import software.amazon.awssdk.regions.Region;

public final class GlobalConfig extends BaseConfig {

  public static final String ENVIRONMENT = getOrThrow("ENV");
  public static final Region AWS_REGION = Region.of(getOrThrow("_AWS_REGION"));
  public static final LambdaEnvType LAMBDA_ENVIRONMENT =
      LambdaEnvType.valueOf(getOrThrow("LAMBDA_ENVIRONMENT"));

  /**
   * INTERNAL LOCAL ENDPOINT: Used by the Lambda function code to talk to LocalStack services.
   * Inside the SAM/Docker container, 'localhost' points to the container itself, NOT LocalStack.
   * 'localhost.localstack.cloud' is a special DNS name provided by LocalStack that resolves
   * correctly to the LocalStack host from within the Docker network.
   */
  public static final URI AWS_LOCAL_INTERNAL_ENDPOINT =
      URI.create("http://localhost.localstack.cloud:4566");

  /** EXTERNAL LOCAL ENDPOINT: External endpoint accessed on the host machine. */
  public static final URI AWS_LOCAL_EXTERNAL_ENDPOINT = URI.create("http://localhost:4566");

  /** The container name of LocalStack as defined in docker-compose.yml */
  public static final String LOCALSTACK_CONTAINER_NAME = "localstack-main";

  /**
   * Utility method to check if the current environment is emulated using LocalStack (either 'local'
   * or 'testing').
   */
  public static boolean isLocalStack() {
    return "local".equalsIgnoreCase(ENVIRONMENT) || "testing".equalsIgnoreCase(ENVIRONMENT);
  }
}
