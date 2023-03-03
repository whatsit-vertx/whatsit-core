package io.github.pangzixiang.whatsit.vertx.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The interface Rest controller.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RestController {
    /**
     * Base path string.
     *
     * @return the string
     */
    String basePath() default "";
}
