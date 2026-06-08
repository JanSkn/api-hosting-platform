package com.hosting.common.aws.repositories;

import com.hosting.common.config.EcrConfig;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.ecr.EcrClient;
import software.amazon.awssdk.services.ecr.model.BatchDeleteImageRequest;
import software.amazon.awssdk.services.ecr.model.ImageIdentifier;

@ApplicationScoped
public class EcrRepository {
  private static final Logger LOGGER = LoggerFactory.getLogger(EcrRepository.class);
  private final EcrClient ecrClient;

  @Inject
  public EcrRepository(EcrClient ecrClient) {
    this.ecrClient = ecrClient;
  }

  private String getRepositoryName() {
    String uri = EcrConfig.REPOSITORY_URI;
    int index = uri.indexOf('/');
    if (index != -1) {
      return uri.substring(index + 1);
    }
    return uri;
  }

  public void deleteImage(String imageTag) {
    try {
      String repositoryName = getRepositoryName();
      BatchDeleteImageRequest deleteImageRequest =
          BatchDeleteImageRequest.builder()
              .repositoryName(repositoryName)
              .imageIds(ImageIdentifier.builder().imageTag(imageTag).build())
              .build();
      ecrClient.batchDeleteImage(deleteImageRequest);
      LOGGER.info("Successfully deleted ECR image from repository: {}", repositoryName);
    } catch (Exception e) {
      LOGGER.error("Failed to delete ECR image", e);
    }
  }
}
