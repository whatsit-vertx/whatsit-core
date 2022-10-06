package io.github.pangzixiang.whatsit.vertx.core.scheduler;

import io.github.pangzixiang.whatsit.vertx.core.annotation.Schedule;
import io.github.pangzixiang.whatsit.vertx.core.context.ApplicationContext;
import io.github.pangzixiang.whatsit.vertx.core.model.HealthDependency;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.Row;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HealthCheckScheduleJob extends BaseScheduleJob{

    public HealthCheckScheduleJob(ApplicationContext applicationContext) {
        super(applicationContext);
    }

    @Override
    @Schedule(configKey = "schedule.healthCheck")
    public void execute() {
        log.debug("Starting to check the Database Health!");
        JDBCPool jdbcPool = getApplicationContext().getJdbcPool();
        jdbcPool
                .preparedQuery("select 1 from dual")
                .execute()
                .onComplete(rowSetAsyncResult -> {
                   if (rowSetAsyncResult.succeeded()) {
                       Row row = rowSetAsyncResult.result().iterator().next();
                       Integer result = row.getInteger(0);
                       if (result.equals(1)) {
                           log.debug("Database Health Check Done!");
                           HealthDependency.DatabaseHealth databaseHealth =
                                   new HealthDependency.DatabaseHealth(true);
                           getApplicationContext().getHealthDependency().setDatabaseHealth(databaseHealth);
                       } else {
                           HealthDependency.DatabaseHealth databaseHealth =
                                   new HealthDependency.DatabaseHealth(false);
                           getApplicationContext().getHealthDependency().setDatabaseHealth(databaseHealth);
                           log.error("Database Health Check Failed, Health Status updated to [FALSE]!");
                       }
                   }
                });
    }
}
