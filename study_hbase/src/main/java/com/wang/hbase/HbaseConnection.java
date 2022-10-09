package com.wang.hbase;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;

/*
Hbase中的几个重要概念：
NameSpace
    命名空间，类似于关系型数据库的DataBase概念，每个命名空间下有多个表。HBase有两个自带的命名空间，
    分别是 hbase 和 default，hbase中存放的是HBase内置的表，default表是用户默认使用的命名空间。

Table
    类似于关系型数据库的表概念。不同的是，HBase 定义表时只需要声明列族即可，不需要声明具体的列。
    因为数据存储时稀疏的，所有往HBase写入数据时，字段可以动态、按需指定。
    因此，和关系型数据库相比，HBase能够轻松应对字段变更的场景。

Row
    HBase 表中的每行数据都由一个 RowKey 和多个 Column（列）组成，数据是按照 RowKey的字典顺序存储的，
    并且查询数据时只能根据 RowKey 进行检索，所以RowKey的设计十分重要。

Column
    HBase中的每个列都由Column Family(列族)和Column Qualifier（列限定符）进行限定，
    例如 info: name，info: age，其他info为列族，name,age为列名。
    建表时，只需指明列族，而列限定符无需预先定义。

TimeStamp
    用于标识数据的不同版本（version），每条数据写入时，如果不指定时间戳，系统会自动为其加上该字段，其值为写入HBase的时间。
    由于hdfs不支持修改文件中的数据，因此通过版本来删除和修改表中列的数据，最新的版本数据即为最新数据

Cell
    由{row key, column family, column Qualifier, time Stamp} 唯一确定的单元。是hbase的最小存储单元
    cell 中的数据是没有类型的，全部是字节形式存贮。

Region
    table在行的方向上分隔为多个Region。Region是HBase中分布式存储和负载均衡的最小单元，
    即不同的region可以分别在不同的Region Server上，但同一个Region是不会拆分到多个server上。
    Region按大小分隔，表中每一行只能属于一个region。随着数据不断插入表，region不断增大，
    当region的某个列族达到一个阈值（默认256M）时就会分成两个新的region。

Store
    每一个region有一个或多个store组成，至少是一个store，hbase会把一起访问的数据放在一个store里面，
    即为每个ColumnFamily建一个store（即有几个ColumnFamily，也就有几个Store）。
    一个Store由一个memStore和0或多个StoreFile组成，
    HBase以store的大小来判断是否需要切分region。

MemStore
    memStore是放在内存里的。保存修改的数据即keyValues。当memStore的大小达到一个阀值（默认64MB）时，
    memStore会被flush到文件，即生成一个快照。目前hbase会有一个线程来负责memStore的flush操作。

StoreFile
    memStore内存中的数据写到文件后就是StoreFile（即memStore的每次flush操作都会生成一个新的StoreFile），
    StoreFile底层是以HFile的格式保存。包括数据本身（keyValue 键值对）、元数据记录、文件信息、
    数据索引、元数据索引和一个固定长度的尾部信息（记录文件的修改情况）
    hdfs上的文件路径为：/hbase/data/{namespace}/{tableName}/{regionId}/{columnFamily}/{storeId}

HFile
    HFile是存储在HDFS上面每一个store文件夹下实际存储数据的文件，是hadoop的二进制格式文件。
    一个StoreFile对应着一个store文件夹。里面存储多种内容。包括数据本身（keyValue 键值对）、
    元数据记录、文件信息、数据索引、元数据索引和一个固定长度的尾部信息（记录文件的修改情况）。

Master
    主节点主要进程，具体实现类为HMaster，通常部署在NameNode上。负责通过ZK监控RegionServer进程状态，
    同时是所有元数据变化的接口。内部启动监控执行region的故障转移和拆分的线程。

    主要功能：
        （1）管理元数据表格hbase:meta，接收用户对表格创建修改删除的命令并执行
        （2）监控region是否需要进行负载均衡，故障转移和region的拆分。
    通过启动多个后台线程监控实现上述功能主要的：
        1) LoadBalancer负载均衡器
            周期性监控region分布在regionServer上面是否均衡，由参数hbase.balancer.period控制周期时间，默认5分钟。
        2) CatalogJanitor元数据管理器
            定期检查和清理hbase:meta中的数据。
        3) MasterProcWAL master预写日志处理器
            把master需要执行的任务记录到预写日志 WAL中，如果master宕机，让backupMaster读取日志继续干。

RegionServer
    数据节点主要进程，具体实现类为HRegionServer，部署在datanode上。主要负责数据cell的处理。
    同时在执行区域(Region)的拆分和合并的时候，由RegionServer来实际执行

    主要功能：
        （1）负责数据cell的处理，例如写入数据put（也会先写预写日志），查询数据get等
        （2）拆分合并region的实际执行者，有master监控，有regionServer执行。

    包括一下几个结构：
        每个Region的每个Store都对应一个写缓存MenStore，因此会有很多MenStore
            写缓存，由于HFile 中的数据要求是有序的，所以数据是先存储在MemStore中，排好序后，
            等到达刷写时机才会刷写到HFile，每次刷写都会形成一个新的HFile，写入到对应的hdfs上Store文件夹中。
            hdfs上的文件路径为：/hbase/data/{namespace}/{tableName}/{regionId}/{columnFamily}/{storeId}
        WAL，预写日志
            由于数据要经MemStore排序后才能刷写到HFile，但把数据保存在内存中会有很高的概率导致数据丢失，
            为了解决这个问题，数据会先写在一个叫做Write-Ahead logfile的文件中，放在hdfs上面
            然后再写入MemStore中。所以在系统出现故障的时候，数据可以通过这个日志文件重建。
       BlockCache，读缓存
            读缓存，每次查询出的数据会缓存在BlockCache中，方便下次查询。


Zookeeper
    HBase通过Zookeeper来做master的高可用、记录RegionServer的部署信息、并且存储有meta表的位置信息。
    HBase对于数据的读写操作时直接访问Zookeeper的，通过从zk获取元数据并连接相关节点进行读写操作

    在客户端对元数据进行操作的时候才会连接master，如果对数据进行读写，
    直接连接zookeeper读取目录/hbase/meta-region-server节点信息，会记录meta表格的位置，
    直接读取即可，不需要访问master，这样可以减轻master的压力，相当于master专注meta表的写操作，客户端可直接读取meta表。


HDFS
    HDFS为Hbase提供最终的底层数据存储服务，同时为HBase提供高容错的支持。

    Hbase存储数据稀疏，数据存储多维，不同的行具有不同的列。
    数据存储单个HFile文件有序，按照RowKey的字典序排列，RowKey为Byte数组


Meta表格介绍：
    全称 hbase: meta,本质上和 HBase 的其他表格一样。
    RowKey为：([table],[region start key],[region id]) 即表名，region起始位置和region的唯一编号。
    只有一个列族为info，其中的列信息包括：
        info：regioninfo 为region信息，存储一个HRegionInfo对象。
        info：server当前region所处的RegionServer信息，包含端口号。
        info：serverstartcode 当前region被分到RegionServer的起始时间。
    如果一个表处于切分的过程中，即region切分，还会多出两列info：splitA和info：splitB，
    存储值也是HRegionInfo对象，拆分结束后，删除这两列。

HBase的KeyValue结构：（参考：https://blog.51cto.com/u_15471709/4876303）
    HBase中KeyValue并不是简单的KV数据对，而是一个具有复杂元素的结构体，
    其中Key由RowKey，ColumnFamily，Qualifier ，TimeStamp，KeyType等多部分组成，Value是一个简单的二进制数据。
    Key中元素KeyType表示该KeyValue的类型，取值分别为Put/Delete/Delete Column/Delete Family等

    HBase支持四种主要的数据操作，分别是Get/Scan/Put/Delete，其中Get和Scan代表数据查询，
    Put操作代表数据插入或更新（如果Put的RowKey不存在则为插入操作、否则为更新操作），
    特别需要注意的是HBase中更新操作并不是直接覆盖修改原数据，而是生成新的数据，新数据和原数据具有不同的版本（时间戳）；
    Delete操作执行数据删除，和数据更新操作相同，HBase执行数据删除并不会马上将数据从数据库中永久删除，
    而只是生成一条删除记录，最后在系统执行文件合并的时候再统一删除。

    HBase设定Key大小首先比较RowKey，RowKey越小Key就越小；RowKey如果相同就看CF，CF越小Key越小；
    CF如果相同看Qualifier，Qualifier越小Key越小；Qualifier如果相同再看Timestamp，
    Timestamp越大表示时间越新，对应的Key越小。如果Timestamp还相同，就看KeyType，
    KeyType按照DeleteFamily -> DeleteColumn -> Delete -> Put 顺序依次对应的Key越来越大。


Hbase写流程：
    （1）首先访问zookeeper，获取hbase:meta表位于哪个Region Server
    （2）访问对应的Region Server，获取hbase:meta表，将其缓存到连接中，
        作为连接属性MetaCache，由于Meta表格具有一定的数据量，导致了创建连接比较慢；
        之后使用创建的连接获取Table，这是一个轻量级的连接，只有在第一次创建的时候会检查表格是否存时访问RegionServer，
        之后在获取Table时不会访问RegionServer；
    （3）调用Table的put方法写入数据，此时还需要解析RowKey，对照缓存的MetaCache，
        查看具体写入的位置有哪个RegionServer；
    （4）将数据顺序写入（追加）到WAL，此处写入是直接落盘的，并设置专门的线程控制WAL预写日志的滚动
    （5）根据写入命令的RowKey和ColumnFamily查看具体写入到哪个MemStore，并且在MemStore中排序；
    （6）向客户端发送ack；
    （7）等达到MemStore的刷写时机后，将数据刷写到对应的store文件夹中，即落盘到Hdfs上。
客户端只需要配置zookeeper的访问地址以及根目录，就可以进行正常的读写请求。不需要配置集群的RegionServer地址列表。
客户端会将hbase:meta元数据表缓存在本地，因此上述步骤中前两步只会在客户端第一次请求的时候发生，
之后所有请求都直接从缓存中加载元数据。如果集群发生某些变化导致hbase:meta元数据更改，
客户端再根据本地元数据表请求的时候就会发生异常，此时客户端需要重新加载一份最新的元数据表到本地。

Hbase读流程：
    （0）创建连接同写流程。
    （1）创建Table对象发送get请求。
    （2）优先访问Block Cache，查找是否之前读取过，并且可以读取HFile的索引信息和布隆过滤器。
    （3）不管读缓存中是否已经有数据了（可能已经过期了），都需要再次读取写缓存和store中的文件。
    （4）最终将所有读取到的数据合并版本，按照get的要求返回即可。

Hbase事务
    HBase和传统数据库一样提供了事务的概念，只是HBase的事务是行级事务，
    可以保证行级数据的原子性、一致性、隔离性以及持久性，即通常所说的ACID特性。
    为了实现事务特性，HBase采用了各种并发控制策略，包括各种锁机制、MVCC机制等

    HBase提供了各种锁机制和MVCC机制来保证数据的原子性、一致性等特性，其中使用互斥锁实现的行锁保证了行级数据的原子性，
    使用JDK提供的读写锁(ReentrantReadWriteLock)实现了Store级别、Region级别的数据一致性，
    同时使用行锁+MVCC机制实现了在高性能非锁定读场景下的数据一致性。
        1. Region更新读写锁：HBase在执行数据读取操作之前都会加一把Region级别的读锁（共享锁），所有读取操作线程之间不会相互阻塞；
            然而，HBase在将memStore数据落盘时会加一把Region级别的写锁（独占锁）。
            因此，在memStore数据落盘时，数据更新操作线程（Put操作、Append操作、Delete操作）都会阻塞等待至该写锁释放。
        2. Region Close保护锁：HBase在执行close操作以及split操作时会首先加一把Region级别的写锁（独占锁），
            阻塞对region的其他操作，比如compact操作、flush操作以及其他更新操作
        3. Store snapshot保护锁：HBase在执行flush memStore的过程中首先会基于memStore做snapshot，
            这个阶段会加一把store级别的写锁（独占锁），用以阻塞其他线程对该memStore的各种更新操作；
            清除snapshot时也相同，会加一把写锁阻塞其他对该memStore的更新操作。

    MVCC，即多版本并发控制技术，它使得事务引擎不再单纯地使用行锁实现数据读写的并发控制，取而代之的是，
    把行锁与行的多个版本结合起来，经过简单的算法就可以实现非锁定读，进而大大的提高系统的并发性能。
    HBase正是使用行锁 ＋ MVCC保证高效的并发读写以及读写数据一致性。

    最简单的数据不一致解决方案是读写线程公用一把行锁，这样可以保证读写之间互斥，
    读写线程同时抢占行锁必然会极大地影响性能，因为获取读锁后尽管读线程不受影响，但是写线程将会被阻塞
    为此，HBase采用MVCC解决方案避免读线程去获取行锁。主要新增了一个写序号和读序号，其实就是数据的版本号。
    简单说来，HBase的ACID就是在各个RegionServer上维护一个我称之为“严格单调递增事务号”(strictly monotonically increasing transaction numbers)。
    当一个写事务(put,delete)开始时将获取到下一个最高事务号，HBase将这个号称为“写入号”(WriteNumber)；
    当一个读事务(scan,get)开始时将获取到上一次提交成功的事务号，这个号被称为“读取点”(ReadPoint)。
    每个创建的KeyValue对都会被标记上它的事务写入号。
    HBase中将这个标签称为”memStore时间戳”。注意将这个时间戳和应用中可见的时间戳区分开。

    HBase的事务非常短暂，且所有事务都是串行提交的（我们都知道：所有的事务可以工作都是基于严格串行的提交的，
    否则一个稍早的未提交的事务可能由于在他之后的事务先提交了而变的可见了）。HBase维护者一个未完成的事务列表。
    一个事务想提交，必须等到之前所有的事务都提交（注意:变更依然是及时的，并发的，只有提交才是串行的）。

Hbase的row key设计原则：
    1. RowKey是一个二进制码流，可以是任意字符串，最大长度为64kb，实际应用中一般为10-100byte，
        以byte[]形式保存，一般设计成定长。建议越短越好，不要超过16个字节，原因如下：
           (1) 数据的持久化文件HFile中时按照Key-Value存储的，如果RowKey过长，
                例如超过100byte，那么1000w行的记录，仅RowKey就需占用近1GB的空间。这样会极大影响HFile的存储效率。
           (2) MemStore会缓存部分数据到内存中，若RowKey字段过长，内存的有效利用率就会降低，
                就不能缓存更多的数据，从而降低检索效率。
           (3) 目前操作系统都是64位系统，内存8字节对齐，控制在16字节，8字节的整数倍利用了操作系统的最佳特性。
    2. 设计的RowKey应均匀的分布在各个HBase节点上，避免使用递增行键/时序数据。
        如果ROW KEY设计的都是按照顺序递增（例如：时间戳），这样会有很多的数据写入时，负载都在一台机器上。
        我们尽量应当将写入大压力均衡到各个RegionServer，主要有如下几种方式：
            salt加盐：Salt是将每一个row key加一个前缀,前缀使用一些随机字符,使得数据分散在多个不同的Region
            Hash散列或者Mod：用Hash散列来替代随机Salt前缀的好处是让一个给定的行有相同的前缀,
                这在分散了Region负载的同时,使读操作也能够腿短,
                确定性Hash(比如MD5后取前4位左前缀)能让客户端重建完整的Row key可以直接使用get操作,
                获取想要的行.数据量越大这样的会使分区更加均衡,如果Row key是数字类型的,也可以考虑Mod方法.
            Reverse反转：针对固定长度的Row key反转后存储,这样可以使Row key中经常改变的部分放在最前面,
                可以有效的随机row key.例如手机号就可以使用反转.
    3. RowKey字段的选择，遵循的 最基本原则是唯一性，RowKey必须能够唯一的识别一行数据。RowKey字段都应该参考最高频的查询场景,
        在对选取的RowKey字段值进行改造，组合字段场景下需要重点考虑字段的顺序，越高频的查询字段排列越靠左。


在HBase中有许多不同的数据集，具有不同的访问模式和服务级别期望。因此，这些经验法则只是一个概述:
    1. 目标区域Region的大小介于10到50GB之间。
        目的是让单元格不超过10MB，如果使用mob，则为50MB 。
        否则，请考虑将您的单元格数据存储在 HDFS 中，并在 HBase 中存储指向数据的指针。
    2. 典型的模式在每个表中有1到3个列族。HBase表不应该被设计成模拟RDBMS表。
    3. 对于具有1或2列族的表格，大约50-100个区域Region是很好的数字。请记住，区域是列族的连续段。
    4. 尽可能短地保留列族名称。列族名称存储在每个值 (忽略前缀编码) 中。
        它们不应该像在典型的RDBMS中一样具有自我记录和描述性。因此每个记录都需要存储列族名称，太大会占用内存
    5. 如果您正在存储基于时间的机器数据或日志记录信息，并且行密钥基于设备ID或服务ID加上时间，
        则最终可能会出现一种模式，即旧数据区域在某个时间段之后永远不会有额外的写入操作。
        在这种情况下，最终会有少量活动区域和大量没有新写入的较旧区域。
        对于这些情况，您可以容忍更多区域，因为您的资源消耗仅由活动区域驱动。
    6. 如果只有一个列族忙于写入，则只有该列族兼容内存。分配资源时请注意写入模式。

HBase的使用场景：
1. 超大数据量：特别是写密集型应用，每天写入量巨大，而相对读数量较小的应用，比如IM的历史消息，游戏的日志等等
2. 不需要复杂查询条件来查询数据的应用，HBase只支持基于row key的查询，对于HBase来说，
    单条记录或者小范围的查询是可以接受的，大范围的查询由于分布式的原因，可能在性能上有点影响，
    而对于像SQL的join等查询，HBase无法支持。
3. 对性能和可靠性要求非常高的应用，由于HBase本身没有单点故障，可用性非常高。
4. 半结构化或非结构化数据：对于数据结构字段不够确定或杂乱无章很难按一个概念去进行抽取的数据适合用HBase
5. 记录非常稀疏：RDBMS的行有多少列是固定的，为null的列浪费了存储空间。
    HBase为null的Column不会被存储，这样既节省了空间又提高了读性能。
6.多版本数据：根据Row key和Column key定位到的Value可以有任意数量的版本值
    因此对于需要存储变动历史记录的数据，用HBase就非常方便了。



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
