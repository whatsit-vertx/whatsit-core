package io.github.pangzixiang.whatsit.vertx.core.scheduler;

import io.github.pangzixiang.whatsit.vertx.core.ApplicationConfiguration;
import io.github.pangzixiang.whatsit.vertx.core.annotation.Schedule;
import io.github.pangzixiang.whatsit.vertx.core.ApplicationContext;
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
    private LocalDateTime lastUpdated = LocalDateTime.now();

    public static final String APPLICATION_HEALTH = "application-health";
    public static final String DATABASE_HEALTH = "database";

    /**
     * Instantiates a new Health check schedule job.
     *
     */
    public DatabaseHealthCheckScheduleJob() {
        SQL = ApplicationConfiguration.getInstance().getHealthCheckSql();
    }

    @Override
    @Schedule(configKey = "database.healthCheck")
    public void execute() {
        log.debug("Starting to check the Database Health!");
        JDBCPool jdbcPool = ApplicationContext.getApplicationContext().getJdbcPool();
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
                            getVertx().sharedData().getLocalMap(APPLICATION_HEALTH).put(DATABASE_HEALTH, true);
                        } else {
                            log.error("Database Health Check Failed, Health Status updated to [FALSE]!");
                            getVertx().sharedData().getLocalMap(APPLICATION_HEALTH).put(DATABASE_HEALTH, false);
                        }
                    } else {
                        log.error("Database Health Check Failed, Health Status updated to [FALSE]!");
                        getVertx().sharedData().getLocalMap(APPLICATION_HEALTH).put(DATABASE_HEALTH, false);
                    }
                });
    }
}
