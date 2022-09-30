package io.github.pangzixiang.whatsit.core.verticle;

import io.github.pangzixiang.whatsit.core.context.ApplicationContext;
import io.github.pangzixiang.whatsit.core.model.HealthDependency;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.jdbcclient.JDBCPool;
import lombok.extern.slf4j.Slf4j;

import static io.github.pangzixiang.whatsit.core.utils.VerticleUtils.deployVerticle;

@Slf4j
public class DatabaseConnectionVerticle extends CoreVerticle {

    public static final String VERIFICATION_SQL = "select 1 from dual";

    public DatabaseConnectionVerticle(ApplicationContext applicationContext) {
        super(applicationContext);
    }

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        super.start();
        log.info("Starting to connect to database");
        connect(startPromise);
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        if (getApplicationContext().getJdbcPool() != null) {
            getApplicationContext().getJdbcPool()
                    .close()
                    .onSuccess(success -> log.info("Database Connection Closed!"));
        }
    }

    private void connect(Promise<Void> startPromise) {
        JDBCPool jdbcPool = JDBCPool.pool(getVertx(),
                getApplicationContext().getApplicationConfiguration().getJDBCConnectOptions(),
                getApplicationContext().getApplicationConfiguration().getJDBCPoolOptions());

        verify(jdbcPool)
                .onComplete(booleanAsyncResult -> {
                    if (booleanAsyncResult.succeeded()) {
                        getApplicationContext().setJdbcPool(jdbcPool);
                        log.info("Database Connected [ {} ]!", booleanAsyncResult.result());
                        HealthDependency.DatabaseHealth databaseHealth = new HealthDependency.DatabaseHealth(booleanAsyncResult.result());
                        getApplicationContext().getHealthDependency().setDatabaseHealth(databaseHealth);
                    } else {
                        startPromise.fail(booleanAsyncResult.cause());
                    }
                })
                .compose(then ->
                        CompositeFuture.all(healthCheckSchedule(jdbcPool), flywayMigration())
                                .onComplete(compositeFutureAsyncResult -> {
                                    if (compositeFutureAsyncResult.succeeded()) {
                                        startPromise.complete();
                                        log.info("Database setup done!");
                                    } else {
                                        startPromise.fail(compositeFutureAsyncResult.cause());
                                    }
                                })
                );
    }

    private Future<Boolean> verify(JDBCPool jdbcPool) {
        return jdbcPool
                .preparedQuery(VERIFICATION_SQL)
                .execute()
                .map(rows -> {
                    Integer result = rows.iterator().next().getInteger(0);
                    if (result.equals(1)) {
                        log.debug("Database Verification passed! [expect: 1, result: {}]", result);
                        return true;
                    } else {
                        String err = String.format("Database Verification Failed! [expect: 1, result: %s]", result);
                        log.error(err);
                        return false;
                    }
                })
                .onFailure(throwable -> {
                    log.error("Database Connection Failed with ERROR: {}, ", throwable.getMessage(), throwable);
                });
    }

    private Future<Void> healthCheckSchedule(JDBCPool jdbcPool) {
        getVertx()
                .setPeriodic(getApplicationContext().getApplicationConfiguration().getHealthCheckPeriod()*1000,
                        handler -> {
                            log.debug("Starting to check the Database Health [ {} s ]",
                                    getApplicationContext().getApplicationConfiguration().getHealthCheckPeriod());
                            verify(jdbcPool)
                                    .onComplete(booleanAsyncResult -> {
                                        if (booleanAsyncResult.succeeded()) {
                                            log.debug("Database Health Check Done! [ Connection: {} ]", booleanAsyncResult.result());
                                            HealthDependency.DatabaseHealth databaseHealth =
                                                    new HealthDependency.DatabaseHealth(booleanAsyncResult.result());
                                            getApplicationContext().getHealthDependency().setDatabaseHealth(databaseHealth);
                                        } else {
                                            HealthDependency.DatabaseHealth databaseHealth =
                                                    new HealthDependency.DatabaseHealth(false);
                                            getApplicationContext().getHealthDependency().setDatabaseHealth(databaseHealth);
                                            log.error("Database Health Check Failed, Health Status updated to [FALSE]!");
                                        }
                                    });
                        });
        log.info("Added periodic Database Health checking!");
        return Future.succeededFuture();
    }

    private Future<String> flywayMigration() {
        if (getApplicationContext().getApplicationConfiguration().isFlywayMigrate()) {
            return deployVerticle(vertx, new FlywayMigrateVerticle(getApplicationContext()))
                    .onComplete(voidAsyncResult -> {
                        if (voidAsyncResult.succeeded()){
                            log.info("FlyWay Migration done!");
                        } else {
                            log.error("FlyWay Migration Failed!", voidAsyncResult.cause());
                        }
                    });
        } else {
            log.info("Flyway is disabled, thus skip migration!");
            return Future.succeededFuture();
        }
    }
}
