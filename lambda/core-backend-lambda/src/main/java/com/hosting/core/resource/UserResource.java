package com.hosting.core.resource;

import com.hosting.common.aws.UserService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path("users")
@RequestScoped // token context per request
public class UserResource extends BaseResource {

  @Inject ClaimsContext claims;
  @Inject UserService userService;

  @DELETE
  @Path("/me")
  public Response deleteUser() {
    String userId = claims.getUserId();

    userService.deleteUser(userId);

    return createResponse(Response.Status.OK, "User deleted");
  }
}
