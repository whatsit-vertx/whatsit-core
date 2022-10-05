package io.github.pangzixiang.whatsit.vertx.core.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import io.github.pangzixiang.whatsit.vertx.core.config.ApplicationConfiguration;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;

import static java.time.LocalDateTime.now;

@Getter
@Setter
public class HealthDependency {

    private DatabaseHealth databaseHealth;

    public HealthDependency(ApplicationConfiguration applicationConfiguration) {
        if (applicationConfiguration.isDatabaseEnable()) {
            this.databaseHealth = new DatabaseHealth(false);
        }
    }

    @Getter
    @Setter
    public static class DatabaseHealth {
        private Boolean isHealth;

        @JsonSerialize(using = LocalDateTimeSerializer.class)
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime lastUpdated;

        public DatabaseHealth(Boolean isHealth) {
            this.isHealth = isHealth;
            this.lastUpdated = now();
        }
    }
}
