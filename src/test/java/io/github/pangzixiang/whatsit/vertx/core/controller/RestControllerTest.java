package io.github.pangzixiang.whatsit.vertx.core.controller;

import com.google.gson.JsonObject;
import io.github.pangzixiang.whatsit.vertx.core.BaseAppTest;
import io.github.pangzixiang.whatsit.vertx.core.context.ApplicationContext;
import io.github.pangzixiang.whatsit.vertx.core.model.HealthCheckResponse;
import io.github.pangzixiang.whatsit.vertx.core.utils.CoreUtils;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RestControllerTest extends BaseAppTest {

    private WebClient webClient;

    @BeforeAll
    void init() {
        webClient = WebClient.create(ApplicationContext.getApplicationContext().getVertx());
    }

    @Test
    void testEchoTest(VertxTestContext vertxTestContext) {
        webClient.get(ApplicationContext.getApplicationContext().getPort(), "localhost", "/v1/echoTest")
                .send()
                .onComplete(vertxTestContext.succeeding(response -> {
                    vertxTestContext.verify(() -> {
                       assertThat(response.statusCode()).isEqualTo(200);
                       assertThat(response.bodyAsString()).contains("echo");
                       vertxTestContext.completeNow();
                    });
                }));
    }

    @Test
    void testPathTest(VertxTestContext vertxTestContext) {
        webClient.get(ApplicationContext.getApplicationContext().getPort(), "localhost", "/v1/echo/test")
                .send()
                .onComplete(vertxTestContext.succeeding(response -> {
                    vertxTestContext.verify(() -> {
                        assertThat(response.statusCode()).isEqualTo(200);
                        assertThat(response.bodyAsString()).contains("ok");
                        vertxTestContext.completeNow();
                    });
                }));
    }

    @Test
    void testPathTest2(VertxTestContext vertxTestContext) {
        webClient.get(ApplicationContext.getApplicationContext().getPort(), "localhost", "/v1/echo/test/test")
                .send()
                .onComplete(vertxTestContext.succeeding(response -> {
                    vertxTestContext.verify(() -> {
                        assertThat(response.statusCode()).isEqualTo(200);
                        assertThat(response.bodyAsString()).contains("ok");
                        vertxTestContext.completeNow();
                    });
                }));
    }

    @Test
    void testCacheTest(VertxTestContext vertxTestContext) {
        webClient.get(ApplicationContext.getApplicationContext().getPort(), "localhost", "/v1/cacheTest")
                .send()
                .onComplete(vertxTestContext.succeeding(response -> {
                    vertxTestContext.verify(() -> {
                        assertThat(response.statusCode()).isEqualTo(200);
                        assertThat(response.bodyAsString()).contains("test");
                        vertxTestContext.completeNow();
                    });
                }));
    }

    @Test
    void testPostTest(VertxTestContext vertxTestContext) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("test", "test");
        webClient.post(ApplicationContext.getApplicationContext().getPort(), "localhost", "/v1/post")
                .sendBuffer(Buffer.buffer(jsonObject.toString()))
                .onComplete(vertxTestContext.succeeding(response -> {
                    vertxTestContext.verify(() -> {
                        assertThat(response.statusCode()).isEqualTo(200);
                        assertThat(response.bodyAsString()).contains("test");
                        vertxTestContext.completeNow();
                    });
                }));
    }

    @Test
    void testHealth(VertxTestContext vertxTestContext) {
        webClient.get(ApplicationContext.getApplicationContext().getPort(), "localhost", "/health")
                .send()
                .onComplete(vertxTestContext.succeeding(response -> {
                    vertxTestContext.verify(() -> {
                        assertThat(response.statusCode()).isEqualTo(200);
                        assertThat(CoreUtils.stringToObject(response.bodyAsString(), HealthCheckResponse.class).isHealthy()).isEqualTo(true);
                        vertxTestContext.completeNow();
                    });
                }));
    }
}
