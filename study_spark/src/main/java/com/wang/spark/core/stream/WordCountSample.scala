package com.wang.spark.core.stream

import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.{DStream, ReceiverInputDStream}
import org.apache.spark.streaming.{Seconds, StreamingContext}

object WordCountSample {

  def main(args: Array[String]): Unit = {
    val checkpointPath = "D:\\idea_project\\study_skill\\file\\spark\\stream_checkpoint";
    // 创建环境对象
    // StreamingContext创建时，需要传递两个参数
    // 第一个参数表示环境配置
    val sparkConf = new SparkConf().setMaster("local[*]").setAppName("stream word count")
    // 第二个参数表示批量处理的周期（采集周期）
    val streamingContext = new StreamingContext(sparkConf, Seconds(3))

    // 从检查点恢复数据， TODO 这个有点问题，待解决
//    val streamingContext = StreamingContext.getActiveOrCreate(checkpointPath, () => {
//      val sparkConf = new SparkConf().setMaster("local[*]").setAppName("SparkStreaming")
//      new StreamingContext(sparkConf, Seconds(3))
//    })
    // 设置检查点
    streamingContext.checkpoint(checkpointPath)

    // 逻辑处理

    // 获取端口数据  打开一个控制台(在D:\java\local\netcat-win32-1.12目录)，输入nc.exe -lp 9999命令，然后启动程序，输入数据
    // 无状态数据操作，只对当前的采集周期内的数据进行处理
    // 在某些场合下，需要保留数据统计结果（状态），实现数据的汇总
    // 使用有状态操作时，需要设定检查点路径
    val lines: ReceiverInputDStream[String] = streamingContext.socketTextStream("localhost", 9999)

    // transform方法可以将底层RDD获取到后进行操作
    // 1. DStream功能不完善
    // 2. 需要代码周期性的执行

    // Code : Driver端
//    val transformDStream: DStream[String] = lines.transform(
//      rdd => {
//        // Code : Driver端，（周期性执行）
//        rdd.map(
//          str => {
//            // Code : Executor端
//            str
//          }
//        )
//      }
//    )
//    // Code : Driver端
//    val mapDStream: DStream[String] = lines.map(
//      data => {
//        // Code : Executor端
//        data
//      }
//    )


    val words = lines.flatMap(_.split(" "))

    val wordToOne = words.map((_, 1))

    // 窗口的范围应该是采集周期的整数倍
    // 窗口可以滑动的，但是默认情况下，一个采集周期进行滑动
    // 这样的话，可能会出现重复数据的计算，为了避免这种情况，可以改变滑动的滑动（步长）
    val windowDStream: DStream[(String, Int)] = wordToOne.window(Seconds(9), Seconds(9))

    // reduceByKeyAndWindow : 当窗口范围比较大，但是滑动幅度比较小，那么可以采用增加数据和删除数据的方式
    // 无需重复计算，提升性能。
//    val reduceByKeyAndWindowDStream: DStream[(String, Int)] =
//    wordToOne.reduceByKeyAndWindow(
//      (x: Int, y: Int) => {
//        x + y
//      },
//      (x: Int, y: Int) => {
//        x - y
//      },
//      Seconds(9), Seconds(3))


    // 无状态的，所以每次只能对时间窗口内的数据进行count
    val wordToCount: DStream[(String, Int)] = windowDStream.reduceByKey(_ + _)
    wordToCount.print()

    // foreachRDD不会出现时间戳，需要小心使用，使用不当会创建多个RDD
//    wordToCount.foreachRDD(
//      rdd => {
//        rdd.collect().foreach(println)
//      }
//    )

    // updateStateByKey：根据key对数据的状态进行更新
    // 传递的参数中含有两个值
    // 第一个值表示相同的key的value数据
    // 第二个值表示缓存区相同key的value数据
//    val stateWordToCount = wordToOne.updateStateByKey(
//      (seq: Seq[Int], buff: Option[Int]) => {
//        val newCount = buff.getOrElse(0) + seq.sum
//        Option(newCount)
//      }
//    )
//    stateWordToCount.print()


    // 由于SparkStreaming采集器是长期执行的任务，所以不能直接关闭
    // 如果main方法执行完毕，应用程序也会自动结束。所以不能让main执行完毕
    //streamingContext.stop()
    // 1. 启动采集器
    streamingContext.start()
    // 2. 等待采集器的关闭
    streamingContext.awaitTermination()
  }

}
