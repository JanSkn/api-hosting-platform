package com.hosting.common.aws.dynamo.models;

import com.hosting.common.enums.DeploymentEnums.Runtime;
import com.hosting.common.enums.DeploymentEnums.Status;
import java.util.StringJoiner;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

// partition key: userId, sort key: deploymentId must be same as in SAM template.yml
@DynamoDbBean
public class Deployment {
  private String userId;
  private String deploymentId;
  private String name;
  private String buildId;
  private Status status;
  private Runtime runtime;
  private String s3ObjectKey; // user code location in S3
  private String apiId;
  private String apiUri;
  private String errorMessage;
  private long createdAt; // Epoch seconds

  public Deployment() {}

  @DynamoDbPartitionKey
  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  @DynamoDbSortKey
  public String getDeploymentId() {
    return deploymentId;
  }

  public void setDeploymentId(String deploymentId) {
    this.deploymentId = deploymentId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getBuildId() {
    return buildId;
  }

  public void setBuildId(String buildId) {
    this.buildId = buildId;
  }

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  public Runtime getRuntime() {
    return runtime;
  }

  public void setRuntime(Runtime runtime) {
    this.runtime = runtime;
  }

  public String getS3ObjectKey() {
    return s3ObjectKey;
  }

  public void setS3ObjectKey(String s3ObjectKey) {
    this.s3ObjectKey = s3ObjectKey;
  }

  public String getApiId() {
    return apiId;
  }

  public void setApiId(String apiId) {
    this.apiId = apiId;
  }

  public String getApiUri() {
    return apiUri;
  }

  public void setApiUri(String apiUri) {
    this.apiUri = apiUri;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  public long getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(long createdAt) {
    this.createdAt = createdAt;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", Deployment.class.getSimpleName() + "[", "]")
        .add("userId='" + userId + "'")
        .add("deploymentId='" + deploymentId + "'")
        .add("name='" + name + "'")
        .add("buildId='" + buildId + "'")
        .add("status='" + status + "'")
        .add("runtime='" + runtime + "'")
        .add("s3ObjectKey='" + s3ObjectKey + "'")
        .add("apiId='" + apiId + "'")
        .add("apiUri='" + apiUri + "'")
        .add("errorMessage='" + errorMessage + "'")
        .add("createdAt=" + createdAt)
        .toString();
  }
}
