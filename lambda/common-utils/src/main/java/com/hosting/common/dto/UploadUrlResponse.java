package com.hosting.common.dto;

public record UploadUrlResponse(String deploymentId, String uploadUrl, long expiresInSeconds) {}
