package com.wang.table;

import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.TableEnvironment;
import org.apache.flink.table.api.java.StreamTableEnvironment;
import org.apache.flink.table.descriptors.Csv;
import org.apache.flink.table.descriptors.FileSystem;
import org.apache.flink.table.descriptors.Schema;
import org.apache.flink.types.Row;

public class TableApiSample {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        StreamTableEnvironment tEnv = TableEnvironment.getTableEnvironment(env);
        tEnv.connect(
                        new FileSystem()
                                .path("file:///D:\\idea_project\\study_skill\\file\\table.txt")
                ).withFormat(
                        new Csv()
                                .field("id", Types.STRING)
                                .field("name", Types.STRING)
                                .field("idCard", Types.STRING)
                                .field("addressCode", Types.STRING)
                                .field("age", Types.INT)
                                .fieldDelimiter(",")
                                .lineDelimiter("\n")
                                .ignoreParseErrors()
                ).withSchema(
                        new Schema()
                                .field("id", Types.STRING)
                                .field("name", Types.STRING)
                                .field("idCard", Types.STRING)
                                .field("addressCode", Types.STRING)
                                .field("age", Types.INT)
                )
                .inAppendMode()
                .registerTableSource("sensor");
        Table table = tEnv.sqlQuery("select id,name,idCard,addressCode,age from sensor");
        tEnv.toAppendStream(table, Row.class).print();

        tEnv.connect(
                        new FileSystem()
                                .path("file:///D:\\idea_project\\study_skill\\file\\table_result.txt")
                ).withFormat(
                        new Csv()
                                .field("id", Types.STRING)
                                .field("name", Types.STRING)
                                .field("idCard", Types.STRING)
                                .field("addressCode", Types.STRING)
                                .field("age", Types.INT)
                                .fieldDelimiter(",")
                ).withSchema(
                        new Schema()
                                .field("id", Types.STRING)
                                .field("name", Types.STRING)
                                .field("idCard", Types.STRING)
                                .field("addressCode", Types.STRING)
                                .field("age", Types.INT)
                )
                .inAppendMode()
                .registerTableSink("sensorOut");
        env.setParallelism(1);
        table.insertInto("sensorOut");
        env.execute(" test ");
    }
}
