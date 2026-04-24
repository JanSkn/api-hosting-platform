package com.hosting.common.config;

public final class DeploymentConfig {

  private DeploymentConfig() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }

  public static final int MAX_PER_USER =
      Integer.parseInt(System.getenv("MAX_DEPLOYMENTS_PER_USER"));
}
