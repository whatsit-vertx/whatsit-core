package io.github.pangzixiang.whatsit.vertx.core;

import io.github.pangzixiang.whatsit.vertx.core.context.ApplicationContext;
import io.github.pangzixiang.whatsit.vertx.core.controller.HealthController;
import io.github.pangzixiang.whatsit.vertx.core.verticle.DatabaseConnectionVerticle;
import io.github.pangzixiang.whatsit.vertx.core.verticle.ServerStartupVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServerOptions;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Optional;

import static io.github.pangzixiang.whatsit.vertx.core.constant.CoreVerticleConstants.SERVER_STARTUP_VERTICLE_ID;
import static io.github.pangzixiang.whatsit.vertx.core.utils.CoreUtils.objectToString;
import static io.github.pangzixiang.whatsit.vertx.core.utils.VerticleUtils.deployVerticle;

@Slf4j
public class ApplicationRunner {

    private final ApplicationContext applicationContext;

    public ApplicationRunner(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        // add Core Controllers
        this.applicationContext.registerController(HealthController.class);
    }

    private Vertx vertx(VertxOptions vertxOptions) {
        if (vertxOptions != null) {
            log.info("Use Custom VertxOptions to startup Vertx! [{}]", vertxOptions);
            applicationContext.getApplicationConfiguration().setVertxOptions(vertxOptions);
        }
        return Vertx.vertx(applicationContext.getApplicationConfiguration().getVertxOptions());
    }

    @SneakyThrows
    public void run(Object...options) {
        System.getProperties()
                .forEach((key, value) ->
                        log.debug("System Property: [{}]->[{}]", key, value));

        VertxOptions vertxOptions = null;
        Optional<Object> vertxOptionsObject = Arrays.stream(options).filter(o -> o instanceof VertxOptions).findFirst();
        if (vertxOptionsObject.isPresent()) {
            vertxOptions = (VertxOptions) vertxOptionsObject.get();
        }

        Optional<Object> httpServerOptionsObject = Arrays.stream(options).filter(o -> o instanceof HttpServerOptions).findFirst();
        if (httpServerOptionsObject.isPresent()) {
            HttpServerOptions httpServerOptions = (HttpServerOptions) httpServerOptionsObject.get();
            log.info("Use Custom HttpServerOptions to startup Http Server! [{}]", objectToString(httpServerOptions));
            this.applicationContext.getApplicationConfiguration()
                    .setHttpServerOptions(httpServerOptions);
        }

        Vertx vertx = vertx(vertxOptions);
        this.applicationContext.setVertx(vertx);
        CompositeFuture.all(
                        deployVerticle(vertx, new ServerStartupVerticle(applicationContext)),
                        applicationContext.getApplicationConfiguration().isDatabaseEnable() ?
                                deployVerticle(vertx, new DatabaseConnectionVerticle(applicationContext)) :
                                Future.succeededFuture()
                )
                .onComplete(compositeFutureAsyncResult -> {
                    if (compositeFutureAsyncResult.succeeded()) {
                        vertx.eventBus().publish(SERVER_STARTUP_VERTICLE_ID, true);
                    } else {
                        log.error(compositeFutureAsyncResult.cause().getMessage(), compositeFutureAsyncResult.cause());
                        System.exit(-1);
                    }
                });
    }
}
