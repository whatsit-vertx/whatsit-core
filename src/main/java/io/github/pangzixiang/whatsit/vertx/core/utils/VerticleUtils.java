package io.github.pangzixiang.whatsit.vertx.core.utils;

import io.vertx.core.*;
import lombok.extern.slf4j.Slf4j;

/**
 * The type Verticle utils.
 */
@Slf4j
public class VerticleUtils {

    private VerticleUtils() {
        super();
    }

    /**
     * Deploy verticle future.
     *
     * @param vertx    the vertx
     * @param verticle the verticle
     * @param options  the options
     * @return the future
     */
    public static Future<String> deployVerticle(Vertx vertx, AbstractVerticle verticle, DeploymentOptions options) {
        return vertx.deployVerticle(verticle, options)
                .onSuccess(id -> {
                    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                        vertx.undeploy(id).onComplete(voidAsyncResult ->
                                log.info("Undeploy Verticle [{} -> {}]"
                                        , verticle.getClass().getSimpleName(), id));
                    }));
                    log.debug("added shutdown hook for Verticle - [{}] -> {}"
                            , verticle.getClass().getSimpleName(), id);
                });
    }

    /**
     * Deploy verticle future.
     *
     * @param vertx    the vertx
     * @param verticle the verticle
     * @return the future
     */
    public static Future<String> deployVerticle(Vertx vertx, AbstractVerticle verticle) {
        return deployVerticle(vertx, verticle, new DeploymentOptions());
    }
}
