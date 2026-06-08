package com.hosting.core.exceptions;

import com.hosting.common.exceptions.SQSBuildJobNotEnqueuedException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class SQSBuildJobNotEnqueuedExceptionMapper
    implements ExceptionMapper<SQSBuildJobNotEnqueuedException> {

  @Override
  public Response toResponse(SQSBuildJobNotEnqueuedException exception) {
    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
        .entity(
            new ErrorPayload(
                "QUEUE_ERROR",
                "Deployment build job could not be enqueued. Please try again later."))
        .build();
  }

  public record ErrorPayload(String error, String message) {}
}
