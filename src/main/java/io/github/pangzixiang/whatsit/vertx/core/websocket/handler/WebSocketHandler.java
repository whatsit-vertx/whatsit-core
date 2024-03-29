package io.github.pangzixiang.whatsit.vertx.core.websocket.handler;

import io.github.pangzixiang.whatsit.vertx.core.websocket.controller.AbstractWebSocketController;
import io.vertx.core.Handler;
import io.vertx.core.http.ServerWebSocket;

/**
 * The interface Web socket handler.
 */
public interface WebSocketHandler extends Handler<ServerWebSocket> {

    /**
     * Create web socket handler.
     *
     * @return the web socket handler
     */
    static WebSocketHandler create() {
        return new WebSocketHandlerImpl();
    }

    /**
     * Register controller.
     *
     * @param clz the clz
     */
    void registerController(Class<? extends AbstractWebSocketController> clz);

}
