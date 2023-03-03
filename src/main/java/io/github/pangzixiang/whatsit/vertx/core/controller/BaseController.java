package io.github.pangzixiang.whatsit.vertx.core.controller;

import io.github.pangzixiang.whatsit.vertx.core.annotation.RestController;
import io.github.pangzixiang.whatsit.vertx.core.annotation.RestEndpoint;
import io.github.pangzixiang.whatsit.vertx.core.context.ApplicationContext;
import io.github.pangzixiang.whatsit.vertx.core.filter.HttpFilter;
import io.github.pangzixiang.whatsit.vertx.core.model.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Comparator;

import static io.github.pangzixiang.whatsit.vertx.core.constant.HttpConstants.*;
import static io.github.pangzixiang.whatsit.vertx.core.utils.CoreUtils.*;
import static io.github.pangzixiang.whatsit.vertx.core.utils.CoreUtils.invokeMethod;
import static io.github.pangzixiang.whatsit.vertx.core.utils.VerticleUtils.deployVerticle;

/**
 * The type Base controller.
 */
@Slf4j
public class BaseController extends AbstractVerticle {

    @Getter
    private final ApplicationContext applicationContext;

    @Getter
    private final Router router;

    /**
     * Instantiates a new Base controller.
     *
     * @param applicationContext the application context
     */
    public BaseController(ApplicationContext applicationContext, Router router) {
        this.applicationContext = applicationContext;
        this.router = router;
    }

    @Override
    public void start() throws Exception {
        log.info("Start to register Controller [{}]", this.getClass().getSimpleName());
        Method[] methods = this.getClass().getMethods();
        RestController restController = this.getClass().getAnnotation(RestController.class);
        Arrays.stream(methods)
                .filter(method -> method.getAnnotation(RestEndpoint.class) != null)
                .sorted(Comparator.comparing(Method::getName))
                .forEach(method -> {
                    RestEndpoint restEndpoint = method.getAnnotation(RestEndpoint.class);
                    method.setAccessible(true);
                    Class<? extends HttpFilter>[] httpFilters = restEndpoint.filter();

                    String path = restController.basePath() + restEndpoint.path();
                    log.debug("Registering path -> {}", path);

                    String url = refactorControllerPath(path, getApplicationContext().getApplicationConfiguration());

                    log.debug("Refactor path [{}] to [{}]", path, url);

                    Route route = router.route(HttpMethod.valueOf(restEndpoint.method().name()), url);

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
                                , restEndpoint.method(), url, httpFilters);
                    } else {
                        log.info("Endpoint [{} -> {}] registered without Filter!", restEndpoint.method(), url);
                    }
                    route.handler(rc -> invokeMethod(method, this, rc));
                });
        log.info("Succeed to register Controller [{}]!", this.getClass().getSimpleName());
    }

    /**
     * Send json response future.
     *
     * @param routingContext the routing context
     * @param status         the status
     * @param data           the data
     * @return the future
     */
    public Future<Void> sendJsonResponse(RoutingContext routingContext, HttpResponseStatus status, Object data) {
        return routingContext.response()
                .putHeader(CONTENT_TYPE, CONTENT_TYPE_JSON)
                .setStatusCode(status.code())
                .end(objectToString(HttpResponse.builder().status(status).data(data).build()))
                .onSuccess(success -> log.info("Succeed to send response to {}", routingContext.normalizedPath()))
                .onFailure(throwable -> log.error("Failed to send response to {}", routingContext.normalizedPath(), throwable));
    }

}
