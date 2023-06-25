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
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
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
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse echoTest() {
        log.info("Echo Controller handle request!");
        return HttpResponse.builder().status(HttpResponseStatus.OK).data("echo").build();
    }

    @Path("/echo/{test}/{test2}")
    @GET
    public HttpResponse pathParamTest(@PathParam("test") boolean test, @PathParam("test2") boolean test2) {
        log.info("received path param {} - {}", test, test2);
        return HttpResponse.builder().status(HttpResponseStatus.OK).data(test&&test2).build();
    }

    @Path("/echo/query")
    @GET
    public HttpResponse queryParamTest(@QueryParam("test") String test, @QueryParam("test2") String test2) {
        log.info("received path param {} - {}", test, test2);
        return HttpResponse.builder().status(HttpResponseStatus.OK).data(test + test2).build();
    }

    @Path("/post")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void postTest(RoutingContext routingContext) {
        log.info(routingContext.body().asString());
        sendJsonResponse(routingContext, HttpResponseStatus.OK, routingContext.body().asString());
    }
}
