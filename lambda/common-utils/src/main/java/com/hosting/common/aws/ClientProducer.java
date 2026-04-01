package com.hosting.common.aws;

import com.hosting.common.config.ProjectConfig;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClientBuilder;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.SqsClientBuilder;

/**
 * Producer class that provides configured AWS SDK clients as injectable CDI beans. This allows
 * other components to easily @Inject them. Note: We are not using static factory methods to ensure
 * full Quarkus compatibility.
 */
@ApplicationScoped
public class ClientProducer {
  /**
   * Produces an application-scoped S3Client. It automatically configures the region and local
   * endpoint overrides if running in a local environment.
   */
  @Produces
  @ApplicationScoped
  public S3Client s3Client() {

    S3ClientBuilder builder = S3Client.builder().region(ProjectConfig.AWS_REGION);

    if (ProjectConfig.isLocal()) {
      builder.endpointOverride(ProjectConfig.AWS_LOCAL_ENDPOINT).forcePathStyle(true);
    }

    S3Client client = builder.build();
    return client;
  }

  /**
   * Produces an application-scoped S3Presigner.It automatically configures the region and local
   * endpoint overrides if running in a local environment. Required for generating pre-signed URLs
   * for secure browser uploads.
   */
  @Produces
  @ApplicationScoped
  public S3Presigner s3Presigner() {
    S3Presigner.Builder builder = S3Presigner.builder().region(ProjectConfig.AWS_REGION);

    if (ProjectConfig.isLocal()) {
      builder.endpointOverride(ProjectConfig.AWS_LOCAL_ENDPOINT);
    }

    return builder.build();
  }

  /**
   * Produces an application-scoped DynamoDbEnhancedClient. It automatically configures the region
   * and local endpoint overrides if running in a local environment.
   */
  @Produces
  @ApplicationScoped
  public DynamoDbEnhancedClient dynamoDbClient() {
    DynamoDbClientBuilder builder = DynamoDbClient.builder().region(ProjectConfig.AWS_REGION);

    if (ProjectConfig.isLocal()) {
      builder.endpointOverride(ProjectConfig.AWS_LOCAL_ENDPOINT);
    }

    DynamoDbClient standardClient = builder.build();
    return DynamoDbEnhancedClient.builder().dynamoDbClient(standardClient).build();
  }

  /**
   * Produces an application-scoped SqsClient. It automatically configures the region and local
   * endpoint overrides if running in a local environment.
   */
  @Produces
  @ApplicationScoped
  public SqsClient sqsClient() {
    SqsClientBuilder builder = SqsClient.builder().region(ProjectConfig.AWS_REGION);

    if (ProjectConfig.isLocal()) {
      builder.endpointOverride(ProjectConfig.AWS_LOCAL_ENDPOINT);
    }

    SqsClient client = builder.build();
    return client;
  }
}
