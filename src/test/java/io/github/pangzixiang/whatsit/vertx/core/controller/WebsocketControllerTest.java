package io.github.pangzixiang.whatsit.vertx.core.controller;

import io.github.pangzixiang.whatsit.vertx.core.BaseAppTest;
import io.github.pangzixiang.whatsit.vertx.core.context.ApplicationContext;
import io.vertx.core.http.HttpClient;
import io.vertx.junit5.VertxTestContext;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class WebsocketControllerTest extends BaseAppTest {
    @Test
    void test(VertxTestContext vertxTestContext) {
        HttpClient httpClient = ApplicationContext.getApplicationContext().getVertx().createHttpClient();
        httpClient.webSocket(ApplicationContext.getApplicationContext().getPort(), "localhost" ,"/v1/ws")
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
