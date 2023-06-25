package io.github.pangzixiang.whatsit.vertx.core.verticle;

import io.github.pangzixiang.whatsit.vertx.core.ApplicationConfiguration;
import io.github.pangzixiang.whatsit.vertx.core.annotation.WebSocketAnnotation;
import io.github.pangzixiang.whatsit.vertx.core.ApplicationContext;
import io.github.pangzixiang.whatsit.vertx.core.controller.BaseController;
import io.github.pangzixiang.whatsit.vertx.core.utils.ClassScannerUtils;
import io.github.pangzixiang.whatsit.vertx.core.websocket.controller.AbstractWebSocketController;
import io.github.pangzixiang.whatsit.vertx.core.websocket.handler.WebSocketHandler;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import jakarta.ws.rs.Path;
import lombok.extern.slf4j.Slf4j;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

import static io.github.pangzixiang.whatsit.vertx.core.utils.CoreUtils.*;
import static io.github.pangzixiang.whatsit.vertx.core.utils.VerticleUtils.deployVerticle;

/**
 * The type Server startup verticle.
 */
@Slf4j
public class ServerStartupVerticle extends CoreVerticle {

    private HttpServer httpServer;

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        super.start();
        Future<Router> routerFuture = registerRouter();

        routerFuture.onSuccess(router -> {
            getVertx().executeBlocking(promise -> {
                log.info("Starting HTTP Server...");
                httpServer = getVertx()
                        .createHttpServer(ApplicationConfiguration.getInstance().getHttpServerOptions())
                        .requestHandler(router);

                List<Class<?>> websocketControllers = ClassScannerUtils.getClassesByCustomFilter(classInfo -> classInfo.hasAnnotation(WebSocketAnnotation.class)
                        && classInfo.implementsInterface(AbstractWebSocketController.class));

                if (!websocketControllers.isEmpty()) {
                    log.info("Start to register websocket {} controllers", websocketControllers.size());
                    WebSocketHandler webSocketHandler = WebSocketHandler.create();
                    websocketControllers.forEach(clz -> webSocketHandler.registerController((Class<? extends AbstractWebSocketController>) clz));
                    httpServer.webSocketHandler(webSocketHandler);
                }

                httpServer.listen(ApplicationConfiguration.getInstance().getPort())
                        .onSuccess(success -> {
                            ApplicationContext.getApplicationContext().setPort(success.actualPort());
                            promise.complete();
                        })
                        .onFailure(failure -> {
                            log.error("Failed to start the application, exiting...");
                            promise.fail(failure);
                        });
            }).onFailure(failure -> {
                log.error(failure.getMessage(), failure);
                startPromise.fail(failure);
                System.exit(-1);
            }).onComplete(unused -> {
                log.info("HTTP Server for Service [{}] started at port [{}] successfully! -> [{} ms]"
                        , ApplicationConfiguration.getInstance().getName().toUpperCase()
                        , ApplicationContext.getApplicationContext().getPort()
                        , System.currentTimeMillis() - ManagementFactory.getRuntimeMXBean().getStartTime());
                startPromise.complete();
            });
        }).onFailure(throwable -> {
            log.error(throwable.getMessage(), throwable);
            startPromise.fail(throwable);
            System.exit(-1);
        });
    }
    @Override
    public void stop(Promise<Void> stopPromise) throws Exception {
        if (httpServer != null) {
            httpServer.close().onComplete(unused -> {
                log.info("Shutdown HTTP Server, Application total runtime -> {}s"
                        , (System.currentTimeMillis() - ManagementFactory.getRuntimeMXBean().getStartTime()) / 1000);
                stopPromise.complete();
            });
        } else {
            stopPromise.complete();
        }
    }

    private Future<Router> registerRouter() {
        Router router = Router.router(getVertx());

        List<Future<String>> controllerRegisterFutures = new ArrayList<>();

        ClassScannerUtils.getClassesByCustomFilter(classInfo -> classInfo.hasAnnotation(Path.class)
                        && classInfo.extendsSuperclass(BaseController.class))
                .forEach(controller -> {
                    Object controllerInstance = createInstance(controller, router);
                    if (controllerInstance == null) {
                        throw new RuntimeException("Cannot find constructor for Class %s, args %s"
                                .formatted(controller.getSimpleName(), Router.class));
                    }
                    controllerRegisterFutures.add(deployVerticle(getVertx(), (BaseController) controllerInstance));
                });
        return Future.all(controllerRegisterFutures).map(router);
    }
}
