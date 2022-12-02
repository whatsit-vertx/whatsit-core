package io.github.pangzixiang.whatsit.vertx.core.model;

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
public class RouteHandlerRequest {
    private RoutingContext routingContext;
}
