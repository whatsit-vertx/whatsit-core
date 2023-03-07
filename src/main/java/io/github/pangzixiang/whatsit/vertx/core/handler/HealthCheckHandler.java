package io.github.pangzixiang.whatsit.vertx.core.handler;

import com.google.gson.JsonObject;
import io.github.pangzixiang.whatsit.vertx.core.model.HealthStatus;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.ext.web.RoutingContext;

/**
 * The interface Health check handler.
 */
public interface HealthCheckHandler extends Handler<RoutingContext> {

    /**
     * Create health check handler.
     *
     * @return the health check handler
     */
    static HealthCheckHandler create() {
        return new HealthCheckHandlerImpl();
    }

    /**
     * Create health check handler.
     *
     * @param info               the info
     * @return the health check handler
     */
    static HealthCheckHandler create(JsonObject info) {
        return new HealthCheckHandlerImpl(info);
    }

    /**
     * Register health check handler.
     *
     * @param name      the name
     * @param procedure the procedure
     * @return the health check handler
     */
    HealthCheckHandler register(String name, Handler<Promise<HealthStatus>> procedure);
}
