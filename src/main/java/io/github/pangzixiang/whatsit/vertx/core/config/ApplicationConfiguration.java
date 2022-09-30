package io.github.pangzixiang.whatsit.vertx.core.config;

import com.typesafe.config.*;
import io.github.pangzixiang.whatsit.vertx.core.config.cache.CacheConfiguration;
import io.github.pangzixiang.whatsit.vertx.core.constant.ConfigurationConstants;
import io.vertx.core.VertxOptions;
import io.vertx.jdbcclient.JDBCConnectOptions;
import io.vertx.sqlclient.PoolOptions;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.requireNonNullElseGet;

@Slf4j
public class ApplicationConfiguration {

    @Getter
    private final Config config;

    public ApplicationConfiguration() {
        this.config = ConfigFactory.load(getEnv());
    }

    private void logForKey(String format, String key) {
        log.warn("Unable to get {} value for key [{}], thus return null"
                , format, key);
    }

    private void logForValue(String value, String defaultValue) {
        log.warn("Unable to get {} value from environment variables, thus set to default value -> {}"
                , value, defaultValue);
    }

    public String getString(String key) {
        try {
            return this.config.getString(key);
        } catch (ConfigException e) {
            logForKey("String", key);
            return null;
        }

    }

    public Integer getInteger(String key) {
        try {
            return this.config.getInt(key);
        } catch (ConfigException e) {
            logForKey("Integer", key);
            return null;
        }
    }

    public Boolean getBoolean(String key) {
        try {
            return this.config.getBoolean(key);
        } catch (ConfigException e) {
            logForKey("Boolean", key);
            return null;
        }
    }

    public Config getConfig(String key) {
        try {
            return this.config.getConfig(key);
        } catch (ConfigException e) {
            logForKey("Config", key);
            return null;
        }
    }

    public String getEnv() {
        return requireNonNullElseGet(System.getProperty(ConfigurationConstants.ENV), () -> {
            logForValue("ENV", ConfigurationConstants.DEFAULT.LOCAL);
            return ConfigurationConstants.DEFAULT.LOCAL;
        });
    }

    public Integer getPort() {
        return requireNonNullElseGet(getInteger(ConfigurationConstants.PORT), () -> {
            logForValue("PORT", String.valueOf(ConfigurationConstants.DEFAULT.PORT));
            return ConfigurationConstants.DEFAULT.PORT;
        });
    }

    public String getName() {
        return requireNonNullElseGet(getString(ConfigurationConstants.NAME), () -> {
            logForValue("Service Name", ConfigurationConstants.DEFAULT.NAME);
            return ConfigurationConstants.DEFAULT.NAME;
        });
    }

    public Integer getHealthCheckPeriod() {
        return requireNonNullElseGet(getInteger(ConfigurationConstants.HEALTH_CHECK_PERIOD), () -> {
           logForValue("Health Check Period", String.valueOf(ConfigurationConstants.DEFAULT.HEALTH_CHECK_PERIOD));
           return ConfigurationConstants.DEFAULT.HEALTH_CHECK_PERIOD;
        });
    }

    public VertxOptions getVertxOptions() {
        VertxOptions options = new VertxOptions();

        options.setWorkerPoolSize(requireNonNullElseGet(getInteger(ConfigurationConstants.WORKER_POOL_SIZE), () -> {
            logForValue("Worker Pool Size", String.valueOf(ConfigurationConstants.DEFAULT.WORKER_POOL_SIZE));
            return ConfigurationConstants.DEFAULT.WORKER_POOL_SIZE;
        }));

        options.setInternalBlockingPoolSize(requireNonNullElseGet(getInteger(ConfigurationConstants.BLOCKING_POOL_SIZE), () -> {
            logForValue("Blocking Pool Size", String.valueOf(ConfigurationConstants.DEFAULT.BLOCKING_POOL_SIZE));
            return ConfigurationConstants.DEFAULT.BLOCKING_POOL_SIZE;
        }));

        options.setEventLoopPoolSize(requireNonNullElseGet(getInteger(ConfigurationConstants.EVENT_LOOP_POOL_SIZE), () -> {
            logForValue("Event Loop Pool Size", String.valueOf(ConfigurationConstants.DEFAULT.EVENT_LOOP_POOL_SIZE));
            return ConfigurationConstants.DEFAULT.EVENT_LOOP_POOL_SIZE;
        }));

        options.setHAEnabled(getBoolean(ConfigurationConstants.HA_ENABLED) != null && getBoolean(ConfigurationConstants.HA_ENABLED));

        options.setHAGroup(requireNonNullElseGet(getString(ConfigurationConstants.HA_GROUP), () -> {
            logForValue("HA Group Name", ConfigurationConstants.DEFAULT.HA_GROUP_NAME);
            return ConfigurationConstants.DEFAULT.HA_GROUP_NAME;
        }));

        return options;
    }

