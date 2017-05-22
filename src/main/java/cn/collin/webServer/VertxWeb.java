package cn.collin.webServer;

import cn.collin.kafka.*;
import com.sun.org.apache.regexp.internal.RE;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;

/**
 * Created by collin on 17-5-3.
 */
public class VertxWeb extends AbstractVerticle{
    private Long messageKey;
    private String messageStr;
    private Producer producer;
    private Consumer consumer;
    private RealTimeConsumer realTimeConsumer;
    private String ESURL = "http://localhost:9200/";
    public static Vertx vertx;

    public static void main(String[] args) {
        vertx = Vertx.vertx();
        // 部署发布rest服务
        vertx.deployVerticle(new VertxWeb());
//        VertxOptions
    }

    @Override
    public void start() throws Exception {
        final Router router = Router.router(vertx);
        router.route().handler(CorsHandler.create("*")
                .allowedMethod(HttpMethod.GET)
                .allowedMethod(HttpMethod.POST)
                .allowedMethod(HttpMethod.OPTIONS)
                .allowedHeader("X-PINGARUNER")
                .allowedHeader("Content-Type"));
        router.route().handler(BodyHandler.create());
        router.post("/startInvoke").handler(this::startInvoke);
        router.post("/endInvoke").handler(this::endInvoke);
        router.post("/getRealtimeData").handler(this::getRealtimeData);
        router.get("/endTest").handler(this::endTest);
        vertx.createHttpServer().requestHandler(router::accept).listen(8080);
    }

    private void getRealtimeData(RoutingContext context) {
        context.response().end("ok");
        System.out.println(context.getBodyAsJson().toString());
    }

    private void startInvoke (RoutingContext context) {
        messageKey = context.getBodyAsJson().getLong("id");
        messageStr = context.getBodyAsString();
        /*producer = new Producer(KafkaProperties.TOPIC, true, messageStr, messageKey);
        producer.run();*/
        SparkProducer sparkProducer = new SparkProducer(KafkaProperties.TOPIC2, false,messageStr,messageKey);
        sparkProducer.run();
//         param = Json.decodeValue(context.getBodyAsString())
        System.out.println("context = [" + context.getBodyAsJson().getLong("id") + "]");
//        System.out.println("context = [" + context.getBodyAsString() + "]");
        context.response().end("ok");
    }

    private void endInvoke (RoutingContext context) {
        messageKey = context.getBodyAsJson().getLong("id");
        messageStr = context.getBodyAsString();
        /*producer = new Producer(KafkaProperties.TOPIC, true, messageStr, messageKey);
        producer.run();*/
        SparkProducer sparkProducer = new SparkProducer(KafkaProperties.TOPIC2, false, messageStr, messageKey);
        sparkProducer.run();
        System.out.println("context = [" + context.getBodyAsJson().getLong("id") + "]");
//        System.out.println("context = [" + context.getBodyAsString() + "]");
        context.response().end("ok");

    }

    private void endTest (RoutingContext context) {
//        System.out.println("context = [" + context.getBodyAsJson().getString("id") + "]");
        /*consumer = new Consumer(KafkaProperties.TOPIC);
        consumer.run();*/
//        String indexData = consumer.consumeData();
//        System.out.println("indexData:"+indexData);
        context.response().end("ok");

        /*vertx.createHttpClient().getNow(9200, "localhost", "/", resp -> {
            System.out.println("Got response " + resp.statusCode());
            resp.bodyHandler(body -> {
                System.out.println("Got data " + body.toString("utf-8"));
            });
        });*/
    }

}
