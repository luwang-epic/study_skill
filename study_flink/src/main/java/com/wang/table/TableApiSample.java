package com.wang.table;

import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.TableEnvironment;

/**
 * flink的表api
 */
public class TableApiSample {

    public static void main(String[] args) throws Exception{
        // 创建执行环境的两种方式，流方式 & 表方式
        // 1 创建执行环境(流方式创建)
        //        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        //        env.setParallelism(1);
        //        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);

        // 2 创建执行环境(表方式创建) 基于alibaba 的 blink planner实现
        EnvironmentSettings settings = EnvironmentSettings.newInstance()
                // 流模式还是批模式，默认是流
                .inStreamingMode()
                //.inBatchMode()
                // 计划器，默认为BlinkPlanner，还有被废弃的useOldPlanner
                .useBlinkPlanner()
                .build();
        TableEnvironment tableEnv = TableEnvironment.create(settings);

        // 3 创建一张连接器表（输入表）
        String createInDDL = "CREATE TABLE clickTable (" +
                "user_name STRING, " +
                "url STRING, " +
                "ts BIGINT " +
                ") WITH (" +
                " 'connector' = 'filesystem'," +
                " 'path' = 'D:/idea_project/study_skill/file/flink/clicks.txt'," +
                " 'format' = 'csv'" +
                ")";
        tableEnv.executeSql(createInDDL);

        // 执行聚合统计查询转换
        Table eggResult = tableEnv.sqlQuery("select user_name,COUNT(url) as cnt from clickTable group by user_name");

        // 创建一张控制台打印的一张表
        String createPrintOutDDL = "CREATE TABLE printOutTable (" +
                "user_name STRING, " +
                "cnt BIGINT " +
                ") WITH (" +
                " 'connector' = 'print' " +
                ")";
        tableEnv.executeSql(createPrintOutDDL);

        // 输出到控制台 +I表示插入，-U表示更新前的数据，+U表示更新后的数据
        eggResult.executeInsert("printOutTable");
    }
}
