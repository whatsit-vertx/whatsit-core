package io.github.pangzixiang.whatsit.vertx.core.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.pangzixiang.whatsit.vertx.core.config.ApplicationConfiguration;
import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class CoreUtils {

    private static final ObjectMapper objectMapper;

    private static final Pattern pattern = Pattern.compile("\\{(.*?)}");

    private static final int CIRCUIT_BREAKER_MAX_FAILURES = 3;

    private static final int CIRCUIT_BREAKER_MAX_RETRIES = 3;

    private static final long CIRCUIT_BREAKER_TIMEOUT_MS = 30_000;

    static {
        objectMapper = new ObjectMapper();
    }

    public static String objectToString(Object o) throws JsonProcessingException {
        return objectMapper.writeValueAsString(o);
    }

    public static <T> T StringToObject(String json, Class<T> clz) {
        return objectMapper.convertValue(json, clz);
    }

    public static String refactorControllerPath(String path, ApplicationConfiguration applicationConfiguration) {
        Matcher matcher = pattern.matcher(path);
        while (matcher.find()) {
            String key = matcher.group(1);
            String value = applicationConfiguration.getString(key);
            if (StringUtils.isNotBlank(value)) {
                log.debug("Parsing Router Path {} with [key: {}, value: {}]", path, key, value);
                path = path.replace(matcher.group(0), value);
            } else {
                String err = String.format("Failed to parse router URL [%s]! [key: %s, value: %s]"
                        , path, key, value);
                throw new IllegalArgumentException(err);
            }
        }

        return path.replaceAll("/+", "/");
    }

    public static CircuitBreaker createCircuitBreaker(String name, Vertx vertx) {
        CircuitBreakerOptions options = new CircuitBreakerOptions();
        options.setMaxFailures(CIRCUIT_BREAKER_MAX_FAILURES);
        options.setMaxRetries(CIRCUIT_BREAKER_MAX_RETRIES);
        options.setTimeout(CIRCUIT_BREAKER_TIMEOUT_MS);
        CircuitBreaker circuitBreaker = CircuitBreaker.create(name, vertx, options);
        circuitBreaker.retryPolicy(retryCount -> retryCount * 500L);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down CircuitBreaker [{}] ...", name);
            circuitBreaker.close();
        }));
        return circuitBreaker;
    }

    public static CircuitBreaker createCircuitBreaker(Vertx vertx) {
        return createCircuitBreaker(UUID.randomUUID().toString(), vertx);
    }
}
