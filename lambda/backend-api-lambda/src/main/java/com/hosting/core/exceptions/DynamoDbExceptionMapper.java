package com.hosting.core.exceptions;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;

@Provider
public class DynamoDbExceptionMapper implements ExceptionMapper<DynamoDbException> {

  @Override
  public Response toResponse(DynamoDbException exception) {
    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
        .entity(
            new ErrorPayload(
                "DATABASE_ERROR",
                "A technical problem occurred with the data store. Please try again later."))
        .build();
  }

  public record ErrorPayload(String error, String message) {}
}
