package io.github.pangzixiang.whatsit.vertx.core.controller;

import io.github.pangzixiang.whatsit.vertx.core.annotation.RestController;
import io.github.pangzixiang.whatsit.vertx.core.context.ApplicationContext;
import io.github.pangzixiang.whatsit.vertx.core.constant.HttpRequestMethod;
import io.github.pangzixiang.whatsit.vertx.core.model.Health;
import io.github.pangzixiang.whatsit.vertx.core.model.HealthDependency;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;

import java.lang.management.ManagementFactory;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * The type Health controller.
 */
@Slf4j
public class HealthController extends BaseController {

    /**
     * Instantiates a new Health controller.
     *
     * @param applicationContext the application context
     */
    public HealthController(ApplicationContext applicationContext) {
        super(applicationContext);
    }

    @Override
    public void start() throws Exception {
        super.start();
    }

    /**
     * Health endpoint.
     *
     * @param routingContext the routing context
     */
    @RestController(path = "/health", method = HttpRequestMethod.GET)
    public void healthEndpoint(RoutingContext routingContext) {
        sendJsonResponse(routingContext
                , HttpResponseStatus.OK
                , Health.builder()
                        .isHealth(isHealth())
                        .name(getApplicationContext().getApplicationConfiguration().getName())
                        .port(getApplicationContext().getPort())
                        .startTime(LocalDateTime.ofInstant(new Date(ManagementFactory.getRuntimeMXBean().getStartTime()).toInstant(), ZoneId.systemDefault()))
                        .upTime(System.currentTimeMillis() - ManagementFactory.getRuntimeMXBean().getStartTime())
                        .dependencies(getApplicationContext().getHealthDependencies())
                        .build());
    }

    private boolean isHealth() {
        return getApplicationContext().getHealthDependencies().stream().allMatch(HealthDependency::isHealth);
    }
}
