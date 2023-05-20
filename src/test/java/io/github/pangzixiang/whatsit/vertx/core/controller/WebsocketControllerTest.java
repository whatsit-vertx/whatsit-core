package io.github.pangzixiang.whatsit.vertx.core.controller;

import io.github.pangzixiang.whatsit.vertx.core.BaseAppTest;
import io.github.pangzixiang.whatsit.vertx.core.context.ApplicationContext;
import io.vertx.core.http.HttpClient;
import io.vertx.junit5.VertxTestContext;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.github.pangzixiang.whatsit.vertx.core.constant.CoreVerticleConstants.SERVER_STARTUP_NOTIFICATION_ID;
import static org.assertj.core.api.Assertions.assertThat;

public class WebsocketControllerTest extends BaseAppTest {

    private HttpClient httpClient;

    @BeforeAll
    void init() {
        httpClient = ApplicationContext.getApplicationContext().getVertx().createHttpClient();
    }

    @Test
    void test(VertxTestContext vertxTestContext) {
        httpClient.webSocket(ApplicationContext.getApplicationContext().getPort(), "localhost", "/v1/ws")
                .onComplete(vertxTestContext.succeeding(ws -> {
                    String testString = String.valueOf(RandomUtils.nextDouble());
                    ws.writeTextMessage(testString);
                    vertxTestContext.verify(() -> {
                        ws.frameHandler(frame -> {
                            assertThat(frame.textData()).isEqualTo(testString);
                            vertxTestContext.completeNow();
                        });
                    });
                }));
    }
}
