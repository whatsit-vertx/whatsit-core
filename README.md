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
1. Set the environment variable, specify the config file for current env.
```shell
java -Dconfig.resource=local.conf -jar xxxx.jar
```
1. Init the Application Context
2. run the application.

```java
import io.github.pangzixiang.whatsit.vertx.core.ApplicationRunner;
import io.github.pangzixiang.whatsit.vertx.core.ApplicationContext;

public class RunWhatsitCoreLocalTest {
    public static void main(String[] args) {
        ApplicationContext applicationContext = new ApplicationContext();
//        applicationContext.getApplicationConfiguration().setHttpServerOptions(new HttpServerOptions().setLogActivity(true));
//        applicationContext.getApplicationConfiguration().setVertxOptions(new VertxOptions());
        ApplicationRunner.run(applicationContext).onSuccess(unused -> {
            deployVerticle(applicationContext.getVertx(), new TestVerticle());
        });
    }
}
```
- Static Config Management
> We are using [typesafe config](https://github.com/lightbend/config) to manage the config

- Controller
1. Create a new Class to extend the BaseController Class and add @Path
2. Create a new method with @Path method
3. Finally, the application will automatically register the endpoint and deploy this Verticle.

```java
import io.github.pangzixiang.whatsit.vertx.core.RestController;
import io.github.pangzixiang.whatsit.vertx.core.HttpRequestMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.ext.web.RoutingContext;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("/v1")
public class SomeController extends BaseController {
    @Path("/test")
    @GET
    public void someEndpoint(RoutingContext routingContext) {
        sendJsonResponse(routingContext
                , HttpResponseStatus.OK
                , "something");
    }

    @Path("/echoTest")
    @GET
    public HttpResponse echoTest() {
        log.info("Echo Controller handle request!");
        return HttpResponse.builder().status(HttpResponseStatus.OK).data("echo").build();
    }

    @Path("/postBody")
    @POST
//    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse postTestBody(@RequestBody TestPojo body) {
        log.info(body.toString());
        return HttpResponse.builder().status(HttpResponseStatus.OK).data(body).build();
    }

    @Path("/echoHeader")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse echoTestHeader(@HeaderParam("test") String test) {
        log.info("Echo Controller handle request!");
        return HttpResponse.builder().status(HttpResponseStatus.OK).data(test).build();
    }

    @Path("/echo/{test}/{test2}")
    @GET
    public HttpResponse pathParamTest(@PathParam("test") boolean test, @PathParam("test2") boolean test2) {
        log.info("received path param {} - {}", test, test2);
        return HttpResponse.builder().status(HttpResponseStatus.OK).data(test&&test2).build();
    }

    @Path("/echo/query")
    @GET
    public HttpResponse queryParamTest(@QueryParam("test") String test, @QueryParam("test2") String test2) {
        log.info("received path param {} - {}", test, test2);
        return HttpResponse.builder().status(HttpResponseStatus.OK).data(test + test2).build();
    }

    @Path("/echo/form")
    @POST
    public HttpResponse queryParamTestForm(@FormParam("test") int test, @FormParam("test2") int test2) {
        log.info("received path param {} - {}", test, test2);
        return HttpResponse.builder().status(HttpResponseStatus.OK).data(test + test2).build();
    }

    @Path("/post")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void postTest(RoutingContext routingContext) {
        log.info(routingContext.body().asString());
        sendJsonResponse(routingContext, HttpResponseStatus.OK, routingContext.body().asString());
    }
}
```

- Filter(Support Multiple Filters)
1. Create a new Class to extend HttpFilter Class
2. Override the doFilter method, adding your filter logic in it
3. Add the filter to the Controller like:

```java

import io.github.pangzixiang.whatsit.vertx.core.annotation.Filter;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("/test")
@GET
@Filter(filter = {xxx.class})
public void someEndpoint(RoutingContext routingContext){
        sendJsonResponse(routingContext
        ,HttpResponseStatus.OK
        ,"something");
        }
```

- logging
> we are using [logback](https://github.com/qos-ch/logback) to manage the log, if the 'config.resource' or 'config.file' not contains 'local'
> , then it will create the log file under the ./log folder,
> for details, please refer to whatsit-core/src/main/resources/logback.xml

- Database
1. just to enable the DB connection via the conf file:
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
}
```
2. then you can get the jdbc pool from ApplicationContext.

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

- WebSocket
1. create a controller class to extend AbstractWebSocketController and add @WebSocketAnnotation to the class
```java
@Slf4j
@WebSocketAnnotation(path = "/ws")
public class TestWebSocketController extends AbstractWebSocketController {
    public TestWebSocketController(ApplicationContext applicationContext, Vertx vertx) {
        super(applicationContext, vertx);
    }

    @Override
    public void startConnect(ServerWebSocket serverWebSocket) {
        log.info(serverWebSocket.binaryHandlerID());
    }

    @Override
    public Handler<WebSocketFrame> onConnect(ServerWebSocket serverWebSocket) {
        return webSocketFrame -> {
            log.info(webSocketFrame.textData());
            serverWebSocket.writeTextMessage("ok");
        };
    }

    @Override
    public Handler<Void> closeConnect(ServerWebSocket serverWebSocket) {
        return v -> {
            log.info("Closed");
        };
    }
}
```
2. finally it will be automatically registered.

### Dependencies:
- [Vert.x](https://vertx.io)
- [Jackson](https://github.com/FasterXML/jackson)
- [Typesafe config](https://github.com/lightbend/config)
- [Logback](https://github.com/qos-ch/logback)
- [Slf4j](https://github.com/qos-ch/slf4j)
- [Lombok](https://github.com/projectlombok/lombok)
