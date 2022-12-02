package io.github.pangzixiang.whatsit.vertx.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The interface Schedule.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Schedule {

    /**
     * Period in millis long.
     *
     * @return the long
     */
    long periodInMillis() default 0;

    /**
     * Delay in millis long.
     *
     * @return the long
     */
    long delayInMillis() default 0;

    /**
     * Config key string.
     *
     * @return the string
     */
    String configKey() default "";
}
