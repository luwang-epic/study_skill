package com.wang.spark.core.batch

import org.apache.spark.SparkConf
import org.apache.spark.sql.expressions.Aggregator
import org.apache.spark.sql.{DataFrame, Dataset, Encoder, Encoders, SparkSession, functions}

object SparkSqlSample {

  def main(args: Array[String]): Unit = {

    // 创建SparkSQL的运行环境
    val sparkConf = new SparkConf().setMaster("local[*]").setAppName("sparkSQL")
    val sparkSession = SparkSession.builder().config(sparkConf).getOrCreate()
    import sparkSession.implicits._


    // 执行逻辑操作
    // DataFrame
    val jsonDataFrame: DataFrame = sparkSession.read.json("D:\\idea_project\\study_skill\\file\\spark\\sql\\user.json")
    jsonDataFrame.show()

    // DataFrame => SQL
    jsonDataFrame.createOrReplaceTempView("user")
//    sparkSession.sql("select * from user").show
//    sparkSession.sql("select age, username from user").show
//    sparkSession.sql("select avg(age) from user").show

    // 自定义udf函数
    sparkSession.udf.register("prefixName", (name: String) => {
      "Name: " + name
    })
    // zhangsan  ==> Name: zhangsan
    sparkSession.sql("select age, prefixName(username) from user").show

    // 自定义udaf函数
    sparkSession.udf.register("ageAvg", functions.udaf(new MyAvgUDAF()))
    sparkSession.sql("select ageAvg(age) from user").show

    // DataFrame => DSL
    // 在使用DataFrame时，如果涉及到转换操作，需要引入转换规则
//    jsonDataFrame.select("age", "username").show
//    jsonDataFrame.select($"age" + 1).show
//    jsonDataFrame.select('age + 1).show

    // DataSet
    // DataFrame其实是特定泛型的DataSet, 看DataFrame内源码：type DataFrame = Dataset[Row]
//    val seq = Seq(1,2,3,4)
//    val dataset: Dataset[Int] = seq.toDS()
//    dataset.show()

    // RDD <=> DataFrame
    val userRdd = sparkSession.sparkContext.makeRDD(List((1, "zhangsan", 30), (2, "lisi", 40)))
    val userDataFrame: DataFrame = userRdd.toDF("id", "name", "age")
    println("DataFrame to RDD ---->")
    userDataFrame.rdd.collect().foreach(println)

    // DataFrame <=> DataSet
    val userDataset: Dataset[User] = userDataFrame.as[User]
    println("Dataset to DataFrame ---->")
    userDataset.toDF().show()
    // 显示执行计划，包括：逻辑计划，优化后的逻辑计划，物理计划

//    userDataset.explain("extended")

    // RDD <=> DataSet
    val userDatasetFromRdd: Dataset[User] = userRdd.map {
      case (id, name, age) => {
        User(id, name, age)
      }
    }.toDS()
    println("Dataset to rdd ---->")
    userDatasetFromRdd.rdd.collect().foreach(println)


    // 读取MySQL数据
    println("spark read data from mysql ====================")
    val mysqlDataFrame = sparkSession.read
      .format("jdbc")
      .option("url", "jdbc:mysql://localhost:3306/test")
      .option("driver", "com.mysql.jdbc.Driver")
      .option("user", "root")
      .option("password", "123456")
      .option("dbtable", "user")
      .load()
    mysqlDataFrame.show

    // 保存数据到mysql
//    mysqlDataFrame.write
//      .format("jdbc")
//      .option("url", "jdbc:mysql://localhost:3306/spark_sql")
//      .option("driver", "com.mysql.jdbc.Driver")
//      .option("user", "root")
//      .option("password", "123456")
//      .option("dbtable", "user")
//      .mode(SaveMode.Append)
//      .save()

    // 关闭环境
    sparkSession.close()
  }

  case class User( id:Int, name:String, age:Int )

  /*
       自定义聚合函数类：计算年龄的平均值
       1. 继承org.apache.spark.sql.expressions.Aggregator, 定义泛型
           IN : 输入的数据类型 Long
           BUF : 缓冲区的数据类型 Buff
           OUT : 输出的数据类型 Long
       2. 重写方法(6)
       */
  case class Buff(var total: Long, var count: Long)

  class MyAvgUDAF extends Aggregator[Long, Buff, Long] {
    // z & zero : 初始值或零值
    // 缓冲区的初始化
    override def zero: Buff = {
      Buff(0L, 0L)
    }

    // 根据输入的数据更新缓冲区的数据
    override def reduce(buff: Buff, in: Long): Buff = {
      buff.total = buff.total + in
      buff.count = buff.count + 1
      buff
    }

    // 合并缓冲区
    override def merge(buff1: Buff, buff2: Buff): Buff = {
      buff1.total = buff1.total + buff2.total
      buff1.count = buff1.count + buff2.count
      buff1
    }

    //计算结果
    override def finish(buff: Buff): Long = {
      buff.total / buff.count
    }

    // 缓冲区的编码操作
    override def bufferEncoder: Encoder[Buff] = Encoders.product

    // 输出的编码操作
    override def outputEncoder: Encoder[Long] = Encoders.scalaLong
  }
}
