package com.hosting.core.resource;

import com.hosting.common.aws.DeploymentService;
import com.hosting.common.dto.CreateDeploymentRequest;
import com.hosting.common.dto.CreateDeploymentResponse;
import com.hosting.common.dto.UploadUrlResponse;
import com.hosting.common.enums.DeploymentEnums.Status;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
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

  /*
  Deployment steps:
  - Client calls POST /initialize to initialize deployment
  - Client calls GET /upload-url to get presigned S3 URL and deploymentId
  - Client calls PATCH /{deploymentId}/status to update deployment status to UPLOADING
  - Client uploads code to S3 using the presigned URL
  - Client calls POST /{deploymentId}/trigger to trigger the deployment
  */

  @POST
  @Path("/initialize")
  public Response initializeDeployment(CreateDeploymentRequest request) {
    String userId = claims.getUserId();

    String deploymentId = deploymentService.initializeDeployment(userId, request);
    CreateDeploymentResponse response = new CreateDeploymentResponse(deploymentId);

    return createResponse(Response.Status.OK, response);
  }

  @GET
  @Path("/upload-url")
  public Response generateS3CodeUploadUrl(@QueryParam("deploymentId") String deploymentId) {
    String userId = claims.getUserId();

    UploadUrlResponse response = deploymentService.generateUploadUrl(userId, deploymentId);

    return createResponse(Response.Status.OK, response);
  }

  @PATCH
  @Path("/{deploymentId}/status")
  public Response setDeploymentStatus(
      @PathParam("deploymentId") String deploymentId, @QueryParam("status") Status status) {
    String userId = claims.getUserId();

    deploymentService.setDeploymentStatus(userId, deploymentId, status);

    return createResponse(Response.Status.OK, "Deployment status updated");
  }

  @POST
  @Path("/{deploymentId}/trigger")
  public Response triggerDeployment(@PathParam("deploymentId") String deploymentId) {
    String userId = claims.getUserId();

    deploymentService.triggerDeployment(userId, deploymentId);

    return createResponse(Response.Status.OK, "Deployment job enqueued");
  }
}
