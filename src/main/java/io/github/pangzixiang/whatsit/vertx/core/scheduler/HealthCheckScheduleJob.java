package io.github.pangzixiang.whatsit.vertx.core.scheduler;

import io.github.pangzixiang.whatsit.vertx.core.annotation.Schedule;
import io.github.pangzixiang.whatsit.vertx.core.context.ApplicationContext;
import io.github.pangzixiang.whatsit.vertx.core.model.HealthDependency;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.Row;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Optional;

import static io.github.pangzixiang.whatsit.vertx.core.verticle.DatabaseConnectionVerticle.DATABASE_HEALTH_NAME;

@Slf4j
public class HealthCheckScheduleJob extends BaseScheduleJob{

    private final String SQL;

    public HealthCheckScheduleJob(ApplicationContext applicationContext) {
        super(applicationContext);
        SQL = applicationContext.getApplicationConfiguration().getHealthCheckSql();
    }

    @Override
    @Schedule(delayInMillis = 10_000, periodInMillis = 30_000, configKey = "database.healthCheck")
    public void execute() {
        log.debug("Starting to check the Database Health!");
        JDBCPool jdbcPool = getApplicationContext().getJdbcPool();
        jdbcPool
                .preparedQuery(SQL)
                .execute()
                .onComplete(rowSetAsyncResult -> {
                   if (rowSetAsyncResult.succeeded()) {
                       Row row = rowSetAsyncResult.result().iterator().next();
                       Integer result = row.getInteger(0);
                       Optional<HealthDependency> databaseHealthOptional = getApplicationContext()
                               .getHealthDependencies().stream()
                               .filter(healthDependency -> healthDependency.getName().equals(DATABASE_HEALTH_NAME))
                               .findFirst();
                       boolean isHealth = false;
                       if (result.equals(1)) {
                           log.debug("Database Health Check Done!");
                           isHealth = true;
                       } else {
                           log.error("Database Health Check Failed, Health Status updated to [FALSE]!");
                       }
                       if (databaseHealthOptional.isPresent()) {
                           databaseHealthOptional.get().setHealth(isHealth);
                           databaseHealthOptional.get().setLastUpdated(LocalDateTime.now());
                       } else {
                           HealthDependency databaseHealth = HealthDependency
                                   .builder()
                                   .isHealth(isHealth)
                                   .name(DATABASE_HEALTH_NAME)
                                   .lastUpdated(LocalDateTime.now())
                                   .build();

                           getApplicationContext().getHealthDependencies().add(databaseHealth);
                       }
                   }
                });
    }
}
