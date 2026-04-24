package com.hosting.common.config;

public final class EcrConfig {

  private EcrConfig() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }

  public static final String REPOSITORY_URI = System.getenv("ECR_REPOSITORY_URI");
}
