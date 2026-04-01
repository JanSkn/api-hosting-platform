package com.hosting.core.exceptions;

import com.hosting.common.exceptions.SQSBuildJobNotEnqueuedException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

@Provider
public class SQSBuildJobNotEnqueuedExceptionMapper
    implements ExceptionMapper<SQSBuildJobNotEnqueuedException> {

  private static final Logger LOG = Logger.getLogger(SQSBuildJobNotEnqueuedException.class);

  @Override
  public Response toResponse(SQSBuildJobNotEnqueuedException exception) {
    LOG.error("SQS build job could not be enqueued: " + exception.getMessage(), exception);

    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
        .entity(
            new ErrorPayload(
                "QUEUE_ERROR",
                "Deployment build job could not be enqueued. Please try again later."))
        .build();
  }

  public static record ErrorPayload(String error, String message) {}
}
