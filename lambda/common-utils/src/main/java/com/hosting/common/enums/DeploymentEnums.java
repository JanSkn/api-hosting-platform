package com.hosting.common.enums;

public class DeploymentEnums {
  public enum Status {
    UPLOADING, // user code upload in progress (S3 presigned URL generated, waiting for code upload)
    IN_PROGRESS,
    FAILED,
    LIVE
  }

  // TODO add missing
  public enum Runtime {
    JAVA_17,
    NODEJS_18_X
  }
}