    public Boolean isDatabaseEnable() {
        return getBoolean(ConfigurationConstants.DATABASE_ENABLE) != null && getBoolean(ConfigurationConstants.DATABASE_ENABLE);
    }

    public Boolean isFlywayMigrate() {
        return getBoolean(ConfigurationConstants.DATABASE_ENABLE) != null && getBoolean(ConfigurationConstants.DATABASE_ENABLE)
                && getBoolean(ConfigurationConstants.DATABASE_FLYWAY_ENABLE) != null && getBoolean(ConfigurationConstants.DATABASE_FLYWAY_ENABLE);
    }

    public String flywayLocation() {
        return requireNonNullElseGet(getString(ConfigurationConstants.DATABASE_FLYWAY_LOCATION), () -> {
            logForValue("Flyway location", ConfigurationConstants.DEFAULT.DATABASE_FLYWAY_LOCATION);
            return ConfigurationConstants.DEFAULT.DATABASE_FLYWAY_LOCATION;
        });
    }

    public PoolOptions getJDBCPoolOptions() {
        PoolOptions poolOptions = new PoolOptions();

        poolOptions.setMaxSize(requireNonNullElseGet(getInteger(ConfigurationConstants.DATABASE_MAX_POOL_SIZE), () -> {
            logForValue("Database Max Pool Size", String.valueOf(ConfigurationConstants.DEFAULT.DATABASE_MAX_POOL_SIZE));
            return ConfigurationConstants.DEFAULT.DATABASE_MAX_POOL_SIZE;
        }));

        poolOptions.setConnectionTimeout(requireNonNullElseGet(getInteger(ConfigurationConstants.DATABASE_CONNECTION_TIMEOUT), () -> {
            logForValue("Database Connection Timeout", String.valueOf(ConfigurationConstants.DEFAULT.DATABASE_CONNECTION_TIMEOUT));
            return ConfigurationConstants.DEFAULT.DATABASE_CONNECTION_TIMEOUT;
        }));
        poolOptions.setConnectionTimeoutUnit(TimeUnit.SECONDS);

        poolOptions.setIdleTimeout(requireNonNullElseGet(getInteger(ConfigurationConstants.DATABASE_IDLE_TIMEOUT), () -> {
            logForValue("Database Idle Timeout", String.valueOf(ConfigurationConstants.DEFAULT.DATABASE_IDLE_TIMEOUT));
            return ConfigurationConstants.DEFAULT.DATABASE_IDLE_TIMEOUT;
        }));
        poolOptions.setIdleTimeoutUnit(TimeUnit.SECONDS);

        poolOptions.setEventLoopSize(requireNonNullElseGet(getInteger(ConfigurationConstants.DATABASE_EVENT_LOOP_SIZE), () -> {
            logForValue("Database Event Loop Size", String.valueOf(ConfigurationConstants.DEFAULT.DATABASE_EVENT_LOOP_SIZE));
            return ConfigurationConstants.DEFAULT.DATABASE_EVENT_LOOP_SIZE;
        }));

        poolOptions.setShared(true);

        return poolOptions;
    }

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

    public Boolean isCacheEnable() {
        return getBoolean(ConfigurationConstants.CACHE_ENABLE) != null && getBoolean(ConfigurationConstants.CACHE_ENABLE);
    }

    public Boolean isCacheAutoCreation() {
        return getBoolean(ConfigurationConstants.CACHE_AUTO_CREATION) != null && getBoolean(ConfigurationConstants.CACHE_AUTO_CREATION);
    }

    public Map<String, CacheConfiguration> getCustomCache() {
        Map<String, CacheConfiguration> result = new HashMap<>();
        List<? extends Config> configList;
        try {
            configList = this.config.getConfigList(ConfigurationConstants.CACHE_CUSTOM);
        } catch (ConfigException e) {
            log.warn("Custom Config NOT FOUND!");
            return result;
        }
        for (Config c: configList) {
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
            } catch (ConfigException e){
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
            } catch (ConfigException e){
                log.warn("Empty Cache Config::expireTime, hence set to DEFAULT");
            }

            result.put(name, cacheConfiguration);
        }
        return result;
    }
}
