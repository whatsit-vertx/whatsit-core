package io.github.pangzixiang.whatsit.vertx.core.context;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.pangzixiang.whatsit.vertx.core.config.ApplicationConfiguration;
import io.github.pangzixiang.whatsit.vertx.core.config.cache.CacheConfiguration;
import io.github.pangzixiang.whatsit.vertx.core.pojo.EventBusRequest;
import io.github.pangzixiang.whatsit.vertx.core.pojo.EventBusRequestCodec;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.healthchecks.HealthCheckHandler;
import io.vertx.ext.healthchecks.Status;
import io.vertx.jdbcclient.JDBCPool;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.lang.management.ManagementFactory;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import static io.github.pangzixiang.whatsit.vertx.core.constant.ConfigurationConstants.HEALTH_ENABLE;

/**
 * The type Application context.
 */
@Slf4j
public class ApplicationContext {

    @Getter
    private final Vertx vertx;

    @Getter
    @Setter
    private int port;

    @Getter
    private final ApplicationConfiguration applicationConfiguration;

    @Getter
    @Setter
    private JDBCPool jdbcPool;

    private HealthCheckHandler healthCheckHandler;

    private final ConcurrentMap<String, Cache<?, ?>> cacheMap;

    /**
     * Instantiates a new Application context.
     */
    public ApplicationContext() {
        this.applicationConfiguration = new ApplicationConfiguration();
        if (this.applicationConfiguration.isCacheEnable()) {
             this.cacheMap = this.initCacheMap();
        } else {
            this.cacheMap = null;
        }
        this.vertx = Vertx.vertx(getApplicationConfiguration().getVertxOptions());

        getVertx()
                .eventBus()
                .unregisterDefaultCodec(EventBusRequest.class)
                .registerDefaultCodec(EventBusRequest.class, new EventBusRequestCodec(EventBusRequest.class));
    }

    /**
     * Gets health check handler.
     *
     * @return the health check handler
     */
    public final HealthCheckHandler getHealthCheckHandler() {
        if (this.healthCheckHandler == null && getApplicationConfiguration().getBoolean(HEALTH_ENABLE)) {
            this.healthCheckHandler = HealthCheckHandler.create(getVertx());
            this.healthCheckHandler.register("app-info", promise -> {
                JsonObject appInfo = new JsonObject();
                appInfo.put("name", getApplicationConfiguration().getName());
                appInfo.put("port", getPort());
                appInfo.put("start-time", Instant.ofEpochMilli(ManagementFactory.getRuntimeMXBean().getStartTime())
                        .atZone(ZoneId.systemDefault())
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                promise.complete(Status.OK(appInfo));
            });
        }
        return this.healthCheckHandler;
    }

    /**
     * Gets cache.
     *
     * @param cacheName the cache name
     * @return the cache
     */
    public Cache<?, ?> getCache(String cacheName) {
        Cache<?, ?> cache = this.cacheMap.get(cacheName);
        if (Objects.isNull(cache)) {
            if (getApplicationConfiguration().isCacheAutoCreation()) {
                log.warn("Cache [{}] NOT FOUND! hence using DEFAULT config to generate the Cache!", cacheName);
                CacheConfiguration cacheConfiguration = new CacheConfiguration();
                Cache<?, ?> c = createCache(cacheConfiguration);
                this.cacheMap.put(cacheName, c);
                return c;
            } else {
                throw new RuntimeException(String.format("Cache [%s] NOT FOUND!", cacheName));
            }
        }

        return cache;
    }

    private ConcurrentMap<String, Cache<?, ?>> initCacheMap() {
        ConcurrentMap<String, Cache<?, ?>> cacheConcurrentMap = new ConcurrentHashMap<>();
        Map<String, CacheConfiguration> cacheConfigurationMap = getApplicationConfiguration().getCustomCache();
        for (Map.Entry<String, CacheConfiguration> entry: cacheConfigurationMap.entrySet()) {
            CacheConfiguration cacheConfiguration = entry.getValue();
            if (cacheConfiguration.isEnable()) {
                cacheConcurrentMap.put(entry.getKey(), createCache(cacheConfiguration));
                log.info("Cache [{}] init successfully with Config::[{}]!", entry.getKey(), cacheConfiguration);
            } else {
                log.warn("Cache [{}] is not enabled, hence SKIP!", entry.getKey());
            }
        }
        return cacheConcurrentMap;
    }

    private Cache<?, ?> createCache(CacheConfiguration cacheConfiguration) {
        Caffeine<?, ?> caffeine = Caffeine.newBuilder()
                .maximumSize(cacheConfiguration.getMaxSize())
                .initialCapacity(cacheConfiguration.getInitSize());

        if (cacheConfiguration.getExpireTime() > 0) {
            caffeine.expireAfterWrite(cacheConfiguration.getExpireTime(), TimeUnit.MINUTES);
        }

        return caffeine.build();
    }
}
