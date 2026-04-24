package com.hosting.common.config;

public final class CodeBuildConfig {

  private CodeBuildConfig() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }

  public static final String PROJECT_NAME = System.getenv("CODEBUILD_PROJECT_NAME");
}
