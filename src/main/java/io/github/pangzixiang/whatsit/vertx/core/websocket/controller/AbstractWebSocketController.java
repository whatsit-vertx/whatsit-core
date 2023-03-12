package io.github.pangzixiang.whatsit.vertx.core.websocket.controller;

import io.vertx.core.Handler;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.http.WebSocketFrame;

/**
 * The type Abstract web socket controller.
 */
public interface AbstractWebSocketController {

    /**
     * Start connect.
     *
     * @param serverWebSocket the server web socket
     */
    void startConnect(ServerWebSocket serverWebSocket);

    /**
     * On connect handler.
     *
     * @param serverWebSocket the server web socket
     * @return the handler
     */
    Handler<WebSocketFrame> onConnect(ServerWebSocket serverWebSocket);

    /**
     * Close connect handler.
     *
     * @param serverWebSocket the server web socket
     * @return the handler
     */
    Handler<Void> closeConnect(ServerWebSocket serverWebSocket);
}
