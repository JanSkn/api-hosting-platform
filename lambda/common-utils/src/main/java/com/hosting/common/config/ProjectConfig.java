package com.hosting.common.config;

import java.net.URI;
import software.amazon.awssdk.regions.Region;

public class ProjectConfig {
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

  public static class Deployment {
    public static final int MAX_PER_USER =
        Integer.parseInt(System.getenv("MAX_DEPLOYMENTS_PER_USER"));
  }

  public static class DynamoDB {
    public static final String DEPLOYMENTS_METADATA_TABLE =
        System.getenv("DEPLOYMENTS_METADATA_TABLE");
  }

  public static class S3 {
    public static final String USER_CODE_BUCKET = System.getenv("USER_CODE_BUCKET");
    public static final String USER_CODE_PREFIX = System.getenv("USER_CODE_BUCKET_PREFIX");
    public static final long PRESIGNED_EXPIRATION_SECONDS = 300;
  }

  public static class SQS {
    public static final URI BUILD_QUEUE_URL = URI.create(System.getenv("BUILD_QUEUE_URL"));
  }

  public static boolean isLocal() {
    return "local".equalsIgnoreCase(ENVIRONMENT);
  }
}
