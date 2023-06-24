package io.github.pangzixiang.whatsit.vertx.core;

import io.vertx.core.Vertx;
import io.vertx.jdbcclient.JDBCPool;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * The type Application context.
 */
@Slf4j
public class ApplicationContext {

    private static ApplicationContext applicationContext;

    @Getter
    private final Vertx vertx;

    @Setter
    private int port;

    @Getter
    @Setter
    private JDBCPool jdbcPool;

    public static ApplicationContext getApplicationContext() {
        if (applicationContext == null) {
            applicationContext = new ApplicationContext();
        }
        return applicationContext;
    }

    private ApplicationContext() {
        this.vertx = Vertx.vertx(ApplicationConfiguration.getInstance().getVertxOptions());
    }

    /**
     * Gets port.
     *
     * @return the port
     */
    public int getPort() {
        if (port == 0) {
            return ApplicationConfiguration.getInstance().getPort();
        } else {
            return port;
        }
    }
}
