package com.hosting.common.config;

import java.net.URI;

public final class SqsConfig extends BaseConfig {

  public static final URI BUILD_QUEUE_URL = URI.create(getOrThrow("BUILD_QUEUE_URL"));
}
