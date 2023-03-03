package io.github.pangzixiang.whatsit.vertx.core.pojo;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

import static io.github.pangzixiang.whatsit.vertx.core.utils.CoreUtils.objectToString;

/**
 * The type Event bus request codec.
 */
@Slf4j
@AllArgsConstructor
public class EventBusRequestCodec implements MessageCodec<EventBusRequest, EventBusRequest> {

    private final Class<EventBusRequest> routeHandlerRequestClass;

    @Override
    public void encodeToWire(Buffer buffer, EventBusRequest eventBusRequest) {
        buffer.appendString(objectToString(eventBusRequest));
    }

    @Override
    public EventBusRequest decodeFromWire(int i, Buffer buffer) {
        return buffer.toJsonObject().mapTo(EventBusRequest.class);
    }

    @Override
    public EventBusRequest transform(EventBusRequest eventBusRequest) {
        return eventBusRequest;
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
