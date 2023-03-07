package io.github.pangzixiang.whatsit.vertx.core;

import io.github.pangzixiang.whatsit.vertx.core.annotation.PreDeploy;
import io.github.pangzixiang.whatsit.vertx.core.context.ApplicationContext;
import io.github.pangzixiang.whatsit.vertx.core.utils.AutoClassLoader;
import io.github.pangzixiang.whatsit.vertx.core.utils.CoreUtils;
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
    public static void run(ApplicationContext applicationContext) {
        System.getProperties()
                .forEach((key, value) ->
                        log.debug("System Property: [{}]->[{}]", key, value));

        List<Class<?>> preDeployVerticles = AutoClassLoader
                .getClassesByCustomFilter(clz -> clz.isAnnotationPresent(PreDeploy.class) && AbstractVerticle.class.isAssignableFrom(clz));

        List<Future> futures = new ArrayList<>(preDeployVerticles.stream().sorted(Comparator.comparing(clz -> {
            PreDeploy preDeploy = clz.getAnnotation(PreDeploy.class);
            return preDeploy.order();
        })).map(clz -> (Future) deployVerticle(applicationContext.getVertx(), (Class<? extends AbstractVerticle>) clz)).toList());

        log.info("auto deploy [{}] verticles with annotation [{}]", futures.size(), PreDeploy.class.getName());

        futures.add(deployVerticle(applicationContext.getVertx(), new ServerStartupVerticle(applicationContext)));

        if (applicationContext.getApplicationConfiguration().isDatabaseEnable()) {
            futures.add(deployVerticle(applicationContext.getVertx(), new DatabaseConnectionVerticle(applicationContext)));
        }


        CompositeFuture.all(futures)
                .onComplete(compositeFutureAsyncResult -> {
                    if (compositeFutureAsyncResult.succeeded()) {
                        applicationContext.getVertx().eventBus().publish(SERVER_STARTUP_VERTICLE_ID, true);
                    } else {
                        log.error(compositeFutureAsyncResult.cause().getMessage(), compositeFutureAsyncResult.cause());
                        System.exit(-1);
                    }
                });
    }

    /**
     * Run application context.
     *
     * @return the application context
     */
    public static ApplicationContext run() {
        ApplicationContext applicationContext = new ApplicationContext();
        run(applicationContext);
        return applicationContext;
    }
}
