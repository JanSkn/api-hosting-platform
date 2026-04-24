package com.hosting.common.config;

import java.net.URI;
import software.amazon.awssdk.regions.Region;

public final class GlobalConfig {

  private GlobalConfig() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }

  public static final String ENVIRONMENT = System.getenv("ENV");
  public static final Region AWS_REGION = Region.of(System.getenv("AWS_REGION"));

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

  public static boolean isLocal() {
    return "local".equalsIgnoreCase(ENVIRONMENT);
  }
}
