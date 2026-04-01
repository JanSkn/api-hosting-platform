package com.hosting.common.exceptions;

public class DeploymentLimitReachedException extends RuntimeException {
  private final int limit;

  public DeploymentLimitReachedException(int limit) {
    super("User has reached the maximum number of deployments: " + limit);
    this.limit = limit;
  }

  public int getLimit() {
    return limit;
  }
}
