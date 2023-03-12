package io.github.pangzixiang.whatsit.vertx.core.websocket.filter;

import io.vertx.core.http.ServerWebSocket;

/**
 * The interface Websocket filter.
 */
public interface WebsocketFilter {
    /**
     * Do filter boolean.
     *
     * @param serverWebSocket the server web socket
     * @return the filter result
     */
    boolean doFilter(ServerWebSocket serverWebSocket);
}
