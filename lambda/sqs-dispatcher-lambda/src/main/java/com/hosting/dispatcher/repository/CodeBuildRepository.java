package com.hosting.dispatcher.repository;

import com.hosting.common.aws.sqs.models.BuildMessage;
import com.hosting.common.config.CodeBuildConfig;
import com.hosting.common.config.EcrConfig;
import com.hosting.common.config.GlobalConfig;
import com.hosting.common.config.S3Config;
import com.hosting.common.logging.LoggingConfig;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.codebuild.CodeBuildClient;
import software.amazon.awssdk.services.codebuild.model.EnvironmentVariable;
import software.amazon.awssdk.services.codebuild.model.StartBuildRequest;
import software.amazon.awssdk.services.codebuild.model.StartBuildResponse;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.*;

public class CodeBuildRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(CodeBuildRepository.class);
  private final CodeBuildClient codeBuildClient;
  private final EventBridgeClient eventBridgeClient;

  public CodeBuildRepository(CodeBuildClient codeBuildClient, EventBridgeClient eventBridgeClient) {
    this.codeBuildClient = codeBuildClient;
    this.eventBridgeClient = eventBridgeClient;
  }

  public String startBuildJob(BuildMessage buildMessage, String imageTag) {
    LOGGER.info("Starting build job for runtime: {}", buildMessage.runtime());

    // prepare dynamic buildspec
    String repositoryUri = EcrConfig.REPOSITORY_URI;
    String s3SourceUri =
        String.format("s3://%s/%s", S3Config.USER_CODE_BUCKET, buildMessage.s3ObjectKey());
    String buildspec =
        generateBuildspec(buildMessage.runtime().name(), imageTag, repositoryUri, s3SourceUri);

    // set env variables to pass IDs and image tag in event bridge event to the
    // function deployer
    // sync with publishCodeBuildSuccessEvent
    List<EnvironmentVariable> envVars = new ArrayList<>();
    envVars.add(
        EnvironmentVariable.builder()
            .name("USER_ID")
            .value(buildMessage.userId())
            .type("PLAINTEXT")
            .build());
    envVars.add(
        EnvironmentVariable.builder()
            .name("DEPLOYMENT_ID")
            .value(buildMessage.deploymentId())
            .type("PLAINTEXT")
            .build());
    envVars.add(
        EnvironmentVariable.builder()
            .name("CORRELATION_ID")
            .value(buildMessage.correlationId())
            .type("PLAINTEXT")
            .build());
    envVars.add(
        EnvironmentVariable.builder().name("IMAGE_TAG").value(imageTag).type("PLAINTEXT").build());

    // pass environment information for local networking fixes
    envVars.add(
        EnvironmentVariable.builder()
            .name("ENV")
            .value(GlobalConfig.ENVIRONMENT)
            .type("PLAINTEXT")
            .build());

    if (GlobalConfig.isLocalStack()) {
      // tell the build container how to reach LocalStack
      envVars.add(
          EnvironmentVariable.builder()
              .name("AWS_ENDPOINT_URL")
              .value("http://" + GlobalConfig.LOCALSTACK_CONTAINER_NAME + ":4566")
              .type("PLAINTEXT")
              .build());
    }

    // we don't override the source location to S3 here but copy from S3 in the buildspec due to
    // localstack's compability
    StartBuildRequest startBuildRequest =
        StartBuildRequest.builder()
            .projectName(CodeBuildConfig.PROJECT_NAME)
            .buildspecOverride(buildspec)
            .environmentVariablesOverride(envVars)
            .build();

    StartBuildResponse response = codeBuildClient.startBuild(startBuildRequest);
    String buildId = response.build().id();

    LoggingConfig.put("buildId", buildId);
    LOGGER.info("Started CodeBuild job");

    if (GlobalConfig.isLocalStack()) {
      LOGGER.info("[LOCAL] Enter waitForBuildCompletion ...", buildId);
      waitForBuildCompletion(buildId);

      publishCodeBuildSuccessEvent(
          CodeBuildConfig.PROJECT_NAME,
          buildId,
          buildMessage.userId(),
          buildMessage.deploymentId(),
          imageTag,
          buildMessage.correlationId());
    }

    return buildId;
  }

  // Dockerfiles populated to S3 in deploy-stack.sh from
  // sqs-dispatcher-lambda/src/main/resources/templates/
  private String generateBuildspec(
      String runtimeName, String imageTag, String repositoryUri, String s3SourceUri) {
    String templateKey = runtimeName + ".Dockerfile";
    String templateUri = buildS3TemplateUri(templateKey);

    String pushTarget = repositoryUri + ":" + imageTag;

    StringBuilder buildspec = new StringBuilder();
    buildspec.append("version: 0.2\n");
    buildspec.append("phases:\n");
    buildspec.append("  pre_build:\n");
    buildspec.append("    commands:\n");

    buildspec.append("      - echo \"Logging into ECR ...\"\n");
    buildspec
        .append("      - aws ecr get-login-password --region ")
        .append(GlobalConfig.AWS_REGION)
        .append(" | docker login --username AWS --password-stdin ")
        .append(repositoryUri)
        .append("\n");

    buildspec
        .append("  build:\n")
        .append("    commands:\n")
        .append("      - echo \"Fetching Dockerfile template from S3...\"\n")
        .append("      - aws s3 cp ")
        .append(templateUri)
        .append(" Dockerfile\n")
        .append("      - echo \"Fetching user code from S3...\"\n")
        .append("      - aws s3 cp ")
        .append(s3SourceUri)
        .append(" source.zip\n")
        .append("      - unzip -j source.zip -d . || unzip source.zip\n")
        .append("      - rm source.zip\n")
        .append("      - echo \"Building Docker Image...\"\n")
        .append("      - docker build -t \"")
        .append(pushTarget)
        .append("\" .\n");

    buildspec
        .append("  post_build:\n")
        .append("    commands:\n")
        .append("      - echo \"Pushing Docker Image to ECR...\"\n")
        .append("      - docker push \"")
        .append(pushTarget)
        .append("\"\n");

    return buildspec.toString();
  }

  private String buildS3TemplateUri(String key) {
    return String.format("s3://%s/%s", S3Config.DOCKERFILE_TEMPLATE_BUCKET, key);
  }

  private void publishCodeBuildSuccessEvent(
      String projectName,
      String buildId,
      String userId,
      String deploymentId,
      String imageTag,
      String correlationId) {

    String detail =
        String.format(
            "{"
                + "\"build-status\": \"SUCCEEDED\","
                + "\"project-name\": \"%s\","
                + "\"build-id\": \"%s\","
                + "\"additional-information\": {"
                + "  \"environment\": {"
                + "    \"environment-variables\": ["
                + "      {\"name\": \"USER_ID\", \"value\": \"%s\", \"type\": \"PLAINTEXT\"},"
                + "      {\"name\": \"DEPLOYMENT_ID\", \"value\": \"%s\", \"type\": \"PLAINTEXT\"},"
                + "      {\"name\": \"CORRELATION_ID\", \"value\": \"%s\", \"type\": \"PLAINTEXT\"},"
                + "      {\"name\": \"IMAGE_TAG\", \"value\": \"%s\", \"type\": \"PLAINTEXT\"},"
                + "      {\"name\": \"ENV\", \"value\": \"%s\", \"type\": \"PLAINTEXT\"}"
                + "    ]"
                + "  }"
                + "}"
                + "}",
            projectName,
            buildId,
            userId,
            deploymentId,
            correlationId,
            imageTag,
            GlobalConfig.ENVIRONMENT);

    PutEventsRequestEntry entry =
        PutEventsRequestEntry.builder()
            .source("aws.codebuild")
            .detailType("CodeBuild Build State Change")
            .detail(detail)
            .eventBusName("default")
            .build();

    eventBridgeClient.putEvents(PutEventsRequest.builder().entries(entry).build());

    LOGGER.info("[LOCAL] Published synthetic CodeBuild SUCCEEDED event to EventBridge");
  }

  // NOTE: this is a workaround method for local development with localstack, which doesn't support
  // CodeBuild's automatic EventBridge event after a successful build.
  // In production this is not needed as we rely on the event to trigger the deployment and not on
  // the build method to synchronously call the deploy method after the build.
  private void waitForBuildCompletion(String buildId) {
    if (!GlobalConfig.isLocalStack()) {
      throw new IllegalStateException(
          "waitForBuildCompletion is ONLY allowed in local environment. "
              + "This method must not be used in production.");
    }

    boolean finished = false;

    while (!finished) {

      var result = codeBuildClient.batchGetBuilds(b -> b.ids(buildId));

      var build = result.builds().get(0);
      String status = build.buildStatusAsString();

      LOGGER.info("Build status: {}", status);

      switch (status) {
        case "SUCCEEDED", "FAILED", "STOPPED" -> finished = true;
        default -> {
          try {
            Thread.sleep(5000);
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
          }
        }
      }
    }
  }
}
