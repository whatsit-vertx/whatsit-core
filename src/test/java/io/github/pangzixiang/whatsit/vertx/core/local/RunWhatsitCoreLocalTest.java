package io.github.pangzixiang.whatsit.vertx.core.local;

import io.github.pangzixiang.whatsit.vertx.core.ApplicationRunner;
import io.github.pangzixiang.whatsit.vertx.core.context.ApplicationContext;
import io.github.pangzixiang.whatsit.vertx.core.local.verticle.TestVerticle;
import lombok.extern.slf4j.Slf4j;

import static io.github.pangzixiang.whatsit.vertx.core.utils.VerticleUtils.deployVerticle;

@Slf4j
public class RunWhatsitCoreLocalTest {
    /**
     * -Dconfig.resource=local.conf
     * -Dcom.sun.management.jmxremote.port=8088
     * -Dcom.sun.management.jmxremote.authenticate=false
     * -Dcom.sun.management.jmxremote.ssl=false
     */
    public static void main(String[] args) {
        ApplicationContext applicationContext = new ApplicationContext();
//        applicationContext.getApplicationConfiguration().setHttpServerOptions(new HttpServerOptions().setLogActivity(true));
//        applicationContext.getApplicationConfiguration().setVertxOptions(new VertxOptions());
        ApplicationRunner.run(applicationContext);
        deployVerticle(applicationContext.getVertx(), new TestVerticle());
    }
}
