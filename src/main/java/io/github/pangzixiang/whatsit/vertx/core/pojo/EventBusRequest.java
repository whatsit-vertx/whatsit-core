package io.github.pangzixiang.whatsit.vertx.core.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The type Event bus request.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventBusRequest {
    private Object data;
}
