package com.hosting.common.config;

import java.net.URI;
import software.amazon.awssdk.regions.Region;

public class ProjectConfig {
  public static final String ENVIRONMENT = System.getenv("ENV");
  public static final Region AWS_REGION = Region.of(System.getenv("AWS_REGION"));
  public static final URI AWS_LOCAL_ENDPOINT = URI.create("http://localhost.localstack.cloud:4566");

  public static class Deployment {
    public static final int MAX_PER_USER =
        Integer.parseInt(System.getenv("MAX_DEPLOYMENTS_PER_USER"));
  }

  public static class DynamoDB {
    public static final String DEPLOYMENTS_METADATA_TABLE = System.getenv("DEPLOYMENTS_METADATA_TABLE");
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
