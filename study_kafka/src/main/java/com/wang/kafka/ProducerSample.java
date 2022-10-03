package com.wang.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.Cluster;

import java.util.Map;
import java.util.Properties;


/*
传统的消息队列的主要应用场景包括：缓存/消峰、解耦和异步通信
    缓存/消费：有助于控制和优化数据流经过系统的速度，解决生产消息和消费消息的处理速度不一致的情况。
    解耦：允许你独立的扩展或修改两边的处理过程，只要确保它们遵守同样的接口约束。
    异步通信：允许用户把一个消息放入队列，但并不立即处理它，然后在需要的时候再去处理它们。


消息队列的两种模式：
    点对点模式：消费者主动拉取数据，消息收到后清除消息
    发布/订阅模式：(1)消费者消费数据之后，不删除数据 (2)每个消费者相互独立，都可以消费到数据
                 (3) 可以有多个topic主题（浏览、点赞、收藏、评论等）


kafka发送数据的流程：
    main线程 ---> 过滤器（Interceptors, 一般不使用） ---> 序列化器（Serializer） ---> 分区器（Partitioner）
    ---> 消息收集器RecorderAccumulator ---> Sender线程 ---> kafka cluster(broker) ---> selector线程

    主线程：负责消息创建，拦截器，序列化器，分区器等操作，并将消息追加到消息收集器RecorderAccumulator中。
        1. 消息收集器RecorderAccumulator为每个分区都维护了一个 Deque<ProducerBatch> 类型的双端队列。
        2. ProducerBatch 可以暂时理解为是 ProducerRecord 的集合，批量发送有利于提升吞吐量，降低网络影响。
        3. 由于生产者客户端使用 java.io.ByteBuffer 在发送消息之前进行消息保存，并维护了一个 BufferPool 实现 ByteBuffer 的复用；
            该缓存池只针对特定大小（ batch.size 指定）的 ByteBuffer进行管理，对于消息过大的缓存，不能做到重复利用。
        4. 每次追加一条ProducerRecord消息，会寻找/新建对应的双端队列，从其尾部获取一个ProducerBatch，
            判断当前消息的大小是否可以写入该批次中。若可以写入则写入；若不可以写入，则新建一个ProducerBatch，
            判断该消息大小是否超过客户端参数配置 batch.size 的值，不超过，则以 batch.size建立新的ProducerBatch，
            这样方便进行缓存重复利用；若超过，则以计算的消息大小建立对应的 ProducerBatch ，缺点就是该内存不能被复用了。

    Sender线程：
        该线程从消息收集器获取缓存的消息，将其处理为 <Node, List<ProducerBatch> 的形式， Node 表示集群的broker节点。
        进一步将<Node, List<ProducerBatch>转化为<Node, Request>形式，此时才可以向服务端发送数据。
        在发送之前，Sender线程将消息以 Map<NodeId, Deque<Request>> 的形式保存到 InFlightRequests 中进行缓存

    默认分区器：
        指明partition的情况下，直接将指明的值作为partition值；
        没有指明partition值但有key的情况下，将key的hash值与topic的partition数进行取余得到partition值
        既没有partition值又没有key值的情况下，Kafka采用Sticky Partition（黏性分区器），
            会随机选择一个分区，并尽可能一直使用该分区，待该分区的batch已满或者已完成，Kafka再随机一个分区进行使用（和上一次的分区不同）。

发生故障（分区丢失，领导者不可用）时，生产者通常会从代理刷新主题元数据。
它还将定期轮询（默认值：每10分钟一次，即600000ms）。如果将此值设置为负值，则仅在失败时刷新元数据。
如果将其设置为零，则每条消息发送后元数据都会刷新（不推荐）
重要说明：仅在消息发送后刷新才发生，因此，如果生产者从不发送消息，则元数据也不会刷新


Kafka生产者发送数据的条件：
    batch.size：只有数据积累到batch.size之后，sender才会发送数据。默认16k
    linger.ms：如果数据迟迟未达到batch.size，sender等
        待linger.ms设置的时间到了之后就会发送数据。单位ms，默认值是0ms，表示没有延迟。

kafka生产者时线程安全的，因此多个线程可以公用一个KafkaProducer实例

生产者如何提高吞吐量：
    batch.size：批次大小，默认16k
    linger.ms：等待时间，修改为5-100ms
    compression.type：压缩snappy
    RecordAccumulator：缓冲区大小，修改为64m

发送到broker的应答策略：
    1. 0：生产者发送过来的数据，不需要等数据落盘应答。
    2. 1：生产者发送过来的数据，Leader收到数据后应答。
    3. -1（all）：生产者发送过来的数据，Leader和ISR队列里面的所有节点收齐数据后应答。-1和all等价。

    Leader维护了一个动态的in-sync replica set（ISR），意为和Leader保持同步的Follower+Leader集合(leader：0，isr:0,1,2)。
    如果Follower长时间未向Leader发送通信请求或同步数据，则该Follower将被踢出ISR。
        该时间阈值由replica.lag.time.max.ms参数设定，默认30s。例如2超时，(leader:0, isr:0,1)。
    数据完全可靠条件 = ACK级别设置为-1 + 分区副本大于等于2 + ISR里应答的最小副本数量大于等于2

    当ack设置为-1（all）时，生产者发送过来的数据，Leader和ISR队列里面的所有节点收齐数据后，
    leader还没有返回ack给生产者时，leader挂了，此时客户端收不到这个消息的ack，那么会重试，从而导致消息重复

kafka消息一致性语义
    At Least Once：消息不会丢，但可能会重复。
    At Most Once：消息会丢，但不会重复。
    Exactly Once：消息不会丢，也不会重复。可以通过幂等性以及ack=-1来实现kafka生产者的exactly once。

    ack=-1：在上面ack机制里已经介绍过，他能实现at least once语义，保证数据不会丢，但可能会有重复数据。

    Kafka的幂等性其实就是将原来需要在下游进行的去重操作放在了数据上游。开启幂等性的生产者在初始化时会被分配一个PID（producer ID），
    该生产者发往同一个分区（Partition）的消息会附带一个序列号（Sequence Number），
    Broker 端会对<PID, Partition, SeqNumber>作为该消息的主键进行缓存，当有相同主键的消息提交时，
    Broker 只会持久化一条。但是生产者重启时PID 就会发生变化，同时不同的 分区（Partition）也具有不同的编号，
    所以生产者幂等性无法保证跨分区和跨会话的 Exactly Once。（这种情况需要通过事务解决，思想是：生产者使用用户配置的事务id代替或者关联PID，并将其存储在zk中）
    producer.properties 设置参数 enable.idempotence = true 即可启用幂等性。

    Kafka引入了一个新的组件Transaction Coordinator，它管理了一个全局唯一的事务ID（Transaction ID），
    并将生产者的PID和事务ID进行绑定，当生产者重启时虽然PID会变，但仍然可以和Transaction Coordinator交互，
    通过事务ID可以找回原来的PID，这样就保证了重启后的生产者也能保证Exactly Once 了。
    同时，Transaction Coordinator 将事务信息写入 Kafka 的一个内部 Topic，即使整个kafka服务重启，
    由于事务状态已持久化到topic，进行中的事务状态也可以得到恢复，然后继续进行。


kafka数据的有序性：
    1. 单分区内，有序条件
        1）kafka在1.x版本之前保证数据单分区有序，条件如下：
            max.in.flight.requests.per.connection=1（不需要考虑是否开启幂等性）。
        2）kafka在1.x及以后版本保证数据单分区有序，条件如下：
        （1）未开启幂等性
            max.in.flight.requests.per.connection需要设置为1。
        （2）开启幂等性
            max.in.flight.requests.per.connection需要设置小于等于5。
            原因说明：因为在kafka1.x以后，启用幂等后，kafka服务端会缓存producer发来的最近5个request的元数据，
            故无论如何，都可以保证最近5个request的数据都是有序的。
    2. 多分区，分区与分区间无序


kafka的几个概念：
    AR: 分区中的所有副本统称为AR（Assigned Replicas）。
    ISR: 所有与leader副本保持一定程度同步的副本（包括leader副本在内）组成ISR（In-Sync Replicas），ISR集合是AR集合中的一个子集。
    OSR: 与leader副本同步滞后过多的副本（不包括leader副本）组成OSR（Out-of-Sync Replicas）
    LEO: Log End Offset：Producer 写入到 Kafka 中的最新一条数据的 offset。
    HW: High Watermark：：指的是消费者能见到的最大的 offset，ISR 队列中最小的 LEO。

    follower 发生故障后会被临时踢出ISR，待该follower恢复后，follower会读取本地磁盘记录的上次的HW，
    并将log文件高于HW的部分截取掉，从HW开始向leader进行同步。等该follower的LEO大于等于该Partition的HW，
    即follower追上leader之后，就可以重新加入ISR了。

    leader 发生故障之后，会从 ISR 中选出一个新的 leader，之后，为保证多个副本之间的数据一致性，
    其余的 follower 会先将各自的 log 文件高于 HW 的部分截掉，然后从新的 leader同步数据。

    这只能保证副本之间的数据一致性，并不能保证数据不丢失或者不重复。
    但是如果ack=-1就可以保证所有ISR成功（即HW为最新消息了），这是数据将不会丢失


高效读写数据：
    Topic是逻辑上的概念，而partition是物理上的概念，每个partition对应于一个log文件，
    该log文件中存储的就是Producer生产的数据。Producer生产的数据会被不断追加到该log文件末端，
    为防止log文件过大导致数据定位效率低下，Kafka采取了分片和索引机制，将每个partition分为多个segment。
    每个segment包括：“.index”文件、“.log”文件和.timeindex等文件。这些文件位于一个文件夹下，
    该文件夹的命名规则为：topic名称+分区序号，例如：first-0。该文件夹下主要包括：
    log 日志文件 和 .index 偏移量索引文件 和 .timeindex 时间戳索引文件 （segment的第一条消息的offset命名）

    Index为稀疏索引，大约每往log文件写入4kb数据，会往index文件写入一条索引。参数log.index.interval.bytes默认4kb。
    Index文件中保存的offset为相对offset，这样能确保offset的值所占空间不会过大，因此能将offset的值控制在固定大小

    Kafka中默认的日志保存时间为7天，可以设置保留时间或者根据文件大小来删除或者compact（compact对于相同的消息key只保留最新的，只有特殊场景下才会使用）消息文件

    高效读写的优化：
        1) Kafka 本身是分布式集群，可以采用分区技术，并行度高
        2）读数据采用稀疏索引，可以快速定位要消费的数据
        3）顺序写磁盘
        4) 页缓存 + 零拷贝技术
            当 Kafka 服务器接收到消息后，其并不直接写入磁盘，而是先写入内存中。
            随后，Kafka 服务端会根据不同设置参数，选择不同的刷盘过程
            如果我们设置 log.flush.interval.messages=1，那么每次来一条消息，就会刷一次磁盘。
            通过这种方式，就可以降低消息丢失的概率，这种情况我们称之为同步刷盘。 反之，我们称之为异步刷盘。

            但要注意的是，Kafka 服务端返回确认之后，仅仅表示该消息已经写入到 Kafka 服务器的 PageCache 中，并不代表其已经写入磁盘了。
            这时候如果 Kafka 所在服务器断电或宕机，那么消息也是丢失了。而如果只是 Kafka 服务崩溃，那么消息并不会丢失


Kafka Controller Leader
    Kafka 集群中有一个 broker 的 Controller 会被选举为 Controller Leader，
    负责管理集群broker 的上下线，所有 topic 的分区副本分配和 Leader 选举等工作。

    选举大致过程：
        1. broker启动后在zk中注册，zk路径为：/brokers/ids/{broker_id}
        2. 向zk创建/controller路径的znode（数据为：{"version":1,"brokerid":0,"timestamp":"1664411906339"}），
            创建成功的节点成为leader
        3. 成为leader后，监听brokers节点变化

Kafka Partition Leader （副本Leader）
    1. Controller Leader决定副本Leader的选举，默认会将AR列表中的第一个选为Leader
        选举规则：在isr中存活为前提，按照AR中排在前面的优先。
    2. Controller Leader将节点信息上传到ZK的路径/brokers/topics/{topicName}/partitions/{partitions_num}/state
        数据为：{"controller_epoch":1,"leader":0,"version":1,"leader_epoch":0,"isr":[0]}
    3. 其他contorller从zk同步相关信息
    4. 假设Broker1中副本Leader挂了， Controller Leader监听到节点变化
        如果不是副本leeader挂了，暂时不处理，等待Controller Leader的负载均衡处理
    5. 获取ISR和AR，选举新的副本Leader（在isr中存活为前提，按照AR中排在前面的优先），并更新分区Leader及ISR

 */


