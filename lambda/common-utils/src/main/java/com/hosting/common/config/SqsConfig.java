package com.hosting.common.config;

import java.net.URI;

public final class SqsConfig {

  private SqsConfig() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }

  public static final URI BUILD_QUEUE_URL = URI.create(System.getenv("BUILD_QUEUE_URL"));
}
