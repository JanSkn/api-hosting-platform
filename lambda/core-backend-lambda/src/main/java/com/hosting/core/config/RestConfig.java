package com.hosting.core.config;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

// base path for all API endpoints, can be changed if needed but must be consistent with health
// endpoint in template.yml
@ApplicationPath("/api/v1")
public class RestConfig extends Application {}
