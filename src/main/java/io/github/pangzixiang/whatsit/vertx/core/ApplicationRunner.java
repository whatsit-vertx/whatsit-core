package io.github.pangzixiang.whatsit.vertx.core;

import io.github.pangzixiang.whatsit.vertx.core.context.ApplicationContext;
import io.github.pangzixiang.whatsit.vertx.core.verticle.DatabaseConnectionVerticle;
import io.github.pangzixiang.whatsit.vertx.core.verticle.ServerStartupVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import static io.github.pangzixiang.whatsit.vertx.core.constant.CoreVerticleConstants.SERVER_STARTUP_VERTICLE_ID;
import static io.github.pangzixiang.whatsit.vertx.core.utils.VerticleUtils.deployVerticle;

/**
 * To start up the Application
 */
@Slf4j
public class ApplicationRunner {

    private ApplicationRunner() {}

    /**
     * the method to bring up the application
     */
    @SneakyThrows
    public static void run(ApplicationContext applicationContext) {
        System.getProperties()
                .forEach((key, value) ->
                        log.debug("System Property: [{}]->[{}]", key, value));

        CompositeFuture.all(
                        deployVerticle(applicationContext.getVertx(), new ServerStartupVerticle(applicationContext)),
                        applicationContext.getApplicationConfiguration().isDatabaseEnable() ?
                                deployVerticle(applicationContext.getVertx(), new DatabaseConnectionVerticle(applicationContext)) :
                                Future.succeededFuture()
                )
                .onComplete(compositeFutureAsyncResult -> {
                    if (compositeFutureAsyncResult.succeeded()) {
                        applicationContext.getVertx().eventBus().publish(SERVER_STARTUP_VERTICLE_ID, true);
                    } else {
                        log.error(compositeFutureAsyncResult.cause().getMessage(), compositeFutureAsyncResult.cause());
                        System.exit(-1);
                    }
                });
    }
}
