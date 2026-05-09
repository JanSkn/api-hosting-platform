package com.hosting.common.config;

public final class CodeBuildConfig extends BaseConfig {

  public static final String PROJECT_NAME = getOrThrow("CODEBUILD_PROJECT_NAME");
}
