package io.github.pangzixiang.whatsit.core.model;

import io.github.pangzixiang.whatsit.core.config.ApplicationConfiguration;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

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

        private Date lastUpdated = new Date(System.currentTimeMillis());

        public DatabaseHealth(Boolean isHealth) {
            this.isHealth = isHealth;
        }
    }
}
