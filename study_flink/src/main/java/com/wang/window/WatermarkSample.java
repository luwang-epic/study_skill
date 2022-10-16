package com.wang.window;

import com.wang.pojo.Event;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.TimestampAssigner;
import org.apache.flink.api.common.eventtime.TimestampAssignerSupplier;
import org.apache.flink.api.common.eventtime.Watermark;
import org.apache.flink.api.common.eventtime.WatermarkGenerator;
import org.apache.flink.api.common.eventtime.WatermarkGeneratorSupplier;
import org.apache.flink.api.common.eventtime.WatermarkOutput;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/*
由于分布式系统中网络传输的延迟和时钟漂移，时间语义就会比较重要，flink中主要的时间语义有：
    1. 处理时间（Processing Time）：就是指执行Flink算子操作的机器的系统时间
    2. 事件时间：是指每个事件在对应的设备上发生的时间，也就是数据生成的时间。
        数据一旦产生，这个时间自然就确定了，所以它可以作为一个属性嵌入到数据中。这其实就是这条数据记录的“时间戳”（Timestamp）。
    3. 摄入时间：它是指数据进入Flink数据流的时间，也就是Source算子读入数据的时间。

所以我们应该把时钟也以数据的形式传递出去，告诉下游任务当前时间的进展；而且这个时钟的传递不会因为窗口聚合之类的运算而停滞。
一种简单的想法是，在数据流中加入一个时钟标记，记录当前的事件时间；这个标记可以直接广播到下游，
当下游任务收到这个标记，就可以更新自己的时钟了。由于类似于水流中用来做标志的记号，在 Flink 中，
这种用来衡量事件时间（Event Time）进展的标记，就被称作“水位线”（Watermark）。

具体实现上，水位线可以看作一条特殊的数据记录，它是插入到数据流中的一个标记点，主要内容就是一个时间戳，
用来指示当前的事件时间。而它插入流中的位置，就应该是在某个数据到来之后；这样就可以从这个数据中提取时间戳，作为当前水位线的时间戳了。

我们可以总结一下水位线的特性：
    1. 水位线是插入到数据流中的一个标记，可以认为是一个特殊的数据
    2. 水位线主要的内容是一个时间戳，用来表示当前事件时间的进展
    3. 水位线是基于数据的时间戳生成的
    4. 水位线的时间戳必须单调递增，以确保任务的事件时间时钟一直向前推进
    5. 水位线可以通过设置延迟，来保证正确处理乱序数据
    6. 一个水位线 Watermark(t)，表示在当前流中事件时间已经达到了时间戳 t,
        这代表 t 之前的所有数据都到齐了，之后流中不会出现时间戳 t’ ≤ t 的数据

水位线其实是流处理中对低延迟和结果正确性的一个权衡机制，而且把控制的权力交给了程序员，我们可以在代码中定义水位线的生成策略。
    如果我们希望计算结果能更加准确，那可以将水位线的延迟设置得更高一些，等待的时间越长，自然也就越不容易漏掉数据。
        不过这样做的代价是处理的实时性降低了，我们可能为极少数的迟到数据增加了很多不必要的延迟。
    如果我们希望处理得更快、实时性更强，那么可以将水位线延迟设得低一些。这种情况下，可能很多迟到数据会在水位线之后才到达，
        就会导致窗口遗漏数据，计算结果不准确。对于这些 “漏网之鱼”，Flink另外提供了窗口处理迟到数据的方法，我们会在后面介绍。
        当然，如果我们对准确性完全不考虑、一味地追求处理速度，可以直接使用处理时间语义，这在理论上可以得到最低的延迟。

水位线的窗口处理延迟时间有两种方式：
    1. 将水位线时间延迟，比如：事件时间是5s，延迟水位线3s，那么发送的水位线为5-3=2s的时间，也就是系统认为此时才是第2s
    2. 延迟关闭窗口，比如：事件事件是5s，发送的水位线也是5s，系统已经到达了5s，但是不关闭窗口，等3s后，即第8s的时候再关闭5s结束的窗口

一个任务有可能会收到来自不同分区上游子任务的数据。而不同分区的子任务时钟并不同步，
所以同一时刻发给下游任务的水位线可能并不相同。这时下游任务又该听谁的呢？
    如果以最小的水位线5秒作为当前时钟就不会有某个分区事件没有处理完的问题，因为确实所有上游分区都已经处理完，不会再发5秒前的数据了。
    这让我们想到“木桶原理”：所有的上游并行任务就像围成木桶的一块块木板，它们中最短的那一块，决定了我们桶中的水位。

水位线在上下游任务之间的传递，非常巧妙地避免了分布式系统中没有统一时钟的问题，
每个任务都以“处理完之前所有数据”为标准来确定自己的时钟，就可以保证窗口处理的结果总是正确的。

如果在map等算子中编写了非常耗时间的代码，将会阻塞水位线的向下传播，因为水位线也是数据流中的一个事件，
位于水位线前面的数据如果没有处理完毕，那么水位线不可能弯道超车绕过前面的数据向下游传播，也就是说会被前面的数据阻塞。
这样就会影响到下游算子的聚合计算，因为下游算子中无论由窗口聚合还是定时器的操作，都需要水位线才能触发执行。

在数据流开始之前，Flink 会插入一个大小是负无穷大（在 Java 中是-Long.MAX_VALUE）的水位线，
而在数据流结束时，Flink 会插入一个正无穷大(Long.MAX_VALUE)的水位线，保证所有的窗口闭合以及所有的定时器都被触发。

Flink 对于离线数据集，只会插入两次水位线，也就是在最开始处插入负无穷大的水位线，
在结束位置插入一个正无穷大的水位线。因为只需要插入两次水位线，就可以保证计算的正确，无需在数据流的中间插入水位线了。

 */

