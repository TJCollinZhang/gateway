package cn.collin.kafka;

import cn.collin.webServer.VertxWeb;
import kafka.utils.ShutdownableThread;
import net.sf.json.JSONObject;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;

import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * Created by collin on 17-5-13.
 */
public class RealTimeConsumer extends ShutdownableThread{
    private final KafkaConsumer<String, String > consumer;
    private final String topic;
    private JSONObject indexData = new JSONObject();
    private JSONObject transData = new JSONObject();
    public static JSONObject realTimeData = new JSONObject();
    private TopicPartition topicPartition = new TopicPartition(KafkaProperties.TOPIC, 0);

    public RealTimeConsumer(String topic, String transData) {
        super("KafkaConsumerExample", false);
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaProperties.KAFKA_SERVER_URL + ":" + KafkaProperties.KAFKA_SERVER_PORT);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "DemoConsumer");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        this.transData = JSONObject.fromObject(transData);
        consumer = new KafkaConsumer<String, String>(props);
        this.topic = topic;
    }

    @Override
    public void doWork() {
        consumer.subscribe(Collections.singletonList(this.topic));
        ConsumerRecords<String, String> records = consumer.poll(1000);
        for (ConsumerRecord<String, String > record  : records){
            System.out.println("Received message: (" + record.key() + ", " + record.value() + ") at offset " + record.offset());
        }
//        records.records()
        List<ConsumerRecord<String, String>> list = records.records(topicPartition);
        System.out.println("listSize:"+list.size());
        consumer.close();
        for (int i=0; i<list.size()-1; i++){
            for (int j=i+1; j<list.size(); j++){
                if (list.get(i).key().equals(list.get(j).key())) {
                    String serverId1 = getServerId(list.get(i).value());
                    String serverId2 = getServerId(list.get(j).value());
                    if (serverId1.equals(serverId2)) {
//                        System.out.println("success");
                        indexData = composeIndex(list.get(i).value(), list.get(j).value());
                        realTimeData.put(i + "", indexData);
                        System.out.println("indexData:" + indexData);
//                        createESIndex(indexData);
                    }
                }
            }

        }


    }

    public String getServerId (String val){
        String serverId = JSONObject.fromObject(val).getString("serverId");
        return serverId;
    }

    public JSONObject composeIndex (String val1, String val2){

        String serverId = getServerId(val1);
        String invokeId = JSONObject.fromObject(val2).getString("invokeId");
//        String dataType = JSONObject.fromObject(val)
        String startTime = JSONObject.fromObject(val1).getString("timestamp");
        String endTime = JSONObject.fromObject(val2).getString("timestamp");
        Long interval = Long.parseLong(startTime) - Long.parseLong(endTime);
        String result = JSONObject.fromObject(val2).getString("result");
        JSONObject jsonObject = new JSONObject();
        /*jsonObject.put("serverId", serverId);
        jsonObject.put("invokeId", invokeId);*/
        jsonObject.put("startTime", startTime);
        jsonObject.put("interval", interval);
        /*jsonObject.put("endTime", endTime);*/
        jsonObject.put("result", result);
        return jsonObject;
    }

    /*public String consumeData (){
        run();
        return indexData;
    }*/

    public void dataHandler (JSONObject indexData) {

//        Vertx vertx = Vertx.vertx();
        /*VertxWeb.vertx.createHttpClient().post(9200, "localhost", "/chain/code", resp -> {
            System.out.println("Got response " + resp.statusCode());
            resp.bodyHandler(body -> {
                System.out.println("Got data " + body.toString("utf-8"));
            });
        }).end(data);*/
    }

    @Override
    public String name() {
        return null;
    }

    @Override
    public boolean isInterruptible() {
        return false;
    }
}
