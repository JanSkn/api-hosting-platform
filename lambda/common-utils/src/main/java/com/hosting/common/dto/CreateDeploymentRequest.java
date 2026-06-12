package com.hosting.common.dto;

import com.hosting.common.enums.DeploymentEnums.Runtime;
import java.util.List;

public record CreateDeploymentRequest(
    String name,
    Runtime runtime,
    String githubUrl,
    List<EnvironmentVariable> environmentVariables) {
  public record EnvironmentVariable(String key, String value, boolean isSecret) {}
}
