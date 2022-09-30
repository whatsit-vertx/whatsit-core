package io.github.pangzixiang.whatsit.core.annotation;

import io.github.pangzixiang.whatsit.core.constant.HttpRequestMethod;
import io.github.pangzixiang.whatsit.core.filter.HttpFilter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RestController {

    String path();

    HttpRequestMethod method();

    Class<? extends HttpFilter>[] filter() default {};
}
