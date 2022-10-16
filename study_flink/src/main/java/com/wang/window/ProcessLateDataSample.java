package com.wang.window;

import com.wang.pojo.Event;
import com.wang.pojo.UrlViewCount;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

/**
 * 处理延迟的数据
 */
public class ProcessLateDataSample {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        DataStreamSource<Event> dataStreamSource = env.addSource(new ClickSource());

        // 方式一：设置 watermark 延迟时间，2 秒钟
        SingleOutputStreamOperator<Event> singleOutputStreamOperator = dataStreamSource.assignTimestampsAndWatermarks(
                WatermarkStrategy.<Event>forBoundedOutOfOrderness(Duration.ofSeconds(2))
                        .withTimestampAssigner(new SerializableTimestampAssigner<Event>() {
                            @Override
                            public long extractTimestamp(Event element, long
                                    recordTimestamp) {
                                return element.getTimestamp();
                            }
                        }));

        // 定义侧输出流标签
        OutputTag<Event> outputTag = new OutputTag<Event>("late") {};
        SingleOutputStreamOperator<UrlViewCount> result = singleOutputStreamOperator.keyBy(data -> data.getUrl())
                .window(TumblingEventTimeWindows.of(Time.seconds(10)))
                // 方式二：允许窗口处理迟到数据，设置 1 分钟的等待时间
                .allowedLateness(Time.minutes(1))
                // 方式三：将最后的迟到数据输出到侧输出流
                .sideOutputLateData(outputTag)
                .aggregate(new UrlViewCountAgg(), new UrlViewCountResult());

        result.print("result");
        result.getSideOutput(outputTag).print("late");
        // 为方便观察，可以将原始数据也输出
        dataStreamSource.print("input");
        env.execute();
    }


    private static class UrlViewCountAgg implements AggregateFunction<Event, Long, Long> {
        @Override
        public Long createAccumulator() {
            return 0L;
        }

        @Override
        public Long add(Event value, Long accumulator) {
            return accumulator + 1;
        }

        @Override
        public Long getResult(Long accumulator) {
            return accumulator;
        }

        @Override
        public Long merge(Long a, Long b) {
            return null;
        }
    }

    private static class UrlViewCountResult extends ProcessWindowFunction<Long, UrlViewCount, String, TimeWindow> {
        @Override
        public void process(String url, Context context, Iterable<Long> elements, Collector<UrlViewCount> out) throws Exception {
            // 结合窗口信息，包装输出内容
            Long start = context.window().getStart();
            Long end = context.window().getEnd();
            out.collect(new UrlViewCount(url, elements.iterator().next(), start, end));
        }
    }

    private static class ClickSource implements SourceFunction<Event> {
        // 声明一个布尔变量，作为控制数据生成的标识位
        private Boolean running = true;

        private List<Event> events = Arrays.asList(
                new Event("Alice", "./home", 1000L),
                new Event("Alice", "./home", 2000L),
                new Event("Alice", "./home", 10000L),
                new Event("Alice", "./home", 9000L),
                new Event("Alice", "./home", 12000L),
                new Event("Alice", "./prod?id=100", 15000L),
                new Event("Alice", "./home", 9000L),
                new Event("Alice", "./home", 8000L),
                new Event("Alice", "./prod?id=200", 70000L),
                new Event("Alice", "./home", 8000L),
                new Event("Alice", "./prod?id=300", 72000L),
                new Event("Alice", "./home", 8000L)
        );

        /**
         * 循环读取数据逻辑
         */
        @Override
        public void run(SourceContext<Event> ctx) throws Exception {
            int index = 0;
            while (running) {
                if (index >= events.size()) {
                    index = 0;
                }
                ctx.collect(events.get(index++));

                // 隔 1 秒生成一个点击事件，方便观测
                Thread.sleep(1000);
            }
        }

        /**
         * 什么时候停止作业，页面cancel会调用这个接口取消作业
         */
        @Override
        public void cancel() {
            running = false;
        }

    }
}
