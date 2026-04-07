package com.hosting.common.aws.sqs.models;

import com.hosting.common.enums.DeploymentEnums.Runtime;

public record BuildMessage(
    String deploymentId, String userId, Runtime runtime, String s3ObjectKey) {}
