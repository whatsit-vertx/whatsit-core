package io.github.pangzixiang.whatsit.vertx.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Schedule {

    long periodInMillis() default 0;

    long delayInMillis() default 0;

    String configKey() default "";
}
