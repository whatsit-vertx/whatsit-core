package io.github.pangzixiang.whatsit.vertx.core.scheduler;

import com.typesafe.config.Config;
import io.github.pangzixiang.whatsit.vertx.core.annotation.Schedule;
import io.github.pangzixiang.whatsit.vertx.core.context.ApplicationContext;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;

import static java.lang.String.format;

@Slf4j
public abstract class BaseScheduleJob extends AbstractVerticle {

    private static final String PERIOD_KEY = "period";

    private static final String DELAY_KEY = "delay";

    @Getter
    private final ApplicationContext applicationContext;

    public BaseScheduleJob(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public abstract void execute();

    @Override
    public void start() throws Exception {
        this.registerJob();
    }

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        this.registerJob().onComplete(startPromise);
    }

    private Future<Void> registerJob() {
        return getVertx().executeBlocking(promise -> {
            try {
                Method method = this.getClass().getMethod("execute");
                Schedule schedule = method.getAnnotation(Schedule.class);
                if (schedule == null) {
                    String err = format("Invalid Schedule Job %s, Method [execute] does not have Annotation [Schedule]", this.getClass().getSimpleName());
                    log.error(err);
                    promise.fail(err);
                } else {
                    String key = schedule.configKey();
                    long period;
                    long delay;
                    if (StringUtils.isNotBlank(key)) {
                        Config config = getApplicationContext().getApplicationConfiguration().getConfig(key);
                        period = config.getLong(PERIOD_KEY);
                        delay = config.getLong(DELAY_KEY);
                    } else {
                        period = schedule.periodInMillis();
                        delay = schedule.delayInMillis();
                    }

                    if (delay > 0) {
                        if (period > 0) {
                            log.info("register schedule job {} with Settings [period: {}, delay: {}]", this.getClass().getSimpleName(), period, delay);
                            getVertx().setTimer(delay, h1 -> {
                                execute();
                                getVertx().setPeriodic(period, h2 -> execute());
                            });
                        } else {
                            log.info("register schedule job {} with Settings [delay: {}]", this.getClass().getSimpleName(), delay);
                            getVertx().setTimer(delay, h1 -> execute());
                        }
                    } else {
                        if (period > 0) {
                            log.info("register schedule job {} with Settings [period: {}]", this.getClass().getSimpleName(), period);
                            execute();
                            getVertx().setPeriodic(period, h1 -> execute());
                        } else {
                            log.warn("ignore schedule job {} with Settings [period = 0 && delay == 0]", this.getClass().getSimpleName());
                        }
                    }
                    promise.complete();
                }
            } catch (NoSuchMethodException e) {
                log.error("Failed to register schedule job {}", this.getClass().getSimpleName(), e);
                promise.fail(e);
            }
        });
    }
}
