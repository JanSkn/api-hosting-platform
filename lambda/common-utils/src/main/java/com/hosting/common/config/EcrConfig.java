package com.hosting.common.config;

public final class EcrConfig extends BaseConfig {

  public static final String REPOSITORY_URI = getOrThrow("ECR_REPOSITORY_URI");
}
