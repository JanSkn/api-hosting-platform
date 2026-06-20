package com.hosting.common.dto;

import java.util.List;

public record DeploymentLogsUrlResponse(List<String> uploadUrls, long expiresInSeconds) {}
