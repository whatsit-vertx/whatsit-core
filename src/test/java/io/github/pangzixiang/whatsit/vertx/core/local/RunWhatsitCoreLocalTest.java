package io.github.pangzixiang.whatsit.vertx.core.local;

import io.github.pangzixiang.whatsit.vertx.core.ApplicationRunner;
import io.github.pangzixiang.whatsit.vertx.core.context.ApplicationContext;
import io.github.pangzixiang.whatsit.vertx.core.local.controller.EchoController;
import io.github.pangzixiang.whatsit.vertx.core.local.verticle.TestVerticle;
import io.github.pangzixiang.whatsit.vertx.core.local.websocket.TestWebSocketController;

import static io.github.pangzixiang.whatsit.vertx.core.utils.VerticleUtils.deployVerticle;

public class RunWhatsitCoreLocalTest {
    public static void main(String[] args) {
        ApplicationContext applicationContext = new ApplicationContext();
        applicationContext.registerController(EchoController.class);
        applicationContext.registerWebSocketController(TestWebSocketController.class);
        ApplicationRunner applicationRunner = new ApplicationRunner(applicationContext);
        applicationRunner.run();
        deployVerticle(applicationContext.getVertx(), new TestVerticle());
    }
}
