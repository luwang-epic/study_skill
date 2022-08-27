package com.wang.hbase;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;

/*

1）NameSpace
    命名空间，类似于关系型数据库的 DatabBase 概念，每个命名空间下有多个表。HBase有两个自带的命名空间，
    分别是 hbase 和 default，hbase 中存放的是 HBase 内置的表，default 表是用户默认使用的命名空间。
2）Region
    table在行的方向上分隔为多个Region。Region是HBase中分布式存储和负载均衡的最小单元，即不同的region可以分别在不同的Region Server上，但同一个Region是不会拆分到多个server上。
    Region按大小分隔，表中每一行只能属于一个region。随着数据不断插入表，region不断增大，当region的某个列族达到一个阈值（默认256M）时就会分成两个新的region。
3）Row
    HBase 表中的每行数据都由一个 RowKey 和多个 Column（列）组成，数据是按照 RowKey的字典顺序存储的，
    并且查询数据时只能根据 RowKey 进行检索，所以 RowKey 的设计十分重要。
4）Column
    HBase 中的每个列都由 Column Family(列族)和 Column Qualifier（列限定符）进行限定，
    例如 info：name，info：age。建表时，只需指明列族，而列限定符无需预先定义。
5）TimeStamp
    用于标识数据的不同版本（version），每条数据写入时，如果不指定时间戳，系统会自动为其加上该字段，其值为写入 HBase 的时间。
    由于hdfs不支持修改文件中的数据，因此通过版本来删除和修改表中列的数据，最新的版本数据即为最新数据
6）Cell
    由{rowkey, column Family：column Qualifier, time Stamp} 唯一确定的单元。
    cell 中的数据是没有类型的，全部是字节码形式存贮。
7) Store
    每一个region有一个或多个store组成，至少是一个store，hbase会把一起访问的数据放在一个store里面，
    即为每个ColumnFamily建一个store（即有几个ColumnFamily，也就有几个Store）。
    一个Store由一个memStore和0或多个StoreFile组成HBase以store的大小来判断是否需要切分region。
8) MemStore
    memStore是放在内存里的。保存修改的数据即keyValues。当memStore的大小达到一个阀值（默认64MB）时，
    memStore会被flush到文件，即生成一个快照。目前hbase 会有一个线程来负责memStore的flush操作。
9) StoreFile
    memStore内存中的数据写到文件后就是StoreFile（即memstore的每次flush操作都会生成一个新的StoreFile），
    StoreFile底层是以HFile的格式保存。
10) HFile
    HFile是HBase中KeyValue数据的存储格式，是hadoop的二进制格式文件。
    一个StoreFile对应着一个HFile。而HFile是存储在HDFS之上的

在 HBase 中有许多不同的数据集，具有不同的访问模式和服务级别期望。因此，这些经验法则只是一个概述:
    1. 目标区域的大小介于10到50 GB之间。
        目的是让单元格不超过10 MB，如果使用 mob，则为50 MB 。
        否则，请考虑将您的单元格数据存储在 HDFS 中，并在 HBase 中存储指向数据的指针。
    2. 典型的模式在每个表中有1到3个列族。HBase 表不应该被设计成模拟 RDBMS 表。
    3. 对于具有1或2列族的表格，大约50-100个区域是很好的数字。请记住，区域是列族的连续段。
    4. 尽可能短地保留列族名称。列族名称存储在每个值 (忽略前缀编码) 中。
        它们不应该像在典型的 RDBMS 中一样具有自我记录和描述性。
    5. 如果您正在存储基于时间的机器数据或日志记录信息，并且行密钥基于设备 ID 或服务 ID 加上时间，
        则最终可能会出现一种模式，即旧数据区域在某个时间段之后永远不会有额外的写入操作。
        在这种情况下，最终会有少量活动区域和大量没有新写入的较旧区域。
        对于这些情况，您可以容忍更多区域，因为您的资源消耗仅由活动区域驱动。
    6. 如果只有一个列族忙于写入，则只有该列族兼容内存。分配资源时请注意写入模式。

 Phoenix是HBase的开源SQL皮肤。可以使用标准JDBC API代替HBase客户端API来创建表，插入数据和查询HBase数据。
 可以将Phoenix理解为客户端，也可以理解为一个数据库。
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
