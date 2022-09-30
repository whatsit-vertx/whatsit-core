package io.github.pangzixiang.whatsit.vertx.core.verticle;

import io.github.pangzixiang.whatsit.vertx.core.context.ApplicationContext;
import io.vertx.core.AbstractVerticle;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class CoreVerticle extends AbstractVerticle {

    @Getter
    private final ApplicationContext applicationContext;

    CoreVerticle(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void start() throws Exception {
        log.info("Core Verticle [{}] deployed!", this.getClass().getSimpleName());
    }
}
