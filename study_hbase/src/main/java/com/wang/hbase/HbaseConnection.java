package com.wang.hbase;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;

/*

1）Name Space

命名空间，类似于关系型数据库的 DatabBase 概念，每个命名空间下有多个表。HBase有两个自带的命名空间，分别是 hbase 和 default，hbase 中存放的是 HBase 内置的表，default 表是用户默认使用的命名空间。

2）Region

类似于关系型数据库的表概念。不同的是，HBase 定义表时只需要声明列族即可，不需要声明具体的列。这意味着，往 HBase 写入数据时，字段可以动态、按需指定。因此，和关系型数据库相比，HBase 能够轻松应对字段变更的场景。

3）Row

HBase 表中的每行数据都由一个 RowKey 和多个 Column（列）组成，数据是按照 RowKey的字典顺序存储的，并且查询数据时只能根据 RowKey 进行检索，所以 RowKey 的设计十分重要。

4）Column

HBase 中的每个列都由 Column Family(列族)和 Column Qualifier（列限定符）进行限定，例如 info：name，info：age。建表时，只需指明列族，而列限定符无需预先定义。

5）Time Stamp

用于标识数据的不同版本（version），每条数据写入时，如果不指定时间戳，系统会自动为其加上该字段，其值为写入 HBase 的时间。
由于hdfs不支持修改文件中的数据，因此通过版本来删除和修改表中列的数据，最新的版本数据即为最新数据

6）Cell

由{rowkey, column Family：column Qualifier, time Stamp} 唯一确定的单元。cell 中的数据是没有类型的，全部是字节码形式存贮。

 */

/**
 * hbase连接
 */
public class HbaseConnection {

    /**
     * connection对象中有两个对象Table和Admin
     *      Table负责操作数据 table是轻量级的，不是线程安全的，不推荐池化或者缓存这个连接
     *      Admin负责操作ddl，admin是轻量级的，不是线程安全的，不推荐池化或者缓存这个连接
     */
    public static Connection connection = null;
    static {
        try {
            init();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void init() throws Exception {
        Configuration conf = new Configuration();
        conf.set("hbase.zookeeper.quorum", "localhost");
        connection = ConnectionFactory.createConnection(conf);
        System.out.println("connnection ---> " + connection);
    }

    public static Connection getConnection() {
        return connection;
    }


    public static void main(String[] args) throws Exception {
        // 创建配置对象
        Configuration conf = new Configuration();
        // hbase使用zk管理，所以只需要给定一个zk地址即可
        conf.set("hbase.zookeeper.quorum", "localhost");

        // 创建连接， 默认是同步连接, 连接对象是重量级的
        Connection connection = ConnectionFactory.createConnection(conf);

        // 可以使用异步连接，不推荐
//        CompletableFuture<AsyncConnection> asyncConnection = ConnectionFactory.createAsyncConnection(conf);

        // 使用连接
        System.out.println(connection);

        // 关闭连接
        connection.close();
    }

}
