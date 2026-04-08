package com.hosting.common.aws.repositories;

import com.hosting.common.config.ProjectConfig;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.Duration;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@ApplicationScoped
public class UserCodeRepository {
  private S3Client s3Client;
  private S3Presigner s3Presigner;

  @Inject
  public UserCodeRepository(S3Client s3Client, S3Presigner s3Presigner) {
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
                req ->
                    req.bucket(ProjectConfig.S3.USER_CODE_BUCKET)
                        .key(objectKey)
                        .contentType("application/zip")
                        .build())
            .build();
    PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);

    return presignedRequest.url().toString();
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

  /** Only used when user uploads from GitHub. */
  public void uploadUserCode(String userId, String deploymentId, byte[] content) {
    String objectKey = generateObjectKey(userId, deploymentId);
    PutObjectRequest putObjectRequest =
        PutObjectRequest.builder()
            .bucket(ProjectConfig.S3.USER_CODE_BUCKET)
            .key(objectKey)
            .contentType("application/zip")
            .build();

    s3Client.putObject(putObjectRequest, RequestBody.fromBytes(content));
  }

  /**
   * @warning Only works when user code is a zip file, otherwise we would need to list and delete
   *     all objects.
   */
  public void deleteUserCode(String userId, String deploymentId) {
    String objectKey = generateObjectKey(userId, deploymentId);
    s3Client.deleteObject(req -> req.bucket(ProjectConfig.S3.USER_CODE_BUCKET).key(objectKey));
  }
}
