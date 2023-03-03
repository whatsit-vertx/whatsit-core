package io.github.pangzixiang.whatsit.vertx.core.pojo;

import io.vertx.ext.web.RoutingContext;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The type Route handler request.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventBusRequest {
    private Object data;
}
