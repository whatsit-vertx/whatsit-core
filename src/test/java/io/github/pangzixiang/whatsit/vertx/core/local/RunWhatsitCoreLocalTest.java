package io.github.pangzixiang.whatsit.vertx.core.local;

import io.github.pangzixiang.whatsit.vertx.core.ApplicationRunner;
import io.github.pangzixiang.whatsit.vertx.core.context.ApplicationContext;
import io.github.pangzixiang.whatsit.vertx.core.local.controller.EchoController;
import io.github.pangzixiang.whatsit.vertx.core.local.verticle.TestVerticle;
import io.github.pangzixiang.whatsit.vertx.core.local.websocket.TestWebSocketController;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;

import static io.github.pangzixiang.whatsit.vertx.core.utils.VerticleUtils.deployVerticle;

public class RunWhatsitCoreLocalTest {
    /**
     * -Dconfig.resource=local.conf
     * -Dcom.sun.management.jmxremote.port=8088
     * -Dcom.sun.management.jmxremote.authenticate=false
     * -Dcom.sun.management.jmxremote.ssl=false
     */
    public static void main(String[] args) {
        ApplicationContext applicationContext = new ApplicationContext();
        applicationContext.registerController(EchoController.class);
        applicationContext.registerWebSocketController(TestWebSocketController.class);
        applicationContext.registerGlobalRouterHandler(SessionHandler.create(LocalSessionStore.create(applicationContext.getVertx())), BodyHandler.create());
//        applicationContext.getApplicationConfiguration().setHttpServerOptions(new HttpServerOptions().setLogActivity(true));
//        applicationContext.getApplicationConfiguration().setVertxOptions(new VertxOptions());
        ApplicationRunner.run(applicationContext);
        deployVerticle(applicationContext.getVertx(), new TestVerticle());
    }
}
