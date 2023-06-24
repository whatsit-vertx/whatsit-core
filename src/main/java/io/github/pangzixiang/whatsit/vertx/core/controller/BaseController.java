package io.github.pangzixiang.whatsit.vertx.core.controller;

import io.github.pangzixiang.whatsit.vertx.core.ApplicationConfiguration;
import io.github.pangzixiang.whatsit.vertx.core.annotation.Filter;
import io.github.pangzixiang.whatsit.vertx.core.filter.HttpFilter;
import io.github.pangzixiang.whatsit.vertx.core.model.HttpResponse;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;

import static io.github.pangzixiang.whatsit.vertx.core.utils.CoreUtils.*;
import static io.github.pangzixiang.whatsit.vertx.core.utils.CoreUtils.invokeMethod;
import static io.github.pangzixiang.whatsit.vertx.core.utils.VerticleUtils.deployVerticle;

/**
 * The type Base controller.
 */
@Slf4j
public class BaseController extends AbstractVerticle {

    @Getter
    private final Router router;

    /**
     * Instantiates a new Base controller.
     *
     * @param router             the router
     */
    public BaseController(Router router) {
        this.router = router;
    }

    @Override
    public void start() throws Exception {
        log.info("Start to register Controller [{}]", this.getClass().getSimpleName());
        Method[] methods = this.getClass().getMethods();
        Path basePath = this.getClass().getAnnotation(Path.class);
        Arrays.stream(methods)
                .filter(method -> method.getAnnotation(Path.class) != null)
                .sorted(Comparator.comparing(Method::getName))
                .forEach(method -> {
                    method.setAccessible(true);
                    Path apiPath = method.getAnnotation(Path.class);
                    Filter filterAnnotation = method.getAnnotation(Filter.class);
                    Class<? extends HttpFilter>[] httpFilters = null;

                    if (filterAnnotation != null) {
                        httpFilters = filterAnnotation.filter();
                    }

                    String path = basePath.value() + apiPath.value();
                    String httpMethod = getHttpMethod(method);
                    if (StringUtils.isEmpty(httpMethod)) {
                        log.warn("Won't register {} for path {} due to invalid http method {}", method.getName(), path, httpMethod);
                    } else {
                        log.debug("Registering path -> {}, method -> {}", path, httpMethod);

                        String url = refactorControllerPath(path, ApplicationConfiguration.getInstance());

                        log.debug("Refactor path [{}] to [{}]", path, url);

                        Route route = router.route(HttpMethod.valueOf(httpMethod), url);

                        // register router
                        if (httpFilters != null && httpFilters.length > 0) {
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
                                                route.handler(filter -> invokeMethod(doFilter, filterInstance, filter));
                                            } else {
                                                log.warn("Skip Invalid Filter [{}]!", httpFilter.getSimpleName());
                                            }
                                        } catch (NoSuchMethodException e) {
                                            throw new RuntimeException(e);
                                        }
                                    });
                            log.info("Endpoint [{} -> {}] registered with Filter -> {}!"
                                    , httpMethod, url, httpFilters);
                        } else {
                            log.info("Endpoint [{} -> {}] registered without Filter!", httpMethod, url);
                        }

                        // register API
                        Consumes consumes = method.getAnnotation(Consumes.class);
                        if (consumes != null) {
                            Arrays.stream(consumes.value()).forEach(route::consumes);
                        }
                        Produces produces = method.getAnnotation(Produces.class);
                        if (produces != null) {
                            Arrays.stream(produces.value()).forEach(route::produces);
                        }
                        route.handler(rc -> {
                            rc.response().putHeader(HttpHeaders.CONTENT_TYPE, rc.getAcceptableContentType());
                            Object result = invokeMethod(method, this, rc);
                            if (result != null && !rc.response().closed() && !rc.response().ended()) {
                                rc.response().end(result instanceof String ? (String) result: Json.encode(result));
                            }
                        });
                    }
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
                .putHeader(HttpHeaders.CONTENT_TYPE.toString(), HttpHeaderValues.APPLICATION_JSON.toString())
                .setStatusCode(status.code())
                .end(Json.encode(HttpResponse.builder().status(status).data(data).build()))
                .onSuccess(success -> log.info("Succeed to send response to {}", routingContext.normalizedPath()))
                .onFailure(throwable -> log.error("Failed to send response to {}", routingContext.normalizedPath(), throwable));
    }

    private String getHttpMethod(Method method) {
        Optional<Annotation> methodOptional = Arrays.stream(method.getAnnotations())
                .filter(annotation -> ApplicationConfiguration.getInstance().getSupportHttpMethodsList().contains(annotation.annotationType()))
                .findFirst();
        if (methodOptional.isPresent()) {
            Annotation annotation = methodOptional.get();
            jakarta.ws.rs.HttpMethod httpMethodAnnotation = annotation.annotationType().getAnnotation(jakarta.ws.rs.HttpMethod.class);
            if (httpMethodAnnotation != null) {
                return httpMethodAnnotation.value();
            }
        }
        return null;
    }

}
