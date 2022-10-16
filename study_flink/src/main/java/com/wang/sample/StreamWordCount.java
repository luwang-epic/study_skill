package com.wang.sample;

import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

/*

在具体的应用场景中，如何跟计算资源交互，对于集群资源分配和占用的方式，可能会有特定的需求。所以 Flink 为各种场景提供了不同的运行模式，主要有以下三种：
    1. 会话模式（Session Mode）
        会话模式需要先启动一个集群，保持一个会话，在这个会话中通过客户端提交作业。集群启动时所有资源就都已经确定，
        所以所有提交的作业会竞争集群中的资源。这种模式最符合常规思维，有个固定的资源，只要等着任务过来使用资源就可以了。

        会话模式比较适合于单个规模小、执行时间短的大量作业。
    2. 单作业模式（Per-Job Mode）
        既然会话模式可能会因为资源共享而导致很多问题，所以为了更好地隔离资源，我们可以考虑为每个提交的作业启动一个集群，
        这就是单作业（Per-Job）模式，这样作业之间就不会相互影响。单作业模式就是严格的一对一集群与作业，
        集群只为这个作业而生，由客户端运行应用程序，然后启动集群，作业被提交给JobManager，进而分发给TaskManager执行。
        作业作业完成后，集群就会关闭，所有资源也会释放。可以看出每个作业都有它自己的 JobManager 管理，
        占用独享的资源，即使发生故障，它的 TaskManager 宕机也不会影响其他作业。这些特性使得单作业模式在生产环境运行更加稳定，
        所以是实际应用的首选模式。但是Flink本身无法直接使用这样运行模式，
        所以单作业模式一般需要借助一些外部资源管理框架来启动集群，比如 YARN、Kubernetes
    3. 应用模式（Application Mode）
        应用模式为每个应用程序创建一个会话集群，在JobManager上直接调用应用程序的main()方法。


其实客户端并不是处理系统的一部分，它只负责作业的提交。具体来说，就是调用程序的main方法，将代码转换成“数据流图”（Dataflow Graph），
也被称为逻辑流图（logical StreamGraph），并最终生成作业图（JobGraph），一并发送给 JobManager。提交之后，任务的执行其实就跟客户端没有关系了；
我们可以在客户端选择断开与JobManager的连接, 当然，客户端可以随时连接到 JobManager，获取当前作业的状态和执行结果，也可以发送请求取消作业

JobManger 又包含 3 个不同的组件：
    1. JobMaster是JobManager中最核心的组件，负责处理单独的作业（Job）。所以 JobMaster和具体的Job是一一对应的，
        多个Job可以同时运行在一个Flink集群中, 每个Job都有一个自己的JobMaster。
        JobMaster会把JobGraph转换成一个物理层面的数据流图，这个图被叫作“执行图”（ExecutionGraph），它包含了所有可以并发执行的任务。
        JobMaster会向资源管理器（ResourceManager）发出请求，申请执行任务必要的资源。一旦它获取到了足够的资源，
        就会将执行图分发到真正运行它们的TaskManager上。
        而在运行过程中，JobMaster会负责所有需要中央协调的操作，比如说检查点（checkpoints）的协调。
    2. ResourceManager主要负责资源的分配和管理，在Flink集群中只有一个。所谓“资源”，主要是指TaskManager的任务槽（task slots）。
        任务槽就是Flink集群中的资源调配单元，包含了机器用来执行计算的一组CPU和内存资源。每一个任务（Task）都需要分配到一个slot上执行。
        Flink的ResourceManager，针对不同的环境和资源管理平台（比如Standalone部署，或者YARN），有不同的具体实现。
    3. Dispatcher主要负责提供一个REST接口，用来提交应用，并且负责为每一个新提交的作业启动一个新的JobMaster组件。
        Dispatcher 在架构中并不是必需的，在不同的部署模式下可能会被忽略掉

TaskManager 是Flink中的工作进程，数据流的具体计算就是它来做的，所以也被称为“Worker”。
    每一个TaskManager都包含了一定数量的任务槽（task slots）。Slot是资源调度的最小单位，
    slot的数量限制了TaskManager能够并行处理的任务数量
    在执行过程中，TaskManager可以缓冲数据，还可以跟其他运行同一应用的TaskManager交换数据。

所有的 Flink 程序都可以归纳为由三部分构成：Source、Transformation 和 Sink。
    Source 表示“源算子”，负责读取数据源。
    Transformation 表示“转换算子”，利用各种算子进行处理加工。
    Sink 表示“下沉算子”，负责数据的输出。

Flink 中任务调度执行的图，按照生成顺序可以分成四层：
    逻辑流图（StreamGraph）→ 作业图（JobGraph）→ 执行图（ExecutionGraph）→ 物理图（Physical Graph）。

要分布式地处理这些数据，就不可避免地要面对数据的网络传输、状态的落盘和故障恢复等问题，这就需要对数据进行序列化和反序列化。
为了方便地处理数据，Flink有自己一整套类型系统。Flink使用“类型信息”（TypeInformation）来统一表示数据类型。
TypeInformation类是Flink中所有类型描述符的基类。它涵盖了类型的一些基本属性，并为每个数据类型生成特定的序列化器、反序列化器和比较器。

 */

