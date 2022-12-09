package io.github.pangzixiang.whatsit.vertx.core.model;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

import static io.github.pangzixiang.whatsit.vertx.core.utils.CoreUtils.objectToString;

/**
 * The type Route handler request codec.
 */
@Slf4j
@AllArgsConstructor
public class RouteHandlerRequestCodec implements MessageCodec<RouteHandlerRequest, RouteHandlerRequest> {

    private final Class<RouteHandlerRequest> routeHandlerRequestClass;

    @Override
    public void encodeToWire(Buffer buffer, RouteHandlerRequest routeHandlerRequest) {
        buffer.appendString(objectToString(routeHandlerRequest));
    }

    @Override
    public RouteHandlerRequest decodeFromWire(int i, Buffer buffer) {
        return buffer.toJsonObject().mapTo(RouteHandlerRequest.class);
    }

    @Override
    public RouteHandlerRequest transform(RouteHandlerRequest routeHandlerRequest) {
        return routeHandlerRequest;
    }

    @Override
    public String name() {
        return routeHandlerRequestClass.getSimpleName() + LocalDateTime.now();
    }

    @Override
    public byte systemCodecID() {
        return -1;
    }
}
