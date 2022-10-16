package com.wang.sample;


import org.apache.flink.api.common.RuntimeExecutionMode;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

import java.util.ArrayList;
import java.util.List;


/**
 * 有节流来实现批处理，推荐使用，通过这个来实现流批统一
 */
public class BoundedStreamWordCount {

    /*
    从 1.12.0 版本起，Flink 实现了 API 上的流批统一。DataStream API 新增了一个重要特性：可以支持不同的“执行模式”（execution mode），
    通过简单的设置就可以让一段 Flink 程序在流处理和批处理之间切换。这样一来，DataSet API也就没有存在的必要了。
        流执行模式（STREAMING）
            这是 DataStream API 最经典的模式，一般用于需要持续实时处理的无界数据流。默认情况下，程序使用的就是 STREAMING 执行模式。
        批执行模式（BATCH）
            专门用于批处理的执行模式, 这种模式下，Flink 处理作业的方式类似于 MapReduce 框架。
        自动模式（AUTOMATIC）
            在这种模式下，将由程序根据输入数据源是否有界，来自动选择执行模式。

    通过命令行配置: bin/flink run -Dexecution.runtime-mode=BATCH
    通过代码配置: env.setRuntimeMode(RuntimeExecutionMode.BATCH);

    建议: 不要在代码中配置，而是使用命令行。这同设置并行度是类似的：在提交作业时指定参数可以更加灵活，
    同一段应用程序写好之后，既可以用于批处理也可以用于流处理。而在代码中硬编码（hard code）的方式可扩展性比较差，一般都不推荐。

     */

    public static void main(String[] args) throws Exception {
        // 1. 创建流式执行环境，默认并行度为cpu的核数
        StreamExecutionEnvironment streamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();
        streamExecutionEnvironment.setRuntimeMode(RuntimeExecutionMode.BATCH);

        // 2. 读取文件
        //DataStreamSource<String> lineDataStreamSource = streamExecutionEnvironment.readTextFile("file:///D:\\idea_project\\study_skill\\file\\flink\\wc.txt");
        List<String> lines = new ArrayList<>();
        lines.add("hello java");
        lines.add("hello flink");
        DataStreamSource<String> lineDataStreamSource = streamExecutionEnvironment.fromCollection(lines);

        // 3. 转换数据格式
        SingleOutputStreamOperator<Tuple2<String, Long>> singleOutputStreamOperator = lineDataStreamSource.flatMap((String line, Collector<Tuple2<String, Long>> collector) -> {
            String[] words = line.toLowerCase().split(" ");
            for (String word : words) {
                collector.collect(Tuple2.of(word, 1L));
            }
        })
        // 当Lambda表达式使用 Java 泛型的时候, 由于泛型擦除的存在, 需要显示的声明类型信息
        .returns(Types.TUPLE(Types.STRING, Types.LONG));;

        // 4. 分组
        KeyedStream<Tuple2<String, Long>, String> keyedStream = singleOutputStreamOperator.keyBy(t -> t.f0);

        // 5. 求和
        SingleOutputStreamOperator<Tuple2<String, Long>> result = keyedStream.sum(1);

        // 6. 打印
        result.print();

        // 7. 执行
        streamExecutionEnvironment.execute();
    }
}
