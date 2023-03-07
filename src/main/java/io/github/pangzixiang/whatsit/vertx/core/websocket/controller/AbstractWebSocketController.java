package io.github.pangzixiang.whatsit.vertx.core.websocket.controller;

import io.vertx.core.Handler;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.http.WebSocketFrame;
import lombok.AllArgsConstructor;

/**
 * The type Abstract web socket controller.
 */
@AllArgsConstructor
public abstract class AbstractWebSocketController {

    /**
     * Start connect.
     *
     * @param serverWebSocket the server web socket
     */
    public abstract void startConnect(ServerWebSocket serverWebSocket);

    /**
     * On connect handler.
     *
     * @param serverWebSocket the server web socket
     * @return the handler
     */
    public abstract Handler<WebSocketFrame> onConnect(ServerWebSocket serverWebSocket);

    /**
     * Close connect handler.
     *
     * @param serverWebSocket the server web socket
     * @return the handler
     */
    public abstract Handler<Void> closeConnect(ServerWebSocket serverWebSocket);
}
