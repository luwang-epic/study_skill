package com.wang.transform;

import com.wang.pojo.Event;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.Partitioner;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

/**
 * 流的转换
 */
public class StreamTransformSample {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        DataStreamSource<Event> stream = env.fromElements(
                new Event("Mary", "./home,./index", 1000L),
                new Event("Bob", "./cart", 2000L),
                new Event("Bob", "./cart", 3000L),
                new Event("Bob", "./cart", 4000L)
        );


        /*
        “分区”（partitioning）操作就是要将数据进行重新分布，传递到不同的流分区去进行下一步处理
        keyBy，它就是一种按照键的哈希值来进行重新分区的操作,只不过这种分区操作只能保证把数据按key“分开”，
        至于分得均不均匀、每个 key 的数据具体会分到哪一区去，这些是完全无从控制的.
        所以我们有时也说，keyBy 是一种逻辑分区（logical partitioning）操作。
        那真正硬核的分区就应该是所谓的“物理分区”（physical partitioning）。
        也就是我们要真正控制分区策略，精准地调配数据，告诉每个数据到底去哪里。

        分区算子并不对数据进行转换处理，只是定义了数据的传输方式
        常见的物理分区策略有随机分配（Random）、轮询分配（Round-Robin）、重缩放（Rescale）和广播（Broadcast）
            1. 随机分区（shuffle）: 将数据随机地分配到下游算子的并行任务中去
            2. 轮询分区（Round-Robin）: 简单来说就是“发牌”，按照先后顺序将数据做依次分发
            3. 重缩放分区（rescale）：和轮询分区类似，只是这个是在一个小团体内部进行轮询分区，比如：让其在同一TaskManager内部进行分区
            4. 广播分区（broadcast）：为经过广播之后，数据会在不同的分区都保留一份，将输入数据复制并发送到下游算子的所有并行任务中去。
            5. 全局分区（global）：全局分区也是一种特殊的分区方式。这种做法非常极端，通过调用.global()方法，
                会将所有的输入流数据都发送到下游算子的第一个并行子任务中去。这就相当于强行让下游任务并行度变成了1，
                所以使用这个操作需要非常谨慎，可能对程序造成很大的压力。
            6. 自定义分区：可 以 通 过 使 用partitionCustom()方法来自定义分区策略
        */
        SingleOutputStreamOperator<Event> singleOutputStreamOperator = stream
                // 随机分区
                //.shuffle()
                // 轮询分区
                //.rebalance()
                // 重缩放分区
                //.rescale()
                // 广播分区
                //.broadcast()
                // 全局分区
                //.global()
                // 自定义分区
                .partitionCustom(new MyPartitioner(), new MyPartitionerKeySelector())
                .filter(new MyFilterFunction()).setParallelism(2)
                .map(new MyMapFunction())
                .flatMap(new MyFlatMapFunction())
                .map(new MyRichMapFunction());

        /*
        DataStream 是没有直接进行聚合的 API 的。因为我们对海量数据做聚合肯定要进行分区并行处理，这样才能提高效率。
        所以在 Flink 中，要做聚合，需要先进行分区；这个操作就是通过keyBy来完成的。keyBy 是聚合前必须要用到的一个算子。
         */

        // 使用Lambda表达式，上面的filter,map,flatmap都可以使用Lambda表达式
        //KeyedStream<Event, String> keyedStream = stream.keyBy(e -> e.user);

        /*
        需要注意的是，keyBy得到的结果是KeyedStream可以认为是“分区流”或者“键控流”，它是对DataStream按照key的一个逻辑分区，
        所以泛型有两个类型：除去当前流中的元素类型外，还需要指定key的类型。
        KeyedStream也继承自DataStream，所以基于它的操作也都归属于DataStream API。
        但它跟之前的转换操作得到的SingleOutputStreamOperator不同，只是一个流的分区操作，并不是一个转换算子。
        KeyedStream 是一个非常重要的数据结构，只有基于它才可以做后续的聚合操作（比如 sum，reduce）；
        而且它可以将当前算子任务的状态（state）也按照key进行划分、限定为仅对当前key有效。
         */
        // 使用自定义类实现
        KeyedStream<Event, String> keyedStream = singleOutputStreamOperator.keyBy(new MyKeySelector());

