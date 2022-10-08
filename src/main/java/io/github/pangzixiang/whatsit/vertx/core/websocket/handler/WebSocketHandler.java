package io.github.pangzixiang.whatsit.vertx.core.websocket.handler;

import io.github.pangzixiang.whatsit.vertx.core.context.ApplicationContext;
import io.github.pangzixiang.whatsit.vertx.core.websocket.controller.AbstractWebSocketController;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.ServerWebSocket;

public interface WebSocketHandler extends Handler<ServerWebSocket> {

    static WebSocketHandler create(ApplicationContext applicationContext, Vertx vertx) {
        return new WebSocketHandlerImpl(applicationContext, vertx);
    }

    void registerController(Class<? extends AbstractWebSocketController> clz);

}
