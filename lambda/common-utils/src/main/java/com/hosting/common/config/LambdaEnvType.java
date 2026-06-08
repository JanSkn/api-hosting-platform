package com.hosting.common.config;

/**
 * Determines which Lambda environment the code is running in. This for example allows the logging
 * implementation to switch between Quarkus logging and SLF4
 */
public enum LambdaEnvType {
  BACKEND_API_LAMBDA,
  SQS_DISPATCHER_LAMBDA,
  FUNCTION_DEPLOYER_LAMBDA;
}
