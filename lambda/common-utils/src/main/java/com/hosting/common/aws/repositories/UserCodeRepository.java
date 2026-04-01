package com.hosting.common.aws.repositories;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hosting.common.config.ProjectConfig;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.Duration;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import software.amazon.awssdk.services.sqs.SqsClient;

@ApplicationScoped
public class UserCodeRepository {
  private final S3Client s3Client;
  private final S3Presigner s3Presigner;

  @Inject
  public UserCodeRepository(
      S3Client s3Client, S3Presigner s3Presigner, SqsClient sqsClient, ObjectMapper objectMapper) {
    this.s3Client = s3Client;
    this.s3Presigner = s3Presigner;
  }

  public String generateObjectKey(String userId, String deploymentId) {
    return String.format("%s/%s/%s.zip", ProjectConfig.S3.USER_CODE_PREFIX, userId, deploymentId);
  }

  public String generatePresignedUploadUrl(String userId, String deploymentId) {
    String objectKey = generateObjectKey(userId, deploymentId);
    PutObjectPresignRequest presignRequest =
        PutObjectPresignRequest.builder()
            .signatureDuration(Duration.ofSeconds(ProjectConfig.S3.PRESIGNED_EXPIRATION_SECONDS))
            .putObjectRequest(
                req -> req.bucket(ProjectConfig.S3.USER_CODE_BUCKET).key(objectKey).build())
            .build();
    PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
    String uploadUrl = presignedRequest.url().toString();

    return uploadUrl;
  }

  public boolean doesObjectExist(String userId, String deploymentId) {
    String objectKey = generateObjectKey(userId, deploymentId);
    try {
      s3Client.headObject(req -> req.bucket(ProjectConfig.S3.USER_CODE_BUCKET).key(objectKey));
      return true;
    } catch (Exception e) {
      return false;
    }
  }
}
