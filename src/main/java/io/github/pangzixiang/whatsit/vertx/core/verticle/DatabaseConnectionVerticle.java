package io.github.pangzixiang.whatsit.vertx.core.verticle;

import io.github.pangzixiang.whatsit.vertx.core.context.ApplicationContext;
import io.github.pangzixiang.whatsit.vertx.core.model.HealthDependency;
import io.github.pangzixiang.whatsit.vertx.core.scheduler.HealthCheckScheduleJob;
import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.jdbcclient.JDBCPool;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

import static io.github.pangzixiang.whatsit.vertx.core.utils.CoreUtils.createCircuitBreaker;
import static io.github.pangzixiang.whatsit.vertx.core.utils.VerticleUtils.deployVerticle;

/**
 * The type Database connection verticle.
 */
@Slf4j
public class DatabaseConnectionVerticle extends CoreVerticle {

    private final String VERIFICATION_SQL;

    private final CircuitBreaker circuitBreaker;

    /**
     * The constant DATABASE_HEALTH_NAME.
     */
    public static final String DATABASE_HEALTH_NAME = "Database";

    /**
     * Instantiates a new Database connection verticle.
     *
     * @param applicationContext the application context
     */
    public DatabaseConnectionVerticle(ApplicationContext applicationContext) {
        super(applicationContext);
        this.circuitBreaker = createCircuitBreaker(applicationContext.getVertx());
        VERIFICATION_SQL = applicationContext.getApplicationConfiguration().getHealthCheckSql();
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

                        HealthDependency databaseHealth = HealthDependency
                                .builder()
                                .isHealth(booleanAsyncResult.result())
                                .name(DATABASE_HEALTH_NAME)
                                .lastUpdated(LocalDateTime.now())
                                .build();

                        getApplicationContext().getHealthDependencies().add(databaseHealth);

                        CompositeFuture.all(healthCheckSchedule(), flywayMigration())
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

    private Future<String> healthCheckSchedule() {
        return deployVerticle(getVertx(), new HealthCheckScheduleJob(getApplicationContext()));
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
