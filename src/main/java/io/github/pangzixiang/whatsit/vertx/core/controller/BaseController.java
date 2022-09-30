package io.github.pangzixiang.whatsit.vertx.core.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.pangzixiang.whatsit.vertx.core.context.ApplicationContext;
import io.github.pangzixiang.whatsit.vertx.core.model.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.ext.web.RoutingContext;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import static io.github.pangzixiang.whatsit.vertx.core.constant.HttpConstants.*;
import static io.github.pangzixiang.whatsit.vertx.core.utils.CoreUtils.objectToString;

@Slf4j
public class BaseController extends AbstractVerticle {

    @Getter
    private final ApplicationContext applicationContext;

    public BaseController(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void start() throws Exception {
        log.info("Controller Verticle [{}] deployed!", this.getClass().getSimpleName());
    }

    public void sendJsonResponse(RoutingContext routingContext, HttpResponseStatus status, Object data) {
        try {
            routingContext.response()
                    .putHeader(CONTENT_TYPE, CONTENT_TYPE_JSON)
                    .setStatusCode(status.code())
                    .end(objectToString(HttpResponse.builder().status(status).data(data).build()));
        } catch (JsonProcessingException e) {
            log.error("Failed to send response ", e);
            routingContext.response()
                    .putHeader(CONTENT_TYPE, CONTENT_TYPE_TEXT)
                    .setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code())
                    .end("SERVER ERROR!");
        }
    }

}
