package com.wang.spark.core.batch

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

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

    // 分区内以行为单位进行操作，来一条数据处理一条，类似于串行操作，效率比较低
    val mapRdd = listRdd.map(num => num * 2)

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

    mapPartitionsRdd.collect().foreach(println)

    // 将处理的数据保存成分区文件, 每个并行度保存一个文件
    // 注意运行前需要确保输出文件夹不存在，存在会报FileAlreadyExistsException异常
//    listRdd.saveAsTextFile("D:\\idea_project\\study_skill\\file\\spark\\rdd_output")

    // 关闭环境
    sc.stop()
  }
}
