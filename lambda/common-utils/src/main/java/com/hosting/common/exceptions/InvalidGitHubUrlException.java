package com.hosting.common.exceptions;

public class InvalidGitHubUrlException extends RuntimeException {
  public InvalidGitHubUrlException(String url) {
    super("Invalid GitHub URL: " + url);
  }
}
