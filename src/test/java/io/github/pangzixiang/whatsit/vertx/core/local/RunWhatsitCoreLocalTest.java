package io.github.pangzixiang.whatsit.vertx.core.local;

import io.github.pangzixiang.whatsit.vertx.core.ApplicationRunner;
import io.github.pangzixiang.whatsit.vertx.core.context.ApplicationContext;
import io.github.pangzixiang.whatsit.vertx.core.local.Controller.EchoController;
import io.github.pangzixiang.whatsit.vertx.core.local.verticle.TestVerticle;

import static io.github.pangzixiang.whatsit.vertx.core.utils.VerticleUtils.deployVerticle;

public class RunWhatsitCoreLocalTest {
    public static void main(String[] args) {
        System.setProperty("whatsit.env", "local");
        ApplicationContext applicationContext = new ApplicationContext();
        applicationContext.registerController(EchoController.class);
        ApplicationRunner applicationRunner = new ApplicationRunner(applicationContext);
        applicationRunner.run().onSuccess(vertx -> {
            deployVerticle(vertx, new TestVerticle());
        });
    }
}