/**
 * 时间语义和窗口
 */
public class WatermarkSample {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // 设置生成水位线的周期时间，单位毫秒
        env.getConfig().setAutoWatermarkInterval(200);

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

        // 设置水位线，越靠近越越好，最好是在源中设置水位线，见com.wang.source.function.CustomParallelSource
        // 水位线的策略包括两部分：水位线生成 和 指定水位线的时间字段

        // flink自带的有序流的水位线策略，实际数据一般都是乱序的，一般不使用这个
        //SingleOutputStreamOperator<Event> singleOutputStreamOperator =
        //        // 定义水位线的生成策略
        //        stream.assignTimestampsAndWatermarks(WatermarkStrategy.<Event>forMonotonousTimestamps()
        //        // 指定水位线的时间字段
        //        .withTimestampAssigner(new SerializableTimestampAssigner<Event>() {
        //            @Override
        //            public long extractTimestamp(Event event, long recordTimestamp) {
        //                return event.getTimestamp();
        //            }
        //        }));

        // flink自带的乱序流的水位线策略
        //SingleOutputStreamOperator<Event> singleOutputStreamOperator =
        //        // 针对乱序流插入水位线，延迟时间设置为5s，也就是将水位线延迟5，可以看底层的实现，和自定义水位线类似
        //        // 上面的WatermarkStrategy.forMonotonousTimestamps()方法，
        //        // 底层的也是通过继承这个类来实现的，只是延迟为0：Duration.ofSeconds(0)
        //        stream.assignTimestampsAndWatermarks(WatermarkStrategy.<Event>forBoundedOutOfOrderness(Duration.ofSeconds(5))
        //        .withTimestampAssigner(new SerializableTimestampAssigner<Event>() {
        //            @Override
        //            public long extractTimestamp(Event event, long recordTimestamp) {
        //                return event.getTimestamp();
        //            }
        //        }));

        /*
        事实上，有序流的水位线生成器本质上和乱序流是一样的，相当于延迟设为0的乱序流水位线生成器，两者完全等同：
            WatermarkStrategy.forMonotonousTimestamps()
            WatermarkStrategy.forBoundedOutOfOrderness(Duration.ofSeconds(0))
         */


