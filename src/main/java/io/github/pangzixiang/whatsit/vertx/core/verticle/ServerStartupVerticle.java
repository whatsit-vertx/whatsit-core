package io.github.pangzixiang.whatsit.vertx.core.verticle;

import io.github.pangzixiang.whatsit.vertx.core.annotation.RestController;
import io.github.pangzixiang.whatsit.vertx.core.context.ApplicationContext;
import io.github.pangzixiang.whatsit.vertx.core.filter.HttpFilter;
import io.github.pangzixiang.whatsit.vertx.core.constant.CoreVerticleConstants;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.management.ManagementFactory;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Comparator;

import static io.github.pangzixiang.whatsit.vertx.core.utils.CoreUtils.refactorControllerPath;
import static io.github.pangzixiang.whatsit.vertx.core.utils.VerticleUtils.deployVerticle;

@Slf4j
public class ServerStartupVerticle extends CoreVerticle {

    public ServerStartupVerticle(ApplicationContext applicationContext) {
        super(applicationContext);
    }

    @Override
    public void start() throws Exception {
        super.start();
        getVertx().eventBus().consumer(CoreVerticleConstants.SERVER_STARTUP_VERTICLE_ID).handler(this::start)
                .exceptionHandler(exception -> log.error(exception.getMessage(), exception))
                .completionHandler(complete -> log.info("EventBus Consumer [{}] registered", CoreVerticleConstants.SERVER_STARTUP_VERTICLE_ID));
    }

    private void start(Message<Object> message) {
        if ((Boolean) message.body()) {
            Router router = registerRouter();

            getVertx().executeBlocking(promise -> {
                log.info("Starting HTTP Server...");
                getVertx().createHttpServer()
                        .requestHandler(router)
                        .listen(getApplicationContext().getApplicationConfiguration().getPort())
                        .onSuccess(success -> {
                            log.info("[{}] HTTP Server for Service [{}] started at port [{}] successfully! -> [{} ms]"
                                    , getApplicationContext().getApplicationConfiguration().getEnv().toUpperCase()
                                    , getApplicationContext().getApplicationConfiguration().getName().toUpperCase()
                                    , success.actualPort()
                                    , System.currentTimeMillis() - ManagementFactory.getRuntimeMXBean().getStartTime());

                            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                                success.close();
                                log.info("Shutdown HTTP Server, Application total runtime -> {}s"
                                        , (System.currentTimeMillis() - ManagementFactory.getRuntimeMXBean().getStartTime())/1000);
                            }));

                            log.info("added shutdown hook for HTTP Server");

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
            Object controllerInstance = createInstance(clz);
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
                                                    Object filterInstance = createInstance(httpFilter);
                                                    deployVerticle(getVertx(), (AbstractVerticle) filterInstance)
                                                            .onFailure(failure -> {
                                                                log.error(failure.getMessage(), failure);
                                                                System.exit(-1);
                                                            });
                                                    doFilter.setAccessible(true);
                                                    route.handler(filter -> invokeMethod(filter, doFilter, filterInstance));
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
                                route.handler(h1 -> invokeMethod(h1, m2, controllerInstance));
                            }
                        } catch (Exception e) {
                            log.error("Failed to register router!", e);
                            System.exit(-1);
                        }
                    });
        });
        return router;
    }

    private void invokeMethod(RoutingContext routingContext, Method method, Object instance) {
        try {
            method.invoke(instance, routingContext);
        } catch (IllegalAccessException | InvocationTargetException e) {
            RestController restController = method.getAnnotation(RestController.class);
            log.error("Failed to invoke method [{}] for Endpoint: [{}]", method.getName(), restController.path());
            routingContext.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private Object invokeMethod(Method method, Object instance) {
        try {
            return method.invoke(instance);
        } catch (IllegalAccessException | InvocationTargetException e) {
            RestController restController = method.getAnnotation(RestController.class);
            log.error("Failed to invoke method [{}] for Endpoint: [{}]", method.getName(), restController.path());
            throw new RuntimeException(e);
        }
    }

    private Object createInstance(Class<? extends AbstractVerticle> clz) {
        try {
            Constructor<? extends AbstractVerticle> constructor = clz.getConstructor(ApplicationContext.class);
            return constructor.newInstance(getApplicationContext());
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            log.error("Failed to init Instance for class[{}]", clz.getSimpleName());
            throw new RuntimeException(e);
        }
    }
}
