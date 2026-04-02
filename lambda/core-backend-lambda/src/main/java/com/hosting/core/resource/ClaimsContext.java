package com.hosting.core.resource;

import io.quarkus.amazon.lambda.http.model.AwsProxyRequest;
import io.quarkus.amazon.lambda.http.model.CognitoAuthorizerClaims;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.core.Context;

@RequestScoped
public class ClaimsContext {

  @Context AwsProxyRequest request;

  public CognitoAuthorizerClaims getClaims() {
    if (request == null
        || request.getRequestContext() == null
        || request.getRequestContext().getAuthorizer() == null
        || request.getRequestContext().getAuthorizer().getClaims() == null) {
      return null;
    }
    return request.getRequestContext().getAuthorizer().getClaims();
  }

  public String getUserId() {
    CognitoAuthorizerClaims claims = getClaims();
    return claims != null ? claims.getUsername() : null;
  }
}
