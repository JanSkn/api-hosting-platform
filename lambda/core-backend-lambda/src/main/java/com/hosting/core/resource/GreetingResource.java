package com.hosting.core.resource;

import io.quarkus.amazon.lambda.http.model.AwsProxyRequest;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;

@Path("/hello")
@RequestScoped
public class GreetingResource extends BaseResource {

  @Inject ClaimsContext claims;

  @GET
  public Response me(@Context AwsProxyRequest request) {
    return Response.status(200).entity(claims.getUserId()).build();
  }
}
