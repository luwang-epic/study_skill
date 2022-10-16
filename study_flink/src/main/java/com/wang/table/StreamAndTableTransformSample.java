package com.wang.table;

import com.wang.pojo.Event;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.Expressions;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

/**
 * 表和流的转换
 */
public class StreamAndTableTransformSample {

    public static void main(String[] args) throws Exception {
        // 获取流执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        // 1. 读取数据源
        SingleOutputStreamOperator<Event> eventStream = env
                .fromElements(
                        new Event("Alice", "./home", 1000L),
                        new Event("Bob", "./cart", 1000L),
                        new Event("Alice", "./prod?id=1", 5 * 1000L),
                        new Event("Cary", "./home", 60 * 1000L),
                        new Event("Bob", "./prod?id=3", 90 * 1000L),
                        new Event("Alice", "./prod?id=7", 105 * 1000L)
                );

        // 2. 获取表环境
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);

        // 3. 将数据流转换成表
        Table eventTable = tableEnv.fromDataStream(eventStream);
        // 可以指定只选取某些字段，以及对字段进行重命名
        //Table eventTable = tableEnv.fromDataStream(eventStream, Expressions.$("user").as("u"), Expressions.$("url"));

        // 这种方式也可以将数据流转换成表，这样后面就可以直接使用event_table了
        tableEnv.createTemporaryView("event_table", eventStream);
        // 也可以指定只选取某些字段，以及对字段进行重命名
        //tableEnv.createTemporaryView("event_table2", eventStream, Expressions.$("user").as("u"), Expressions.$("url"));

        // 4. 用执行SQL 的方式提取数据
        Table resultTable1 = tableEnv.sqlQuery("select url, user from " + eventTable);

        // 5. 基于Table直接转换，一般不使用这种方式，而是直接使用sql
        Table resultTable2 = eventTable.select(Expressions.$("user"), Expressions.$("url"))
                .where(Expressions.$("user").isEqual("Alice"));

        // 聚合，涉及更新操作的表，如：统计每个用户的点击次数；这里直接使用了上面注册的event_table
        Table urlCountTable = tableEnv.sqlQuery("SELECT user, COUNT(url) FROM event_table GROUP BY user");

        // 6. 将表转换成数据流，打印输出，这种只有
        tableEnv.toDataStream(resultTable1).print("sql result");
        tableEnv.toDataStream(resultTable2).print("table result");

        // 涉及更新操作的，需要使用change log stream，否则报错；
        tableEnv.toChangelogStream(urlCountTable).print("agg result");
        // change log stream流也可以用于没有更新的输出
        tableEnv.toChangelogStream(resultTable2).print("change log result");


        // 执行程序
        env.execute();
    }
}
