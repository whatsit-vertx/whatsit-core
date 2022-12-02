package io.github.pangzixiang.whatsit.vertx.core.config;

import com.typesafe.config.*;
import io.github.pangzixiang.whatsit.vertx.core.config.cache.CacheConfiguration;
import io.github.pangzixiang.whatsit.vertx.core.constant.ConfigurationConstants;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.jdbcclient.JDBCConnectOptions;
import io.vertx.sqlclient.PoolOptions;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    /**
     * Instantiates a new Application configuration.
     */
    public ApplicationConfiguration() {
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

            log.info("Init DEFAULT VertxOptions [{}]", vertxOptions);
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
     * Is flyway migrate boolean.
     *
     * @return the boolean
     */
    public Boolean isFlywayMigrate() {
        return getBoolean(ConfigurationConstants.DATABASE_ENABLE) != null && getBoolean(ConfigurationConstants.DATABASE_ENABLE)
                && getBoolean(ConfigurationConstants.DATABASE_FLYWAY_ENABLE) != null && getBoolean(ConfigurationConstants.DATABASE_FLYWAY_ENABLE);
    }

    /**
     * Flyway location string.
     *
     * @return the string
     */
    public String flywayLocation() {
        return getString(ConfigurationConstants.DATABASE_FLYWAY_LOCATION);
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

    /**
     * Is cache enable boolean.
     *
     * @return the boolean
     */
    public Boolean isCacheEnable() {
        return getBoolean(ConfigurationConstants.CACHE_ENABLE) != null && getBoolean(ConfigurationConstants.CACHE_ENABLE);
    }

    /**
     * Is cache auto creation boolean.
     *
     * @return the boolean
     */
    public Boolean isCacheAutoCreation() {
        return getBoolean(ConfigurationConstants.CACHE_AUTO_CREATION) != null && getBoolean(ConfigurationConstants.CACHE_AUTO_CREATION);
    }

    /**
     * Gets custom cache.
     *
     * @return the custom cache
     */
    public Map<String, CacheConfiguration> getCustomCache() {
        Map<String, CacheConfiguration> result = new HashMap<>();
        List<? extends Config> configList;
        try {
            configList = this.config.getConfigList(ConfigurationConstants.CACHE_CUSTOM);
        } catch (ConfigException e) {
            log.warn("Custom Config NOT FOUND!");
            return result;
        }
        for (Config c : configList) {
            CacheConfiguration cacheConfiguration = new CacheConfiguration();
            String name;
            try {
                name = c.getString(ConfigurationConstants.CUSTOM_CACHE_NAME);
            } catch (ConfigException e) {
                log.warn("Empty Cache name, hence SKIP!");
                continue;
            }

            if (StringUtils.isBlank(name)) {
                log.warn("Invalid Cache name [{}], hence SKIP", name);
                continue;
            }

            try {
                boolean enable = c.getBoolean(ConfigurationConstants.CUSTOM_CACHE_ENABLE);
                cacheConfiguration.setEnable(enable);
            } catch (ConfigException e) {
                log.warn("Empty Cache Config::enable, hence set to DEFAULT");
            }

            try {
                int initSize = c.getInt(ConfigurationConstants.CUSTOM_CACHE_INIT_SIZE);
                cacheConfiguration.setInitSize(initSize);
            } catch (ConfigException e) {
                log.warn("Empty Cache Config::initSize, hence set to DEFAULT");
            }

            try {
                int maxSize = c.getInt(ConfigurationConstants.CUSTOM_CACHE_MAX_SIZE);
                cacheConfiguration.setMaxSize(maxSize);
            } catch (ConfigException e) {
                log.warn("Empty Cache Config::maxSize, hence set to DEFAULT");
            }

            try {
                int expireTime = c.getInt(ConfigurationConstants.CUSTOM_CACHE_EXPIRE_TIME);
                cacheConfiguration.setExpireTime(expireTime);
            } catch (ConfigException e) {
                log.warn("Empty Cache Config::expireTime, hence set to DEFAULT");
            }

            result.put(name, cacheConfiguration);
        }
        return result;
    }
}
