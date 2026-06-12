package com.hosting.common.aws.repositories;

import com.hosting.common.config.ParameterStoreConfig;
import com.hosting.common.dto.CreateDeploymentRequest.EnvironmentVariable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.DeleteParametersRequest;
import software.amazon.awssdk.services.ssm.model.GetParametersByPathRequest;
import software.amazon.awssdk.services.ssm.model.GetParametersByPathResponse;
import software.amazon.awssdk.services.ssm.model.Parameter;
import software.amazon.awssdk.services.ssm.model.ParameterType;
import software.amazon.awssdk.services.ssm.model.PutParameterRequest;

@ApplicationScoped
public class ParameterStoreRepository {
  private static final Logger LOGGER = LoggerFactory.getLogger(ParameterStoreRepository.class);
  private final SsmClient ssmClient;

  @Inject
  public ParameterStoreRepository(SsmClient ssmClient) {
    this.ssmClient = ssmClient;
  }

  /**
   * Create environment variables isolated PER deployment. This is required for cases where the user
   * uses variables with equal keys across deployments (e.g., ENVIRONMENT).
   */
  public void createUserEnvVars(
      List<EnvironmentVariable> envVars, String userId, String deploymentId) {
    for (EnvironmentVariable envVar : envVars) {
      String parameterName =
          String.format(
              "/%s/%s/%s/%s",
              ParameterStoreConfig.ENV_VARIABLES_PREFIX, userId, deploymentId, envVar.key());

      PutParameterRequest.Builder requestBuilder =
          PutParameterRequest.builder().name(parameterName).value(envVar.value()).overwrite(true);

      if (envVar.isSecret()) {
        requestBuilder.type(ParameterType.SECURE_STRING); // encrypted with KMS
      } else {
        requestBuilder.type(ParameterType.STRING);
      }

      ssmClient.putParameter(requestBuilder.build());

      LOGGER.info("Successfully created environment variables");
    }
  }

  public Map<String, String> getUserEnvVars(String userId, String deploymentId) {
    String userPath = String.format("/%s/%s/", ParameterStoreConfig.ENV_VARIABLES_PREFIX, userId);
    Map<String, String> envMap = new HashMap<>();

    GetParametersByPathResponse ssmResponse =
        ssmClient.getParametersByPath(
            GetParametersByPathRequest.builder()
                .path(userPath)
                .recursive(true)
                .withDecryption(true)
                .build());

    for (Parameter param : ssmResponse.parameters()) {
      String key = param.name().replace(userPath, "");
      String value = param.value();
      envMap.put(key, value);
    }

    return envMap;
  }

  public void deleteAllUserEnvVariables(String userId) {
    String userPath = String.format("/%s/%s/", ParameterStoreConfig.ENV_VARIABLES_PREFIX, userId);

    GetParametersByPathResponse ssmResponse =
        ssmClient.getParametersByPath(
            GetParametersByPathRequest.builder().path(userPath).recursive(true).build());

    List<String> namesToDelete =
        ssmResponse.parameters().stream().map(Parameter::name).collect(Collectors.toList());

    if (!namesToDelete.isEmpty()) {
      ssmClient.deleteParameters(DeleteParametersRequest.builder().names(namesToDelete).build());
      LOGGER.info("All {} environment variables deleted", namesToDelete.size());
    }
  }
}
