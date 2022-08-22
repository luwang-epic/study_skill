package com.wang.spark.core.batch

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

/*
sc.textFile("D:\\idea_project\\study_skill\\file\\wc.text").flatMap(_.split(" ")).map((_,1)).reduceByKey(_+_).collect

.\bin\spark-submit --class org.apache.spark.examples.SparkPi --master local[2] ./examples/jars/spark-examples_2.12-3.0.0.jar 10

 */

object WordCountSample {

  def main(args: Array[String]): Unit = {

    // Application
    // Spark框架
    // 建立和Spark框架的连接
    // JDBC : Connection
    val sparConf = new SparkConf().setMaster("local").setAppName("WordCount")
    val sc = new SparkContext(sparConf)

    // 执行业务操作

    // 1. 读取文件，获取一行一行的数据
    //  path路径可以是文件的具体路径，也可以目录名称   path路径还可以使用通配符 *
    val lines: RDD[String] = sc.textFile("D:\\idea_project\\study_skill\\file\\wc.text")

    // 2. 将一行数据进行拆分，形成一个一个的单词（分词）
    //    扁平化：将整体拆分成个体的操作
    //   "hello world" => hello, world, hello, world
    val words: RDD[String] = lines.flatMap(_.split(" "))

    // 3. 将数据根据单词进行分组，便于统计
    //    (hello, hello, hello), (world, world)
    val wordGroup: RDD[(String, Iterable[String])] = words.groupBy(word=>word)

    // 4. 对分组后的数据进行转换
    //    (hello, hello, hello), (world, world)
    //    (hello, 3), (world, 2)
    val wordToCount = wordGroup.map {
      case ( word, list ) => {
        (word, list.size)
      }
    }

    // 5. 将转换结果采集到控制台打印出来
    val array: Array[(String, Int)] = wordToCount.collect()
    array.foreach(println)

    // 关闭连接
    sc.stop()

  }
}
