package io.github.pangzixiang.whatsit.vertx.core.verticle;

import io.vertx.core.AbstractVerticle;
import lombok.extern.slf4j.Slf4j;

/**
 * The type Core verticle.
 */
@Slf4j
class CoreVerticle extends AbstractVerticle {
    @Override
    public void start() throws Exception {
        log.info("Core Verticle [{}] deployed!", this.getClass().getSimpleName());
    }
}
