package io.github.pangzixiang.whatsit.vertx.core.constant;

public class ConfigurationConstants {
    private ConfigurationConstants() {
        super();
    }

    public static final String CONFIG_FILE = "config.resource";

    public static final String PORT = "app.port";

    public static final String NAME = "app.name";

    public static final String WORKER_POOL_SIZE = "vertx.worker-pool-size";

    public static final String BLOCKING_POOL_SIZE = "vertx.blocking-pool-size";

    public static final String EVENT_LOOP_POOL_SIZE = "vertx.eventLoop-pool-size";

    public static final String HA_ENABLED = "vertx.ha-enabled";

    public static final String HA_GROUP = "vertx.ha-group";

    public static final String JMX_METRICS_ENABLE = "vertx.jmxMetrics.enable";

    public static final String JMX_METRICS_PERIOD_IN_SECOND = "vertx.jmxMetrics.periodInSec";

    public static final String DATABASE_ENABLE = "database.enable";

    public static final String DATABASE_URL = "database.url";

    public static final String DATABASE_USER = "database.user";

    public static final String DATABASE_PASSWORD = "database.password";

    public static final String DATABASE_MAX_POOL_SIZE = "database.maxPoolSize";

    public static final String DATABASE_EVENT_LOOP_SIZE = "database.eventLoopSize";

    public static final String DATABASE_CONNECTION_TIMEOUT = "database.connectionTimeout";

    public static final String DATABASE_IDLE_TIMEOUT = "database.idleTimeout";

    public static final String DATABASE_FLYWAY_ENABLE = "database.flyway.enable";

    public static final String DATABASE_FLYWAY_LOCATION = "database.flyway.location";

    public static final String DATABASE_HEALTH_CHECK_SQL = "database.healthCheckSql";

    public static final String CACHE_ENABLE = "cache.enable";

    public static final String CACHE_CUSTOM = "cache.custom";

    public static final String CUSTOM_CACHE_NAME = "name";

    public static final String CUSTOM_CACHE_EXPIRE_TIME = "expireTime";

    public static final String CUSTOM_CACHE_MAX_SIZE = "maxSize";

    public static final String CUSTOM_CACHE_INIT_SIZE = "initSize";

    public static final String CUSTOM_CACHE_ENABLE = "enable";

    public static final String CACHE_AUTO_CREATION = "cache.autoCreation";
}
