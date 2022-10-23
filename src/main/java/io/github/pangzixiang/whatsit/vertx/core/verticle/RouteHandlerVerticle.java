package io.github.pangzixiang.whatsit.vertx.core.verticle;

import io.github.pangzixiang.whatsit.vertx.core.context.ApplicationContext;
import io.github.pangzixiang.whatsit.vertx.core.model.RouteHandlerRequest;

import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

import static io.github.pangzixiang.whatsit.vertx.core.utils.CoreUtils.invokeMethod;


@Slf4j
public class RouteHandlerVerticle extends CoreVerticle{

    private final String address;

    private final Object instance;

    private final Method method;


    RouteHandlerVerticle(ApplicationContext applicationContext, String address, Method method, Object instance) {
        super(applicationContext);
        this.address = address;
        this.method = method;
        this.instance = instance;
    }

    @Override
    public void start() throws Exception {
        super.start();
        getVertx()
                .eventBus()
                .consumer(address)
                .handler(message -> {
                    RouteHandlerRequest routeHandlerRequest = (RouteHandlerRequest) message.body();
                    RoutingContext routingContext = routeHandlerRequest.getRoutingContext();
                    log.debug("[{}] received request!"
                            , address);
                    invokeMethod(method, instance, routingContext);
                });
    }
}
