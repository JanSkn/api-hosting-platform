package com.hosting.common.config;

public final class DynamoConfig {

  private DynamoConfig() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }

  public static final String DEPLOYMENTS_METADATA_TABLE =
      System.getenv("DEPLOYMENTS_METADATA_TABLE");
}
