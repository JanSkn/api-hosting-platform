package com.hosting.core.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Path("/health") // if renamed, change in template.yml as well
public class HealthResource {

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response checkHealth() {
    Map<String, Object> healthInfo = new LinkedHashMap<>();

    healthInfo.put("status", "UP");
    healthInfo.put("timestamp", Instant.now().toString());

    Map<String, Object> systemInfo = new LinkedHashMap<>();
    systemInfo.put("javaVersion", System.getProperty("java.version"));
    systemInfo.put("osName", System.getProperty("os.name"));

    Runtime runtime = Runtime.getRuntime();
    systemInfo.put("availableProcessors", runtime.availableProcessors());
    systemInfo.put("freeMemoryBytes", runtime.freeMemory());
    systemInfo.put("totalMemoryBytes", runtime.totalMemory());
    systemInfo.put("maxMemoryBytes", runtime.maxMemory());

    healthInfo.put("system", systemInfo);

    return Response.ok(healthInfo).build();
  }
}
