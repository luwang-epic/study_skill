package com.wang.spark.core.batch

import org.apache.spark.broadcast.Broadcast
import org.apache.spark.util.AccumulatorV2
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.mutable

object AccumulatorSample {

  def main(args: Array[String]): Unit = {
    val sparConf = new SparkConf().setMaster("local").setAppName("Acc")
    val sc = new SparkContext(sparConf)

    val rdd = sc.makeRDD(List(1, 2, 3, 4))


    var sum = 0
    rdd.foreach(
      num => {
        sum += num
      }
    )
    // 不使用累加器, 由于是在不同线程（机器）执行，sum的结果不会返回，因此不会生效
    println("没有使用累加器的sum = " + sum)

    // 获取系统累加器
    // Spark默认就提供了简单数据聚合的累加器
    val sumAcc = sc.longAccumulator("sum")
    //sc.doubleAccumulator
    //sc.collectionAccumulator
    rdd.foreach(
      num => {
        // 使用累加器
        sumAcc.add(num)
      }
    )
    // 获取累加器的值
    println("使用累加器的sum = " + sumAcc.value)

    sumAcc.reset()
    val mapRdd = rdd.map(
      num => {
        // 使用累加器
        sumAcc.add(num)
        num
      }
    )

    // 获取累加器的值
    // 少加：转换算子中调用累加器，如果没有行动算子的话，那么不会执行
    println("行动算子之前---> sum = " + sumAcc.value)

    // 多加：转换算子中调用累加器，如果有多个行动算子的话，那么会多加
    mapRdd.collect()
    println("一个行动算子之后---> sum = " + sumAcc.value)

    mapRdd.collect()
    // 一般情况下，累加器会放置在行动算子进行操作
    println("多个行动算子之后---> sum = " + sumAcc.value)


    println("自定义累加器=========================")
    val wcRdd = sc.makeRDD(List("hello", "spark", "hello"))
    // 累加器 : WordCount
    // 创建累加器对象
    val wcAcc = new MyAccumulator()
    // 向Spark进行注册
    sc.register(wcAcc, "wordCountAcc")

    wcRdd.foreach(
      word => {
        // 数据的累加（使用累加器）
        wcAcc.add(word)
      }
    )
    // 获取累加器累加的结果
    println("自定义累加器结果 ---> " + wcAcc.value)


    println("广播变量==============================")
    val rdd1 = sc.makeRDD(List(
      ("a", 1), ("b", 2), ("c", 3)
    ))
    val map = mutable.Map(("a", 4), ("b", 5), ("c", 6))

    /*
      闭包数据，都是以Task为单位发送的，每个任务重包含闭包数据，一个Executor中可能有多个Task
      这样可能会导致一个Executor中包含大量重复数据，并且占用大量的内存

      Executor其实就是一个JVM，所以在启动时，会自动分配内存
      完全可以将任务中的闭包数据放置在Executor的内存中，达到共享的目的

      Spark中的广播变量可以将闭包的数据保存到Executor的内存中
      Spark中的广播变量不能够更改，是分布式共享只读变量
     */

    // 封装广播变量
    val bc: Broadcast[mutable.Map[String, Int]] = sc.broadcast(map)
    rdd1.map {
      case (w, c) => {
        // 方法广播变量
        val l: Int = bc.value.getOrElse(w, 0)
        (w, (c, l))
      }
    }.collect().foreach(println)


    sc.stop()
  }

  /*
       自定义数据累加器：WordCount

       1. 继承AccumulatorV2, 定义泛型
          IN : 累加器输入的数据类型 String
          OUT : 累加器返回的数据类型 mutable.Map[String, Long]

       2. 重写方法（6）
      */
  class MyAccumulator extends AccumulatorV2[String, mutable.Map[String, Long]] {

    private var wcMap = mutable.Map[String, Long]()

    // 判断是否初始状态
    override def isZero: Boolean = {
      wcMap.isEmpty
    }

    // 复制累加器
    override def copy(): AccumulatorV2[String, mutable.Map[String, Long]] = {
      val accumulator = new MyAccumulator()
      accumulator.merge(this)
      accumulator
    }

    // 重置累加器
    override def reset(): Unit = {
      wcMap.clear()
    }

    // 获取累加器需要计算的值
    override def add(word: String): Unit = {
      val newCnt = wcMap.getOrElse(word, 0L) + 1
      wcMap.update(word, newCnt)
    }

    // Driver合并多个累加器
    override def merge(other: AccumulatorV2[String, mutable.Map[String, Long]]): Unit = {
      val myMap = this.wcMap
      val otherMap = other.value
      otherMap.foreach {
        case (word, count) => {
          val newCount = myMap.getOrElse(word, 0L) + count
          myMap.update(word, newCount)
        }
      }
    }

    // 累加器结果
    override def value: mutable.Map[String, Long] = {
      wcMap
    }
  }
}
