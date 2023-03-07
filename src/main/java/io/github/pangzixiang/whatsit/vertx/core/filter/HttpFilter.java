package io.github.pangzixiang.whatsit.vertx.core.filter;

import io.vertx.core.AbstractVerticle;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;

/**
 * The type Http filter.
 */
@Slf4j
public abstract class HttpFilter extends AbstractVerticle {

    @Override
    public void start() throws Exception {
        log.info("Filter Verticle [{}] registered", this.getClass().getSimpleName());
    }

    /**
     * Do filter.
     *
     * @param routingContext the routing context
     */
    public abstract void doFilter(RoutingContext routingContext);
}
