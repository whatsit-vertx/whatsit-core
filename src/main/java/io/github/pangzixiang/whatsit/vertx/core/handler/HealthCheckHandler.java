package io.github.pangzixiang.whatsit.vertx.core.handler;

import com.google.gson.JsonObject;
import io.github.pangzixiang.whatsit.vertx.core.context.ApplicationContext;
import io.github.pangzixiang.whatsit.vertx.core.model.HealthStatus;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.ext.web.RoutingContext;

public interface HealthCheckHandler extends Handler<RoutingContext> {

    static HealthCheckHandler create(ApplicationContext applicationContext) {
        return new HealthCheckHandlerImpl(applicationContext);
    }

    static HealthCheckHandler create(ApplicationContext applicationContext, JsonObject info) {
        return new HealthCheckHandlerImpl(applicationContext, info);
    }

    HealthCheckHandler register(String name, Handler<Promise<HealthStatus>> procedure);
}
