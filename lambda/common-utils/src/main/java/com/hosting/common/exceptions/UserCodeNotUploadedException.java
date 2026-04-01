package com.hosting.common.exceptions;

public class UserCodeNotUploadedException extends RuntimeException {
  public UserCodeNotUploadedException() {
    super("User code not uploaded yet");
  }
}
