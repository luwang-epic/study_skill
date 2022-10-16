package com.wang.transform;

import com.wang.pojo.Event;
import com.wang.source.function.CustomSource;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;


/**
 * 分流和合流
 */
public class SplitAndUnionStreamSample {


    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        SingleOutputStreamOperator<Event> stream = env.addSource(new CustomSource());

        // 定义输出标签，侧输出流的数据类型为三元组(user, url, timestamp)
        OutputTag<Tuple3<String, String, Long>> maryTag = new OutputTag<Tuple3<String, String, Long>>("Mary"){};
        OutputTag<Tuple3<String, String, Long>> bobTag = new OutputTag<Tuple3<String, String, Long>>("Bob"){};
        SingleOutputStreamOperator<Event> processedStream = stream.process(new ProcessFunction<Event, Event>() {
            @Override
            public void processElement(Event value, Context ctx, Collector<Event> out) throws Exception {
                if (value.getUser().equals("Mary")) {
                    // 使用测数据流进行分流
                    ctx.output(maryTag, new Tuple3<>(value.getUser(), value.getUrl(), value.getTimestamp()));
                } else if (value.getUser().equals("Bob")) {
                    ctx.output(bobTag, new Tuple3<>(value.getUser(), value.getUrl(), value.getTimestamp()));
                } else {
                    out.collect(value);
                }
            }
        });

        processedStream.getSideOutput(maryTag).print("Mary");
        processedStream.getSideOutput(bobTag).print("Bob");
        processedStream.print("main");

        // union合流
        processedStream.getSideOutput(maryTag).union(processedStream.getSideOutput(bobTag)).print("merge");

        env.execute();
    }
}

