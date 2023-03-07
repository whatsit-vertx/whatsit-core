package io.github.pangzixiang.whatsit.vertx.core.local.controller;

import com.github.benmanes.caffeine.cache.Cache;
import io.github.pangzixiang.whatsit.vertx.core.annotation.RestController;
import io.github.pangzixiang.whatsit.vertx.core.annotation.RestEndpoint;
import io.github.pangzixiang.whatsit.vertx.core.context.ApplicationContext;
import io.github.pangzixiang.whatsit.vertx.core.constant.HttpRequestMethod;
import io.github.pangzixiang.whatsit.vertx.core.controller.BaseController;
import io.github.pangzixiang.whatsit.vertx.core.local.filter.EchoFilter;
import io.github.pangzixiang.whatsit.vertx.core.local.filter.TestFilter;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController(basePath = "/v1")
public class EchoController extends BaseController {
    public EchoController(Router router) {
        super(router);
    }

    private Cache<String, String> cache;

    @Override
    public void start() throws Exception {
        getRouter().route().handler(BodyHandler.create());
        getRouter().route().handler(SessionHandler.create(LocalSessionStore.create(getVertx())));
        cache = (Cache<String, String>) ApplicationContext.getApplicationContext().getCache("cache2");
        cache.put("test", "test");
        super.start();
    }

    @RestEndpoint(path = "/echoTest", method = HttpRequestMethod.GET, filter = {EchoFilter.class, TestFilter.class})
    public void echoTest(RoutingContext routingContext) {
        log.info("Echo Controller handle request!");
        sendJsonResponse(routingContext, HttpResponseStatus.OK, "echo");
    }

    @RestEndpoint(path = "{echo.test}", method = HttpRequestMethod.GET)
    public void pathTest(RoutingContext routingContext) {
        sendJsonResponse(routingContext, HttpResponseStatus.OK, "ok");
    }

    @RestEndpoint(path = "{echo.test}/{echo.test2.path}", method = HttpRequestMethod.GET)
    public void pathTest2(RoutingContext routingContext) {
        sendJsonResponse(routingContext, HttpResponseStatus.OK, "ok");
    }

    @RestEndpoint(path = "/cacheTest", method = HttpRequestMethod.GET)
    public void testCache(RoutingContext routingContext) {
        sendJsonResponse(routingContext, HttpResponseStatus.OK, cache.getIfPresent("test"));
    }

    @RestEndpoint(path = "/post", method = HttpRequestMethod.POST)
    public void postTest(RoutingContext routingContext) {
        log.info(routingContext.body().asString());
        getVertx().setTimer(10_000, promise -> {
            sendJsonResponse(routingContext, HttpResponseStatus.OK, "ok");
        });
    }
}
