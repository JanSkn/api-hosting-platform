package com.hosting.common.config;

public final class DeploymentConfig extends BaseConfig {

  public static final int MAX_PER_USER = Integer.parseInt(getOrThrow("MAX_DEPLOYMENTS_PER_USER"));
}
