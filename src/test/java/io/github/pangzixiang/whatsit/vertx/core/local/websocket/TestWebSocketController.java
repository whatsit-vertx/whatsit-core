package io.github.pangzixiang.whatsit.vertx.core.local.websocket;

import io.github.pangzixiang.whatsit.vertx.core.annotation.WebSocketAnnotation;
import io.github.pangzixiang.whatsit.vertx.core.websocket.controller.AbstractWebSocketController;
import io.vertx.core.Handler;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.http.WebSocketFrame;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@WebSocketAnnotation(path = "/ws")
public class TestWebSocketController extends AbstractWebSocketController {
    @Override
    public void startConnect(ServerWebSocket serverWebSocket) {
        log.info(serverWebSocket.binaryHandlerID());
    }

    @Override
    public Handler<WebSocketFrame> onConnect(ServerWebSocket serverWebSocket) {
        return webSocketFrame -> {
            log.info(webSocketFrame.textData());
            serverWebSocket.writeTextMessage("ok");
        };
    }

    @Override
    public Handler<Void> closeConnect(ServerWebSocket serverWebSocket) {
        return v -> {
            log.info("Closed");
        };
    }
}
