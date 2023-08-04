package com.wang.baidu.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * 消费kafka
 *
 * @Author: wanglu51
 * @Date: 2023/1/9 16:05
 */
public class MyKafkaConsumer {

    private static final String BOOTSTRAP_SERVERS = "gzbh-sandbox23-6271.gzbh:8793,gzbh-sandbox24-6271.gzbh:8793";
    // "gzbh-sandbox23-6271.gzbh:8793,gzbh-sandbox24-6271.gzbh:8793";
    // "localhost:9092";  // "gzbh-sandbox23-6271.gzbh:9093,gzbh-sandbox24-6271.gzbh:9093";
    private static final String TOPIC_NAME = "alarm_late_metrics"; //""metric_proto"; //"test_go"; //"metric_proto"; //"alarm_proto"; // "aggr_metric_proto";
    private static final String GROUP_ID = "test_wang_consumer"; //"test_wang_metric_consumer"; //"test_wang_alarm_consumer"; //"test_wang_aggr_consumer";


    public static void main(String[] args) {
        String topic = TOPIC_NAME;
        if (null != args && args.length > 0) {
            topic = args[0];
        }
        System.out.println("topic ----> " + topic);

        List<String> containStrs = new ArrayList<>();
        if (null != args && args.length > 1) {
            for (int i = 1; i < args.length; i++) {
                containStrs.add(args[i]);
            }
        }
        System.out.println("containStrs ----> " + containStrs);


        Properties props = new Properties();
        props.put("bootstrap.servers", BOOTSTRAP_SERVERS);
        props.put("group.id", GROUP_ID);
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        props.put("session.timeout.ms", "30000");
        props.put("max.poll.records", 1000);
        props.put("auto.offset.reset", "latest");
        props.put("key.deserializer", StringDeserializer.class.getName());
        props.put("value.deserializer", StringDeserializer.class.getName());
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);

        // all topics
        Map<String, List<PartitionInfo>> topicNamePartitionsMap = consumer.listTopics();
        System.out.println(topicNamePartitionsMap.keySet());
        System.out.println(topicNamePartitionsMap);

        consumer.subscribe(Arrays.asList(topic));

        int messageNo = 1;
        System.out.println("---------开始消费---------");
        try {
            while (true) {
                ConsumerRecords<String, String> msgList = consumer.poll(Duration.ofSeconds(1));
                if (null != msgList && msgList.count() > 0) {
                    for (ConsumerRecord<String, String> record : msgList) {
                        if (!record.value().contains("453bf9588c9e488f9ba2c984129090dc")) {
                            continue;
                        }

                        boolean isPrint = true;
                        for (String containStr : containStrs) {
                            if (!record.value().contains(containStr)) {
                                isPrint = false;
                                break;
                            }
                        }
                        if (!isPrint) {
                            continue;
                        }

                        System.out.println(messageNo + "=======receive: key = " + record.key() + ", value = " + record.value() + " offset===" + record.offset());

                        // 当消费了1000条就退出
                        if (messageNo % 1000 == 0) {
                            break;
                        }
                        messageNo++;
                    }
                } else {
                    Thread.sleep(1000);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            consumer.close();
        }

    }


}
