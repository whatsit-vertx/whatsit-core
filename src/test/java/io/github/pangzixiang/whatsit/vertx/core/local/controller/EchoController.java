package io.github.pangzixiang.whatsit.vertx.core.local.controller;

import io.github.pangzixiang.whatsit.vertx.core.annotation.Filter;
import io.github.pangzixiang.whatsit.vertx.core.controller.BaseController;
import io.github.pangzixiang.whatsit.vertx.core.local.filter.EchoFilter;
import io.github.pangzixiang.whatsit.vertx.core.local.filter.TestFilter;
import io.github.pangzixiang.whatsit.vertx.core.model.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Path("/v1")
public class EchoController extends BaseController {
    public EchoController(Router router) {
        super(router);
    }

    @Override
    public void start() throws Exception {
        getRouter().route().handler(BodyHandler.create());
        getRouter().route().handler(SessionHandler.create(LocalSessionStore.create(getVertx())));
        super.start();
    }

    @Path("/echoTest")
    @GET
    @Filter(filter = {EchoFilter.class, TestFilter.class})
    public HttpResponse echoTest() {
        log.info("Echo Controller handle request!");
        return HttpResponse.builder().status(HttpResponseStatus.OK).data("echo").build();
    }

    @Path("{echo.test}")
    @GET
    public void pathTest(RoutingContext routingContext) {
        sendJsonResponse(routingContext, HttpResponseStatus.OK, "ok");
    }

    @Path("{echo.test}/{echo.test2.path}")
    @GET
    public void pathTest2(RoutingContext routingContext) {
        sendJsonResponse(routingContext, HttpResponseStatus.OK, "ok");
    }

    @Path("/post")
    @POST
    public void postTest(RoutingContext routingContext) {
        log.info(routingContext.body().asString());
        sendJsonResponse(routingContext, HttpResponseStatus.OK, routingContext.body().asString());
    }
}
