package io.github.pangzixiang.whatsit.vertx.core.local.filter;

import io.github.pangzixiang.whatsit.vertx.core.context.ApplicationContext;
import io.github.pangzixiang.whatsit.vertx.core.filter.HttpFilter;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EchoFilter extends HttpFilter {
    public EchoFilter(ApplicationContext applicationContext) {
        super(applicationContext);
    }

    @Override
    public void doFilter(RoutingContext routingContext) {
        log.info("Echo Filter handle request!");
        routingContext.next();
    }
}
