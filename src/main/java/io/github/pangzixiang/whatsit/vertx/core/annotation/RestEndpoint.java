package io.github.pangzixiang.whatsit.vertx.core.annotation;

import io.github.pangzixiang.whatsit.vertx.core.constant.HttpRequestMethod;
import io.github.pangzixiang.whatsit.vertx.core.filter.HttpFilter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The interface Rest controller.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RestEndpoint {

    /**
     * Path string.
     *
     * @return the string
     */
    String path();

    /**
     * Method http request method.
     *
     * @return the http request method
     */
    HttpRequestMethod method();

    /**
     * Filter class [ ].
     *
     * @return the class [ ]
     */
    Class<? extends HttpFilter>[] filter() default {};
}
