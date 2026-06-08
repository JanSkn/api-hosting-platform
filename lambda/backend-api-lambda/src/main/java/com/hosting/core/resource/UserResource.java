package com.hosting.core.resource;

import com.hosting.common.aws.UserService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("users")
@RequestScoped // token context per request
public class UserResource extends BaseResource {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserResource.class);
  @Inject ClaimsContext claims;
  @Inject UserService userService;

  @DELETE
  @Path("/me")
  public Response deleteUser() {
    LOGGER.info("Initializing user deletion");
    userService.deleteUser(claims.getUserId());

    return createResponse(Response.Status.OK, "User deleted");
  }
}
