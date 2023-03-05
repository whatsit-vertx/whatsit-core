package io.github.pangzixiang.whatsit.vertx.core.scheduler;

import io.github.pangzixiang.whatsit.vertx.core.annotation.Schedule;
import io.github.pangzixiang.whatsit.vertx.core.context.ApplicationContext;
import io.github.pangzixiang.whatsit.vertx.core.handler.HealthCheckHandler;
import io.github.pangzixiang.whatsit.vertx.core.model.HealthStatus;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.Row;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

/**
 * The type Health check schedule job.
 */
@Slf4j
public class DatabaseHealthCheckScheduleJob extends BaseScheduleJob {

    private final String SQL;

    @Getter
    private Boolean isHealth = false;

    @Getter
    private LocalDateTime lastUpdated = LocalDateTime.now();

    /**
     * The constant DATABASE_HEALTH_NAME.
     */
    public static final String DATABASE_HEALTH_NAME = "Database";

    /**
     * Instantiates a new Health check schedule job.
     *
     * @param applicationContext the application context
     */
    public DatabaseHealthCheckScheduleJob(ApplicationContext applicationContext) {
        super(applicationContext);
        SQL = applicationContext.getApplicationConfiguration().getHealthCheckSql();
        HealthCheckHandler healthCheckHandler = getApplicationContext().getHealthCheckHandler();
        if (healthCheckHandler != null) {
            healthCheckHandler.register(DATABASE_HEALTH_NAME, promise -> {
                promise.complete(getIsHealth()? HealthStatus.succeed(getLastUpdated()): HealthStatus.fail(getLastUpdated()));
            });
        }
    }

    @Override
    @Schedule(configKey = "database.healthCheck")
    public void execute() {
        log.debug("Starting to check the Database Health!");
        JDBCPool jdbcPool = getApplicationContext().getJdbcPool();
        jdbcPool
                .preparedQuery(SQL)
                .execute()
                .onComplete(rowSetAsyncResult -> {
                    this.lastUpdated = LocalDateTime.now();
                    if (rowSetAsyncResult.succeeded()) {
                        Row row = rowSetAsyncResult.result().iterator().next();
                        Integer result = row.getInteger(0);
                        if (result.equals(1)) {
                            log.debug("Database Health Check Done!");
                            isHealth = true;
                        } else {
                            log.error("Database Health Check Failed, Health Status updated to [FALSE]!");
                            isHealth = false;
                        }
                    } else {
                        log.error("Database Health Check Failed, Health Status updated to [FALSE]!");
                        isHealth = false;
                    }
                });
    }
}
