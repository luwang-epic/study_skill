package com.wang.window;

import com.wang.pojo.Event;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.datastream.WindowedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

/*
窗口可以把流切割成有限大小的多个“存储桶”（bucket)；每个数据都会分发到对应的桶中，
当到达窗口结束时间时，就对每个桶中收集的数据进行计算处理。

我们最容易想到的就是按照时间段去截取数据，这种窗口就叫作“时间窗口”（TimeWindow）。这在实际应用中最常见.
除了由时间驱动之外，窗口其实也可以由数据驱动，也就是说按照固定的个数，来截取一段数据集，这种窗口叫作“计数窗口”（Count Window）
    时间窗口以时间点来定义窗口的开始（start）和结束（end），所以截取出的就是某一时间段的数据。
        到达结束时间时，窗口不再收集数据，触发计算输出结果，并将窗口关闭销毁。
    计数窗口基于元素的个数来截取数据，到达固定的个数时就触发计算并关闭窗口。

根据分配数据的规则，窗口的具体实现可以分为 4 类：
    滚动窗口（Tumbling Window）:
        滚动窗口有固定的大小，是一种对数据进行“均匀切片”的划分方式。
        窗口之间没有重叠，也不会有间隔，是“首尾相接”的状态。
    滑动窗口（Sliding Window）:
        与滚动窗口类似，滑动窗口的大小也是固定的。区别在于，窗口之间并不是首尾相接的，
        而是可以“错开”一定的位置。如果看作一个窗口的运动，那么就像是向前小步“滑动”一样。
    会话窗口（Session Window）:
        简单来说，就是数据来了之后就开启一个会话窗口，如果接下来还有数据陆续到来，那么就一直保持会话；
        如果一段时间一直没收到数据，那就认为会话超时失效，窗口自动关闭。
    全局窗口（Global Window）:
        这种窗口全局有效，会把相同 key 的所有数据都分配到同一个窗口中；说直白一点，就跟没分窗口一样。
        无界流的数据永无止尽，所以这种窗口也没有结束的时候，默认是不会做触发计算的。
        如果希望它能对数据进行计算处理，还需要自定义“触发器”（Trigger）。
        Flink中的计数窗口（Count Window），底层就是用全局窗口实现的。

在定义窗口操作之前，首先需要确定，到底是基于按键分区（Keyed）的数据流 KeyedStream来开窗，
还是直接在没有按键分区的 DataStream 上开窗。也就是说，在调用窗口算子之前，是否有 keyBy 操作。
    按键分区窗口（Keyed Windows）
        经过按键分区 keyBy 操作后，数据流会按照 key 被分为多条逻辑流（logical streams），这就是 KeyedStream。
        基于 KeyedStream 进行窗口操作时, 窗口计算会在多个并行子任务上同时执行。相同 key 的数据会被发送到同一个并行子任务，
        而窗口操作会基于每个 key 进行单独的处理。所以可以认为，每个 key 上都定义了一组窗口，各自独立地进行统计计算。

        使用方式：stream.keyBy(...).window(...)
    非按键分区（Non-Keyed Windows）
        如果没有进行 keyBy，那么原始的 DataStream 就不会分成多条逻辑流。这时窗口逻辑只能在一个任务（task）上执行，
        就相当于并行度变成了 1。所以在实际应用中一般不推荐使用这种方式。
        使用方式：stream.windowAll(...)

定义窗口分配器（Window Assigners）是构建窗口算子的第一步，它的作用就是定义数据应该被“分配”到哪个窗口。
定义了窗口分配器，我们只是知道了数据属于哪个窗口，可以将数据收集起来了；至于收集起来到底要做什么，其实还完全没有头绪。
所以在窗口分配器之后，必须再接上一个定义窗口如何进行计算的操作，这就是所谓的“窗口函数”（window functions）。

窗口的声明周期：
    1. 窗口的创建
        窗口的类型和基本信息由窗口分配器（window assigners）指定，但窗口不会预先创建好，
        而是由数据驱动创建。当第一个应该属于这个窗口的数据元素到达时，就会创建对应的窗口。
    2. 窗口计算的触发
        除了窗口分配器，每个窗口还会有自己的窗口函数（window functions）和触发器（trigger）。
        窗口函数可以分为增量聚合函数和全窗口函数，主要定义了窗口中计算的逻辑；而触发器则是指定调用窗口函数的条件。

        对于不同的窗口类型，触发计算的条件也会不同。例如，一个滚动事件时间窗口，应该在水位线到达窗口结束时间的时候触发计算，
        属于“定点发车”；而一个计数窗口，会在窗口中元素数量达到定义大小时触发计算，属于“人满就发车”。
        所以 Flink 预定义的窗口类型都有对应内置的触发器。对于事件时间窗口而言，除去到达结束时间的“定点发车”，
        还有另一种情形。当我们设置了允许延迟，那么如果水位线超过了窗口结束时间、但还没有到达设定的最大延迟时间，
        这期间内到达的迟到数据也会触发窗口计算。这类似于没有准时赶上班车的人又追上了车，这时车要再次停靠、开门，将新的数据整合统计进来。
    3. 窗口的销毁
        一般情况下，当时间达到了结束点，就会直接触发计算输出结果、进而清除状态销毁窗口。
        这时窗口的销毁可以认为和触发计算是同一时刻。这里需要注意，Flink 中只对时间窗口（TimeWindow）有销毁机制；
        由于计数窗口（CountWindow）是基于全局窗口（GlobalWindw）实现的，而全局窗口不会清除状态，所以就不会被销毁。

        在特殊的场景下，窗口的销毁和触发计算会有所不同。事件时间语义下，如果设置了允许延迟，
        那么在水位线到达窗口结束时间时，仍然不会销毁窗口；窗口真正被完全删除的时间点，是窗口的结束时间加上用户指定的允许延迟时间。

Flink对迟到数据的处理：
    1. 设置水位线延迟时间
    2. 允许窗口处理迟到数据
    3. 将迟到数据放入窗口侧输出流

 */

