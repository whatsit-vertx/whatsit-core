package io.github.pangzixiang.whatsit.vertx.core.verticle;

import io.github.pangzixiang.whatsit.vertx.core.context.ApplicationContext;
import io.github.pangzixiang.whatsit.vertx.core.model.HealthDependency;
import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.jdbcclient.JDBCPool;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

import static io.github.pangzixiang.whatsit.vertx.core.utils.CoreUtils.createCircuitBreaker;
import static io.github.pangzixiang.whatsit.vertx.core.utils.VerticleUtils.deployVerticle;

@Slf4j
public class DatabaseConnectionVerticle extends CoreVerticle {

    public static final String VERIFICATION_SQL = "select 1 from dual";

    private CircuitBreaker circuitBreaker;

    public DatabaseConnectionVerticle(ApplicationContext applicationContext) {
        super(applicationContext);
    }

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        super.start();
        log.info("Starting to connect to database");
        this.circuitBreaker = createCircuitBreaker(getVertx());
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

                        CompositeFuture.all(healthCheckSchedule(jdbcPool), flywayMigration())
                                .onComplete(compositeFutureAsyncResult -> {
                                    if (compositeFutureAsyncResult.succeeded()) {
                                        startPromise.complete();
                                        log.info("Database setup done!");
                                    } else {
                                        startPromise.fail(compositeFutureAsyncResult.cause());
                                    }
                                });
                    } else {
                        startPromise.fail(booleanAsyncResult.cause());
                        log.error("Database Connection FAILED!!!");
                        System.exit(-1);
                    }
                });
    }

    private Future<Boolean> verify(JDBCPool jdbcPool) {
        return circuitBreaker.execute(promise -> {
            jdbcPool
                    .preparedQuery(VERIFICATION_SQL)
                    .execute()
                    .compose(rows -> {
                        Integer result = rows.iterator().next().getInteger(0);
                        if (result.equals(1)) {
                            log.debug("Database Verification passed! [expect: 1, result: {}]", result);
                            return Future.succeededFuture(true);
                        } else {
                            String err = String.format("Database Verification Failed! [expect: 1, result: %s]", result);
                            log.error(err);
                            return Future.succeededFuture(false);
                        }
                    })
                    .onFailure(throwable -> {
                        log.error("Database Connection Failed with ERROR: {}, ", throwable.getMessage(), throwable);
                        promise.fail(throwable);
                    })
                    .onSuccess(promise::complete);
        });
    }

    private Future<Void> healthCheckSchedule(JDBCPool jdbcPool) {
        getVertx()
                .setPeriodic(TimeUnit.SECONDS.toMillis(getApplicationContext().getApplicationConfiguration().getHealthCheckPeriod()),
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
        log.debug("Added periodic Database Health checking!");
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
            log.debug("Flyway is disabled, thus skip migration!");
            return Future.succeededFuture();
        }
    }
}
