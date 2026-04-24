package com.hosting.common.config;

import software.amazon.awssdk.services.lambda.model.Architecture;
import software.amazon.awssdk.services.lambda.model.PackageType;

public final class UserLambdaConfig {

  private UserLambdaConfig() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }

  public static final PackageType PACKAGE_TYPE = PackageType.IMAGE;
  public static final Architecture ARCHITECTURE = Architecture.ARM64;
  public static final int TIMEOUT_SECONDS = 30;
  public static final int MEMORY_SIZE_MB = 128;
}
