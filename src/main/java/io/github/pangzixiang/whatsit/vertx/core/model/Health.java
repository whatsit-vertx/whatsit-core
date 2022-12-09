package io.github.pangzixiang.whatsit.vertx.core.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * The type Health.
 */
@Data
@Builder
public class Health {
    private boolean isHealth;
    private String name;
    private int port;
    private LocalDateTime startTime;
    private long upTime;
    private List<HealthDependency> dependencies;
}
