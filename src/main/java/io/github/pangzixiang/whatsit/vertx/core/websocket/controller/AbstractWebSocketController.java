package io.github.pangzixiang.whatsit.vertx.core.websocket.controller;

import io.github.pangzixiang.whatsit.vertx.core.context.ApplicationContext;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.http.WebSocketFrame;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * The type Abstract web socket controller.
 */
@AllArgsConstructor
public abstract class AbstractWebSocketController {

    @Getter
    private final ApplicationContext applicationContext;

    @Getter
    private final Vertx vertx;

    /**
     * perform action when connection start
     *
     * @param serverWebSocket serverWebSocket
     */
    public abstract void startConnect(ServerWebSocket serverWebSocket);

    /**
     * perform action during the connection
     *
     * @param serverWebSocket serverWebSocket
     * @return WebSocketFrame handler
     */
    public abstract Handler<WebSocketFrame> onConnect(ServerWebSocket serverWebSocket);

    /**
     * perform action when connection close
     *
     * @param serverWebSocket serverWebSocket
     * @return Void handler
     */
    public abstract Handler<Void> closeConnect(ServerWebSocket serverWebSocket);
}
