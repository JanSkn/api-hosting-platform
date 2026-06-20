package com.hosting.common.config;

public final class CodeBuildLogConfig extends BaseConfig {
  public static final String LOG_GROUP = getOrThrow("CODEBUILD_LOG_GROUP");
}
