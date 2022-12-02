package io.github.pangzixiang.whatsit.vertx.core.verticle;

import io.github.pangzixiang.whatsit.vertx.core.annotation.RestController;
import io.github.pangzixiang.whatsit.vertx.core.context.ApplicationContext;
import io.github.pangzixiang.whatsit.vertx.core.filter.HttpFilter;
import io.github.pangzixiang.whatsit.vertx.core.constant.CoreVerticleConstants;
import io.github.pangzixiang.whatsit.vertx.core.model.RouteHandlerRequest;
import io.github.pangzixiang.whatsit.vertx.core.model.RouteHandlerRequestCodec;
import io.github.pangzixiang.whatsit.vertx.core.websocket.handler.WebSocketHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Comparator;

import static io.github.pangzixiang.whatsit.vertx.core.utils.CoreUtils.*;
import static io.github.pangzixiang.whatsit.vertx.core.utils.VerticleUtils.deployVerticle;

@Slf4j
public class ServerStartupVerticle extends CoreVerticle {

    private final DeploymentOptions options = new DeploymentOptions()
            .setWorker(true)
            .setWorkerPoolSize(
                    getApplicationContext()
                            .getApplicationConfiguration()
                            .getVertxOptions()
                            .getWorkerPoolSize());

    public ServerStartupVerticle(ApplicationContext applicationContext) {
        super(applicationContext);
    }

    @Override
    public void start() throws Exception {
        super.start();
        getVertx()
                .eventBus()
                .unregisterDefaultCodec(RouteHandlerRequest.class)
                .registerDefaultCodec(RouteHandlerRequest.class, new RouteHandlerRequestCodec(RouteHandlerRequest.class));
        getVertx().eventBus().consumer(CoreVerticleConstants.SERVER_STARTUP_VERTICLE_ID).handler(this::start)
                .exceptionHandler(exception -> log.error(exception.getMessage(), exception))
                .completionHandler(complete -> log.debug("EventBus Consumer [{}] registered", CoreVerticleConstants.SERVER_STARTUP_VERTICLE_ID));
    }

    private void start(Message<Object> message) {
        if ((Boolean) message.body()) {
            Router router = registerRouter();

            WebSocketHandler webSocketHandler = WebSocketHandler.create(getApplicationContext(), getVertx());
            getApplicationContext().getWebsocketControllers().forEach(webSocketHandler::registerController);

            getVertx().executeBlocking(promise -> {
                log.info("Starting HTTP Server...");
                getVertx().createHttpServer(getApplicationContext().getApplicationConfiguration().getHttpServerOptions())
                        .requestHandler(router)
                        .webSocketHandler(webSocketHandler)
                        .listen(getApplicationContext().getApplicationConfiguration().getPort())
                        .onSuccess(success -> {

                            getApplicationContext().setPort(success.actualPort());

                            log.info("HTTP Server for Service [{}] started at port [{}] successfully! -> [{} ms]"
                                    , getApplicationContext().getApplicationConfiguration().getName().toUpperCase()
                                    , success.actualPort()
                                    , System.currentTimeMillis() - ManagementFactory.getRuntimeMXBean().getStartTime());

                            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                                success.close();
                                log.info("Shutdown HTTP Server, Application total runtime -> {}s"
                                        , (System.currentTimeMillis() - ManagementFactory.getRuntimeMXBean().getStartTime()) / 1000);
                            }));

                            log.debug("added shutdown hook for HTTP Server");

                            promise.complete();
                        })
                        .onFailure(failure -> {
                            log.error("Failed to start the application, exiting...");
                            promise.fail(failure);
                        });
            }).onFailure(failure -> {
                log.error(failure.getMessage(), failure);
                System.exit(-1);
            });
        } else {
            log.error("HTTP Server NOT Started, existing...");
            System.exit(-1);
        }
    }

    private Router registerRouter() {
        Router router = Router.router(getVertx());
        router.route().handler(BodyHandler.create());
        getApplicationContext().getControllers().forEach(clz -> {
            Method[] endpoints = clz.getMethods();
            Object controllerInstance = createInstance(clz, getApplicationContext());
            deployVerticle(getVertx(), (AbstractVerticle) controllerInstance)
                    .onFailure(failure -> {
                        log.error(failure.getMessage(), failure);
                        System.exit(-1);
                    });
            Arrays.stream(endpoints)
                    .filter(m1 -> m1.getAnnotation(RestController.class) != null)
                    .sorted(Comparator.comparing(Method::getName))
                    .forEach(m2 -> {
                        RestController restController = m2.getAnnotation(RestController.class);
                        m2.setAccessible(true);

                        Class<? extends HttpFilter>[] httpFilters = restController.filter();

                        try {
                            String path = restController.path();
                            log.debug("Registering path -> {}", path);

                            String url = refactorControllerPath(path, getApplicationContext().getApplicationConfiguration());

                            log.debug("Refactor path [{}] to [{}]", path, url);

                            Route route = router.route(HttpMethod.valueOf(restController.method().name()), url);

                            //Filter
                            if (httpFilters.length > 0) {
                                Arrays.stream(httpFilters)
                                        .forEach(httpFilter -> {
                                            try {
                                                Method doFilter = httpFilter.getMethod("doFilter", RoutingContext.class);
                                                if (!Modifier.isAbstract(doFilter.getModifiers())) {
                                                    Object filterInstance = createInstance(httpFilter, getApplicationContext());
                                                    deployVerticle(getVertx(), (AbstractVerticle) filterInstance)
                                                            .onFailure(failure -> {
                                                                log.error(failure.getMessage(), failure);
                                                                System.exit(-1);
                                                            });
                                                    doFilter.setAccessible(true);
                                                    route.handler(filter -> invokeMethod(doFilter, filterInstance, filter));
                                                } else {
                                                    log.warn("Skip Invalid Filter [{}]!", httpFilter.getSimpleName());
                                                }
                                            } catch (NoSuchMethodException e) {
                                                throw new RuntimeException(e);
                                            }
                                        });
                                log.info("Endpoint [{} -> {}] registered with Filter -> {}!"
                                        , restController.method(), url, httpFilters);
                            } else {
                                log.info("Endpoint [{} -> {}] registered without Filter!", restController.method(), url);
                            }

                            // Main
                            if (Handler.class.isAssignableFrom(m2.getReturnType())) {
                                route.handler((Handler<RoutingContext>) invokeMethod(m2, controllerInstance));
                            } else {
                                deployVerticle(getVertx(), new RouteHandlerVerticle(getApplicationContext(), url, m2, controllerInstance), options)
                                        .onSuccess(s -> route.handler(h1 -> getVertx()
                                                .eventBus()
                                                .publish(url, new RouteHandlerRequest(h1))))
                                        .onFailure(throwable -> {
                                            log.error("Failed to register endpoint [{}]", url, throwable);
                                            System.exit(-1);
                                        });
                            }
                        } catch (Exception e) {
                            log.error("Failed to register router!", e);
                            System.exit(-1);
                        }
                    });
        });
        return router;
    }
}
