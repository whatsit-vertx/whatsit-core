package io.github.pangzixiang.whatsit.vertx.core.filter;

import io.github.pangzixiang.whatsit.vertx.core.context.ApplicationContext;
import io.vertx.core.AbstractVerticle;
import io.vertx.ext.web.RoutingContext;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class HttpFilter extends AbstractVerticle {
    @Getter
    private final ApplicationContext applicationContext;

    public HttpFilter(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void start() throws Exception {
        log.info("Filter Verticle [{}] registered", this.getClass().getSimpleName());
    }

    public abstract void doFilter(RoutingContext routingContext);
}
