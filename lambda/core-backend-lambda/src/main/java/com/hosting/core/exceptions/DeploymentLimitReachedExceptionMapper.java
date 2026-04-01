package com.hosting.core.exceptions;

import com.hosting.common.config.ProjectConfig;
import com.hosting.common.exceptions.DeploymentLimitReachedException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

@Provider
public class DeploymentLimitReachedExceptionMapper
    implements ExceptionMapper<DeploymentLimitReachedException> {

  private static final Logger LOG = Logger.getLogger(DeploymentLimitReachedExceptionMapper.class);

  @Override
  public Response toResponse(DeploymentLimitReachedException exception) {
    LOG.error("Deployment Limit Reached: " + exception.getMessage(), exception);

    return Response.status(Response.Status.FORBIDDEN)
        .entity(
            new ErrorPayload(
                "DEPLOYMENT_LIMIT_REACHED",
                String.format(
                    "Deployment limit of %d deployments reached. Please delete existing deployments.",
                    ProjectConfig.Deployment.MAX_PER_USER)))
        .build();
  }

  public static record ErrorPayload(String error, String message) {}
}
