package io.github.pangzixiang.whatsit.vertx.core.controller;

import io.github.pangzixiang.whatsit.vertx.core.annotation.RestController;
import io.github.pangzixiang.whatsit.vertx.core.context.ApplicationContext;
import io.github.pangzixiang.whatsit.vertx.core.constant.HttpRequestMethod;
import io.github.pangzixiang.whatsit.vertx.core.model.Health;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;

import java.lang.management.ManagementFactory;
import java.util.Date;

@Slf4j
public class HealthController extends BaseController {

    public HealthController(ApplicationContext applicationContext) {
        super(applicationContext);
    }

    @Override
    public void start() throws Exception {
        super.start();
    }

    @RestController(path = "/health", method = HttpRequestMethod.GET)
    public void healthEndpoint(RoutingContext routingContext) {
        sendJsonResponse(routingContext
                , HttpResponseStatus.OK
                , Health.builder()
                        .isHealth(isHealth())
                        .name(getApplicationContext().getApplicationConfiguration().getName())
                        .port(getApplicationContext().getPort())
                        .startTime(new Date(ManagementFactory.getRuntimeMXBean().getStartTime()).toString())
                        .upTime(System.currentTimeMillis() - ManagementFactory.getRuntimeMXBean().getStartTime())
                        .dependency(getApplicationContext().getHealthDependency())
                        .build());
    }

    private boolean isHealth() {
        if (getApplicationContext().getApplicationConfiguration().isDatabaseEnable()) {
            return getApplicationContext().getHealthDependency().getDatabaseHealth().getIsHealth();
        } else {
            return true;
        }
    }
}
