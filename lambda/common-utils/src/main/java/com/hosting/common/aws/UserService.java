package com.hosting.common.aws;

import com.hosting.common.config.AuthConfig;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminDeleteUserRequest;

@ApplicationScoped
public class UserService {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);
  @Inject DeploymentService deploymentService;
  @Inject CognitoIdentityProviderClient cognitoClient;

  public void deleteUser(String userId) {
    deploymentService.deleteDeployments(userId);

    AdminDeleteUserRequest deleteRequest =
        AdminDeleteUserRequest.builder()
            .userPoolId(AuthConfig.USER_POOL_ID)
            .username(userId)
            .build();

    cognitoClient.adminDeleteUser(deleteRequest);
    LOGGER.info("Successfully deleted user from Cognito");
  }
}
