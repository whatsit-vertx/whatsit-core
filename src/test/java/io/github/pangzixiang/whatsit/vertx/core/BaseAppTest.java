package io.github.pangzixiang.whatsit.vertx.core;

import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(VertxExtension.class)
public class BaseAppTest {

    @BeforeAll
    void beforeAll(VertxTestContext vertxTestContext) {
        System.setProperty("config.resource", "test.conf");
        ApplicationRunner.run().onComplete(vertxTestContext.succeeding(unused -> vertxTestContext.completeNow()));
    }
}
