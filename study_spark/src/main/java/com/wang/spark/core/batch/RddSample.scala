package com.wang.spark.core.batch

import org.apache.spark.rdd.RDD
import org.apache.spark.{HashPartitioner, SparkConf, SparkContext}

object RddSample {

  def main(args: Array[String]): Unit = {

    // 准备环境
    val sparkConf = new SparkConf().setMaster("local[*]").setAppName("RDD")
    val sc = new SparkContext(sparkConf)

    // 创建RDD
    // 从文件中创建RDD，将文件中的数据作为处理的数据源

    // textFile : 以行为单位来读取数据，读取的数据都是字符串
    // wholeTextFiles : 以文件为单位读取数据
    //    读取的结果表示为元组，第一个元素表示文件路径，第二个元素表示文件内容
    val rdd = sc.wholeTextFiles("D:\\idea_project\\study_skill\\file\\spark\\rdd")
    rdd.collect().foreach(println)

    // parallelize : 并行
    //val rdd: RDD[Int] = sc.parallelize(seq)
    // makeRDD方法在底层实现时其实就是调用了rdd对象的parallelize方法。
    // 从内存中创建RDD，将内存中集合的数据作为处理的数据源
    /* makeRDD方法可以传递第二个参数，这个参数表示分区的数量
          第二个参数可以不传递的，那么makeRDD方法会使用默认值 ： defaultParallelism（默认并行度）
            spark在默认情况下，从配置对象中获取配置参数：spark.default.parallelism
            如果获取不到，那么使用totalCores属性，这个属性取值为当前运行环境的最大可用核数
     */
    val listRdd = sc.makeRDD(List(1, 2, 3, 4), 2)

    // 转换操作
    // coalesce方法默认情况下不会将分区的数据打乱重新组合
    // 这种情况下的缩减分区可能会导致数据不均衡，出现数据倾斜
    // 如果想要让数据均衡，可以进行shuffle处理
    //val newRDD: RDD[Int] = listRdd.coalesce(2)
//    val coalesceRdd = listRdd.coalesce(2, true)

    // coalesce算子可以扩大分区的，但是如果不进行shuffle操作，是没有意义，不起作用。
    // 所以如果想要实现扩大分区的效果，需要使用shuffle操作
//    val repartitionRdd = listRdd.coalesce(3, true)
    // 扩大分区：repartition, 底层代码调用的就是coalesce，而且肯定采用shuffle, 和上面等价
//    val repartitionRdd = listRdd.repartition(3)


    // 分区内以行为单位进行操作，来一条数据处理一条，类似于串行操作，效率比较低
    val mapRdd = listRdd.map(num => num * 2)

    // RDD中不存储数据，如果一个RDD需要重复使用，那么需要从头再次执行来获取数据
    // cache默认持久化的操作，只能将数据保存到内存中，如果想要保存到磁盘文件，需要更改存储级别
//    mapRdd.cache() // 底层调用 persist(StorageLevel.MEMORY_ONLY)
    // 持久化操作是在行动算子执行时完成的。
//    mapRdd.persist(StorageLevel.DISK_ONLY)

    // checkpoint 也可以完成持久化，需要落盘，需要指定检查点保存路径
    // 检查点路径保存的文件，当作业执行完毕后，不会被删除；而persist的数据在作业执行完成后会被删除
    // 一般保存路径都是在分布式存储系统：HDFS
    sc.setCheckpointDir("D:\\idea_project\\study_skill\\file\\spark\\rdd_checkpoint")
    mapRdd.checkpoint()

    /* 数据持久化操作不一定是为了RDD重用，某些中间结果很重要的时候，也可以将其持久化，方便查看

      cache : 将数据临时存储在内存中进行数据重用，内存中空间不够等，将会丢弃
              会在血缘关系中添加新的依赖。一旦，出现问题，可以重头读取数据
      persist : 将数据临时存储在磁盘文件中进行数据重用
                涉及到磁盘IO，性能较低，但是数据安全
                如果作业执行完毕，临时保存的数据文件就会丢失
      checkpoint : 将数据长久地保存在磁盘文件中进行数据重用
                涉及到磁盘IO，性能较低，但是数据安全
                为了保证数据安全，所以一般情况下，会独立执行作业
                为了能够提高效率，一般情况下，是需要和cache联合使用
                执行过程中，会切断血缘关系。重新建立新的血缘关系， checkpoint等同于改变数据源
                可以查看源码，在doCheckpoint()中又启动了一个作业，
                但是如果cache成功，就不需要执行每个RDD算子获取数据了，直接从cache获取就可以了
     */


    // mapPartitions : 可以以分区为单位进行数据转换操作
    //                 但是会将整个分区的数据加载到内存进行引用
    //                 如果处理完的数据是不会被释放掉，存在对象的引用。
    //                 在内存较小，数据量较大的场合下，容易出现内存溢出。
    val mapPartitionsRdd: RDD[Int] = mapRdd.mapPartitions(
      iter => {
        println("mapPartitions >>>>>>>>>>")
        iter.map(_ * 2)
      }
    )

    // 原理：查看源码是通过其他函数来实现，具体为：map(x => (x, null)).reduceByKey((x, _) => x, numPartitions).map(_._1)
    val distinctRdd = mapPartitionsRdd.distinct()

    val filterRdd = distinctRdd.filter(_ > 0)

    // groupBy会将数据源中的每一个数据进行分组判断，根据返回的分组key进行分组
    // 相同的key值的数据会放置在一个组中
    def groupFunction(num: Int) = {
      num % 8
    }
    // 分组和分区没有必然的关系
    val groupByRdd: RDD[(Int, Iterable[Int])] = filterRdd.groupBy(groupFunction)

    // 查看上游依赖，每个RDD中都记录了该RDD的上游依赖
    println(groupByRdd.dependencies)
    // 查看血缘关系，通过记录的上游依赖拼接出完整的血缘关系
    println(groupByRdd.toDebugString)


    // collect : 方法会将不同分区的数据按照分区顺序采集到Driver端内存中，形成数组
    groupByRdd.collect().foreach(println)
    // foreach 其实是Executor端内存数据打印，
    // 这个foreach中的代码需要传递到Executor中，是在Executor段执行的，涉及网络传输，其中的类需要序列化
//    rdd.foreach(println)


    // 将处理的数据保存成分区文件, 每个并行度保存一个文件
    // 注意运行前需要确保输出文件夹不存在，存在会报FileAlreadyExistsException异常
//    listRdd.saveAsTextFile("D:\\idea_project\\study_skill\\file\\spark\\rdd_output")


    println("集合操作------------------------->")

    // 交集，并集和差集要求两个数据源数据类型保持一致
    // 拉链操作两个数据源的类型可以不一致
    val rdd1 = sc.makeRDD(List(1, 2, 3, 4))
    val rdd2 = sc.makeRDD(List(3, 4, 5, 6))

    // 交集 : 【3，4】
    val rdd3: RDD[Int] = rdd1.intersection(rdd2)
    //val rdd8 = rdd1.intersection(rdd7)
    println(rdd3.collect().mkString(","))

    // 并集 : 【1，2，3，4，3，4，5，6】
    val rdd4: RDD[Int] = rdd1.union(rdd2)
    println(rdd4.collect().mkString(","))

    // 差集 : 【1，2】
    val rdd5: RDD[Int] = rdd1.subtract(rdd2)
    println(rdd5.collect().mkString(","))

    // 拉链 : 【1-3，2-4，3-5，4-6】
    // 两个数据源要求分区数量要保持一致
    // 两个数据源要求分区中数据数量保持一致
    val rdd6: RDD[(Int, Int)] = rdd1.zip(rdd2)
    println(rdd6.collect().mkString(","))


    println("聚合操作------------------------->")

    // sortBy方法可以根据指定的规则对数据源中的数据进行排序，默认为升序，第二个参数可以改变排序的方式
    // sortBy默认情况下，不会改变分区。但是中间存在shuffle操作
    val tupleRdd = sc.makeRDD(List(("1", 1), ("11", 2), ("2", 3), ("2", 3)), 2)
    val sortByRdd = tupleRdd.sortBy(t => t._1.toInt, false)

    // RDD => PairRDDFunctions
    // 隐式转换（二次编译）
    // partitionBy根据指定的分区规则对数据进行重分区
    val partitionByRdd = sortByRdd.partitionBy(new HashPartitioner(2))
    partitionByRdd.partitionBy(new HashPartitioner(2))

    // reduceByKey : 相同的key的数据进行value数据的聚合操作
    // scala语言中一般的聚合操作都是两两聚合，spark基于scala开发的，所以它的聚合也是两两聚合
    // reduceByKey中如果key的数据只有一个，是不会参与运算的。
    val reduceByKeyRdd: RDD[(String, Int)] = partitionByRdd.reduceByKey((x: Int, y: Int) => {
      println(s"x = ${x}, y = ${y}")
      x + y
    })

    // groupByKey : 将数据源中的数据，相同key的数据分在一个组中，形成一个对偶元组
    //              元组中的第一个元素就是key，
    //              元组中的第二个元素就是相同key的value的集合
    val groupByKeyRdd: RDD[(String, Iterable[Int])] = reduceByKeyRdd.groupByKey()
    // 注意value的类型和groupByKey不一样
//    val groupByRdd: RDD[(String, Iterable[(String, Int)])] = reduceByKeyRdd.groupBy(_._1)


    val aggregateRdd = sc.makeRDD(List(
      ("a", 1), ("a", 2), ("b", 3),
      ("b", 4), ("b", 5), ("a", 6)
    ), 2)

    // aggregateByKey存在函数柯里化，有两个参数列表
    // 第一个参数列表,需要传递一个参数，表示为初始值
    //       主要用于当碰见第一个key的时候，和value进行分区内计算
    // 第二个参数列表需要传递2个参数
    //      第一个参数表示分区内计算规则
    //      第二个参数表示分区间计算规则
    aggregateRdd.aggregateByKey(5)(
      (x, y) => math.max(x, y),
      (x, y) => x + y
    ).collect.foreach(println)

    /*
           reduceByKey:

                combineByKeyWithClassTag[V](
                    (v: V) => v, // 第一个值不会参与计算
                    func, // 分区内计算规则
                    func, // 分区间计算规则
                    )

           aggregateByKey :

               combineByKeyWithClassTag[U](
                   (v: V) => cleanedSeqOp(createZero(), v), // 初始值和第一个key的value值进行的分区内数据操作
                   cleanedSeqOp, // 分区内计算规则
                   combOp,       // 分区间计算规则
                   )

           foldByKey:

               combineByKeyWithClassTag[V](
                   (v: V) => cleanedFunc(createZero(), v), // 初始值和第一个key的value值进行的分区内数据操作
                   cleanedFunc,  // 分区内计算规则
                   cleanedFunc,  // 分区间计算规则
                   )

           combineByKey :

               combineByKeyWithClassTag(
                   createCombiner,  // 相同key的第一条数据进行的处理函数
                   mergeValue,      // 表示分区内数据的处理函数
                   mergeCombiners,  // 表示分区间数据的处理函数
                   )
     */
//    aggregateRdd.reduceByKey(_ + _) // wordcount
//    aggregateRdd.aggregateByKey(0)(_ + _, _ + _) // wordcount
//    aggregateRdd.foldByKey(0)(_ + _) // wordcount
//    aggregateRdd.combineByKey(v => v, (x: Int, y) => x + y, (x: Int, y: Int) => x + y) // wordcount

    println("join操作------------------------->")
    val joinRdd1 = sc.makeRDD(List(
      ("a", 1), ("a", 2), ("c", 3)
    ))
    val joinRdd2 = sc.makeRDD(List(
      ("a", 5), ("c", 6), ("a", 4)
    ))

    // join : 两个不同数据源的数据，相同的key的value会连接在一起，形成元组
    //        如果两个数据源中key没有匹配上，那么数据不会出现在结果中
    //        如果两个数据源中key有多个相同的，会依次匹配，可能会出现笛卡尔乘积，数据量会几何性增长，会导致性能降低。
//    val joinRdd: RDD[(String, (Int, Int))] = joinRdd1.join(joinRdd2)
//    val leftJoinRdd = joinRdd1.leftOuterJoin(joinRdd2)
//    val rightJoinRdd = joinRdd1.rightOuterJoin(joinRdd2)

    // cogroup : connect + group (分组，连接)
    val cogroupRdd: RDD[(String, (Iterable[Int], Iterable[Int]))] = joinRdd1.cogroup(joinRdd2)
    cogroupRdd.collect().foreach(println)


    // 算子 ： Operator（操作）
    //         RDD的方法和Scala集合对象的方法不一样
    //         集合对象的方法都是在同一个节点的内存中完成的。
    //         RDD的方法可以将计算逻辑发送到Executor端（分布式节点）执行
    //         为了区分不同的处理效果，所以将RDD的方法称之为算子。
    //        RDD的方法外部的操作都是在Driver端执行的，而方法内部的逻辑代码是在Executor端执行。

    // 关闭环境
    sc.stop()
  }
}
