package com.hosting.common.aws;

import com.hosting.common.config.ProjectConfig;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminDeleteUserRequest;

@ApplicationScoped
public class UserService {

  @Inject DeploymentService deploymentService;
  @Inject CognitoIdentityProviderClient cognitoClient;

  public void deleteUser(String userId) {
    deploymentService.deleteDeployments(userId);

    AdminDeleteUserRequest deleteRequest =
        AdminDeleteUserRequest.builder()
            .userPoolId(ProjectConfig.Cognito.USER_POOL_ID)
            .username(userId)
            .build();

    cognitoClient.adminDeleteUser(deleteRequest);
  }
}
