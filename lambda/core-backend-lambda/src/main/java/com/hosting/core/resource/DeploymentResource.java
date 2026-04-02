package com.hosting.core.resource;

import com.hosting.common.aws.DeploymentService;
import com.hosting.common.dto.UploadUrlResponse;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

@Path("deployments")
@RequestScoped // token context per request
public class DeploymentResource extends BaseResource {

  @Inject ClaimsContext claims;
  @Inject DeploymentService deploymentService;

  @GET
  @Path("")
  public Response getDeployments() {
    String userId = claims.getUserId();
    return deploymentService
        .getDeployments(userId)
        .map(deployments -> createResponse(Response.Status.OK, deployments))
        .orElseGet(() -> createResponse(Response.Status.NOT_FOUND, "Deployments not found"));
  }

  @GET
  @Path("/{deploymentId}")
  public Response getDeployment(@PathParam("deploymentId") String deploymentId) {
    String userId = claims.getUserId();
    return deploymentService
        .getDeployment(userId, deploymentId)
        .map(value -> createResponse(Response.Status.OK, value))
        .orElseGet(() -> createResponse(Response.Status.NOT_FOUND, "Deployment not found"));
  }

  @GET
  @Path("/{deploymentId}/status")
  public Response getDeploymentStatus(@PathParam("deploymentId") String deploymentId) {
    String userId = claims.getUserId();
    return deploymentService
        .getDeploymentStatus(userId, deploymentId)
        .map(value -> createResponse(Response.Status.OK, value))
        .orElseGet(() -> createResponse(Response.Status.NOT_FOUND, "Deployment not found"));
  }

  @POST // post ant not get because we create a new deployment entry
  @Path("/upload-url")
  public Response generateS3CodeUploadUrl() {
    String userId = claims.getUserId();

    UploadUrlResponse response = deploymentService.createDeployment(userId);

    return createResponse(Response.Status.OK, response);
  }

  @POST
  @Path("/{deploymentId}/trigger")
  public Response triggerDeployment(@PathParam("deploymentId") String deploymentId) {
    String userId = claims.getUserId();

    deploymentService.triggerDeployment(userId, deploymentId);

    return createResponse(Response.Status.OK, "Deployment job enqueued");
  }
}
