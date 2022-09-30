package io.github.pangzixiang.whatsit.vertx.core.local.Controller;

import com.github.benmanes.caffeine.cache.Cache;
import io.github.pangzixiang.whatsit.vertx.core.annotation.RestController;
import io.github.pangzixiang.whatsit.vertx.core.context.ApplicationContext;
import io.github.pangzixiang.whatsit.vertx.core.constant.HttpRequestMethod;
import io.github.pangzixiang.whatsit.vertx.core.controller.BaseController;
import io.github.pangzixiang.whatsit.vertx.core.local.filter.EchoFilter;
import io.github.pangzixiang.whatsit.vertx.core.local.filter.TestFilter;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EchoController extends BaseController {
    public EchoController(ApplicationContext applicationContext) {
        super(applicationContext);
    }

    private Cache<String, String> cache;

    @Override
    public void start() throws Exception {
        super.start();
        cache = (Cache<String, String>) getApplicationContext().getCache("cache2");
        cache.put("test", "test");
    }

    @RestController(path = "/echoTest", method = HttpRequestMethod.GET, filter = {EchoFilter.class, TestFilter.class})
    public void echoTest(RoutingContext routingContext) {
        log.info("Echo Controller handle request!");
        sendJsonResponse(routingContext, HttpResponseStatus.OK, "echo");
    }

    @RestController(path = "{echo.test}", method = HttpRequestMethod.GET)
    public void pathTest(RoutingContext routingContext) {
        sendJsonResponse(routingContext, HttpResponseStatus.OK, "ok");
    }

    @RestController(path = "{echo.test}/{echo.test2.path}", method = HttpRequestMethod.GET)
    public void pathTest2(RoutingContext routingContext) {
        sendJsonResponse(routingContext, HttpResponseStatus.OK, "ok");
    }

    @RestController(path = "/cacheTest", method = HttpRequestMethod.GET)
    public void testCache(RoutingContext routingContext) {
        sendJsonResponse(routingContext, HttpResponseStatus.OK, cache.getIfPresent("test"));
    }
}
