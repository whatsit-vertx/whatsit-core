package io.github.pangzixiang.whatsit.vertx.core.local.verticle;

import io.github.pangzixiang.whatsit.vertx.core.annotation.PreDeploy;
import io.vertx.core.AbstractVerticle;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@PreDeploy
public class TestVerticle extends AbstractVerticle {
    @Override
    public void start() throws Exception {
        log.info("{} started", this.getClass().getSimpleName());
    }

    @Override
    public void stop() throws Exception {
        log.info("{} stopped", this.getClass().getSimpleName());
    }
}
