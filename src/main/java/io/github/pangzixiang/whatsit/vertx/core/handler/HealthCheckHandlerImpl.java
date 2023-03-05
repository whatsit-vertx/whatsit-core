package io.github.pangzixiang.whatsit.vertx.core.handler;

import com.google.gson.JsonObject;
import io.github.pangzixiang.whatsit.vertx.core.constant.HttpConstants;
import io.github.pangzixiang.whatsit.vertx.core.context.ApplicationContext;
import io.github.pangzixiang.whatsit.vertx.core.model.HealthCheckResponse;
import io.github.pangzixiang.whatsit.vertx.core.model.HealthStatus;
import io.github.pangzixiang.whatsit.vertx.core.utils.CoreUtils;
import io.vertx.core.*;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class HealthCheckHandlerImpl implements HealthCheckHandler {

    private final ApplicationContext applicationContext;

    private final JsonObject info;

    private final Map<String, HealthStatus> resultMap = new ConcurrentHashMap<>();

    private final Map<String, Handler<Promise<HealthStatus>>> handlerMap = new ConcurrentHashMap<>();

    public HealthCheckHandlerImpl(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        this.info = null;
    }

    public HealthCheckHandlerImpl(ApplicationContext applicationContext, JsonObject info) {
        this.applicationContext = applicationContext;
        this.info = info;
    }

    @Override
    public HealthCheckHandler register(String name, Handler<Promise<HealthStatus>> procedure) {
        this.handlerMap.put(name, procedure);
        return this;
    }

    private CompositeFuture checkResult() {
        List<Future> futures = new ArrayList<>();
        for (Map.Entry<String, Handler<Promise<HealthStatus>>> entry: this.handlerMap.entrySet()) {
            String name = entry.getKey();
            Handler<Promise<HealthStatus>> handler = entry.getValue();
            Promise<HealthStatus> promise = Promise.promise();

            Future<HealthStatus> future = promise.future();

            future.onComplete(status -> resultMap.put(name, status.result()));

            futures.add(future);
            try {
                handler.handle(promise);
            } catch (Exception e) {
                resultMap.put(name, HealthStatus.fail());
            }
        }

        return CompositeFuture.all(futures);
    }

    @Override
    public void handle(RoutingContext routingContext) {
        checkResult()
                .onComplete(unused -> {
                    HealthCheckResponse response = HealthCheckResponse
                            .builder()
                            .info(this.info)
                            .healthDependencies(this.resultMap)
                            .isHealthy(this.resultMap.entrySet().stream().allMatch(entry -> entry.getValue().isHealthy()))
                            .build();

                    HttpServerResponse res = routingContext.response();
                    res.putHeader(HttpHeaders.CONTENT_TYPE, HttpConstants.CONTENT_TYPE_JSON);

                    res.end(CoreUtils.objectToString(response));
                });
    }
}
