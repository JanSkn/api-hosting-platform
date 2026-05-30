package com.hosting.common.config;

public final class S3Config extends BaseConfig {

  public static final String USER_CODE_BUCKET = getOrThrow("USER_CODE_BUCKET");
  public static final String USER_CODE_PREFIX = getOrThrow("USER_CODE_BUCKET_PREFIX");
  public static final String DOCKERFILE_TEMPLATE_BUCKET = getOrThrow("DOCKERFILE_TEMPLATE_BUCKET");
  public static final long PRESIGNED_EXPIRATION_SECONDS = 300;
}
