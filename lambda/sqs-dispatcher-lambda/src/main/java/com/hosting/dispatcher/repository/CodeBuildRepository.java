package com.hosting.dispatcher.repository;

import com.hosting.common.aws.sqs.models.BuildMessage;
import com.hosting.common.config.CodeBuildConfig;
import com.hosting.common.config.EcrConfig;
import com.hosting.common.config.S3Config;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.codebuild.CodeBuildClient;
import software.amazon.awssdk.services.codebuild.model.StartBuildRequest;
import software.amazon.awssdk.services.codebuild.model.StartBuildResponse;

public class CodeBuildRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(CodeBuildRepository.class);
  private final CodeBuildClient codeBuildClient;

  public CodeBuildRepository(CodeBuildClient codeBuildClient) {
    this.codeBuildClient = codeBuildClient;
  }

  public String generateImageTag(String userId, String deploymentId) {
    return userId + "_" + deploymentId;
  }

  public String startBuildJob(BuildMessage buildMessage, String imageTag) {
    LOGGER.info("Reading Dockerfile template for runtime: {}", buildMessage.runtime());

    String dockerfileName = buildMessage.runtime().name() + ".Dockerfile";
    String dockerfileContent = readTemplate("/templates/" + dockerfileName);

    // prepare dynamic buildspec
    String repositoryUri = EcrConfig.REPOSITORY_URI;
    String buildspec = generateBuildspec(dockerfileContent, imageTag, repositoryUri);

    String s3SourcePath =
        String.format(
            "%s/%s/%s",
            S3Config.USER_CODE_BUCKET, S3Config.USER_CODE_PREFIX, buildMessage.s3ObjectKey());

    StartBuildRequest startBuildRequest =
        StartBuildRequest.builder()
            .projectName(CodeBuildConfig.PROJECT_NAME)
            .buildspecOverride(buildspec)
            .sourceLocationOverride(s3SourcePath)
            .sourceTypeOverride("S3")
            .build();

    StartBuildResponse response = codeBuildClient.startBuild(startBuildRequest);
    String buildId = response.build().id();
    LOGGER.info("Started CodeBuild job with ID: {}", buildId);

    return buildId;
  }

  private String readTemplate(String templatePath) {
    try (InputStream is = getClass().getResourceAsStream(templatePath)) {
      if (is == null) {
        throw new RuntimeException("Template not found in resources: " + templatePath);
      }
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
        return reader.lines().collect(Collectors.joining("\n"));
      }
    } catch (Exception e) {
      LOGGER.error("Failed to read Dockerfile template: {}", templatePath, e);
      throw new RuntimeException("Failed to read template", e);
    }
  }

  private String generateBuildspec(
      String dockerfileContent, String imageTag, String repositoryUri) {
    return "version: 0.2\n"
        + "phases:\n"
        + "  pre_build:\n"
        + "    commands:\n"
        + "      - aws ecr get-login-password --region $AWS_DEFAULT_REGION | docker login --username AWS --password-stdin "
        + repositoryUri
        + "\n"
        + "  build:\n"
        + "    commands:\n"
        + "      - echo \"Building Dockerfile from template\"\n"
        + "      - cat << 'EOF' > Dockerfile\n"
        + dockerfileContent
        + "\n"
        + "EOF\n"
        + "      - echo \"Building Docker Image for user code...\"\n"
        + "      - docker build -t \""
        + repositoryUri
        + ":"
        + imageTag
        + "\" .\n"
        + "  post_build:\n"
        + "    commands:\n"
        + "      - echo \"Pushing Docker Image...\"\n"
        + "      - docker push \""
        + repositoryUri
        + ":"
        + imageTag
        + "\"\n";
  }
}
