package io.github.pangzixiang.whatsit.vertx.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import io.github.pangzixiang.whatsit.vertx.core.utils.ClassScannerUtils;
import io.github.pangzixiang.whatsit.vertx.core.verticle.DatabaseConnectionVerticle;
import io.github.pangzixiang.whatsit.vertx.core.verticle.ServerStartupVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.jackson.DatabindCodec;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static io.github.pangzixiang.whatsit.vertx.core.utils.VerticleUtils.deployVerticle;

/**
 * The type Application runner.
 */
@Slf4j
public class ApplicationRunner {

    static {
        ObjectMapper objectMapper = DatabindCodec.mapper();
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        LocalDateTimeSerializer localDateTimeSerializer = new LocalDateTimeSerializer(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        LocalDateTimeDeserializer localDateTimeDeserializer = new LocalDateTimeDeserializer(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        LocalDateSerializer localDateSerializer = new LocalDateSerializer(DateTimeFormatter.ISO_LOCAL_DATE);
        LocalDateDeserializer localDateDeserializer = new LocalDateDeserializer(DateTimeFormatter.ISO_LOCAL_DATE);
        javaTimeModule.addSerializer(LocalDateTime.class, localDateTimeSerializer);
        javaTimeModule.addSerializer(LocalDate.class, localDateSerializer);
        javaTimeModule.addDeserializer(LocalDateTime.class, localDateTimeDeserializer);
        javaTimeModule.addDeserializer(LocalDate.class, localDateDeserializer);
        objectMapper.registerModule(javaTimeModule);
    }

    private ApplicationRunner() {
    }

    /**
     * Run.
     *
     * @param applicationContext the application context
     */
    @SneakyThrows
    public static Future<Void> run(ApplicationContext applicationContext) {
        System.getProperties()
                .forEach((key, value) ->
                        log.debug("System Property: [{}]->[{}]", key, value));

        ApplicationConfiguration applicationConfiguration = ApplicationConfiguration.getInstance();

        Map<Class<? extends AbstractVerticle>, String> coreVerticleDeployIdMap = new ConcurrentHashMap<>();

        Future<Void> deployCoreVerticleFuture = Future.future(promise -> {
            if (applicationConfiguration.isDatabaseEnable()) {
                deployVerticle(applicationContext.getVertx(), new DatabaseConnectionVerticle())
                        .onSuccess(id -> {
                            log.info("Database connection verticle deployed successfully (deployId={})", id);
                            coreVerticleDeployIdMap.put(DatabaseConnectionVerticle.class, id);
                            promise.complete();
                        }).onFailure(throwable -> {
                            log.error("Failed to deploy database connection verticle", throwable);
                            promise.fail(throwable);
                        });
            } else {
                log.warn("Database feature is disabled hence won't deploy database connection verticle");
                promise.complete();
            }
        }).compose(unused -> deployVerticle(applicationContext.getVertx(), new ServerStartupVerticle())
                .compose(id -> {
                    log.info("Server startup Verticle deployed successfully (deployId={})", id);
                    coreVerticleDeployIdMap.put(ServerStartupVerticle.class, id);
                    return Future.succeededFuture();
                }, throwable -> {
                    log.error("Failed to deploy database connection verticle");
                    return Future.failedFuture(throwable);
                })).mapEmpty();

        return deployCoreVerticleFuture.onComplete(result -> {
            ClassScannerUtils.closeScanResult();
            if (result.succeeded()) {
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    log.info("Start to shutdown the application gracefully...");
                    String serverVerticleId = coreVerticleDeployIdMap.get(ServerStartupVerticle.class);
                    String databaseVerticleId = coreVerticleDeployIdMap.get(DatabaseConnectionVerticle.class);
                    applicationContext.getVertx().undeploy(serverVerticleId)
                            .onComplete(unused -> {
                                log.info("Shutdown Server Startup Verticle done (deployId={})", serverVerticleId);
                                if (databaseVerticleId != null) {
                                    applicationContext.getVertx().undeploy(databaseVerticleId).onComplete(unused2 -> {
                                        log.info("Shutdown Database Verticle done (deployId={})", databaseVerticleId);
                                    });
                                }
                            });
                }));
            } else {
                log.error("Failed to start up", result.cause());
                System.exit(1);
            }
        });
    }

    /**
     * Run application context.
     */
    public static Future<Void> run() {
        return run(ApplicationContext.getApplicationContext());
    }
}
