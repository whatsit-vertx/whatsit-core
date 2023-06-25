package io.github.pangzixiang.whatsit.vertx.core.controller;

import io.github.pangzixiang.whatsit.vertx.core.model.EndpointInfo;
import io.github.pangzixiang.whatsit.vertx.core.model.HttpResponse;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import jakarta.ws.rs.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;

import static io.github.pangzixiang.whatsit.vertx.core.utils.CoreUtils.*;

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
     * @param router the router
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
                .map(method -> new EndpointInfo(getVertx(), method))
                .forEach(endpointInfo -> {
                    String baseUri = basePath.value();
                    if (!baseUri.startsWith("/")) {
                        baseUri = "/" + baseUri;
                    }
                    String path = baseUri + endpointInfo.getUri();
                    String httpMethod = endpointInfo.getHttpMethod();
                    if (StringUtils.isEmpty(endpointInfo.getHttpMethod())) {
                        log.warn("Won't register {} for path {} due to invalid http method {}", endpointInfo.getName(), path, httpMethod);
                    } else {
                        log.debug("Registering path -> {}, method -> {}", path, httpMethod);
                        String url = refactorControllerPath(path);
                        log.debug("Refactor path [{}] to [{}]", path, url);
                        Route route = router.route(HttpMethod.valueOf(httpMethod), url);
                        if (!endpointInfo.getFilterFunctions().isEmpty()) {
                            endpointInfo.getFilterFunctions().forEach(routingContextObjectFunction -> route.handler(routingContextObjectFunction::apply));
                            log.info("Endpoint [{} -> {}] registered with Filter -> {}!"
                                    , httpMethod, url, endpointInfo.getFilterNames());
                        } else {
                            log.info("Endpoint [{} -> {}] registered without Filter!", httpMethod, url);
                        }
                        if (endpointInfo.getConsumeTypes() != null) {
                            Arrays.stream(endpointInfo.getConsumeTypes()).forEach(route::consumes);
                        }
                        if (endpointInfo.getProduceTypes() != null) {
                            Arrays.stream(endpointInfo.getProduceTypes()).forEach(route::produces);
                        }
                        route.handler(routingContext -> {
                            routingContext.response().putHeader(HttpHeaders.CONTENT_TYPE, routingContext.getAcceptableContentType());
                            Object result = endpointInfo.getExecuteResult().apply(this, routingContext);
                            if (result != null && !routingContext.response().closed() && !routingContext.response().ended()) {
                                routingContext.response().end(result instanceof String ? (String) result : Json.encode(result));
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

}
