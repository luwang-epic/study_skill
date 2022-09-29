package com.wang.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Properties;

/*
消费模式：
    推模式：消息中间件主动将消息推送给消费者
        推模式接收消息是最有效的一种消息处理方式。消费者必须设置一个缓冲区缓存这些消息。
        优点是消费者总是有一堆在内存中待处理的消息，所以当真正去消费消息时效率很高。缺点就是缓冲区可能会溢出。
        要想实现高吞吐量，消费者需要使用推模式。

        用Push方式主动推送有很多弊端；首先是加大Server端的工作量，进而影响Server的性能，
        其次Client的处理能力各不相同，Client的状态不受Server控制，
        如果Client不能及时处理Server推送过来的消息，会造成各种潜在问题。
    拉模式：消费者主动从消息中间件拉取消息
        拉模式在消费者需要时才去消息中间件拉取消息，这段网络开销会明显增加消息延迟，降低系统吞吐量。
        由于拉模式需要消费者手动去中拉取消息，所以实时性较差；消费者难以获取实时消息
        但是消费者可以根据自己的消费能力调整消息拉取的速率，不至于消息消费不过来而导致消费端堆积等

        Client端循环地从Server端拉取消息，主动权在Client手里，自己拉取到一定量消息后，处理妥当了再接着取。
        Pull方式的问题是循环拉取消息的间隔不好设定，间隔太短就处在一个“忙等”的状态，浪费资源；
        每个Pull的时间间隔太长，Server端有消息到来有可能没有被及时处理。

一个consumer group中有多个consumer组成，一个topic有多个partition组成，Kafka有四种主流的分区分配策略： Range、RoundRobin、Sticky、CooperativeSticky。
可以通过配置参数partition.assignment.strategy，修改分区的分配策略。默认策略是Range + CooperativeSticky。Kafka可以同时使用多个分区分配策略。
    1. Range是对每个topic 而言的。首先对同一个 topic 里面的分区按照序号进行排序，并对消费者按照字母顺序进行排序。
        通过partitions数/consumer数 来决定每个消费者应该消费几个分区。如果除不尽，那么前面几个消费者将会多消费 1 个分区。
    2. RoundRobin 轮询分区策略，是把所有的partition（消费者组可以消费多个topic，这里是把所有topic的分区列出来）和所有的consumer都列出来，
        然后按照 hashcode 进行排序，最后通过轮询算法来分配partition给到各个消费者。
    3. 粘性分区定义：可以理解为分配的结果带有“粘性的”。即在执行一次新的分配之前，考虑上一次分配的结果，
        尽量少的调整分配的变动，可以节省大量的开销。首先会尽量均衡的放置分区到消费者上面，
        在出现同一消费者组内消费者出现问题的时候，会尽量保持原有分配的分区不变化。

    每个消费者都会和coordinator保持心跳（默认3s），一旦超时（session.timeout.ms=45s），该消费者会被移除，
    并触发再平衡；或者消费者处理消息的时间过长（max.poll.interval.ms5分钟），也会触发再平衡

    如果某一个消费者挂了，会重新平衡，重新平衡是对所有分区再利用分配的策略
        如：Range策略，7各分区，3个消费者a,b,c组成的消费者组；那么分配为：a->1,2,3  b->4,5  c->6,7
        如果这是a挂了，那么会触发重平衡，此时的结果为：b->1,2,3,4  c->5,6,7

从0.9版本开始，consumer默认将offset保存在Kafka一个内置的topic中，该topic为__consumer_offsets
__consumer_offsets 主题里面采用 key 和 value 的方式存储数据。key是group.id+topic+分区号，value就是当前offset的值。
每隔一段时间，kafka内部会对这个topic进行compact，也就是每个group.id+topic+分区号就保留最新数据。

0.10.0.0版本的kafka消费者和消费组已经不在zk上注册节点了，通过和kafka服务器的协调器coordinator心跳机制来保持连接

消费者组消费消息的流程：
    1. coordinator：辅助实现消费者组的初始化和分区的分配。
        coordinator节点选择 = groupid的hashcode值 % 50（ __consumer_offsets的分区数量）
        例如： groupid的hashcode值 = 1，1% 50 = 1，那么__consumer_offsets主题的1号分区，
            在哪个broker上，就选择这个节点的coordinator作为这个消费者组的老大。
            消费者组下的所有的消费者提交offset的时候就往这个分区去提交offset
    2. 选出一个consumer作为leader，把要消费的topic情况发送给leader 消费者
    3. leader会根据分区分配策略制定消费方案，把消费方案发给coordinator
    4. Coordinator就把消费方案下发给各个consumer
    5. consumer根据消费方案选择自己的消费分区
    6. 每个消费者都会和coordinator保持心跳（默认3s），一旦超时（session.timeout.ms=45s），该消费者会被移除，
        并触发再平衡；或者消费者处理消息的时间过长（max.poll.interval.ms5分钟），也会触发再平衡
    7. 某个消费者发送消费请求（sendFetches），从kafka集群拉取数据，拉取方案有如下几种：
            1) Fetch.min.bytes每批次最小抓取大小，默认1字节 (超过这个大小才会拉取数据)
            2) fetch.max.wait.ms一批数据最小值未达到的超时时间，默认500ms （或者超过这个时间没有拉取，才会拉取数据）
            3) Fetch.max.bytes每批次最大抓取大小，默认50m （每次最大拉取多少数据）
    8. 拉取回来的数据放入到消费者端队列中
    9. 消费者从队列中抓取数据，Max.poll.records一次拉取数据返回消息的最大条数，默认500条
    10. 然后讲过反序列化器，拦截器（Interceptors），最后处理数据


手动提交offset的方法有两种：分别是commitSync（同步提交）和commitAsync（异步提交）。
两者的相同点是，都会将本次提交的一批数据最高的偏移量提交；不同点是，同步提交阻塞当前线程，一直到提交成功，
并且会自动失败重试（由不可控因素导致，也会出现提交失败）；而异步提交则没有失败重试机制，故有可能提交失败。
    commitSync（同步提交）：必须等待offset提交完毕，再去消费下一批数据。
        虽然同步提交 offset 更可靠一些，但是由于其会阻塞当前线程，直到提交成功。
        因此吞吐量会受到很大的影响。因此更多的情况下，会选用异步提交 offset 的方式。
    commitAsync（异步提交）：发送完提交offset请求后，就开始消费下一批数据了。


重复消费：已经消费了数据，但是 offset 没提交。
漏消费：先提交 offset 后消费，有可能会造成数据的漏消费。
精准一次性消费：如果想完成Consumer端的精准一次性消费，那么需要Kafka消费端将消费过程和提交offset过程做原子绑定。
    此时我们需要将Kafka的offset保存到支持事务的自定义介质（比如：MySQL）。


数据积压（消费者如何提高吞吐量）
    1）如果是Kafka消费能力不足，则可以考虑增加Topic的分区数，并且同时提升消费组的消费者数量，消费者数 = 分区数。（两者缺一不可）
    2）如果是下游的数据处理不及时：提高每批次拉取的数量。批次拉取数据过少（拉取数据/处理时间 < 生产速度），
        使处理的数据小于生产的数据，也会造成数据积压。


 */