/**
 * 窗口演示
 */
public class WindowSample {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        DataStreamSource<Event> dataStreamSource = env.fromElements(
                new Event("Mary","./home", 1000L),
                new Event("Bob", "./cart", 2000L),
                new Event("Alice", "./prod?id=100", 3000L),
                new Event("Alice", "./prod?id=200", 3500L),
                new Event("Bob", "./prod?id=2", 2500L),
                new Event("Alice", "./prod?id=300", 3600L),
                new Event("Bob", "./home", 3000L),
                new Event("Bob", "./prod?id=1", 2300L),
                new Event("Bob", "./prod?id=3", 3300L));

        // 定义事件时间
        SingleOutputStreamOperator<Event> singleOutputStreamOperator = dataStreamSource.assignTimestampsAndWatermarks(
                WatermarkStrategy.<Event>forMonotonousTimestamps()
                .withTimestampAssigner(new SerializableTimestampAssigner<Event>() {
                    @Override
                    public long extractTimestamp(Event event, long recordTimestamp) {
                        return event.getTimestamp();
                    }
                }));


        // 定义窗口，需要配置事件时间使用
        WindowedStream<Event, String, TimeWindow> windowedStream = singleOutputStreamOperator.keyBy(event -> event.getUser())
                // 滚动事件事件窗口
                .window(TumblingEventTimeWindows.of(Time.seconds(10)))
                // 滚动处理时间窗口
                //.window(TumblingProcessingTimeWindows.of(Time.hours(1)))
                // 滑动事件时间窗口，窗口大小为1h，滑动步长为5min
                //.window(SlidingEventTimeWindows.of(Time.hours(1), Time.minutes(5)))
                // 事件时间会话窗口
                //.window(EventTimeSessionWindows.withGap(Time.seconds(2)))

                // 其他的窗口函数

