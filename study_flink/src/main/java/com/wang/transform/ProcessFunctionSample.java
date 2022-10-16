package com.wang.transform;

import com.wang.pojo.Event;
import com.wang.source.function.CustomSource;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;

import java.sql.Timestamp;
import java.time.Duration;


/*
在更底层，我们可以不定义任何具体的算子（比如 map，filter，或者 window），
而只是提炼出一个统一的“处理”（process）操作——它是所有转换算子的一个概括性的表达，可以自定义处理逻辑，
所以这一层接口就被叫作“处理函数”（process function）。

于是必须祭出大招——处理函数（ProcessFunction）了。处理函数提供了一个“定时服务”（TimerService），
我们可以通过它访问流中的事件（event）、时间戳（timestamp）、水位线（watermark），甚至可以注册“定时事件”。
而且处理函数继承了 AbstractRichFunction 抽象类，所以拥有富函数类的所有特性，同样可以访问状态（state）和其他运行时信息。
此外，处理函数还可以直接将数据输出到侧输出流（side output）中。所以，处理函数是最为灵活的处理方法，
可以实现各种自定义的业务逻辑；同时也是整个 DataStream API 的底层基础。

Flink 提供了 8 个不同的处理函数：
（1）ProcessFunction
    最基本的处理函数，基于 DataStream 直接调用.process()时作为参数传入。
（2）KeyedProcessFunction
    对流按键分区后的处理函数，基于 KeyedStream 调用.process()时作为参数传入。要想使用定时器，比如基于 KeyedStream。
（3）ProcessWindowFunction
    开窗之后的处理函数，也是全窗口函数的代表。基于 WindowedStream 调用.process()时作为参数传入。
（4）ProcessAllWindowFunction
    同样是开窗之后的处理函数，基于 AllWindowedStream 调用.process()时作为参数传入。
（5）CoProcessFunction
    合并（connect）两条流之后的处理函数，基于 ConnectedStreams 调用.process()时作为参数传入。
（6）ProcessJoinFunction
    间隔连接（interval join）两条流之后的处理函数，基于 IntervalJoined 调用.process()时作为参数传入。
（7）BroadcastProcessFunction
    广播连接流处理函数，基于 BroadcastConnectedStream 调用.process()时作为参数传入。这里的“广播连接流”BroadcastConnectedStream，
    是一个未 keyBy 的普通 DataStream 与一个广播流（BroadcastStream）做连接（conncet）之后的产物。
（8）KeyedBroadcastProcessFunction
    按键分区的广播连接流处理函数，同样是基于 BroadcastConnectedStream 调用.process()时作为参数传入。
    与 BroadcastProcessFunction 不同的是，这时的广播连接流，是一个KeyedStream与广播流（BroadcastStream）做连接之后的产物。

 */

/**
 * 处理函数
 */
public class ProcessFunctionSample {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        SingleOutputStreamOperator<Event> singleOutputStreamOperator = env.addSource(new CustomSource()).assignTimestampsAndWatermarks(
                WatermarkStrategy.<Event>forBoundedOutOfOrderness(Duration.ZERO)
                        .withTimestampAssigner(new SerializableTimestampAssigner<Event>() {
                            @Override
                            public long extractTimestamp(Event element, long recordTimestamp) {
                                return element.getTimestamp();
                            }
                        }));

        // 处理函数ProcessFunction
        SingleOutputStreamOperator<String> processFunctionStream = singleOutputStreamOperator.process(new ProcessFunction<Event, String>() {
            @Override
            public void processElement(Event event, ProcessFunction<Event, String>.Context ctx, Collector<String> out) throws Exception {
                out.collect(event.toString());

                System.out.println("timestamp: " + ctx.timestamp());
                System.out.println("watermark: " + ctx.timerService().currentWatermark());

                System.out.println("index: " + getRuntimeContext().getIndexOfThisSubtask());
                // 状态数据
                //getRuntimeContext().getState()
            }
        });
        processFunctionStream.print();


        // 处理函数KeyedProcessFunction
        SingleOutputStreamOperator<String> keyedProcessFunctionStream = singleOutputStreamOperator.keyBy(event -> event.getUser())
                .process(new KeyedProcessFunction<String, Event, String>() {
                    @Override
                    public void processElement(Event value, KeyedProcessFunction<String, Event, String>.Context ctx, Collector<String> out) throws Exception {
                        // 事件时间
                        //long currentTime = ctx.timestamp();
                        // 处理时间
                        long currentTime = ctx.timerService().currentProcessingTime();
                        out.collect(ctx.getCurrentKey() + "数据到达，到达时间：" + new Timestamp(currentTime));

                        // 要用定时器，必须基于KeyedStream
                        // 注册一个10秒后的定时器
                        ctx.timerService().registerProcessingTimeTimer(currentTime + 10 * 1000L);
                    }

                    @Override
                    public void onTimer(long timestamp, KeyedProcessFunction<String, Event, String>.OnTimerContext ctx, Collector<String> out) throws Exception {
                        out.collect(ctx.getCurrentKey() + "定时器触发，触发时间：" + new Timestamp(timestamp));
                    }
                });
        keyedProcessFunctionStream.print();


        env.execute();
    }

}
