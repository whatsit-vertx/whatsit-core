package io.github.pangzixiang.whatsit.vertx.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * The type Health dependency.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HealthDependency {
    private String name;
    private boolean isHealth;
    private LocalDateTime lastUpdated;
}
