package com.hosting.dispatcher.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hosting.common.aws.eventbridge.models.BuildEventDetail;
import com.hosting.common.aws.sqs.models.BuildMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequest;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequestEntry;

public class EventBridgeRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(EventBridgeRepository.class);
  private final EventBridgeClient eventBridgeClient;
  private final ObjectMapper objectMapper = new ObjectMapper();

  public EventBridgeRepository(EventBridgeClient eventBridgeClient) {
    this.eventBridgeClient = eventBridgeClient;
  }

  public void emitBuildSucceededEvent(BuildMessage buildMessage, String imageTag) {
    // must match the pattern in template.yml
    try {
      BuildEventDetail buildEvent =
          new BuildEventDetail(
              buildMessage.deploymentId(),
              buildMessage.userId(),
              imageTag,
              buildMessage.correlationId());
      String eventDetail = objectMapper.writeValueAsString(buildEvent);

      PutEventsRequestEntry entry =
          PutEventsRequestEntry.builder()
              .source("com.hosting.dispatcher")
              .detailType("CodeBuildJobStarted")
              .detail(eventDetail)
              .build();

      PutEventsRequest eventsRequest = PutEventsRequest.builder().entries(entry).build();

      eventBridgeClient.putEvents(eventsRequest);
      LOGGER.info("Emitted EventBridge event after Image push");
    } catch (Exception e) {
      LOGGER.error("Failed to emit EventBridge event after Image push", e);
    }
  }
}
