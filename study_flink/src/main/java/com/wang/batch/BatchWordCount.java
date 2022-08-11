package com.wang.batch;

import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.operators.AggregateOperator;
import org.apache.flink.api.java.operators.DataSource;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.util.Collector;

public class BatchWordCount {

    public static void main(String[] args) throws Exception {
        ExecutionEnvironment executionEnvironment = ExecutionEnvironment.getExecutionEnvironment();

        DataSource<String> stringDataSource = executionEnvironment.readTextFile("file:///D:\\idea_project\\study_skill\\file\\wc.text");

        AggregateOperator<Tuple2<String, Long>> result = stringDataSource.flatMap((String x, Collector<Tuple2<String, Long>> out) -> {
                    String[] words = x.toLowerCase().split(" ");
                    for (String word : words) {
                        out.collect(Tuple2.of(word, 1L));
                    }
                })
                .returns(Types.TUPLE(Types.STRING, Types.LONG))
                .groupBy(0)
                .sum(1);

        result.print();
    }
}
