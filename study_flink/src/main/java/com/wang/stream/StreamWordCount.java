package com.wang.stream;

import org.apache.flink.api.common.functions.Partitioner;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.core.fs.FileSystem;
import org.apache.flink.runtime.state.StateBackend;
import org.apache.flink.runtime.state.filesystem.FsStateBackend;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

import java.util.Arrays;
import java.util.Random;


public class StreamWordCount {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment executionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();

        // 开启checkpoint
        executionEnvironment.enableCheckpointing(10);
        // 设置时间类型
        executionEnvironment.setStreamTimeCharacteristic(TimeCharacteristic.ProcessingTime);
        // 设置状态管理存储方式
        executionEnvironment.setStateBackend((StateBackend)new FsStateBackend("file:///D:\\idea_project\\study_skill\\file\\checkpoints"));

        DataStreamSource<String> fileDataStreamSource = executionEnvironment.readTextFile("file:///D:\\idea_project\\study_skill\\file\\wc.text");
        DataStreamSource<String> collectionDataStreamSource = executionEnvironment.fromCollection(Arrays.asList("java java", "java flink"));

        Random random = new Random();
        SingleOutputStreamOperator<Tuple2<String, Long>> counts =
                fileDataStreamSource.union(collectionDataStreamSource).shuffle().partitionCustom(new Partitioner<String>() {
                            @Override
                            public int partition(String s, int numPartitions) {
                                return random.nextInt(numPartitions);
                            }
                        }, new KeySelector<String, String>() {
                            @Override
                            public String getKey(String s) throws Exception {
                                return s;
                            }
                        })
                        .flatMap((String x, Collector<Tuple2<String, Long>> out) -> {
                            String[] words = x.toLowerCase().split(" ");
                            for (String word : words) {
                                out.collect(Tuple2.of(word, 1L));
                            }
                        })
                        .returns(Types.TUPLE(Types.STRING, Types.LONG))
                        .keyBy(0)
                        .sum(1);

        counts.writeAsText("file:///D:\\idea_project\\study_skill\\file\\wc_result.text", FileSystem.WriteMode.OVERWRITE).setParallelism(1);
        System.out.println("print result to console...");
        counts.print();

        executionEnvironment.execute("streaming word count");
    }
}
