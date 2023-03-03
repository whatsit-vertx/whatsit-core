package io.github.pangzixiang.whatsit.vertx.core.constant;

/**
 * The type Configuration constants.
 */
public class ConfigurationConstants {
    private ConfigurationConstants() {
        super();
    }

    /**
     * The constant CONFIG_FILE.
     */
    public static final String CONFIG_FILE = "config.file";

    /**
     * The constant CONFIG_RESOURCE.
     */
    public static final String CONFIG_RESOURCE = "config.resource";

    /**
     * The constant PORT.
     */
    public static final String PORT = "app.port";

    /**
     * The constant NAME.
     */
    public static final String NAME = "app.name";

    /**
     * The constant WORKER_POOL_SIZE.
     */
    public static final String WORKER_POOL_SIZE = "vertx.worker-pool-size";

    /**
     * The constant BLOCKING_POOL_SIZE.
     */
    public static final String BLOCKING_POOL_SIZE = "vertx.blocking-pool-size";

    /**
     * The constant EVENT_LOOP_POOL_SIZE.
     */
    public static final String EVENT_LOOP_POOL_SIZE = "vertx.eventLoop-pool-size";

    /**
     * The constant HA_ENABLED.
     */
    public static final String HA_ENABLED = "vertx.ha-enabled";

    /**
     * The constant HA_GROUP.
     */
    public static final String HA_GROUP = "vertx.ha-group";

    /**
     * The constant DATABASE_ENABLE.
     */
    public static final String DATABASE_ENABLE = "database.enable";

    /**
     * The constant DATABASE_URL.
     */
    public static final String DATABASE_URL = "database.url";

    /**
     * The constant DATABASE_USER.
     */
    public static final String DATABASE_USER = "database.user";

    /**
     * The constant DATABASE_PASSWORD.
     */
    public static final String DATABASE_PASSWORD = "database.password";

    /**
     * The constant DATABASE_MAX_POOL_SIZE.
     */
    public static final String DATABASE_MAX_POOL_SIZE = "database.maxPoolSize";

    /**
     * The constant DATABASE_EVENT_LOOP_SIZE.
     */
    public static final String DATABASE_EVENT_LOOP_SIZE = "database.eventLoopSize";

    /**
     * The constant DATABASE_CONNECTION_TIMEOUT.
     */
    public static final String DATABASE_CONNECTION_TIMEOUT = "database.connectionTimeout";

    /**
     * The constant DATABASE_IDLE_TIMEOUT.
     */
    public static final String DATABASE_IDLE_TIMEOUT = "database.idleTimeout";

    /**
     * The constant DATABASE_FLYWAY_ENABLE.
     */
    public static final String DATABASE_FLYWAY_ENABLE = "database.flyway.enable";

    /**
     * The constant DATABASE_FLYWAY_LOCATION.
     */
    public static final String DATABASE_FLYWAY_LOCATION = "database.flyway.location";

    /**
     * The constant DATABASE_HEALTH_CHECK_SQL.
     */
    public static final String DATABASE_HEALTH_CHECK_SQL = "database.healthCheckSql";

    /**
     * The constant CACHE_ENABLE.
     */
    public static final String CACHE_ENABLE = "cache.enable";

    /**
     * The constant CACHE_CUSTOM.
     */
    public static final String CACHE_CUSTOM = "cache.custom";

    /**
     * The constant CUSTOM_CACHE_NAME.
     */
    public static final String CUSTOM_CACHE_NAME = "name";

    /**
     * The constant CUSTOM_CACHE_EXPIRE_TIME.
     */
    public static final String CUSTOM_CACHE_EXPIRE_TIME = "expireTime";

    /**
     * The constant CUSTOM_CACHE_MAX_SIZE.
     */
    public static final String CUSTOM_CACHE_MAX_SIZE = "maxSize";

    /**
     * The constant CUSTOM_CACHE_INIT_SIZE.
     */
    public static final String CUSTOM_CACHE_INIT_SIZE = "initSize";

    /**
     * The constant CUSTOM_CACHE_ENABLE.
     */
    public static final String CUSTOM_CACHE_ENABLE = "enable";

    /**
     * The constant CACHE_AUTO_CREATION.
     */
    public static final String CACHE_AUTO_CREATION = "cache.autoCreation";

    public static final String HEALTH_ENABLE = "health.enable";

    public static final String HEALTH_PATH = "health.path";
}
