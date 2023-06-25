package io.github.pangzixiang.whatsit.vertx.core.local.websocket;

import io.github.pangzixiang.whatsit.vertx.core.annotation.WebSocketAnnotation;
import io.github.pangzixiang.whatsit.vertx.core.local.filter.WebSocketTestFilter;
import io.github.pangzixiang.whatsit.vertx.core.local.filter.WebSocketTestFilter2;
import io.github.pangzixiang.whatsit.vertx.core.websocket.controller.AbstractWebSocketController;
import io.vertx.core.Handler;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.http.WebSocketFrame;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@WebSocketAnnotation(path = "/ws", filter = {WebSocketTestFilter.class, WebSocketTestFilter2.class})
public class TestWebSocketController implements AbstractWebSocketController {
    @Override
    public void startConnect(ServerWebSocket serverWebSocket) {
        log.info(serverWebSocket.binaryHandlerID());
    }

    @Override
    public Handler<WebSocketFrame> onConnect(ServerWebSocket serverWebSocket) {
        return webSocketFrame -> {
            log.info(webSocketFrame.textData());
            serverWebSocket.writeTextMessage(webSocketFrame.textData());
        };
    }

    @Override
    public Handler<Void> closeConnect(ServerWebSocket serverWebSocket) {
        return v -> {
            log.info("Closed");
        };
    }
}
