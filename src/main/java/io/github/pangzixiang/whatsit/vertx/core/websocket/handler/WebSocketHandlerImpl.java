package io.github.pangzixiang.whatsit.vertx.core.websocket.handler;

import io.github.pangzixiang.whatsit.vertx.core.ApplicationConfiguration;
import io.github.pangzixiang.whatsit.vertx.core.annotation.WebSocketAnnotation;
import io.github.pangzixiang.whatsit.vertx.core.ApplicationContext;
import io.github.pangzixiang.whatsit.vertx.core.websocket.filter.WebsocketFilter;
import io.github.pangzixiang.whatsit.vertx.core.utils.CoreUtils;
import io.github.pangzixiang.whatsit.vertx.core.websocket.controller.AbstractWebSocketController;
import io.vertx.core.http.ServerWebSocket;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;

/**
 * The type Web socket handler.
 */
@Slf4j
public class WebSocketHandlerImpl implements WebSocketHandler {
    private final ConcurrentMap<String, AbstractWebSocketController> controllerConcurrentMap = new ConcurrentHashMap<>();

    private final ConcurrentMap<String, List<WebsocketFilterObject>> filterConcurrentMap = new ConcurrentHashMap<>();

    @Override
    public void handle(ServerWebSocket serverWebSocket) {
        String path = serverWebSocket.path();
        if (controllerConcurrentMap.containsKey(path)) {
            AbstractWebSocketController abstractWebSocketController = controllerConcurrentMap.get(path);
            serverWebSocket.closeHandler(abstractWebSocketController.closeConnect(serverWebSocket));

            if (filtersCheck(path, serverWebSocket)) {
                abstractWebSocketController.startConnect(serverWebSocket);
                if (!serverWebSocket.isClosed()) {
                    serverWebSocket.frameHandler(abstractWebSocketController.onConnect(serverWebSocket));
                }
            } else {
                log.debug("Reject websocket connection for [{}] by filter", path);
                serverWebSocket.reject();
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
                Constructor<? extends AbstractWebSocketController> constructor = clz.getConstructor();
                AbstractWebSocketController o = constructor.newInstance();
                String path = CoreUtils.refactorControllerPath(webSocketAnnotation.path(),
                        ApplicationConfiguration.getInstance());
                controllerConcurrentMap.put(path, o);
                if (webSocketAnnotation.filter().length > 0) {
                    Stream.of(webSocketAnnotation.filter())
                            .filter(WebsocketFilter.class::isAssignableFrom)
                            .forEach(filterClz -> {
                                try {
                                    Object instance = CoreUtils.createInstance(filterClz);
                                    Method doFilter = filterClz.getMethod("doFilter", ServerWebSocket.class);
                                    List<WebsocketFilterObject> exist = filterConcurrentMap.get(path);
                                    if (exist == null) {
                                        exist = new ArrayList<>();
                                    }
                                    exist.add(WebsocketFilterObject.builder().doFilter(doFilter).instance(instance).build());
                                    filterConcurrentMap.put(path, exist);
                                } catch (Exception e) {
                                    log.error("Failed to register Filter [{}] for websocket controller [{} -> {}]"
                                            , filterClz.getSimpleName(), clz.getSimpleName(), path);
                                }
                            });
                }
                log.info("Added WebSocket Controller [{} -> {}]", clz.getSimpleName(), path);
            } catch (Exception e) {
                log.error("FAILED to register WebSocket Controller [{}]", clz.getSimpleName(), e);
            }
        } else {
            log.warn("INVALID WebSocket Controller [{}]", clz.getSimpleName());
        }
    }

    private boolean filtersCheck(String path, ServerWebSocket serverWebSocket) {
        boolean isPassed = true;
        List<WebsocketFilterObject> filterObjects = filterConcurrentMap.get(path);

        if (filterObjects != null) {
            for (WebsocketFilterObject filterObject : filterObjects) {
                try {
                    isPassed = (boolean) CoreUtils.invokeMethod(filterObject.getDoFilter(), filterObject.getInstance(), serverWebSocket);
                } catch (Exception e) {
                    log.error("Failed to pass filter", e);
                    return false;
                }
            }
        }
        return isPassed;
    }
}