                // 还可以自定义触发器，触发器主要是用来控制窗口什么时候触发计算。所谓的“触发计算”，
                // 本质上就是执行窗口函数，所以可以认为是计算得到结果并输出的过程。
                //.trigger()
                // 移除器主要用来定义移除某些数据的逻辑。可以定义触发计算之前和之后做一些操作
                //.evictor()
                /*
                在事件时间语义下，窗口中可能会出现数据迟到的情况。这是因为在乱序流中，
                水位线（watermark）并不一定能保证时间戳更早的所有数据不会再来。当水位线已经到达窗口结束时间时，
                窗口会触发计算并输出结果，这时一般也就要销毁窗口了；如果窗口关闭之后，又有本属于窗口内的数据姗姗来迟，
                默认情况下就会被丢弃。这也很好理解：窗口触发计算就像发车，如果要赶的车已经开走了，
                又不能坐其他的车（保证分配窗口的正确性），那就只好放弃坐班车了。不过在多数情况下，
                直接丢弃数据也会导致统计结果不准确，我们还是希望该上车的人都能上来。为了解决迟到数据的问题，
                Flink 提供了一个特殊的接口，可以为窗口算子设置一个“允许的最大延迟”（Allowed Lateness）。
                也就是说，我们可以设定允许延迟一段时间，在这段时间内，窗口不会销毁，继续到来的数据依然可以进入窗口中并触发计算。
                直到水位线推进到了 窗口结束时间 + 延迟时间，才真正将窗口的内容清空，正式关闭窗口。
                 */
                .allowedLateness(Time.seconds(10));
                /*
                即使可以设置窗口的延迟时间，终归还是有限的，后续的数据还是会被丢弃。
                如果不想丢弃任何一个数据，我们可以将未收入窗口的迟到数据，放入“侧输出流”（side output）进行另外的处理。
                所谓的侧输出流，相当于是数据流的一个“分支”，这个流中单独放置那些错过了该上的车、本该被丢弃的数据。
                 */
                //.sideOutputLateData();


        //WindowedStream<Event, String, GlobalWindow> windowedStream = stream.keyBy(event -> event.getUser())
        //        // 滑动计数窗口，本身底层是基于全局窗口（Global Window）实现的
        //        //.countWindow(2);
        //        // 滚动计数窗口
        //        .countWindow(4, 2);
        //        // 全局窗口
        //        .window(GlobalWindows.create());



        // 定义窗口处理函数
        //windowedStream.sum("timestamp").print();

        /*
        窗口函数定义了要对窗口中收集的数据做的计算操作，根据处理的方式可以分为两类：增量聚合函数和全窗口函数

        增量聚合函数（incremental aggregation functions）
            窗口将数据收集起来，最基本的处理操作当然就是进行聚合。窗口对无限流的切分，可以看作得到了一个有界数据集。
            如果我们等到所有数据都收集齐，在窗口到了结束时间要输出结果的一瞬间再去进行聚合，
            显然就不够高效了——这相当于真的在用批处理的思路来做实时流处理。为了提高实时性，我们可以再次将流处理的思路发扬光大：
            就像DataStream的简单聚合一样，每来一条数据就立即进行计算，中间只要保持一个简单的聚合状态就可以了；
            区别只是在于不立即输出结果，而是要等到窗口结束时间。等到窗口到了结束时间需要输出计算结果的时候，
            我们只需要拿出之前聚合的状态直接输出，这无疑就大大提高了程序运行的效率和实时性。
            典型的增量聚合函数有两个：ReduceFunction 和 AggregateFunction。
        全窗口函数（full window functions）
            窗口操作中的另一大类就是全窗口函数。与增量聚合函数不同，全窗口函数需要先收集窗口中的数据，
            并在内部缓存起来，等到窗口要输出结果的时候再取出数据进行计算。
            在Flink中，全窗口函数也有两种：WindowFunction 和 ProcessWindowFunction。
                WindowFunction的作用可以被ProcessWindowFunction全覆盖，所以之后可能会逐渐弃用。
                一般在实际应用，直接使用ProcessWindowFunction就可以了
         */
        // 增量聚合函数 reduce
        windowedStream.reduce(new ReduceFunction<Event>() {
            @Override
            public Event reduce(Event event1, Event event2) throws Exception {
                return new Event(event2.getUser(), event2.getUrl(), event1.getTimestamp() + event2.getTimestamp());
            }
        }).print();

        // 增量聚合函数 aggregate
        windowedStream.aggregate(new MyAggregateFunction()).print();