        // 自定义的水位线策略
        SingleOutputStreamOperator<Event> singleOutputStreamOperator =
                stream.assignTimestampsAndWatermarks(new CustomWatermarkStrategy());

        singleOutputStreamOperator.filter(event -> event.getUser().equals("Alice")).print();

        env.execute();
    }


    /*
     水位线的策略包括两部分：水位线生成 和 指定水位线的时间字段
     */
    /**
     * 自定义水位线策略
     */
    private static class CustomWatermarkStrategy implements WatermarkStrategy<Event> {

        // 指定水位线的时间字段
        @Override
        public TimestampAssigner<Event> createTimestampAssigner(TimestampAssignerSupplier.Context context) {
            System.out.println("createTimestampAssigner");
            return new SerializableTimestampAssigner<Event>() {
                @Override
                public long extractTimestamp(Event event, long recordTimestamp) {
                    return event.getTimestamp();
                }
            };
        }

        // 水位线生成方式
        @Override
        public WatermarkGenerator<Event> createWatermarkGenerator(WatermarkGeneratorSupplier.Context context) {
            System.out.println("createWatermarkGenerator");
            return new MyPeriodWatermarkGenerator();
        }
    }


    /*
    onEvent()和 onPeriodicEmit()，前者是在每个事件到来时调用，而后者由框架周期性调用。
    周期性调用的方法中发出水位线，自然就是周期性生成水位线；而在事件触发的方法中发出水位线，自然就是断点式生成了。
    两种方式的不同就集中体现在这两个方法的实现上。
     */
    /**
     * 周期性水位线生成器（Periodic Generator）
     */
    private static class MyPeriodWatermarkGenerator implements WatermarkGenerator<Event> {
        // 延迟时间
        private Long delayTime = 5000L;
        // 观察到的最大时间戳
        private Long maxTs = Long.MIN_VALUE + delayTime + 1L;

        // 每来一个事件，都会调用这个方法
        @Override
        public void onEvent(Event event, long eventTimestamp, WatermarkOutput output) {
            maxTs = Math.max(event.getTimestamp(), maxTs); // 更新最大时间戳
        }

        /*
        这里需要注意的是，乱序流中生成的水位线真正的时间戳，其实是当前最大时间戳 – 延迟时间 – 1，
        这里的单位是毫秒。为什么要减 1 毫秒呢？我们可以回想一下水位线的特点：时间戳为t的水位线，
        表示时间戳≤t的数据全部到齐，不会再来了。如果考虑有序流，也就是延迟时间为 0 的情况，
        那么时间戳为7秒的数据到来时，之后其实是还有可能继续来7秒的数据的；
        所以生成的水位线不是7秒，而是6秒999毫秒，7秒的数据还可以继续来。
        这一点可以在BoundedOutOfOrdernessWatermarks的源码中明显地看到：
            public void onPeriodicEmit(WatermarkOutput output) {
                output.emitWatermark(new Watermark(maxTimestamp - outOfOrdernessMillis - 1));
            }
         */

        // 周期调用这个接口，周期时间在env中设置，默认200ms调用一次
        @Override
        public void onPeriodicEmit(WatermarkOutput output) {
            output.emitWatermark(new Watermark(maxTs - delayTime - 1L));
        }
    }

    /**
     * 断点式水位线生成器（Periodic Generator）
     */
    private static class MyPunctuatedWatermarkGenerator implements WatermarkGenerator<Event> {
        @Override
        public void onEvent(Event event, long eventTimestamp, WatermarkOutput output) {
            // 只有在遇到特定的事件时，才发出水位线
            if (event.getUser().equals("Mary")) {
                output.emitWatermark(new Watermark(event.getTimestamp() - 1));
            }
        }
        @Override
        public void onPeriodicEmit(WatermarkOutput output) {
            // 不需要做任何事情，因为我们在 onEvent 方法中发射了水位线
        }
    }
}
