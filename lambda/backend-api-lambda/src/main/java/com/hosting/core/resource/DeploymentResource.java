package com.hosting.core.resource;

import com.hosting.common.aws.DeploymentService;
import com.hosting.common.dto.CloudWatchLogsResponse;
import com.hosting.common.dto.CreateDeploymentRequest;
import com.hosting.common.dto.CreateDeploymentResponse;
import com.hosting.common.dto.UploadUrlResponse;
import com.hosting.common.enums.DeploymentEnums.Status;
import com.hosting.common.logging.LoggingConfig;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("deployments")
@RequestScoped // token context per request
public class DeploymentResource extends BaseResource {

  private static final Logger LOGGER = LoggerFactory.getLogger(DeploymentResource.class);
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

  These deployments steps get connected in the logs using the correlationId from the X-Correlation-ID header set in MDCLogFilter
  */

  @POST
  @Path("/initialize")
  public Response initializeDeployment(CreateDeploymentRequest request) {
    LOGGER.info("Initializing new deployment");

    String deploymentId = deploymentService.initializeDeployment(claims.getUserId(), request);
    CreateDeploymentResponse response = new CreateDeploymentResponse(deploymentId);

    return createResponse(Response.Status.OK, response);
  }

  @GET
  @Path("/upload-url")
  public Response generateS3CodeUploadUrl(@QueryParam("deploymentId") String deploymentId) {
    LoggingConfig.put(LoggingConfig.DEPLOYMENT_ID_MDC_KEY, deploymentId);
    LOGGER.info("Generating presigned S3 upload URL");

    UploadUrlResponse response =
        deploymentService.generateUploadUrl(claims.getUserId(), deploymentId);

    return createResponse(Response.Status.OK, response);
  }

  @PATCH
  @Path("/{deploymentId}/status")
  public Response setDeploymentStatus(
      @PathParam("deploymentId") String deploymentId, @QueryParam("status") Status status) {
    LoggingConfig.put(LoggingConfig.DEPLOYMENT_ID_MDC_KEY, deploymentId);
    LOGGER.info("Updating status to {}", status);

    deploymentService.setDeploymentStatus(claims.getUserId(), deploymentId, status);

    return createResponse(Response.Status.OK, "Deployment status updated");
  }

  @POST
  @Path("/{deploymentId}/trigger")
  public Response triggerDeployment(@PathParam("deploymentId") String deploymentId) {
    LoggingConfig.put(LoggingConfig.DEPLOYMENT_ID_MDC_KEY, deploymentId);
    LOGGER.info("Triggering build");

    deploymentService.triggerDeployment(claims.getUserId(), deploymentId);

    return createResponse(Response.Status.OK, "Deployment job enqueued");
  }

  @DELETE
  @Path("/{deploymentId}")
  public Response deleteDeployment(@PathParam("deploymentId") String deploymentId) {
    LoggingConfig.put(LoggingConfig.DEPLOYMENT_ID_MDC_KEY, deploymentId);

    deploymentService.deleteDeployment(claims.getUserId(), deploymentId);

    return createResponse(Response.Status.OK, "Deployment deleted");
  }

  @GET
  @Path("/logs")
  public Response getCloudWatchDeploymentLogs(
      @QueryParam("deploymentId") String deploymentId, @QueryParam("nextToken") String nextToken) {
    LoggingConfig.put(LoggingConfig.DEPLOYMENT_ID_MDC_KEY, deploymentId);
    LOGGER.info("Fetching CloudWatch deployment logs");

    CloudWatchLogsResponse response =
        deploymentService.getCloudWatchLogs(claims.getUserId(), deploymentId, nextToken);

    return createResponse(Response.Status.OK, response);
  }
}