/**
 * 生产者
 */
public class ProducerSample {

    public static void main(String[] args) {
        // 1. 创建 kafka 生产者的配置对象
        Properties properties = new Properties();
        // 2. 给 kafka 配置对象添加配置信息：bootstrap.servers
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

        // key,value 序列化（必须）：key.serializer，value.serializer
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringSerializer");
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringSerializer");

        // 指定分区器
        properties.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, "com.wang.kafka.ProducerSample$MyPartitioner");

        // batch.size：批次大小，默认 16K
        properties.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        // linger.ms：等待时间，默认 0
        properties.put(ProducerConfig.LINGER_MS_CONFIG, 100);
        // RecordAccumulator：缓冲区大小，默认 32M：buffer.memory
        properties.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
        // compression.type：压缩，默认 none，可配置值 gzip、snappy、lz4 和 zstd
        //properties.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");

        // 设置 acks -1和all都可以
        properties.put(ProducerConfig.ACKS_CONFIG, "all");
        // 重试次数 retries，默认是int最大值，2147483647
        properties.put(ProducerConfig.RETRIES_CONFIG, 3);

        // 设置幂等性，默认为true，开启状态
        properties.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        // 设置事务 id（必须），事务 id 任意起名； 使用事务必须开启幂等性
        properties.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, "transaction_1");


        // 3. 创建 kafka 生产者对象
        KafkaProducer<String, String> kafkaProducer = new KafkaProducer<>(properties);

        // 初始化事务
        kafkaProducer.initTransactions();
        // 开启事务
        kafkaProducer.beginTransaction();

        try {
            // 4. 调用 send 方法,发送消息
            for (int i = 0; i < 5; i++) {
                // 异步发送
                //kafkaProducer.send(new ProducerRecord<>("test","wang async" + i));
                // 有回调的异步发送
                //kafkaProducer.send(new ProducerRecord<>("first","wang async " + i), new Callback() {
                //    // 该方法在Producer收到ack时调用，为异步调用
                //    @Override
                //    public void onCompletion(RecordMetadata metadata, Exception exception) {
                //        if (exception == null) {
                //            // 没有异常,输出信息到控制台
                //            System.out.println(" 主题： " + metadata.topic() + " -> " + "分区：" + metadata.partition());
                //        } else {
                //            // 出现异常打印
                //            exception.printStackTrace();
                //        }
                //    }
                //});

                // 指定分区发送
                //kafkaProducer.send(new ProducerRecord<>("test", 1, "", "wang async" + i));

                // 不指定分区使用分区器分配分区，通过查看消费端来确定消息发送到哪个分区了
                kafkaProducer.send(new ProducerRecord<>("test", "wang async" + i));

                // 同步发送
                //kafkaProducer.send(new ProducerRecord<>("test","wang sync" + i)).get();
            }

            // 提交事务
            kafkaProducer.commitTransaction();
        } catch (Exception e) {
            // 终止事务
            kafkaProducer.abortTransaction();
        } finally {
            // 5. 关闭资源
            kafkaProducer.close();
        }
    }


    /**
     * 1. 实现接口 Partitioner
     * 2. 实现 3 个方法:partition,close,configure
     * 3. 编写 partition 方法,返回分区号
     */
    public static class MyPartitioner implements Partitioner {
        /**
         * 返回信息对应的分区
         *
         * @param topic      主题
         * @param key        消息的 key
         * @param keyBytes   消息的 key 序列化后的字节数组
         * @param value      消息的 value
         * @param valueBytes 消息的 value 序列化后的字节数组
         * @param cluster    集群元数据可以查看分区信息
         * @return
         */
        @Override
        public int partition(String topic, Object key, byte[]
                keyBytes, Object value, byte[] valueBytes, Cluster cluster) {
            // 获取消息
            String msgValue = value.toString();
            // 创建 partition
            int partition;
            // 使用value的hash
            if (msgValue.hashCode() % 2 == 0) {
                partition = 0;
            } else {
                partition = 1;
            }
            // 返回分区号
            return partition;
        }
        // 关闭资源
        @Override
        public void close() {
        }
        // 配置方法
        @Override
        public void configure(Map<String, ?> configs) {
        }

    }

}
