package com.hosting.core.filters;

import com.hosting.common.logging.LoggingConstants;
import com.hosting.core.resource.ClaimsContext;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import java.util.UUID;
import org.slf4j.MDC;

/**
 * This filter handles two main identifiers for request tracing:
 *
 * <ul>
 *   <li><b>correlationId:</b> A unique identifier that spans multiple requests and services. It
 *       allows tracing a single user transaction as it flows through the frontend, the backend API,
 *       and asynchronous worker lambdas. Useful when tracing flows between different log groups. If
 *       the client does not provide an X-Correlation-ID header, a new one is generated and returned
 *       in the response header.
 *   <li><b>awsRequestId:</b> The unique identifier for a single AWS Lambda execution. It helps in
 *       isolating logs for a specific request when debugging issues within the Lambda environment.
 * </ul>
 */
@Provider
@ApplicationScoped
public class MdcLogFilter implements ContainerRequestFilter, ContainerResponseFilter {

  @Inject ClaimsContext claims;

  @Override
  public void filter(ContainerRequestContext requestContext) {
    String correlationId = requestContext.getHeaderString(LoggingConstants.CORRELATION_ID_HEADER);
    if (correlationId == null || correlationId.isEmpty()) {
      correlationId = UUID.randomUUID().toString();
    }

    MDC.put(LoggingConstants.CORRELATION_ID_MDC_KEY, correlationId);
    MDC.put(LoggingConstants.USER_ID_MDC_KEY, claims.getUserId());
    MDC.put(LoggingConstants.AWS_REQUEST_ID_MDC_KEY, claims.getRequestId());

    Log.debug("MDC context initialized");
  }

  @Override
  public void filter(
      ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
    String correlationId = MDC.get(LoggingConstants.CORRELATION_ID_MDC_KEY);
    if (correlationId != null) {
      responseContext.getHeaders().add(LoggingConstants.CORRELATION_ID_HEADER, correlationId);
    }
    MDC.clear();
  }
}
