package com.hosting.common.dto;

import com.hosting.common.enums.DeploymentEnums.Runtime;

public record CreateDeploymentRequest(String name, Runtime runtime, String githubUrl) {}
