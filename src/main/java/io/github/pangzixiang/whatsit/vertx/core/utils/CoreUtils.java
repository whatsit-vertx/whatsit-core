package io.github.pangzixiang.whatsit.vertx.core.utils;

import io.github.pangzixiang.whatsit.vertx.core.ApplicationConfiguration;
import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The type Core utils.
 */
@Slf4j
public class CoreUtils {

    private CoreUtils() { super(); }

    private static final Pattern pattern = Pattern.compile("\\{(.*?)}");

    private static final int CIRCUIT_BREAKER_MAX_FAILURES = 3;

    private static final int CIRCUIT_BREAKER_MAX_RETRIES = 3;

    private static final long CIRCUIT_BREAKER_TIMEOUT_MS = 30_000;

    /**
     * Refactor controller path string.
     *
     * @param path                     the path
     * @param applicationConfiguration the application configuration
     * @return the string
     */
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

    /**
     * Create circuit breaker circuit breaker.
     *
     * @param name  the name
     * @param vertx the vertx
     * @return the circuit breaker
     */
    public static CircuitBreaker createCircuitBreaker(String name, Vertx vertx) {
        CircuitBreakerOptions options = new CircuitBreakerOptions();
        options.setMaxFailures(CIRCUIT_BREAKER_MAX_FAILURES);
        options.setMaxRetries(CIRCUIT_BREAKER_MAX_RETRIES);
        options.setTimeout(CIRCUIT_BREAKER_TIMEOUT_MS);
        return createCircuitBreaker(name, vertx, options);
    }

    /**
     * Create circuit breaker circuit breaker.
     *
     * @param vertx the vertx
     * @return the circuit breaker
     */
    public static CircuitBreaker createCircuitBreaker(Vertx vertx) {
        return createCircuitBreaker(UUID.randomUUID().toString(), vertx);
    }

    /**
     * Create circuit breaker circuit breaker.
     *
     * @param name    the name
     * @param vertx   the vertx
     * @param options the options
     * @return the circuit breaker
     */
    public static CircuitBreaker createCircuitBreaker(String name, Vertx vertx, CircuitBreakerOptions options) {
        CircuitBreaker circuitBreaker = CircuitBreaker.create(name, vertx, options);
        circuitBreaker.retryPolicy(retryCount -> retryCount * 500L);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down CircuitBreaker [{}] ...", name);
            circuitBreaker.close();
        }));
        return circuitBreaker;
    }

    /**
     * Invoke method object.
     *
     * @param method   the method
     * @param instance the instance
     * @param args     the args
     * @return the object
     */
    public static Object invokeMethod(Method method, Object instance, Object ... args) {
        try {
            if (method.getParameterCount() == 0) {
                return method.invoke(instance);
            }
            return method.invoke(instance, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            log.error("Failed to invoke method [{}]", method.getName());
            throw new RuntimeException(e);
        }
    }

    /**
     * Create instance object.
     *
     * @param clz  the clz
     * @param args the args
     * @return the object
     */
    public static Object createInstance(Class<?> clz, Object...args) {
        try {
            Constructor<?>[] constructors = clz.getConstructors();
            for (Constructor<?> constructor : constructors) {
                if (constructor.getParameterCount() == args.length) {
                    return constructor.newInstance(args);
                }
            }
            return null;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            log.error("Failed to init Instance for class[{}]", clz.getSimpleName(), e);
            throw new RuntimeException(e);
        }
    }
}
