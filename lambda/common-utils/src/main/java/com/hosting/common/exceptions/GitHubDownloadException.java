package com.hosting.common.exceptions;

public class GitHubDownloadException extends RuntimeException {
  public GitHubDownloadException(String message) {
    super(message);
  }

  public GitHubDownloadException(String message, Throwable cause) {
    super(message, cause);
  }
}
