package io.github.pangzixiang.whatsit.vertx.core.local;

import io.github.pangzixiang.whatsit.vertx.core.ApplicationRunner;
import io.github.pangzixiang.whatsit.vertx.core.context.ApplicationContext;
import io.github.pangzixiang.whatsit.vertx.core.local.controller.EchoController;
import io.github.pangzixiang.whatsit.vertx.core.local.verticle.TestVerticle;
import io.github.pangzixiang.whatsit.vertx.core.local.websocket.TestWebSocketController;

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
        ApplicationRunner applicationRunner = new ApplicationRunner(applicationContext);
//        applicationRunner.run(new HttpServerOptions().setLogActivity(true));
        applicationRunner.run();
        deployVerticle(applicationContext.getVertx(), new TestVerticle());
    }
}
