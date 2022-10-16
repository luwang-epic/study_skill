package com.wang.source;

import com.wang.pojo.Event;
import com.wang.source.function.CustomSource;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * 自定义数据源
 */
public class CustomSourceStreamSample {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        // 有了自定义的 source function，调用 addSource 方法
        DataStreamSource<Event> stream = env.addSource(new CustomSource()); // 如果设置并行度大于2会报错
        //DataStreamSource<Event> stream = env.addSource(new CustomParallelSource()).setParallelism(2);

        // 流的名称，可以在输出的前面看到这个名称
        stream.print("custom source");
        env.execute();
    }

}
