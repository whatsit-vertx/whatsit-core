package io.github.pangzixiang.whatsit.vertx.core.utils;

import io.vertx.core.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VerticleUtils {

    private VerticleUtils() {
        super();
    }

    public static Future<String> deployVerticle(Vertx vertx, AbstractVerticle verticle, DeploymentOptions options) {
        return vertx.deployVerticle(verticle, options)
                .onSuccess(id -> {
                    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                        vertx.undeploy(id)
                                .onFailure(failure -> log.error("Failed to undeploy Verticle [{} -> {}]"
                                        , verticle.getClass().getSimpleName(), id, failure));
                        log.info("Undeploy Verticle [{} -> {}]"
                                , verticle.getClass().getSimpleName(), id);
                    }));

                    log.info("added shutdown hook for Verticle - [{}] -> {}"
                            , verticle.getClass().getSimpleName(), id);
                });
    }

    public static Future<String> deployVerticle(Vertx vertx, AbstractVerticle verticle) {
        return deployVerticle(vertx, verticle, new DeploymentOptions());
    }
}
