package io.github.pangzixiang.whatsit.vertx.core.controller;

import io.github.pangzixiang.whatsit.vertx.core.context.ApplicationContext;
import io.github.pangzixiang.whatsit.vertx.core.model.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.ext.web.RoutingContext;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import static io.github.pangzixiang.whatsit.vertx.core.constant.HttpConstants.*;
import static io.github.pangzixiang.whatsit.vertx.core.utils.CoreUtils.objectToString;

/**
 * The type Base controller.
 */
@Slf4j
public class BaseController extends AbstractVerticle {

    @Getter
    private final ApplicationContext applicationContext;

    /**
     * Instantiates a new Base controller.
     *
     * @param applicationContext the application context
     */
    public BaseController(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void start() throws Exception {
        log.info("Controller Verticle [{}] deployed!", this.getClass().getSimpleName());
    }

    /**
     * Send json response future.
     *
     * @param routingContext the routing context
     * @param status         the status
     * @param data           the data
     * @return the future
     */
    public Future<Void> sendJsonResponse(RoutingContext routingContext, HttpResponseStatus status, Object data) {
        return routingContext.response()
                .putHeader(CONTENT_TYPE, CONTENT_TYPE_JSON)
                .setStatusCode(status.code())
                .end(objectToString(HttpResponse.builder().status(status).data(data).build()))
                .onSuccess(success -> log.info("Succeed to send response to {}", routingContext.normalizedPath()))
                .onFailure(throwable -> log.error("Failed to send response to {}", routingContext.normalizedPath(), throwable));
    }

}
