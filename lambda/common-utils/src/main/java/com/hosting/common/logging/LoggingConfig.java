package com.hosting.common.logging;

import java.util.Map;
import org.slf4j.MDC;

public final class LoggingConfig {
  public static final String USER_ID_MDC_KEY = "userId";
  public static final String CORRELATION_ID_MDC_KEY = "correlationId";
  public static final String AWS_REQUEST_ID_MDC_KEY = "awsRequestId";
  public static final String DEPLOYMENT_ID_MDC_KEY = "deploymentId";

  public static final String SQS_MESSAGE_ID_MDC_KEY = "sqsMessageId";

  public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";

  private LoggingConfig() {
    // Prevent instantiation
  }

  public static void put(String key, String value) {
    MDC.put(key, value);
  }

  @SuppressWarnings("PMD.LooseCoupling")
  public static void putAll(Map<String, String> contextMap) {
    if (contextMap != null) {
      contextMap.forEach(MDC::put);
    }
  }

  public static void putAll(String... kvPairs) {
    if (kvPairs.length % 2 != 0) {
      throw new IllegalArgumentException(
          "Keys and values must be pairs (even number of arguments)");
    }
    for (int i = 0; i < kvPairs.length; i += 2) {
      MDC.put(kvPairs[i], kvPairs[i + 1]);
    }
  }

  public static void clear() {
    MDC.clear();
  }
}
