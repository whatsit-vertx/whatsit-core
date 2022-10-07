# Whatsit
> A Toolkit to build web microservice using [Vert.x](https://vertx.io)

# Getting Started
### Prerequisite
- JDK 17
- Maven

### Technology Stack
- JDK 17
- [Vert.x](https://vertx.io)

### Features
The whatsit-core library provides the following features:

- Application startup
1. Setting the environment variable, specify the config file for current env.
```shell
java -Dconfig.file=local.conf -jar xxxx.jar
```
2. Init the Application Context
3. Init the Initializer and pass your Controllers into it.
4. Finally, run the application.

```java
import io.github.pangzixiang.whatsit.vertx.core.ApplicationRunner;
import io.github.pangzixiang.whatsit.vertx.core.ApplicationContext;

public class RunWhatsitCoreLocalTest {
    public static void main(String[] args) {
        ApplicationContext applicationContext = new ApplicationContext();
        applicationContext.registerController(SomeController.class);
        ApplicationRunner applicationRunner = new ApplicationRunner(applicationContext);
        applicationRunner.run();
    }
}
```
- Static Config Management
> We are using [typesafe config](https://github.com/lightbend/config) to manage the config, if you set the 'whatsit.env' to 'local', then it will load the local.conf file from classpath.

- Controller
1. Create a new Class to extend the BaseController Class
2. Create a new method with @RestController method
3. Finally, after you add the controller to the ApplicationInitializer, then the application will automatically register the endpoint and deploy this Verticle.

```java
import io.github.pangzixiang.whatsit.vertx.core.RestController;
import io.github.pangzixiang.whatsit.vertx.core.ApplicationContext;
import io.github.pangzixiang.whatsit.vertx.core.HttpRequestMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.ext.web.RoutingContext;

public class SomeController extends BaseController {

    public SomeController(ApplicationContext applicationContext) {
        super(applicationContext);
    }

    @Override
    public void start() throws Exception {
        super.start();
    }

    @RestController(path = "/something", method = HttpRequestMethod.GET)
    public void someEndpoint(RoutingContext routingContext) {
        sendJsonResponse(routingContext
                , HttpResponseStatus.OK
                , "something");
    }
}
```

- Filter(Support Multiple Filters)
1. Create a new Class to extend HttpFilter Class
2. Override the doFilter method, adding your filter logic in it
3. Add the filter to the Controller like:
```java
@RestController(path = "/something", method = HttpRequestMethod.GET, filter = SomeFilter.class)
public void someEndpoint(RoutingContext routingContext) {
    sendJsonResponse(routingContext
        , HttpResponseStatus.OK
        , "something");
}
```

- logging
> we are using [logback](https://github.com/qos-ch/logback) to manage the log, if the 'whatsit.env' not contains 'local'
> , then it will create the log file under the ./log folder,
> for details, please refer to whatsit-core/src/main/resources/logback.xml

- Database(Including [Flyway](https://flywaydb.org/) migration)
1. just to enable the DB connection and Flyway via the conf file:
```text
database: {
  enable: true
  url: "jdbc:h2:file:./h2/core-test-dev"
  user: "sa"
  password: ""
  maxPoolSize: 4
  eventLoopSize: 2
  connectionTimeout: 30
  idleTimeout: 60
  flyway: {
    enable: true
    location: flyway/migration
  }
}
```
2. then you can get the jdbc pool from ApplicationContext.

- Swagger(ongoing)
1. setting the base url to conf file
```text
swagger: {
  baseUrl: /swagger/v1
}
```
2. register the Swagger Controller and your Self Controller

```java
import io.github.pangzixiang.whatsit.local.Controller.EchoController;
import io.github.pangzixiang.whatsit.controller.SwaggerController;
import io.github.pangzixiang.whatsit.vertx.core.ApplicationRunner;
import io.github.pangzixiang.whatsit.vertx.core.ApplicationContext;

public class RunWhatsitSwaggerLocalTest {
    public static void main(String[] args) {
        System.setProperty("whatsit.env", "local");
        ApplicationContext applicationContext = new ApplicationContext();
        applicationContext.registerController(SwaggerController.class, EchoController.class);
        ApplicationRunner applicationRunner = new ApplicationRunner(applicationContext);
        applicationRunner.run();
    }
}
```

3. finally open 'localhost:port/swagger/v1', then you can see the Swagger UI.

- Cache([Caffeine](https://github.com/ben-manes/caffeine))
1. add the cache config to the conf file like below
```text
cache: {
  enable: true,
  autoCreation: true,
  custom: [
    {
      name: cache1
      enable: true,
      expireTime: 1,
      maxSize: 100,
      initSize: 10
    }
  ]
}
```
> while cache.autoCreation = true, then it will auto create the cache even though does not specify it in the conf file.
2. Get the Cache from ApplicationContext:
```java
public class EchoController extends BaseController {
    public EchoController(ApplicationContext applicationContext) {
        super(applicationContext);
    }

    private Cache<String, String> cache;

    @Override
    public void start() throws Exception {
        super.start();
        cache = (Cache<String, String>) getApplicationContext().getCache("cache2");
        cache.put("test", "test");
    }

    @RestController(path = "/cacheTest", method = HttpRequestMethod.GET)
    public void testCache(RoutingContext routingContext) {
        sendJsonResponse(routingContext, HttpResponseStatus.OK, cache.getIfPresent("test"));
    }
}
```

- Schedule Job
1. extend Class BaseScheduleJob
2. put the logic to the abstract method 'execute'
3. add Annotation 'Schedule' to the method 'execute'
4. specify the period or delay to the Annotation or pass the config key into it
```java
public class TestScheduleJob extends BaseScheduleJob{
    public TestScheduleJob(ApplicationContext applicationContext) {
        super(applicationContext);
    }

    @Override
    @Schedule(configKey = "schedule.testJob")
    // or @Schedule(periodInMillis = 10_000, delayInMillis = 0)
    public void execute() {
        // do some thing
    }
}
```
config:
```text
schedule: {
  testJob: {
    period: 30000
    delay: 5000
  }
}
```

### Upcoming feature:
- [Swagger](https://github.com/swagger-api/swagger-ui) Integration (ongoing)
- [Caffeine](https://github.com/ben-manes/caffeine) Cache Integration (done)
- JUnit
- Multiple Filter Support (done)
- TBC

### Dependencies:
- [Vert.x](https://vertx.io)
- [Jackson](https://github.com/FasterXML/jackson)
- [Typesafe config](https://github.com/lightbend/config)
- [Logback](https://github.com/qos-ch/logback)
- [Slf4j](https://github.com/qos-ch/slf4j)
- [Lombok](https://github.com/projectlombok/lombok)
- [Flyway](https://flywaydb.org/)
- [Swagger](https://github.com/swagger-api/swagger-ui)
- [Caffeine](https://github.com/ben-manes/caffeine)
