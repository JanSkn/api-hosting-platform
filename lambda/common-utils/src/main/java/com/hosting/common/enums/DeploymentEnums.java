package com.hosting.common.enums;

public class DeploymentEnums {
  public enum Status {
    INITIALIZED,
    UPLOADING, // user code upload in progress (waiting for code upload)
    IN_PROGRESS,
    FAILED,
    LIVE
  }

  /** Must be kept in sync with runtime options in Dockerfile templates */
  public enum Runtime {
    JAVA_11,
    JAVA_17,
    JAVA_21,

    NODEJS_18_X,
    NODEJS_20_X,
    NODEJS_22_X,

    PYTHON_3_10,
    PYTHON_3_11,
    PYTHON_3_12,
    PYTHON_3_13,
    PYTHON_3_14
  }
}
