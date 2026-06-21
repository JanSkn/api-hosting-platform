package com.hosting.common.aws.repositories;

import com.hosting.common.config.CodeBuildLogConfig;
import com.hosting.common.dto.CloudWatchLogsResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.CreateLogStreamRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DeleteLogStreamRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.GetLogEventsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.GetLogEventsResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.InputLogEvent;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutLogEventsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.ResourceAlreadyExistsException;
import software.amazon.awssdk.services.cloudwatchlogs.model.ResourceNotFoundException;

@ApplicationScoped
public class DeploymentLogsRepository {
  private CloudWatchLogsClient cloudWatchLogsClient;

  @Inject
  public DeploymentLogsRepository(CloudWatchLogsClient cloudWatchLogsClient) {
    this.cloudWatchLogsClient = cloudWatchLogsClient;
  }

  public static String getLogStreamName(String userId, String deploymentId) {
    return userId + "/" + deploymentId;
  }

  public void uploadFakeLogs(String buildId, String userId, String deploymentId) {
    String buildIdShort = buildId.contains(":") ? buildId.split(":")[1] : buildId;
    String logGroupName = CodeBuildLogConfig.LOG_GROUP;
    String logStreamName = getLogStreamName(userId, deploymentId);
    StringBuilder s3FakeLogBuilder = new StringBuilder();

    try {
      try {
        cloudWatchLogsClient.createLogStream(
            CreateLogStreamRequest.builder()
                .logGroupName(logGroupName)
                .logStreamName(logStreamName)
                .build());
      } catch (ResourceAlreadyExistsException e) {
        // ignore
      }

      List<InputLogEvent> logEvents = new ArrayList<>();
      long baseTimestamp = System.currentTimeMillis();

      for (int i = 0; i < 100; ++i) {
        String formattedLogContent =
            String.format(
                "[LOCAL] [%d/100] Fake CodeBuild log row for build %s\n", i + 1, buildIdShort);
        logEvents.add(
            InputLogEvent.builder()
                .message(formattedLogContent)
                .timestamp(baseTimestamp + i * 1000)
                .build());

        // for S3
        s3FakeLogBuilder.append(formattedLogContent);
      }

      cloudWatchLogsClient.putLogEvents(
          PutLogEventsRequest.builder()
              .logGroupName(logGroupName)
              .logStreamName(logStreamName)
              .logEvents(logEvents)
              .build());
    } catch (Exception e) {
      // ignore failures for local environment robustness
    }
  }

  public void deleteUserLogs(String userId, String deploymentId) {
    String logGroupName = CodeBuildLogConfig.LOG_GROUP;
    String logStreamName = getLogStreamName(userId, deploymentId);
    try {
      cloudWatchLogsClient.deleteLogStream(
          DeleteLogStreamRequest.builder()
              .logGroupName(logGroupName)
              .logStreamName(logStreamName)
              .build());
    } catch (ResourceNotFoundException e) {
      // ignore if stream didn't exist
    } catch (Exception e) {
      // ignore other errors for deletion cleanup robustness
    }
  }

  /** If nextToken is null, returns all logs until the latest */
  public CloudWatchLogsResponse getCloudWatchLogs(
      String userId, String deploymentId, String nextToken) {
    String logGroupName = CodeBuildLogConfig.LOG_GROUP;
    String logStreamName = getLogStreamName(userId, deploymentId);

    try {
      GetLogEventsRequest.Builder requestBuilder =
          GetLogEventsRequest.builder()
              .logGroupName(logGroupName)
              .logStreamName(logStreamName)
              .startFromHead(true);

      if (nextToken != null && !nextToken.trim().isEmpty()) {
        requestBuilder.nextToken(nextToken);
      }

      GetLogEventsResponse response = cloudWatchLogsClient.getLogEvents(requestBuilder.build());

      List<CloudWatchLogsResponse.LogEvent> events =
          response.events().stream()
              .map(event -> new CloudWatchLogsResponse.LogEvent(event.message(), event.timestamp()))
              .collect(Collectors.toList());

      return new CloudWatchLogsResponse(events, response.nextForwardToken());
    } catch (ResourceNotFoundException e) {
      return new CloudWatchLogsResponse(List.of(), null);
    }
  }
}
