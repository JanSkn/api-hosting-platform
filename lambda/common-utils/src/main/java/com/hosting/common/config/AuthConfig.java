package com.hosting.common.config;

public final class AuthConfig {

  private AuthConfig() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }

  public static final String USER_POOL_ID = System.getenv("COGNITO_USER_POOL_ID");
}
