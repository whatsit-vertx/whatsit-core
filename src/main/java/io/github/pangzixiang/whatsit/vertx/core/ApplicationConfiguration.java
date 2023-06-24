package io.github.pangzixiang.whatsit.vertx.core;

import com.typesafe.config.*;
import io.github.pangzixiang.whatsit.vertx.core.constant.ConfigurationConstants;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.Json;
import io.vertx.jdbcclient.JDBCConnectOptions;
import io.vertx.sqlclient.PoolOptions;
import jakarta.ws.rs.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * The type Application configuration.
 */
@Slf4j
public class ApplicationConfiguration {

    @Getter
    private final Config config;

    @Setter
    private VertxOptions vertxOptions;

    @Setter
    private HttpServerOptions httpServerOptions;

    @Getter
    private final List<Class<? extends Annotation>> supportHttpMethodsList = List.of(GET.class, HEAD.class, POST.class, OPTIONS.class, PUT.class, DELETE.class, PATCH.class);

    private static ApplicationConfiguration applicationConfiguration;

    public static ApplicationConfiguration getInstance() {
        if (applicationConfiguration == null) {
            applicationConfiguration = new ApplicationConfiguration();
        }
        return applicationConfiguration;
    }

    /**
     * Instantiates a new Application configuration.
     */
    private ApplicationConfiguration() {
        log.info("LOAD CONFIG FILE [{}]", Objects.requireNonNullElseGet(getConfigResource(),
                () -> Objects.requireNonNullElse(getConfigFile(), "reference.conf")));
        this.config = ConfigFactory.load();
    }

    /**
     * Gets value.
     *
     * @param key the key
     * @return the value
     */
    public Object getValue(String key) {
        try {
            return this.config.getValue(key).unwrapped();
        } catch (ConfigException e) {
            log.warn("Unable to get value for key [{}], thus return null", key, e);
            return null;
        }
    }

    /**
     * Gets string.
     *
     * @param key the key
     * @return the string
     */
    public String getString(String key) {
        return (String) this.getValue(key);
    }

    /**
     * Gets integer.
     *
     * @param key the key
     * @return the integer
     */
    public Integer getInteger(String key) {
        return (Integer) this.getValue(key);
    }

    /**
     * Gets boolean.
     *
     * @param key the key
     * @return the boolean
     */
    public Boolean getBoolean(String key) {
        return (Boolean) this.getValue(key);
    }

    /**
     * Gets config.
     *
     * @param key the key
     * @return the config
     */
    public Config getConfig(String key) {
        return this.config.getConfig(key);
    }

    /**
     * Gets config file.
     *
     * @return the config file
     */
    public String getConfigFile() {
        return System.getProperty(ConfigurationConstants.CONFIG_FILE);
    }

    /**
     * Gets config resource.
     *
     * @return the config resource
     */
    public String getConfigResource() {
        return System.getProperty(ConfigurationConstants.CONFIG_RESOURCE);
    }

    /**
     * Gets port.
     *
     * @return the port
     */
    public Integer getPort() {
        return getInteger(ConfigurationConstants.PORT);
    }

    /**
     * Gets name.
     *
     * @return the name
     */
    public String getName() {
        return getString(ConfigurationConstants.NAME);
    }

    /**
     * Gets vertx options.
     *
     * @return the vertx options
     */
    public VertxOptions getVertxOptions() {
        if (vertxOptions == null) {
            vertxOptions = new VertxOptions();

            vertxOptions.setWorkerPoolSize(getInteger(ConfigurationConstants.WORKER_POOL_SIZE));

            vertxOptions.setInternalBlockingPoolSize(getInteger(ConfigurationConstants.BLOCKING_POOL_SIZE));

            vertxOptions.setEventLoopPoolSize(getInteger(ConfigurationConstants.EVENT_LOOP_POOL_SIZE));

            vertxOptions.setHAEnabled(getBoolean(ConfigurationConstants.HA_ENABLED) != null && getBoolean(ConfigurationConstants.HA_ENABLED));

            vertxOptions.setHAGroup(getString(ConfigurationConstants.HA_GROUP));

            log.info("Init DEFAULT VertxOptions [{}]", Json.encode(vertxOptions));
        }
        return vertxOptions;
    }

    /**
     * Gets http server options.
     *
     * @return the http server options
     */
    public HttpServerOptions getHttpServerOptions() {
        if (httpServerOptions == null) {
            httpServerOptions = new HttpServerOptions();
        }
        return httpServerOptions;
    }

    /**
     * Is database enable boolean.
     *
     * @return the boolean
     */
    public Boolean isDatabaseEnable() {
        return getBoolean(ConfigurationConstants.DATABASE_ENABLE) != null && getBoolean(ConfigurationConstants.DATABASE_ENABLE);
    }

    /**
     * Gets health check sql.
     *
     * @return the health check sql
     */
    public String getHealthCheckSql() {
        return getString(ConfigurationConstants.DATABASE_HEALTH_CHECK_SQL);
    }

    /**
     * Gets jdbc pool options.
     *
     * @return the jdbc pool options
     */
    public PoolOptions getJDBCPoolOptions() {
        PoolOptions poolOptions = new PoolOptions();

        poolOptions.setMaxSize(getInteger(ConfigurationConstants.DATABASE_MAX_POOL_SIZE));

        poolOptions.setConnectionTimeout(getInteger(ConfigurationConstants.DATABASE_CONNECTION_TIMEOUT));
        poolOptions.setConnectionTimeoutUnit(TimeUnit.SECONDS);

        poolOptions.setIdleTimeout(getInteger(ConfigurationConstants.DATABASE_IDLE_TIMEOUT));
        poolOptions.setIdleTimeoutUnit(TimeUnit.SECONDS);

        poolOptions.setEventLoopSize(getInteger(ConfigurationConstants.DATABASE_EVENT_LOOP_SIZE));

        poolOptions.setShared(true);

        return poolOptions;
    }

    /**
     * Gets jdbc connect options.
     *
     * @return the jdbc connect options
     */
    public JDBCConnectOptions getJDBCConnectOptions() {
        String url = getString(ConfigurationConstants.DATABASE_URL);
        String user = getString(ConfigurationConstants.DATABASE_USER);
        String password = getString(ConfigurationConstants.DATABASE_PASSWORD);
        if (StringUtils.isAnyBlank(url, user)) {
            String err = String.format("Failed to get JDBC connection options, " +
                    "url & user expect NonBlank, but got -> url: [%s], user: [%s]", url, user);
            log.error(err, new RuntimeException(err));
            System.exit(-1);
        }

        if (password == null) {
            String err = "Failed to get JDBC connection options, password expects NonNull";
            log.error(err, new RuntimeException(err));
            System.exit(-1);
        }

        JDBCConnectOptions jdbcConnectOptions = new JDBCConnectOptions();
        jdbcConnectOptions.setJdbcUrl(url);
        jdbcConnectOptions.setUser(user);
        jdbcConnectOptions.setPassword(password);

        return jdbcConnectOptions;
    }
}
