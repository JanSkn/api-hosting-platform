package com.hosting.common.dto;

import java.util.List;

public record CloudWatchLogsResponse(List<LogEvent> events, String nextToken) {
  public record LogEvent(String message, Long timestamp) {}
}
