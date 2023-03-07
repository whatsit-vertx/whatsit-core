package io.github.pangzixiang.whatsit.vertx.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The interface Pre deploy.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface PreDeploy {
    /**
     * Order int.
     *
     * @return the int
     */
    int order() default 99999;
}
