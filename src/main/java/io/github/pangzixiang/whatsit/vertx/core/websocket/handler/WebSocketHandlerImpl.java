package io.github.pangzixiang.whatsit.vertx.core.websocket.handler;

import io.github.pangzixiang.whatsit.vertx.core.annotation.WebSocketAnnotation;
import io.github.pangzixiang.whatsit.vertx.core.context.ApplicationContext;
import io.github.pangzixiang.whatsit.vertx.core.websocket.controller.AbstractWebSocketController;
import io.vertx.core.Vertx;
import io.vertx.core.http.ServerWebSocket;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Constructor;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * The type Web socket handler.
 */
@Slf4j
public class WebSocketHandlerImpl implements WebSocketHandler{
    private final ApplicationContext applicationContext;

    private final Vertx vertx;

    private final ConcurrentMap<String, AbstractWebSocketController> controllerConcurrentMap = new ConcurrentHashMap<>();

    /**
     * Instantiates a new Web socket handler.
     *
     * @param applicationContext the application context
     * @param vertx              the vertx
     */
    public WebSocketHandlerImpl(ApplicationContext applicationContext, Vertx vertx) {
        this.applicationContext = applicationContext;
        this.vertx = vertx;
    }

    @Override
    public void handle(ServerWebSocket serverWebSocket) {
        String path = serverWebSocket.path();
        if (controllerConcurrentMap.containsKey(path)) {
            AbstractWebSocketController abstractWebSocketController = controllerConcurrentMap.get(serverWebSocket.path());
            serverWebSocket.closeHandler(abstractWebSocketController.closeConnect(serverWebSocket));
            abstractWebSocketController.startConnect(serverWebSocket);
            if (!serverWebSocket.isClosed()) {
                serverWebSocket.frameHandler(abstractWebSocketController.onConnect(serverWebSocket));
            }
        } else {
            log.warn("Reject websocket connection for Invalid Path [{}]", path);
            serverWebSocket.reject();
        }
    }

    @Override
    public void registerController(Class<? extends AbstractWebSocketController> clz) {
        WebSocketAnnotation webSocketAnnotation = clz.getAnnotation(WebSocketAnnotation.class);
        if (webSocketAnnotation != null && StringUtils.isNotBlank(webSocketAnnotation.path())) {
            try {
                Constructor<? extends AbstractWebSocketController> constructor = clz.getConstructor(ApplicationContext.class, Vertx.class);
                AbstractWebSocketController o = constructor.newInstance(applicationContext, vertx);
                controllerConcurrentMap.put(webSocketAnnotation.path(), o);
                log.info("Added WebSocket Controller [{}]", clz.getSimpleName());
            } catch (Exception e) {
                log.error("FAILED to register WebSocket Controller [{}]", clz.getSimpleName(), e);
            }
        } else {
            log.warn("INVALID WebSocket Controller [{}]", clz.getSimpleName());
        }
    }
}
