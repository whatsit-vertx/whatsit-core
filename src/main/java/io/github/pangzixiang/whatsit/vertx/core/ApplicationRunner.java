package io.github.pangzixiang.whatsit.vertx.core;

import io.github.pangzixiang.whatsit.vertx.core.annotation.PreDeploy;
import io.github.pangzixiang.whatsit.vertx.core.context.ApplicationContext;
import io.github.pangzixiang.whatsit.vertx.core.utils.ClassScannerUtils;
import io.github.pangzixiang.whatsit.vertx.core.verticle.DatabaseConnectionVerticle;
import io.github.pangzixiang.whatsit.vertx.core.verticle.ServerStartupVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static io.github.pangzixiang.whatsit.vertx.core.constant.CoreVerticleConstants.SERVER_STARTUP_VERTICLE_ID;
import static io.github.pangzixiang.whatsit.vertx.core.utils.VerticleUtils.deployVerticle;

/**
 * The type Application runner.
 */
@Slf4j
public class ApplicationRunner {

    private ApplicationRunner() {
    }

    /**
     * Run.
     *
     * @param applicationContext the application context
     */
    @SneakyThrows
    public static Future<Void> run(ApplicationContext applicationContext) {
        System.getProperties()
                .forEach((key, value) ->
                        log.debug("System Property: [{}]->[{}]", key, value));

        List<Class<?>> preDeployVerticles = ClassScannerUtils
                .getClassesByCustomFilter(classInfo -> classInfo.hasAnnotation(PreDeploy.class) && classInfo.extendsSuperclass(AbstractVerticle.class));

        List<Future> futures = new ArrayList<>(preDeployVerticles.stream().sorted(Comparator.comparing(clz -> {
            PreDeploy preDeploy = clz.getAnnotation(PreDeploy.class);
            return preDeploy.order();
        })).map(clz -> (Future) deployVerticle(applicationContext.getVertx(), (Class<? extends AbstractVerticle>) clz)).toList());

        log.info("auto deploy [{}] verticles with annotation [{}]", futures.size(), PreDeploy.class.getName());

        futures.add(deployVerticle(applicationContext.getVertx(), new ServerStartupVerticle()));

        if (applicationContext.getApplicationConfiguration().isDatabaseEnable()) {
            futures.add(deployVerticle(applicationContext.getVertx(), new DatabaseConnectionVerticle()));
        }


        return CompositeFuture.all(futures)
                .onComplete(compositeFutureAsyncResult -> {
                    if (compositeFutureAsyncResult.succeeded()) {
                        applicationContext.getVertx().eventBus().publish(SERVER_STARTUP_VERTICLE_ID, true);
                    } else {
                        log.error(compositeFutureAsyncResult.cause().getMessage(), compositeFutureAsyncResult.cause());
                        System.exit(-1);
                    }
                }).mapEmpty();
    }

    /**
     * Run application context.
     *
     */
    public static Future<Void> run() {
        return run(ApplicationContext.getApplicationContext());
    }
}
