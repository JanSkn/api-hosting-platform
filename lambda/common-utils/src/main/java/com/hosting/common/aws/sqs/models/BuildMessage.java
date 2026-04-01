package com.hosting.common.aws.sqs.models;

public record BuildMessage(String deploymentId, String userId, String s3ObjectKey) {}
