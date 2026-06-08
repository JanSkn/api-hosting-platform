package com.hosting.core.exceptions;

import com.hosting.common.exceptions.DuplicateDeploymentNameException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class DuplicateDeploymentNameExceptionMapper
    implements ExceptionMapper<DuplicateDeploymentNameException> {

  @Override
  public Response toResponse(DuplicateDeploymentNameException exception) {
    return Response.status(Response.Status.CONFLICT)
        .entity(new ErrorPayload("DUPLICATE_DEPLOYMENT_NAME", exception.getMessage()))
        .build();
  }

  public record ErrorPayload(String error, String message) {}
}
