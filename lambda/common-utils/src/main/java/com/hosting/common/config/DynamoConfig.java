package com.hosting.common.config;

public final class DynamoConfig extends BaseConfig {

  public static final String DEPLOYMENTS_METADATA_TABLE = getOrThrow("DEPLOYMENTS_METADATA_TABLE");
}
