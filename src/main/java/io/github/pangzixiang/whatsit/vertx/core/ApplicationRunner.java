package io.github.pangzixiang.whatsit.vertx.core;

import io.github.pangzixiang.whatsit.vertx.core.context.ApplicationContext;
import io.github.pangzixiang.whatsit.vertx.core.controller.HealthController;
import io.github.pangzixiang.whatsit.vertx.core.verticle.DatabaseConnectionVerticle;
import io.github.pangzixiang.whatsit.vertx.core.verticle.ServerStartupVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;

import static io.github.pangzixiang.whatsit.vertx.core.constant.CoreVerticleConstants.SERVER_STARTUP_VERTICLE_ID;
import static io.github.pangzixiang.whatsit.vertx.core.utils.VerticleUtils.deployVerticle;

@Slf4j
public class ApplicationRunner {

    private final Vertx vertx;

    private final ApplicationContext applicationContext;

    public ApplicationRunner(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        this.vertx = vertx();
        this.applicationContext.setVertx(this.vertx);
        // add Core Controllers
        this.applicationContext.registerController(HealthController.class);
    }

    private Vertx vertx() {
        return Vertx.vertx(applicationContext.getApplicationConfiguration().getVertxOptions());
    }

    public void run() {
        System.getProperties()
                .forEach((key, value) ->
                        log.debug("System Property: [{}]->[{}]", key, value));
        CompositeFuture.all(
                        deployVerticle(vertx, new ServerStartupVerticle(applicationContext)),
                        applicationContext.getApplicationConfiguration().isDatabaseEnable() ?
                                deployVerticle(vertx, new DatabaseConnectionVerticle(applicationContext)) :
                                Future.succeededFuture()
                )
                .onComplete(compositeFutureAsyncResult -> {
                    if (compositeFutureAsyncResult.succeeded()) {
                        this.vertx.eventBus().publish(SERVER_STARTUP_VERTICLE_ID, true);
                    } else {
                        log.error(compositeFutureAsyncResult.cause().getMessage(), compositeFutureAsyncResult.cause());
                        System.exit(-1);
                    }
                });
    }
}
