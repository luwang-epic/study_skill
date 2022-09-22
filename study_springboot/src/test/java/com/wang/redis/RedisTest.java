package com.wang.redis;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

/*
Redis 是一个开源的 key-value 存储系统。
    有五种基本数据类型：字符串（String），列表（List），集合（Set），有序集合（ZSet），哈希（Hash）
    有3中复杂数据类型：Bitmaps，HyperLogLog（适合存放统计信息，例如存放点击，计算：uv，pv很方便），Geospatial（地理位置）

Redis 发布订阅 (pub/sub) 是一种消息通信模式：发送者 (pub) 发送消息，订阅者 (sub) 接收消息。
Redis 客户端可以订阅任意数量的频道。

Redis 事务是一个单独的隔离操作：事务中的所有命令都会序列化、按顺序地执行。
事务在执行的过程中，不会被其他客户端发送来的命令请求所打断。
Redis 事务的主要作用就是串联多个命令防止别的命令插队。

Redis 事务中有 Multi、Exec 和 discard 三个指令，在 Redis 中，
从输入 Multi 命令开始，输入的命令都会依次进入命令队列中，但不会执行，
直到输入 Exec 后，Redis 会将之前的命令队列中的命令依次执行。而组队的过程中可以通过 discard 来放弃组队。
组队中某个命令出现了报告错误，执行时整个的所有队列都会被取消。
如果执行阶段某个命令报出了错误，则只有报错的命令不会被执行，而其他的命令都会执行，不会回滚。

悲观锁 (Pessimistic Lock)，顾名思义，就是很悲观，每次去拿数据的时候都认为别人会修改，
所以每次在拿数据的时候都会上锁，这样别人想拿这个数据就会 block 直到它拿到锁。
传统的关系型数据库里边就用到了很多这种锁机制，比如行锁，表锁等，读锁，写锁等，都是在做操作之前先上锁。

乐观锁 (Optimistic Lock)，顾名思义，就是很乐观，每次去拿数据的时候都认为别人不会修改，所以不会上锁，
但是在更新的时候会判断一下在此期间别人有没有去更新这个数据，可以使用版本号等机制。
乐观锁适用于多读的应用类型，这样可以提高吞吐量。Redis 就是利用这种 check-and-set 机制实现事务的。

在执行 multi 之前，先执行 watch key1 [key2]，可以监视一个 (或多个) key ，
如果在事务执行之前这个 (或这些) key 被其他命令所改动，那么事务将被打断。

Redis 事务三特性
    1. 单独的隔离操作 ：事务中的所有命令都会序列化、按顺序地执行。
        事务在执行的过程中，不会被其他客户端发送来的命令请求所打断。
    2. 没有隔离级别的概念 ：队列中的命令没有提交之前都不会实际被执行，因为事务提交前任何指令都不会被实际执行。
    3. 不保证原子性 ：事务中如果有一条命令执行失败，其后的命令仍然会被执行，没有回滚 。

Redis 提供了 2 个不同形式的持久化方式：
    1. RDB（Redis DataBase） 默认开始
        在指定的时间间隔内将内存中的数据集快照写入磁盘，
        也就是行话讲的 Snapshot 快照，它恢复时是将快照文件直接读到内存里。

        流程如下：
            Redis 会单独创建（fork）一个子进程来进行持久化，首先会将数据写入到一个临时文件中，待持久化过程都结束了，
            再用这个临时文件替换上次持久化好的文件。整个过程中，主进程是不进行任何 IO 操作的，这就确保了极高的性能。
            如果需要进行大规模数据的恢复，且对于数据恢复的完整性不是非常敏感，那 RDB 方式要比 AOF 方式更加的高效。
            RDB 的缺点是最后一次持久化后的数据可能丢失。
    2. AOF（Append Of File） 默认不开启
        以日志的形式来记录每个写操作（增量保存），将 Redis 执行过的所有写指令记录下来 (读操作不记录)，
        只许追加文件但不可以改写文件，redis 启动之初会读取该文件重新构建数据，
        换言之，redis 重启的话就根据日志文件的内容将写指令从前到后执行一次以完成数据的恢复工作。

        流程如下：
            客户端的请求写命令会被 append 追加到 AOF 缓冲区内；
            AOF 缓冲区根据 AOF 持久化策略 [always,everysec,no] 将操作 sync 同步到磁盘的 AOF 文件中；
            AOF 文件大小超过重写策略或手动重写时，会对 AOF 文件 rewrite 重写，压缩 AOF 文件容量；
            Redis 服务重启时，会重新 load 加载 AOF 文件中的写操作达到数据恢复的目的。

Redis主从模式：
主机数据更新后根据配置和策略， 自动同步到备机的 master/slaver 机制，
Master 以写为主，Slave 以读为主，主从复制节点间数据是全量的。
主要用于：1. 读写分离，性能扩展；2. 容灾快速恢复

复制原理：
    Slave 启动成功连接到 master 后会发送一个 sync 命令；
    Master 接到命令启动后台的存盘进程，同时收集所有接收到的用于修改数据集命令，
        在后台进程执行完毕之后，master 将传送整个数据文件到 slave，以完成一次完全同步。
    全量复制：slave 服务器在接收到数据库文件数据后，将其存盘并加载到内存中。
    增量复制：Master 继续将新的所有收集到的修改命令依次传给 slave，完成同步。
    但是只要是重新连接 master，一次完全同步（全量复制) 将被自动执行。

哨兵模式 (sentinel)
    反客为主：当一个 master 宕机后，后面的 slave 可以立刻升为 master，其后面的 slave 不用做任何修改。
    用 slaveof no one 指令将从机变为主机。而哨兵模式是反客为主的自动版，能够后台监控主机是否故障，
    如果故障了根据投票数自动将从库转换为主库。

由于所有的写操作都是先在 Master 上操作，然后同步更新到 Slave 上，
所以从 Master 同步到 Slave 机器有一定的延迟，
当系统很繁忙的时候，延迟问题会更加严重，Slave 机器数量的增加也会使这个问题更加严重。

Redis 集群（cluster 模式）
    Redis 集群（包括很多小集群）实现了对 Redis 的水平扩容，即启动 N 个 redis 节点，
    将整个数据库分布存储在这 N 个节点中，每个节点存储总数据的 1/N，即一个小集群存储 1/N 的数据，
    每个小集群里面维护好自己的 1/N 的数据。

    Redis 集群通过分区（partition）来提供一定程度的可用性（availability）：
    即使集群中有一部分节点失效或者无法进行通讯， 集群也可以继续处理命令请求。

    该模式的 redis 集群特点是：分治、分片。

    一个 Redis 集群包含 16384 个插槽（hash slot），数据库中的每个键都属于这 16384 个插槽的其中一个。
    集群使用公式 CRC16 (key) % 16384 来计算键 key 属于哪个槽，
    其中 CRC16 (key) 语句用于计算键 key 的 CRC16 校验和 。
    集群中的每个节点负责处理一部分插槽。 举个例子， 如果一个集群可以有主节点， 其中：
        节点 A 负责处理 0 号至 5460 号插槽。
        节点 B 负责处理 5461 号至 10922 号插槽。
        节点 C 负责处理 10923 号至 16383 号插槽。

Redis的应用问题包括：缓存穿透，缓存击穿，缓存雪崩
1. 缓存穿透
问题描述
    key 对应的数据在数据源并不存在，每次针对此 key 的请求从缓存获取不到，
    请求都会压到数据源（数据库），从而可能压垮数据源。比如：用一个不存在的用户 id 获取用户信息，
    不论缓存还是数据库都没有，若黑客利用此漏洞进行攻击可能压垮数据库。

缓存穿透发生的条件：
    应用服务器压力变大
    redis 命中率降低
    一直查询数据库，使得数据库压力太大而压垮
其实 redis 在这个过程中一直平稳运行，崩溃的是我们的数据库（如 MySQL）。

缓存穿透发生的原因：
    黑客或者其他非正常用户频繁进行很多非正常的 url 访问，使得 redis 查询不到数据库。

解决方案：
    1. 对空值缓存：如果一个查询返回的数据为空（不管是数据是否不存在），
        我们仍然把这个空结果（null）进行缓存，设置空结果的过期时间会很短，最长不超过五分钟。
    2. 设置可访问的名单（白名单）：使用 bitmaps 类型定义一个可以访问的名单，名单 id 作为 bitmaps 的偏移量，
        每次访问和 bitmap 里面的 id 进行比较，如果访问 id 不在 bitmaps 里面，进行拦截，不允许访问。
    3. 采用布隆过滤器：布隆过滤器（Bloom Filter）是 1970 年由布隆提出的。
        它实际上是一个很长的二进制向量 (位图) 和一系列随机映射函数（哈希函数）。
        布隆过滤器可以用于检索一个元素是否在一个集合中。它的优点是空间效率和查询时间都远远超过一般的算法，
        缺点是有一定的误识别率和删除困难。
    4. 进行实时监控：当发现 Redis 的命中率开始急速降低，需要排查访问对象和访问的数据，
        和运维人员配合，可以设置黑名单限制服务。

2. 缓存击穿
问题描述：
    key 对应的数据存在，但在 redis 中过期，此时若有大量并发请求过来，
    这些请求发现缓存过期一般都会从后端数据库加载数据并回设到缓存，
    这个时候大并发的请求可能会瞬间把后端数据库压垮。

缓存击穿的现象：
    数据库访问压力瞬时增加，数据库崩溃
    redis 里面没有出现大量 key 过期
    redis 正常运行
缓存击穿发生的原因：redis 某个 key 过期了，大量访问使用这个 key（热门 key）。

解决方案
    1. key 可能会在某些时间点被超高并发地访问，是一种非常 “热点” 的数据。
    2. 预先设置热门数据：在 redis 高峰访问之前，把一些热门数据提前存入到 redis 里面，
        加大这些热门数据 key 的时长。
    3. 实时调整：现场监控哪些数据热门，实时调整 key 的过期时长。
    4. 使用锁：
        就是在缓存失效的时候（判断拿出来的值为空），不是立即去 load db。
        先使用缓存工具的某些带成功操作返回值的操作（比如 Redis 的 SETNX）去 set 一个 mutex key。
        当操作返回成功时，再进行 load db 的操作，并回设缓存，最后删除 mutex key；
        当操作返回失败，证明有线程在 load db，当前线程睡眠一段时间再重试整个 get 缓存的方法。

3.缓存雪崩
问题描述：
    key 对应的数据存在，但在 redis 中过期，此时若有大量并发请求过来，
    这些请求发现缓存过期一般都会从后端数据库加载数据并回设到缓存，
    这个时候大并发的请求可能会瞬间把后端数据库压垮。

缓存雪崩与缓存击穿的区别在于这里针对很多 key 缓存，前者则是某一个 key 正常访问。

解决方案：
    1. 构建多级缓存架构：nginx 缓存 + redis 缓存 + 其他缓存（ehcache 等）。
    2. 使用锁或队列：用加锁或者队列的方式来保证不会有大量的线程对数据库一次性进行读写，
        从而避免失效时大量的并发请求落到底层存储系统上，该方法不适用高并发情况。
    3. 设置过期标志更新缓存：记录缓存数据是否过期（设置提前量），
        如果过期会触发通知另外的线程在后台去更新实际 key 的缓存。
    4. 将缓存失效时间分散开：比如可以在原有的失效时间基础上增加一个随机值，比如 1-5 分钟随机，
        这样每一个缓存的过期时间的重复率就会降低，就很难引发集体失效的事件。


Jedis和Lettuce的区别
jedis和Lettuce都是Redis的客户端，它们都可以连接Redis服务器，
但是在SpringBoot2.0之后默认都是使用的Lettuce这个客户端连接Redis服务器。
因为当使用Jedis客户端连接Redis服务器的时候，每个线程都要拿自己创建的Jedis实例去连接Redis客户端，
当有很多个线程的时候，不仅开销大需要反复的创建关闭一个Jedis连接，而且也是线程不安全的，
一个线程通过Jedis实例更改Redis服务器中的数据之后会影响另一个线程；

但是如果使用Lettuce这个客户端连接Redis服务器的时候，就不会出现上面的情况，Lettuce底层使用的是Netty，
当有多个线程都需要连接Redis服务器的时候，可以保证只创建一个Lettuce连接，使所有的线程共享这一个Lettuce连接，
这样可以减少创建关闭一个Lettuce连接时候的开销；而且这种方式也是线程安全的，
不会出现一个线程通过Lettuce更改Redis服务器中的数据之后而影响另一个线程的情况

 */
@SpringBootTest
public class RedisTest {

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 查看注入到redisTemplate中的factory是哪一种类型
     *      有JedisConnectionFactory 和 LettuceConnectionFactory 两种类型
     */

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    @Test
    void testRedis(){
        ValueOperations<String, String> operations = redisTemplate.opsForValue();

        operations.set("hello","world");

        String hello = operations.get("hello");
        System.out.println(hello);

        System.out.println(redisConnectionFactory.getClass());
    }
}
