package io.github.pangzixiang.whatsit.vertx.core.verticle;

import io.github.pangzixiang.whatsit.vertx.core.context.ApplicationContext;
import io.vertx.core.Promise;
import io.vertx.jdbcclient.JDBCConnectOptions;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.Location;

import java.nio.charset.StandardCharsets;

@Slf4j
public class FlywayMigrateVerticle extends CoreVerticle{

    public FlywayMigrateVerticle(ApplicationContext applicationContext) {
        super(applicationContext);
    }

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        super.start();
        migrate(startPromise);
    }

    private void migrate(Promise<Void> startPromise) {
        log.debug("Starting to migrate the database...");
        JDBCConnectOptions jdbcConnectOptions = getApplicationContext().getApplicationConfiguration().getJDBCConnectOptions();
        Flyway flyway = Flyway.configure()
                .initSql(getApplicationContext().getApplicationConfiguration().getHealthCheckSql())
                .dataSource(jdbcConnectOptions.getJdbcUrl(), jdbcConnectOptions.getUser(), jdbcConnectOptions.getPassword())
                .encoding(StandardCharsets.UTF_8)
                .connectRetries(3)
                .failOnMissingLocations(true)
                .locations(new Location(getApplicationContext().getApplicationConfiguration().flywayLocation()))
                .validateMigrationNaming(true)
                .validateOnMigrate(true)
                .load();
        flyway.migrate();
        startPromise.complete();
    }
}
