package com.hosting.common.logging;

public final class LoggingConstants {
  public static final String USER_ID_MDC_KEY = "userId";
  public static final String CORRELATION_ID_MDC_KEY = "correlationId";
  public static final String AWS_REQUEST_ID_MDC_KEY = "awsRequestId";
  public static final String DEPLOYMENT_ID_MDC_KEY = "deploymentId";

  public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";

  private LoggingConstants() {
    // Prevent instantiation
  }
}
