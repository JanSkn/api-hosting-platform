package com.hosting.common.exceptions;

public class SQSBuildJobNotEnqueuedException extends RuntimeException {
  public SQSBuildJobNotEnqueuedException() {
    super("Failed to enqueue build job in SQS");
  }
}
