package io.github.pangzixiang.whatsit.core.local.filter;

import io.github.pangzixiang.whatsit.core.context.ApplicationContext;
import io.github.pangzixiang.whatsit.core.filter.HttpFilter;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestFilter extends HttpFilter {
    public TestFilter(ApplicationContext applicationContext) {
        super(applicationContext);
    }

    @Override
    public void doFilter(RoutingContext routingContext) {
        log.info("test filter invoked");
        routingContext.next();
    }
}
