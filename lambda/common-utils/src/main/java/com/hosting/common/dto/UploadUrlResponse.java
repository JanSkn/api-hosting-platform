package com.hosting.core.dto;

public record UploadUrlResponse(String deploymentId, String uploadUrl, long expiresInSeconds) {}
