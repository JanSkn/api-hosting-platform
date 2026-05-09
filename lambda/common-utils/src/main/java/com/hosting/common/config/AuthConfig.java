package com.hosting.common.config;

public final class AuthConfig extends BaseConfig {

  public static final String USER_POOL_ID = getOrThrow("COGNITO_USER_POOL_ID");
}
