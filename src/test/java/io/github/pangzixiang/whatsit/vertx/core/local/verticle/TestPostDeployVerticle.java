package io.github.pangzixiang.whatsit.vertx.core.local.verticle;

import io.github.pangzixiang.whatsit.vertx.core.annotation.PostDeploy;
import io.github.pangzixiang.whatsit.vertx.core.context.ApplicationContext;
import io.vertx.core.AbstractVerticle;

@PostDeploy
public class TestPostDeployVerticle extends AbstractVerticle {
    private final ApplicationContext applicationContext;

    public TestPostDeployVerticle(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
}
