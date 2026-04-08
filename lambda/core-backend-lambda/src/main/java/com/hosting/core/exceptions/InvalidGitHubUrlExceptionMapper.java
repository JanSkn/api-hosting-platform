package com.hosting.core.exceptions;

import com.hosting.common.exceptions.InvalidGitHubUrlException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class InvalidGitHubUrlExceptionMapper implements ExceptionMapper<InvalidGitHubUrlException> {

  @Override
  public Response toResponse(InvalidGitHubUrlException exception) {
    return Response.status(Response.Status.BAD_REQUEST)
        .entity(
            new ErrorPayload(
                "INVALID_GITHUB_URL",
                "The provided GitHub URL is invalid. Please check the URL and try again."))
        .build();
  }

  public record ErrorPayload(String error, String message) {}
}
