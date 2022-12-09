package io.github.pangzixiang.whatsit.vertx.core.context;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.pangzixiang.whatsit.vertx.core.config.ApplicationConfiguration;
import io.github.pangzixiang.whatsit.vertx.core.config.cache.CacheConfiguration;
import io.github.pangzixiang.whatsit.vertx.core.controller.BaseController;
import io.github.pangzixiang.whatsit.vertx.core.model.HealthDependency;
import io.github.pangzixiang.whatsit.vertx.core.websocket.controller.AbstractWebSocketController;
import io.vertx.core.Vertx;
import io.vertx.jdbcclient.JDBCPool;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * Application Context
 */
@Slf4j
public class ApplicationContext {

    /**
     * Vertx Instance
     */
    @Getter
    @Setter
    private Vertx vertx;

    /**
     * Server Port
     */
    @Getter
    @Setter
    private int port;

    /**
     * Application Configuration
     */
    @Getter
    private final ApplicationConfiguration applicationConfiguration;

    /**
     * JDBC pool
     */
    @Getter
    @Setter
    private JDBCPool jdbcPool;

    /**
     * HealthDependencies
     */
    @Getter
    private final List<HealthDependency> healthDependencies = new ArrayList<>();

    /**
     * Controllers
     */
    @Getter
    private final List<Class<? extends BaseController>> controllers = new ArrayList<>();

    /**
     * Cache Map storing the Cache
     */
    private final ConcurrentMap<String, Cache<?, ?>> cacheMap;

    /**
     * Websocket Controller
     */
    @Getter
    private final List<Class<? extends AbstractWebSocketController>> websocketControllers = new ArrayList<>();

    /**
     * Constructor for ApplicationContext
     */
    public ApplicationContext() {
        this.applicationConfiguration = new ApplicationConfiguration();
        if (this.applicationConfiguration.isCacheEnable()) {
             this.cacheMap = this.initCacheMap();
        } else {
            this.cacheMap = null;
        }
    }

    /**
     * Method to register web controllers
     *
     * @param controller Class extends BaseController
     */
    @SafeVarargs
    public final void registerController(Class<? extends BaseController>... controller) {
        this.controllers.addAll(Arrays.asList(controller));
    }

    /**
     * Method to register websocket controllers
     *
     * @param controller Class extends AbstractWebSocketController
     */
    @SafeVarargs
    public final void registerWebSocketController(Class<? extends AbstractWebSocketController> ... controller) {
        this.websocketControllers.addAll(Arrays.asList(controller));
    }

    /**
     * Method to get Cache from Cache Map
     *
     * @param cacheName Cache Name
     * @return Cache cache
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

    /**
     * Method to init Cache Map
     *
     * @return Cache Map
     */
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
