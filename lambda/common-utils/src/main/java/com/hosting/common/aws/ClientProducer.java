package com.hosting.common.aws;

import com.hosting.common.config.GlobalConfig;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.services.codebuild.CodeBuildClient;
import software.amazon.awssdk.services.codebuild.CodeBuildClientBuilder;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClientBuilder;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClientBuilder;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.EventBridgeClientBuilder;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.LambdaClientBuilder;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;
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

    S3ClientBuilder builder = S3Client.builder().region(GlobalConfig.AWS_REGION);

    if (GlobalConfig.isLocal()) {
      builder.endpointOverride(GlobalConfig.AWS_LOCAL_INTERNAL_ENDPOINT).forcePathStyle(true);
    }

    return builder
        .build();
  }

  /**
   * Produces an application-scoped S3Presigner. It automatically configures the region and local
   * endpoint overrides if running in a local environment. Required for generating pre-signed URLs
   * for secure browser uploads.
   */
  @Produces
  @ApplicationScoped
  public S3Presigner s3Presigner() {
    S3Presigner.Builder builder = S3Presigner.builder().region(GlobalConfig.AWS_REGION);

    if (GlobalConfig.isLocal()) {
      S3Configuration s3Configuration =
          S3Configuration.builder().pathStyleAccessEnabled(true).build();

      builder
          .endpointOverride(GlobalConfig.AWS_LOCAL_EXTERNAL_ENDPOINT)
          .serviceConfiguration(s3Configuration);
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
    DynamoDbClientBuilder builder = DynamoDbClient.builder().region(GlobalConfig.AWS_REGION);

    if (GlobalConfig.isLocal()) {
      builder.endpointOverride(GlobalConfig.AWS_LOCAL_INTERNAL_ENDPOINT);
    }

    DynamoDbClient standardClient = builder
        .build();
    return DynamoDbEnhancedClient.builder().dynamoDbClient(standardClient).build();
  }

  /**
   * Produces an application-scoped SqsClient. It automatically configures the region and local
   * endpoint overrides if running in a local environment.
   */
  @Produces
  @ApplicationScoped
  public SqsClient sqsClient() {
    SqsClientBuilder builder = SqsClient.builder().region(GlobalConfig.AWS_REGION);

    if (GlobalConfig.isLocal()) {
      builder.endpointOverride(GlobalConfig.AWS_LOCAL_INTERNAL_ENDPOINT);
    }

    return builder
        .build();
  }

  /**
   * Returns a CodeBuildClient. It automatically configures the region and local endpoint overrides
   * if running in a local environment.
   */
  public CodeBuildClient codeBuildClient() { // not used by quarkus, so no annotations
    CodeBuildClientBuilder builder = CodeBuildClient.builder().region(GlobalConfig.AWS_REGION);

    if (GlobalConfig.isLocal()) {
      builder.endpointOverride(GlobalConfig.AWS_LOCAL_INTERNAL_ENDPOINT);
    }

    return builder
        .build();
  }

  /**
   * Returns an EventBridgeClient. It automatically configures the region and local endpoint
   * overrides if running in a local environment.
   */
  public EventBridgeClient eventBridgeClient() { // not used by quarkus, so no annotations
    EventBridgeClientBuilder builder = EventBridgeClient.builder().region(GlobalConfig.AWS_REGION);

    if (GlobalConfig.isLocal()) {
      builder.endpointOverride(GlobalConfig.AWS_LOCAL_INTERNAL_ENDPOINT);
    }

    return builder
        .build();
  }

  /**
   * Produces an application-scoped CognitoIdentityProviderClient. It automatically configures the
   * region and local endpoint overrides if running in a local environment.
   */
  @Produces
  @ApplicationScoped
  public CognitoIdentityProviderClient cognitoClient() {
    CognitoIdentityProviderClientBuilder builder =
        CognitoIdentityProviderClient.builder().region(GlobalConfig.AWS_REGION);

    if (GlobalConfig.isLocal()) {
      builder.endpointOverride(GlobalConfig.AWS_LOCAL_INTERNAL_ENDPOINT);
    }

    return builder
        .build();
  }

  /**
   * Returns a LambdaClient. It automatically configures the region and local endpoint overrides if
   * running in a local environment.
   */
  public LambdaClient lambdaClient() { // not used by quarkus, so no annotations
    LambdaClientBuilder builder = LambdaClient.builder().region(GlobalConfig.AWS_REGION);

    if (GlobalConfig.isLocal()) {
      builder.endpointOverride(GlobalConfig.AWS_LOCAL_INTERNAL_ENDPOINT);
    }

    return builder
        .build();
  }
}
