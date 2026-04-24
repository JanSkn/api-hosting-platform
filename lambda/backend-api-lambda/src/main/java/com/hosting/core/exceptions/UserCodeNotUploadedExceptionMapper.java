package com.hosting.core.exceptions;

import com.hosting.common.exceptions.UserCodeNotUploadedException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class UserCodeNotUploadedExceptionMapper
    implements ExceptionMapper<UserCodeNotUploadedException> {

  @Override
  public Response toResponse(UserCodeNotUploadedException exception) {
    return Response.status(Response.Status.BAD_REQUEST)
        .entity(new ErrorPayload("USER_CODE_NOT_UPLOADED", "User code not uploaded yet."))
        .build();
  }

  public record ErrorPayload(String error, String message) {}
}
