package com.hosting.common.exceptions;

public class DuplicateDeploymentNameException extends RuntimeException {
  private final String name;

  public DuplicateDeploymentNameException(String name) {
    super("A deployment with the name '" + name + "' already exists.");
    this.name = name;
  }

  public String getName() {
    return name;
  }
}