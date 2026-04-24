package com.hosting.core.resource;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public abstract class BaseResource {

  protected Response createResponse(Status status, Object entity) {
    return Response.status(status).entity(entity).build();
  }

  protected Response createResponse(int statusCode, Object entity) {
    return Response.status(statusCode).entity(entity).build();
  }
}
