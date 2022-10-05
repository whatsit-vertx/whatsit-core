package io.github.pangzixiang.whatsit.vertx.core.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Health {
    private boolean isHealth;
    private String name;
    private int port;
    private String startTime;
    private long upTime;
    private HealthDependency dependency;
}
