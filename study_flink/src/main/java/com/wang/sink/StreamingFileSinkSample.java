package com.wang.sink;

import com.wang.pojo.Event;
import org.apache.flink.api.common.serialization.SimpleStringEncoder;
import org.apache.flink.core.fs.Path;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.filesystem.StreamingFileSink;
import org.apache.flink.streaming.api.functions.sink.filesystem.rollingpolicies.DefaultRollingPolicy;

import java.util.concurrent.TimeUnit;

/*
Flink 作为一个快速的分布式实时流处理系统，对稳定性和容错性要求极高。
一旦出现故障，我们应该有能力恢复之前的状态，保障处理结果的正确性。这种性质一般被称作“状态一致性”。
Flink 内部提供了一致性检查点（checkpoint）来保障我们可以回滚到正确的状态；但如果我们在处理过程中任意读写外部系统，
发生故障后就很难回退到从前了。为了避免这样的问题，Flink的DataStream API专门提供了向外部写入数据的方法：addSink
与Source算子非常类似，除去一些Flink预实现的Sink，一般情况下Sink算子的创建是通过调用DataStream的.addSink()方法实现的。

常用的sink有：
    控制台输出
    输出到文件
    输出到Kafka
    输出到Redis
    输出到Elasticsearch
    输出到MySQL（JDBC）
    自定义Sink输出，比如输出到Hbase等
 */

/**
 * 文件输出sink演示
 */
public class StreamingFileSinkSample {
    /*
    StreamingFileSink 为批处理和流处理提供了一个统一的 Sink，它可以将分区文件写入Flink支持的文件系统。
    它可以保证精确一次的状态一致性，大大改进了之前流式文件 Sink 的方式
     */
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(4);

        DataStreamSource<Event> stream = env.fromElements(
                new Event("Mary","./home", 1000L),
                new Event("Bob", "./cart", 2000L),
                new Event("Alice", "./prod?id=100", 3000L),
                new Event("Alice", "./prod?id=200", 3500L),
                new Event("Bob", "./prod?id=2", 2500L),
                new Event("Alice", "./prod?id=300", 3600L),
                new Event("Bob", "./home", 3000L),
                new Event("Bob", "./prod?id=1", 2300L),
                new Event("Bob", "./prod?id=3", 3300L));
        StreamingFileSink<String> fileSink = StreamingFileSink.<String>forRowFormat(
                new Path("D:\\idea_project\\study_skill\\file\\flink\\output"),
                        new SimpleStringEncoder<>("UTF-8"))
                .withRollingPolicy(DefaultRollingPolicy.builder()
                                .withRolloverInterval(TimeUnit.MINUTES.toMillis(15))
                                .withInactivityInterval(TimeUnit.MINUTES.toMillis(5))
                                .withMaxPartSize(1024 * 1024 * 1024)
                                .build())
                .build();
        // 将 Event 转换成 String 写入文件
        stream.map(Event::toString).addSink(fileSink);

        env.execute();
    }
}
