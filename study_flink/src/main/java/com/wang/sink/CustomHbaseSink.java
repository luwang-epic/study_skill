package com.wang.sink;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;

import java.nio.charset.StandardCharsets;

/**
 * 自定义的hbase输出Sink
 *  直接通过 stream.addSink(new CustomHbaseSink())即可使用了
 */
public class CustomHbaseSink extends RichSinkFunction<String> {

    // 管理 Hbase 的配置信息,这里因为 Configuration 的重名问题，将类以完整路径导入
    public org.apache.hadoop.conf.Configuration configuration;
    public Connection connection; // 管理 Hbase 连接

    @Override
    public void open(Configuration parameters) throws
            Exception {
        super.open(parameters);
        configuration = HBaseConfiguration.create();
        configuration.set("hbase.zookeeper.quorum", "localhost:2181");
        connection = ConnectionFactory.createConnection(configuration);
    }

    @Override
    public void invoke(String value, Context context) throws Exception {
        // 表名为 test
        Table table = connection.getTable(TableName.valueOf("test"));
        // 指定 rowkey
        Put put = new Put("rowkey".getBytes(StandardCharsets.UTF_8));
        // 指定列名, 并写入的数据
        put.addColumn("info".getBytes(StandardCharsets.UTF_8), value.getBytes(StandardCharsets.UTF_8), "1".getBytes(StandardCharsets.UTF_8));
        // 执行 put 操作
        table.put(put);
        // 将表关闭
        table.close();
    }


    @Override
    public void close() throws Exception {
        super.close();
        connection.close(); // 关闭连接
    }
}
