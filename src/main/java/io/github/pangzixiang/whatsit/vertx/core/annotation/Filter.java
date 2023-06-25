package io.github.pangzixiang.whatsit.vertx.core.annotation;

import io.github.pangzixiang.whatsit.vertx.core.filter.HttpFilter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Filter {
    /**
     * Filter class [ ].
     *
     * @return the class [ ]
     */
    Class<? extends HttpFilter>[] filter() default {};
}
