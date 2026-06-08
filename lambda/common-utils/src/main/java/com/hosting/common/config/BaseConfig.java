package com.hosting.common.config;

/**
 * Base class for configuration classes. Provides a protected method to fetch environment variables
 * with mandatory presence checks.
 */
public class BaseConfig {

  protected BaseConfig() {
    // Should not be directly instantiated
  }

  protected static String getOrThrow(String key) {
    String value = System.getenv(key);
    if (value == null || value.isBlank()) {
      throw new RuntimeException(
          "CRITICAL CONFIG ERROR: Environment variable '" + key + "' is not set");
    }
    return value;
  }
}