        // 按键分区之后进行聚合, pojo对象只能使用字段名称来聚合，不能使用下标，对于tuple类型可以直接使用下标
        //keyedStream.sum("timestamp").print();
        //keyedStream.min("timestamp").print();
        /*
        minBy()：与 min()类似，在输入流上针对指定字段求最小值。不同的是，min()只计算指定字段的最小值，
        其他字段会保留最初第一个数据的值；而 minBy()则会返回包含字段最小值的整条数据。
         */
        //keyedStream.minBy("timestamp").print();

        keyedStream.reduce(new MyReduceFunction()).print();


        env.execute();
    }

    /*
    partitionCustom()方法在调用时，方法需要传入两个参数，第一个是自定义分区器（Partitioner）对象，
    第二个是应用分区器的字段，它的指定方式与keyBy指定key基本一样：可以通过字段名称指定，
    也可以通过字段位置索引来指定，还可以实现一个KeySelector。
     */

    /**
     * 自定义分区器，类型为KeySelector中的返回类型
     */
    private static class MyPartitioner implements Partitioner<String> {
        @Override
        public int partition(String key, int numPartitions) {
            return key.hashCode() % numPartitions;
        }
    }

    /**
     * 自定义分区的分区字段选择器
     */
    private static class MyPartitionerKeySelector implements KeySelector<Event, String> {
        @Override
        public String getKey(Event event) throws Exception {
            return event.getUser();
        }
    }


    /*
    富函数类可以获取运行环境的上下文，并拥有一些生命周期方法，所以可以实现更复杂的功能。
    典型的生命周期方法有：open()方法，是Rich Function的初始化方法，close()方法，是生命周期中的最后一个调用的方法

    需要注意的是，这里的生命周期方法，对于一个并行子任务来说只会调用一次；而对应的，实际工作方法，
    例如 RichMapFunction 中的 map()，在每条数据到来后都会触发一次调用。
     */
    /**
     * 富函数，每个类型的算子都有对应的富函数，这里用map来演示
     */
    private static class MyRichMapFunction extends RichMapFunction<Event, Event> {
        // 只调用一次
        @Override
        public void open(Configuration parameters) throws Exception {
            super.open(parameters);
            System.out.println("索引为 " + getRuntimeContext().getIndexOfThisSubtask() + " 的任务开始");
        }

        // 只调用一次
        @Override
        public void close() throws Exception {
            super.close();
            System.out.println("索引为 " + getRuntimeContext().getIndexOfThisSubtask() + " 的任务结束");
        }

        // 调用多次
        @Override
        public Event map(Event event) throws Exception {
            System.out.println("调用map函数......");
            return event;
        }
    }

    /**
     * reduce处理逻辑
     */
    private static class MyReduceFunction implements ReduceFunction<Event> {
        // event1是reduce返回的数据，event2是新来的事件
        @Override
        public Event reduce(Event event1, Event event2) throws Exception {
            return new Event(event1.getUser(), event1.getUrl(), event1.getTimestamp() + event2.getTimestamp());
        }
    }


    /**
     * 定义自己的key分区策略
     */
    private static class MyKeySelector implements KeySelector<Event, String> {
        @Override
        public String getKey(Event event) throws Exception {
            return event.getUser();
        }
    }

    /**
     * flatmap处理逻辑
     */
    private static class MyFlatMapFunction implements FlatMapFunction<Event, Event> {
        @Override
        public void flatMap(Event event, Collector<Event> out) throws Exception {
            String[] urls = event.getUrl().split(",");
            for (String url : urls) {
                out.collect(new Event(event.getUser(), url, event.getTimestamp()));
            }
        }
    }

    /**
     * map处理逻辑
     */
    private static class MyMapFunction implements MapFunction<Event, Event> {
        @Override
        public Event map(Event event) throws Exception {
            if (StringUtils.isNotBlank(event.getUrl())) {
                event.setUrl(event.getUrl().trim().toLowerCase());
            }
            return event;
        }
    }

    /**
     * filter处理逻辑
     */
    private static class MyFilterFunction implements FilterFunction<Event> {
        @Override
        public boolean filter(Event event) throws Exception {
            return StringUtils.isNotBlank(event.getUser()) && StringUtils.isNotBlank(event.getUrl());
        }
    }

}
