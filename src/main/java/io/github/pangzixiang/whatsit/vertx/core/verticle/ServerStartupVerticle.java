package io.github.pangzixiang.whatsit.vertx.core.verticle;

import io.github.pangzixiang.whatsit.vertx.core.annotation.PostDeploy;
import io.github.pangzixiang.whatsit.vertx.core.annotation.RestController;
import io.github.pangzixiang.whatsit.vertx.core.annotation.WebSocketAnnotation;
import io.github.pangzixiang.whatsit.vertx.core.context.ApplicationContext;
import io.github.pangzixiang.whatsit.vertx.core.controller.BaseController;
import io.github.pangzixiang.whatsit.vertx.core.constant.CoreVerticleConstants;
import io.github.pangzixiang.whatsit.vertx.core.utils.ClassScannerUtils;
import io.github.pangzixiang.whatsit.vertx.core.websocket.controller.AbstractWebSocketController;
import io.github.pangzixiang.whatsit.vertx.core.websocket.handler.WebSocketHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import lombok.extern.slf4j.Slf4j;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static io.github.pangzixiang.whatsit.vertx.core.constant.ConfigurationConstants.HEALTH_ENABLE;
import static io.github.pangzixiang.whatsit.vertx.core.constant.ConfigurationConstants.HEALTH_PATH;
import static io.github.pangzixiang.whatsit.vertx.core.constant.CoreVerticleConstants.SERVER_STARTUP_NOTIFICATION_ID;
import static io.github.pangzixiang.whatsit.vertx.core.constant.CoreVerticleConstants.SERVER_STARTUP_VERTICLE_ID;
import static io.github.pangzixiang.whatsit.vertx.core.utils.CoreUtils.*;
import static io.github.pangzixiang.whatsit.vertx.core.utils.VerticleUtils.deployVerticle;

/**
 * The type Server startup verticle.
 */
@Slf4j
public class ServerStartupVerticle extends CoreVerticle {

    @Override
    public void start() throws Exception {
        super.start();
        getVertx().eventBus().consumer(CoreVerticleConstants.SERVER_STARTUP_VERTICLE_ID).handler(this::start)
                .exceptionHandler(exception -> log.error(exception.getMessage(), exception))
                .completionHandler(complete -> log.debug("EventBus Consumer [{}] registered", CoreVerticleConstants.SERVER_STARTUP_VERTICLE_ID));
    }

    private void start(Message<Object> message) {
        if ((Boolean) message.body()) {
            Router router = registerRouter();

            getVertx().executeBlocking(promise -> {
                log.info("Starting HTTP Server...");
                HttpServer httpServer = getVertx()
                        .createHttpServer(ApplicationContext.getApplicationContext().getApplicationConfiguration().getHttpServerOptions())
                        .requestHandler(router);

                List<Class<?>> websocketControllers = ClassScannerUtils.getClassesByCustomFilter(classInfo -> classInfo.hasAnnotation(WebSocketAnnotation.class)
                        && classInfo.implementsInterface(AbstractWebSocketController.class));

                if (!websocketControllers.isEmpty()) {
                    log.info("Start to register websocket {} controllers", websocketControllers.size());
                    WebSocketHandler webSocketHandler = WebSocketHandler.create();
                    websocketControllers.forEach(clz -> webSocketHandler.registerController((Class<? extends AbstractWebSocketController>) clz));
                    httpServer.webSocketHandler(webSocketHandler);
                }

                httpServer.listen(ApplicationContext.getApplicationContext().getApplicationConfiguration().getPort())
                        .onSuccess(success -> {

                            ApplicationContext.getApplicationContext().setPort(success.actualPort());

                            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                                success.close();
                                log.info("Shutdown HTTP Server, Application total runtime -> {}s"
                                        , (System.currentTimeMillis() - ManagementFactory.getRuntimeMXBean().getStartTime()) / 1000);
                            }));

                            log.debug("added shutdown hook for HTTP Server");

                            List<Class<?>> postDeployVerticles = ClassScannerUtils
                                    .getClassesByCustomFilter(classInfo -> classInfo.hasAnnotation(PostDeploy.class) && classInfo.extendsSuperclass(AbstractVerticle.class));

                            List<Future> futures = new ArrayList<>(postDeployVerticles.stream().sorted(Comparator.comparing(clz -> {
                                PostDeploy postDeploy = clz.getAnnotation(PostDeploy.class);
                                return postDeploy.order();
                            })).map(clz -> (Future) deployVerticle(getVertx(), (Class<? extends AbstractVerticle>) clz)).toList());

                            CompositeFuture.all(futures).onComplete(promise::complete);

                            log.info("auto deploy [{}] verticles with annotation [{}]", futures.size(), PostDeploy.class.getName());
                        })
                        .onFailure(failure -> {
                            log.error("Failed to start the application, exiting...");
                            promise.fail(failure);
                        });
            }).onFailure(failure -> {
                log.error(failure.getMessage(), failure);
                System.exit(-1);
            }).onComplete(unused -> {
                getVertx().eventBus().publish(SERVER_STARTUP_NOTIFICATION_ID, true);
                log.info("HTTP Server for Service [{}] started at port [{}] successfully! -> [{} ms]"
                        , ApplicationContext.getApplicationContext().getApplicationConfiguration().getName().toUpperCase()
                        , ApplicationContext.getApplicationContext().getPort()
                        , System.currentTimeMillis() - ManagementFactory.getRuntimeMXBean().getStartTime());
            });
        } else {
            log.error("HTTP Server NOT Started, existing...");
            System.exit(-1);
        }
    }

    private Router registerRouter() {
        Router router = Router.router(getVertx());

        if (ApplicationContext.getApplicationContext().getApplicationConfiguration().getBoolean(HEALTH_ENABLE)) {
            router.get(ApplicationContext.getApplicationContext().getApplicationConfiguration().getString(HEALTH_PATH))
                    .handler(ApplicationContext.getApplicationContext().getHealthCheckHandler());
        }

        ClassScannerUtils.getClassesByCustomFilter(classInfo -> classInfo.hasAnnotation(RestController.class)
                        && classInfo.extendsSuperclass(BaseController.class))
                .forEach(controller -> {
                    Object controllerInstance = createInstance(controller, router);
                    if (controllerInstance == null) {
                        throw new RuntimeException("Cannot find constructor for Class %s, args %s"
                                .formatted(controller.getSimpleName(), Router.class));
                    }
                    deployVerticle(getVertx(), (BaseController) controllerInstance);
                });
        return router;
    }
}
