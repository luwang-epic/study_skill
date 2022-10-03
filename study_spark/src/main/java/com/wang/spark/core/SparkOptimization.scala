package com.wang.spark.core

import org.apache.spark.SparkConf
import org.apache.spark.sql.{DataFrame, SparkSession}


/*
Spark sql的执行计划：
dataframe/sql query -> Analysis -> unresolved logical plan -> logical plan -> optimized logical plan
-> physical plans -> cost model optimization -> selected physical plan -> code generation
-> submit & execute

1. unresolved logical plan: Parser 组件检查 SQL 语法上是否有问题，
  然后生成 Unresolved（未决断）的逻辑计划，不检查表名、不检查列名。
2. Logical Plan: 通过访问 Spark 中的 Catalog 存储库来解析验证语义、列名、类型、表名等，然后得到逻辑执行计划
3. optimized logical plan: Catalyst优化器根据各种规则进行优化，RBO（基于规则的优化）主要包括：谓词下推等。
4. physical plans： 可执行的物理计划，
  1）HashAggregate运算符表示数据聚合，一般HashAggregate是成对出现，第一个HashAggregate是将执行节点本地的数据进行局部聚合，
    另一个HashAggregate是将各个分区的数据进一步进行聚合计算。
  2）Exchange 运算符其实就是 shuffle，表示需要在集群上移动数据。很多时候HashAggregate会以Exchange分隔开来。
  3）Project 运算符是SQL中的投影操作，就是选择列（例如：select name, age…）。
  4）BroadcastHashJoin运算符表示通过基于广播方式进行HashJoin。
  5）LocalTableScan 运算符就是全表扫描本地的表。
5. cost model optimization: 基于代价的优化，主要时使用表的统计信息进行优化


spark内存相关优化
  spark内存包括堆内和堆外内存，堆内主要包括：JVM中spark和程序的类，变量等内存，Execution类型（主要是shuffle使用）
      和 缓存内存（包括persist/cache函数以及广播变量）；堆外内存主要包括：Execution内存和缓存内存

  一些优化手段：缓存公共的rdd，防止重复计算 & 使用kryo序列化并且使用rdd序列化缓存级别，减少缓存的数据量


spark CPU 低效的原因一般包括：
  1）并发度较低、数据分片较大容易导致 CPU 线程挂起
  2）并发度过高、数据过于分散会让调度开销更多
为了合理利用资源，一般会将并发度（task 数）设置成并行度（vcore核数）的 2 倍到 3 倍


spark sql RBO 优化主要包括：
  1. 谓词下推：将过滤条件的谓词逻辑都尽可能提前执行，减少下游处理的数据量。
    需要注意过滤条件写在 on 与 where，结果可能不一样的
  2. 列剪裁：就是扫描数据源的时候，只读取那些与查询相关的字段。
  3. 常量替换：假设我们在年龄上加的过滤条件是 “age < 12 + 18”，Catalyst会使用ConstantFolding规则，
    自动帮我们把条件变成 “age < 30”

spark sql CBO 的优化主要包括：
  CBO 优化主要在物理计划层面，原理是计算所有可能的物理计划的代价，并挑选出代价最小的物理执行计划。
  充分考虑了数据本身的特点（如大小、分布）以及操作算子的特点（中间结果集的分布及大小）及代价，
  从而更好的选择执行代价最小的物理执行计划。

  通过 "spark.sql.cbo.enabled"来开启，默认是false。配置开启CBO后，CBO优化器可以基于表和列的统计信息，
  进行一系列的估算，最终选择出最优的查询计划。比如：Build侧选择、优化Join类型、优化多表Join顺序等。
  还可以根据过滤后表大小是否达到广播join的表数据量标准，符合条件进行广播Join

广播Join优化：
  Spark join 策略中，如果当一张小表足够小并且可以先缓存到内存中，那么可以使用Broadcast Hash Join,
  其原理就是先将小表聚合到 driver 端，再广播到各个大表分区中，那么再次进行 join 的时候，
  就相当于大表的各自分区的数据与小表进行本地 join，从而规避了shuffle。

  广播 join 默认值为 10MB，由 spark.sql.autoBroadcastJoinThreshold 参数控制。

SMB Join优化：
  SMB JOIN 是sort merge bucket操作，需要进行分桶，首先会进行排序，然后根据key值合并，把相同key的数据放到同一个bucket中（按照key进行hash）。
  分桶的目的其实就是把大表化成小表。相同key的数据都在同一个桶中之后，再进行join操作，那么在联合的时候就会大幅度的减小无关项的扫描。

  使用条件：
    （1）两表进行分桶，也就是join的表必须是分桶表（hive中的一种特殊类型表），桶的个数必须相等
        如果hive表不是分桶表，可以考虑提前生成分桶表，然后再对生成好的分桶表进行join
    （2）两边进行 join 时，join列=排序列=分桶列

  例如：A表根据id字段分为2个桶，如：id1~id5分到桶1中，id6~id10分到桶2中，
      B表根据key分为2个桶（必须和A表一样），如：id2~id3分到桶1中，id6~id11分到桶2中
      排序key为id，由于A，B都是有序的，有序的数据处理更快；这样A join B时，只需要每个桶内进行join就行


spark处理数据数据倾斜
  可以从spark的阶段cpu执行时间图，或者shuffle阶段的task数据读入和写出，或者task的执行时长情况 推断出是否发送了倾斜

  大key的定位：从所有 key 中，把其中每一个 key 随机取出来一部分，然后进行一个百分比的推算，
      这是用局部取推算整体，虽然有点不准确，但是在整体概率上来说，我们只需要大概就可以定位那个最多的key了
      spark提供了抽样函数sample函数进行抽样操作

  单表数据倾斜优化（主要是group by语句）：
    1. 为了减少shuffle数据量以及reduce端的压力，通常Spark SQL在map端会做一个partial aggregate（通常叫做预聚合或者偏聚合），
      即在shuffle前将同一分区内所属同key的记录先进行一个预结算，再将结果进行shuffle，
      发送到reduce端做一个汇总，类似MR的提前Combiner，所以执行计划中HashAggregate通常成对出现。

    2. 两阶段聚合（加盐局部聚合+去盐全局聚合）; 也就是对于同一个key，如果数据量很大，
      可以对key加一个前缀或者后缀的随机数(如果分为100份，0-100的随机数)
      然后进行预聚合，此时会得到<key1_1, value>, <key2_1, value>等之类的数据
      然后再进行二次预聚合，去除这些随机数，将会得到: <key1, value>, <key2, value>，从而进行打散
      例如下面的sql语句：
        select
          courseid, sum(course_sell) totalSell
        from (select remove_random_prefix(random_courseid) courseid, course_sell
            from ( select random_courseid, sum(sellmoney) course_sell
                from ( select random_prefix(courseid, 6) random_courseid, sellmoney
                    from sparktuning.course_shopping_cart
                  ) t1
                group by random_courseid
              ) t2
          ) t3
        group by courseid

  Join数据倾斜优化
    1. 广播Join： 适用于小表join大表。小表足够小，可被加载进Driver并通过Broadcast方法广播到各个Executor中。
    2. 拆分大key打散大表 扩容小表：适用于一般的join数据倾斜，步骤如下：
      1）将存在倾斜的表，根据抽样结果，拆分为倾斜 key（skew 表）和没有倾斜key（common）的两个数据集。
      2）将skew表的key全部加上随机前缀，然后对另外一个不存在严重数据倾斜的数据集（old 表）整体与随机前缀集作笛卡尔乘积（即将数据量扩大N倍，得到new表）。
        以下为打散大 key 和扩容小表的实现思路：
          1）打散大表：实际就是数据一进一出进行处理，对大 key 前拼上随机前缀实现打散 （打散成n份）
          2）扩容小表：实际就是将DataFrame中每一条数据，转成一个集合，并往这个集合里循环添加n条数据，
                最后使用flatmap压平此集合，达到扩容的效果
      3）打散的skew表join扩容的new表 union Common表 join old表
    3. spark sql开启AQE功能，AQE可以帮我们实现2的步骤

spark map端优化：
  1. Map 端聚合
  2. 读取小文件优化（将小文件合并到一个task中（默认spark为一个文件一个task））
  3. 增大map溢写时输出流buffer
      1) map端Shuffle Write有一个缓冲区，初始阈值5m，超过会尝试增加到 2*当前使用内存。
        如果申请不到内存，则进行溢写。而是指输出流对应的缓冲区，这个参数是internal，指定无效
      2) 溢写时使用输出流缓冲区默认32k，这些缓冲区减少了磁盘搜索和系统调用次数，适当提高可以提升溢写效率。


spark reduce端优化：
  1. 过多的cpu资源出现空转浪费，过少影响任务性能。合理设置并行度、并发度，如何设置参考上面
  2. 输出产生小文件优化，可以在插入表数据前进行缩小分区操作来解决小文件过多问题，如 coalesce、repartition算子。
  3. Spark Shuffle 过程中，shuffle reduce task 的 buffer 缓冲区大小决定了 reduce task 每次能够缓冲的数据量，
    也就是每次能够拉取的数据量，如果内存资源较为充足，适当增加拉取数据缓冲区的大小，可以减少拉取数据的次数，
    也就可以减少网络传输的次数，进而提升性能。
    reduce 端数据拉取缓冲区的大小可以通过 spark.reducer.maxSizeInFlight 参数进行设置，默认为48MB。
  4. 调节reduce端拉取数据重试次数
    Spark Shuffle 过程中，reduce task 拉取属于自己的数据时，如果因为网络异常等原因导致失败会自动进行重试。
    对于那些包含了特别耗时的 shuffle 操作的作业，建议增加重试最大次数（比如 60 次），
    以避免由于JVM的full gc 或者网络不稳定等因素导致的数据拉取失败。在实践中发现，
    对于针对超大数据量（数十亿~上百亿）的 shuffle 过程，调节该参数可以大幅度提升稳定性。

    reduce 端拉取数据重试次数可以通过 spark.shuffle.io.maxRetries参数进行设置，
    该参数就代表了可以重试的最大次数。如果在指定次数之内拉取还是没有成功，就可能会导致作业执行失败，默认为3
  5. 调节 reduce 端拉取数据等待间隔
    Spark Shuffle 过程中，reduce task 拉取属于自己的数据时，如果因为网络异常等原因导致失败会自动进行重试，
    在一次失败后，会等待一定的时间间隔再进行重试，可以通过加大间隔时长（比如 60s），
    以增加shuffle操作的稳定性。reduce端拉取数据等待间隔可以通过spark.shuffle.io.retryWait参数进行设置，默认值为5s。
  6. 合理利用bypass （Spark的5种Join策略：https://www.cnblogs.com/jmx-bigdata/p/14021183.html）


spark整体优化：
  1. 调节数据本地化等待时长，可以从spark图形界面的task列表中查看数据的本地化级别，从而判断是否需要增加等待时间
    如果大部分都是PROCESS_LOCAL、NODE_LOCAL，那么就无需进行调节，
    但是如果发现很多的级别都是RACK_LOCAL、ANY，那么需要对本地化的等待时长进行调节
    主要参数：spark.locality.wait  spark.locality.wait.process spark.locality.wait.node spark.locality.wait.rack
  2. 使用堆外内存，主要参数为：spark.memory.offHeap.size
    使用堆外内存可以减轻垃圾回收的工作，也加快了复制的速度。当需要缓存非常大的数据量时，
    虚拟机将承受非常大的 GC 压力，因为虚拟机必须检查每个对象是否可以收集并必须访问所有内存页。
    本地缓存是最快的，但会给虚拟机带来GC压力，所以，当你需要处理非常多GB的数据量时可以考虑使用堆外内存来进行优化，
    因为这不会给 Java 垃圾收集器带来任何压力。让 JAVA GC 为应用程序完成工作，缓存操作交给堆外。
  3. 调节连接等待时长，为了避免长时间暂停(如 GC)导致的超时，可以考虑调节连接的超时时长，
    主要参数：spark.core.connection.ack.wait.timeout，
    判断依据为：当出现连接超时时，有时会遇到 file not found、file lost 这类错误


spark AQE 优化：
  Spark在3.0版本推出了AQE（Adaptive Query Execution），即自适应查询执行。
  AQE是Spark SQL的一种动态优化机制，在运行时，每当Shuffle Map阶段执行完毕，AQE都会结合这个阶段的统计信息，
  基于既定的规则动态地调整、修正尚未执行的逻辑计划和物理计划，来完成对原始查询语句的运行时优化。

  主要包括：
  1. 动态合并分区 和 动态申请资源
  2. 动态切换 Join 策略
  3. 动态优化 Join 倾斜

 */

object SparkOptimization {

  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setMaster("local[*]").setAppName("sparkSQL")
    val sparkSession = SparkSession.builder().config(sparkConf).getOrCreate()

    val jsonDataFrame: DataFrame = sparkSession.read.json("D:\\idea_project\\study_skill\\file\\spark\\sql\\user.json")
    // DataFrame => SQL
    jsonDataFrame.createOrReplaceTempView("user")
    val sql: DataFrame = sparkSession.sql("select username, age from user")

    // 查看执行计划
    sql.explain("extended")

    sql.show(1)

    while(true) {
      Thread.sleep(5 * 60 * 1000)
    }

    // 关闭环境
    sparkSession.close()
  }

}