/**
 * stream模式的word count程序，
 *  通过本地socket读取数据
 *  命令行提交作业：./bin/flink.bat run -m localhost:8081 -c com.wang.sample.StreamWordCount -p 1 D:/idea_project/study_skill/study_flink/target/study_flink-1.0-SNAPSHOT.jar
 *      提交之后可以将客户端关闭，不影响作业的运行
 */
public class StreamWordCount {

    // 可以发现，除了数据流的来源不一样，其他和有界流都是一样的

    public static void main(String[] args) throws Exception {
        // 1. 创建流式执行环境
        StreamExecutionEnvironment streamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();

        /*
        所以在没有指定并行度的时候，就会采用配置文件中的集群默认并行度。
        在开发环境中，没有配置文件，默认并行度就是当前机器的 CPU 核心数。

        它们的优先级如下：
        （1）对于一个算子，首先看在代码中是否单独指定了它的并行度，这个特定的设置优先级最高，会覆盖后面所有的设置。
        （2）如果没有单独设置，那么采用当前代码中执行环境全局设置的并行度。
        （3）如果代码中完全没有设置，那么采用提交时-p 参数指定的并行度。
        （4）如果提交时也未指定-p 参数，那么采用集群配置文件中的默认并行度。

        这里需要说明的是，算子的并行度有时会受到自身具体实现的影响。比如之前我们用到的读取socket文本流的算子socketTextStream，
        它本身就是非并行的Source算子，所以无论怎么它在运行时的并行度都是1，但是可以对后面的map算子设置并行度，比如2等

        一般在代码中只针对算子设置并行度，不设置全局并行度，这样方便我们提交作业时进行动态扩容
         */
        //streamExecutionEnvironment.setParallelism(1);

        // 获取端口数据  打开一个控制台(在D:\java\local\netcat-win32-1.12目录)，输入nc.exe -lp 7777命令，然后启动程序，输入数据
        // 2. 从socket读取数据流
        DataStreamSource<String> lineDataStreamSource = streamExecutionEnvironment.socketTextStream("localhost", 7777);

        // 3. 转换数据格式
        SingleOutputStreamOperator<Tuple2<String, Long>> singleOutputStreamOperator = lineDataStreamSource.flatMap((String line, Collector<Tuple2<String, Long>> collector) -> {
                    String[] words = line.toLowerCase().split(" ");
                    for (String word : words) {
                        collector.collect(Tuple2.of(word, 1L));
                    }
                })
                // 当Lambda表达式使用 Java 泛型的时候, 由于泛型擦除的存在, 需要显示的声明类型信息
                .returns(Types.TUPLE(Types.STRING, Types.LONG)) // 内置了一些类型
                //.returns(new TypeHint<Tuple2<String, Long>>(){}) // 如果不是内置的类型，可以使用TypeHint类

                // 来设置当前算子的并行度
                .setParallelism(2);

        // 4. 分组
        // 这里要注意的是，由于keyBy不是算子Operator，所以无法对keyBy设置并行度。
        KeyedStream<Tuple2<String, Long>, String> wordAndOneKS = singleOutputStreamOperator.keyBy(t -> t.f0);

        // 5. 求和
        SingleOutputStreamOperator<Tuple2<String, Long>> result = wordAndOneKS.sum(1).setParallelism(1);
        // 6. 打印
        result.print();
        // 7. 执行
        streamExecutionEnvironment.execute();
    }

}
