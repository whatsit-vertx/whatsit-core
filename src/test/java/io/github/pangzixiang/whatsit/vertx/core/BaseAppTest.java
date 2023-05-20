package io.github.pangzixiang.whatsit.vertx.core;

import io.github.pangzixiang.whatsit.vertx.core.context.ApplicationContext;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

import static io.github.pangzixiang.whatsit.vertx.core.constant.CoreVerticleConstants.SERVER_STARTUP_NOTIFICATION_ID;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(VertxExtension.class)
public class BaseAppTest {

    @BeforeAll
    void beforeAll(VertxTestContext vertxTestContext) {
        System.setProperty("config.resource", "test.conf");
        ApplicationContext.getApplicationContext().getVertx().eventBus().consumer(SERVER_STARTUP_NOTIFICATION_ID).handler(unused -> vertxTestContext.completeNow());
        vertxTestContext.assertComplete(ApplicationRunner.run());
    }
}
