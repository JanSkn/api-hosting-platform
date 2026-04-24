package com.hosting.common.config;

public final class S3Config {

  private S3Config() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }

  public static final String USER_CODE_BUCKET = System.getenv("USER_CODE_BUCKET");
  public static final String USER_CODE_PREFIX = System.getenv("USER_CODE_BUCKET_PREFIX");
  public static final long PRESIGNED_EXPIRATION_SECONDS = 300;
}
