package com.hosting.core.exceptions;

import com.hosting.common.exceptions.GitHubDownloadException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class GitHubDownloadExceptionMapper implements ExceptionMapper<GitHubDownloadException> {

  @Override
  public Response toResponse(GitHubDownloadException exception) {
    return Response.status(Response.Status.BAD_GATEWAY)
        .entity(
            new ErrorPayload(
                "GITHUB_DOWNLOAD_FAILED",
                "The server failed to download the code from GitHub. Please try again later."))
        .build();
  }

  public record ErrorPayload(String error, String message) {}
}
