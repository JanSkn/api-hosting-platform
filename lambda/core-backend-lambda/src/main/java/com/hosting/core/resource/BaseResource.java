package com.hosting.core.resource;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import java.util.Map;

class UserClaims {
  private final Map<String, Object> claims;

  public UserClaims(Map<String, Object> claims) {
    this.claims = claims;
  }

  public String getUsername() {
    return String.valueOf(claims.get("cognito:username"));
  }

  public String getEmail() {
    return String.valueOf(claims.get("email"));
  }

  public String getSub() {
    return String.valueOf(claims.get("sub"));
  }

  public boolean hasGroup(String group) {
    String groups = String.valueOf(claims.get("cognito:groups"));
    return groups != null && groups.contains(group);
  }

  public String getClaim(String key) {
    return String.valueOf(claims.get(key));
  }
}

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public abstract class BaseResource {

  @Context APIGatewayProxyRequestEvent event;

  protected UserClaims getUserClaims() {
    return new UserClaims(event.getRequestContext().getAuthorizer());
  }

  protected Response createResponse(Status status, Object entity) {
    return Response.status(status).entity(entity).build();
  }

  protected Response createResponse(int statusCode, Object entity) {
    return Response.status(statusCode).entity(entity).build();
  }
}
