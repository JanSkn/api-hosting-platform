package com.hosting.core.exceptions;

import com.hosting.common.exceptions.UserCodeNotUploadedException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

@Provider
public class UserCodeNotUploadedExceptionMapper
    implements ExceptionMapper<UserCodeNotUploadedException> {

  private static final Logger LOG = Logger.getLogger(UserCodeNotUploadedExceptionMapper.class);

  @Override
  public Response toResponse(UserCodeNotUploadedException exception) {
    LOG.error("User Code Not Uploaded: " + exception.getMessage(), exception);

    return Response.status(Response.Status.BAD_REQUEST)
        .entity(new ErrorPayload("USER_CODE_NOT_UPLOADED", "User code not uploaded yet."))
        .build();
  }

  public static record ErrorPayload(String error, String message) {}
}