        // 全窗口函数 ProcessWindowFunction
        windowedStream.process(new MyProcessWindowFunction()).print();

        /*
        增量聚合函数处理计算会更高效。举一个最简单的例子，对一组数据求和。大量的数据连续不断到来，全窗口函数只是把它们收集缓存起来，
        并没有处理；到了窗口要关闭、输出结果的时候，再遍历所有数据依次叠加，得到最终结果。而如果我们采用增量聚合的方式，
        那么只需要保存一个当前和的状态，每个数据到来时就会做一次加法，更新状态；到了要输出结果的只要将当前状态直接拿出来就可以了。
        增量聚合相当于把计算量“均摊”到了窗口收集数据的过程中，自然就会比全窗口聚合更加高效、输出更加实时。
        而全窗口函数的优势在于提供了更多的信息，可以认为是更加“通用”的窗口操作。它只负责收集数据、提供上下文相关信息，
        把所有的原材料都准备好，至于拿来做什么我们完全可以任意发挥。这就使得窗口计算更加灵活，功能更加强大。
        所以在实际应用中，我们往往希望兼具这两者的优点，把它们结合在一起使用。Flink 的Window API就给我们实现了这样的用法。
        我们之前在调用 WindowedStream 的.reduce()和.aggregate()方法时，
        只是简单地直接传入了一个 ReduceFunction 或 AggregateFunction 进行增量聚合。
        除此之外，其实还可以传入第二个参数：一个全窗口函数，可以是 WindowFunction 或者 ProcessWindowFunction。
         */
        windowedStream.aggregate(new MyAggregateFunction(), new ProcessWindowFunction<String, String, String, TimeWindow>() {
            @Override
            public void process(String key, ProcessWindowFunction<String, String, String, TimeWindow>.Context context, Iterable<String> elements, Collector<String> out) throws Exception {
                long start = context.window().getStart();
                long end = context.window().getEnd();
                String avg = elements.iterator().next();
                out.collect("窗口 " + start + " ~ " + end + " 平均值为：" + avg);
            }
        }).print();


        env.execute();
    }

    /**
     * 计算访问时间的平均值
     * Event: 输入事件类型
     * String: 输出类型
     * String: 分区key的类型
     * TimeWindow: 窗口类型
     */
    public static class MyProcessWindowFunction extends ProcessWindowFunction<Event, String, String, TimeWindow> {
        // 等数据都聚集了才会触发
        @Override
        public void process(String key, ProcessWindowFunction<Event, String, String, TimeWindow>.Context context, Iterable<Event> elements, Collector<String> out) throws Exception {
            int count = 0;
            long sum = 0;
            for (Event event : elements) {
                count++;
                sum += event.getTimestamp();
            }

            out.collect("MyProcessWindowFunction ---> " + sum / count);
        }
    }

    /**
     * 计算访问时间的平均值AggregateFunction
     * Event: 输入
     * Tuple2<Long, Integer>: 保存中间值的累加器类型，记录（时间总和，个数）的二元组
     * String: 输出类型，输出平均值
     */
    private static class MyAggregateFunction implements AggregateFunction<Event, Tuple2<Long, Integer>, String> {
        // 累加器的初始值
        @Override
        public Tuple2<Long, Integer> createAccumulator() {
            return Tuple2.of(0L, 0);
        }

        // 每来了一个事件，如何累加
        @Override
        public Tuple2<Long, Integer> add(Event event, Tuple2<Long, Integer> accumulator) {
            return Tuple2.of(accumulator.f0 + event.getTimestamp(), accumulator.f1 + 1);
        }

        // 输出结果
        @Override
        public String getResult(Tuple2<Long, Integer> accumulator) {
            return "MyAggregateFunction ---> " + accumulator.f0 / accumulator.f1;
        }

        // 两个窗口合并的时候才会调用，一般在使用会话窗口的时候才会涉及；作用是如何合并累加器
        @Override
        public Tuple2<Long, Integer> merge(Tuple2<Long, Integer> a, Tuple2<Long, Integer> b) {
            return Tuple2.of(a.f0 + b.f0, a.f1 + b.f1);
        }
    }

}
