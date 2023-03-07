package io.github.pangzixiang.whatsit.vertx.core.model;

import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * The type Health check response.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HealthCheckResponse {
    private boolean isHealthy;
    private JsonObject info;
    private Map<String, HealthStatus> healthDependencies;
    private final LocalDateTime lastUpdated = LocalDateTime.now();
}

