package com.hosting.common.aws.eventbridge.models;

public record BuildEventDetail(
    String deploymentId, String userId, String imageTag, String correlationId) {}
