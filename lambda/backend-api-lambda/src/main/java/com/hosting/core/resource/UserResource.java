package com.hosting.core.resource;

import com.hosting.common.aws.UserService;
import io.quarkus.logging.Log;
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
    // TODO also delete actual deployed lambdas, not only metadata in DynamoDB (can be done
    // asynchronously via SQS and another Lambda to avoid timeout issues)
    // also delete dokcer images etc
    Log.info("Initializing user deletion");
    userService.deleteUser(claims.getUserId());

    return createResponse(Response.Status.OK, "User deleted");
  }
}