/**
 * 消费者
 */
public class ConsumerSample {

    public static void main(String[] args) {
        // 1.创建消费者的配置对象
        Properties properties = new Properties();
        // 2.给消费者配置对象添加参数
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        // 配置序列化 必须
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class.getName());

        // 配置消费者组（组名任意起名） 必须
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "test");

        // 是否自动提交offset， 默认为true
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        //properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        // 提交offset的时间周期1000ms，默认5s
        properties.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, 1000);

        // 修改分区分配策略
        properties.put(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG, "org.apache.kafka.clients.consumer.RoundRobinAssignor");

        // 创建消费者对象
        KafkaConsumer<String, String> kafkaConsumer = new KafkaConsumer<String, String>(properties);

        // 注册要消费的主题（可以消费多个主题）
        ArrayList<String> topics = new ArrayList<>();
        topics.add("test");
        kafkaConsumer.subscribe(topics);

        // 消费某个主题的某个分区数据
        //ArrayList<TopicPartition> topicPartitions = new ArrayList<>();
        //topicPartitions.add(new TopicPartition("test", 0));
        //kafkaConsumer.assign(topicPartitions);

        // 指定分区开始消费，需要为新的消费者组，否则对于旧的还是从记录的offset位置开始
        // 对于旧的消费者组，可以通过重置offset的方式消费之前的数据
        //Set<TopicPartition> assignment = new HashSet<>();
        //while (assignment.size() == 0) {
        //    kafkaConsumer.poll(Duration.ofSeconds(1));
        //    // 获取消费者分区分配信息（有了分区分配信息才能开始消费）
        //    assignment = kafkaConsumer.assignment();
        //}
        //// 遍历所有分区，并指定offset从1700的位置开始消费
        //for (TopicPartition tp : assignment) {
        //    kafkaConsumer.seek(tp, 1700);
        //}


        // 拉取数据打印
        while (true) {
            // 设置 1s 中消费一批数据
            ConsumerRecords<String, String> consumerRecords = kafkaConsumer.poll(Duration.ofSeconds(2));
            // 打印消费到的数据
            for (ConsumerRecord<String, String> consumerRecord : consumerRecords) {
                System.out.println(consumerRecord);
            }

            // 同步提交 offset
            //kafkaConsumer.commitSync();
            // 异步提交 offset
            //kafkaConsumer.commitAsync();
        }
    }
}
