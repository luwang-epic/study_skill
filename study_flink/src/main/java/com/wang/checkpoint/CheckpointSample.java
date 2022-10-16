package com.wang.checkpoint;

import com.wang.pojo.Event;
import com.wang.source.function.CustomSource;
import org.apache.flink.runtime.state.hashmap.HashMapStateBackend;
import org.apache.flink.runtime.state.storage.FileSystemCheckpointStorage;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;


/*
在 Flink 的状态管理机制中，很重要的一个功能就是对状态进行持久化（persistence）保存，
这样就可以在发生故障后进行重启恢复。Flink 对状态进行持久化的方式，就是将当前所有分布式状态进行“快照”保存，
写入一个“检查点”（checkpoint）或者保存点（savepoint）保存到外部存储系统中。
具体的存储介质，一般是分布式文件系统（distributed file system）。

如果保存检查点之后又处理了一些数据，然后发生了故障，那么重启恢复状态之后这些数据带来的状态改变会丢失。
为了让最终处理结果正确，我们还需要让源（Source）算子重新读取这些数据，再次处理一遍。
这就需要流的数据源具有“数据重放”的能力，一个典型的例子就是 Kafka，我们可以通过保存消费数据的偏移量、
故障重启后重新提交来实现数据的重放。这是对“至少一次”（at least once）状态一致性的保证，
如果希望实现“精确一次”（exactly once）的一致性，还需要数据写入外部系统时的相关保证。

默认情况下，检查点是被禁用的，需要在代码中手动开启。直接调用执行环境的.enableCheckpointing()方法就可以开启检查点

在Flink中，状态的存储、访问以及维护，都是由一个可插拔的组件决定的，这个组件就叫作状态后端（state backend）。
状态后端主要负责两件事：一是本地的状态管理，二是将检查点（checkpoint）写入远程的持久化存储。

Flink 中提供了两类不同的状态后端，一种是“哈希表状态后端”（HashMapStateBackend），系统默认的状态后端;
另一种是“内嵌 RocksDB 状态后端”（EmbeddedRocksDBStateBackend）。如果没有特别配置，
两种状态后端最大的区别，就在于本地状态存放在哪里：前者是内存，后者是RocksDB(硬盘存储)。
需要注意状态存放在哪里和检查点没有关系，检查点可以认为是状态的备份，是需要单独保存的，具体配置来决定

HashMapStateBackend 是内存计算，读写速度非常快；但是，状态的大小会受到集群可用内存的限制，
如果应用的状态随着时间不停地增长，就会耗尽内存资源。而 RocksDB 是硬盘存储，所以可以根据可用的磁盘空间进行扩展，
而且是唯一支持增量检查点的状态后端，所以它非常适合于超级海量状态的存储。不过由于每个状态的读写都需要做序列化/反序列化，
而且可能需要直接从磁盘读取数据，这就会导致性能的降低，平均读写性能要比HashMapStateBackend慢一个数量级。

检查点是Flink容错机制的核心。

当所有任务都恰好处理完一个相同的输入数据的时候，将它们的状态保存下来。
首先，这样避免了除状态之外其他额外信息的存储，提高了检查点保存的效率。
其次，一个数据要么就是被所有任务完整地处理完，状态得到了保存；要么就是没处理完，状态全部没保存：
这就相当于构建了一个“事务”（transaction）。如果出现故障，我们恢复到之前保存的状态，故障时正在处理的所有数据都需要重新处理；
所以我们只需要让源（source）任务向数据源重新提交偏移量、请求重放数据就可以了。
这需要源任务可以把偏移量作为算子状态保存下来，而且外部数据源能够重置偏移量；Kafka 就是满足这些要求的一个最好的例子

这里我们也可以发现，想要正确地从检查点中读取并恢复状态，必须知道每个算子任务状态的类型和它们的先后顺序（拓扑结构）；
因此为了可以从之前的检查点中恢复状态，我们在改动程序、修复 bug 时要保证状态的拓扑顺序和类型不变。
状态的拓扑结构在JobManager上可以由 JobGraph 分析得到，而检查点保存的定期触发也是由JobManager控制的；
所以故障恢复的过程需要JobManager的参与。

在不暂停整体流处理的前提下，将状态备份保存到检查点。在Flink中，采用了基于 Chandy-Lamport算法的分布式快照
    我们可以借鉴水位线（watermark）的设计，在数据流中插入一个特殊的数据结构，专门用来表示触发检查点保存的时间点。
    收到保存检查点的指令后，Source 任务可以在当前数据流中插入这个结构；之后的所有任务只要遇到它就开始对状态做持久化快照保存。
    由于数据流是保持顺序依次处理的，因此遇到这个标识就代表之前的数据都处理完了，可以保存一个检查点；
    而在它之后的数据，引起的状态改变就不会体现在这个检查点中，而需要保存到下一个检查点。

    这种特殊的数据形式，把一条流上的数据按照不同的检查点分隔开，所以就叫作检查点的“分界线”（Checkpoint Barrier）
    在JobManager中有一个“检查点协调器”（checkpoint coordinator），专门用来协调处理检查点的相关工作。

    当上游任务向多个并行下游任务发送barrier时，需要广播出去；而当多个上游任务向同一个下游任务传递barrier时，
    需要在下游任务执行“分界线对齐”（barrier alignment）操作，也就是需要等到所有并行分区的barrier都到齐，才可以开始状态的保存。

    完成检查点保存之后，任务就可以继续正常处理数据了。
    这时如果有等待分界线对齐时缓存的数据，需要先做处理；然后再按照顺序依次处理新到的数据。

    由于分界线对齐要求先到达的分区做缓存等待，一定程度上会影响处理的速度；
    当出现背压（backpressure）时，下游任务会堆积大量的缓冲数据，检查点可能需要很久才可以保存完毕。
    为了应对这种场景，Flink 1.11 之后提供了不对齐的检查点保存方式，可以将未处理的缓冲数据（in-flight data）也保存进检查点。
    这样，当我们遇到一个分区barrier时就不需等待对齐，而是可以直接启动状态的保存了。


保存点与检查点最大的区别，就是触发的时机。检查点是由 Flink 自动管理的，定期创建，发生故障之后自动读取进行恢复，
这是一个“自动存盘”的功能；而保存点不会自动创建，必须由用户明确地手动触发保存操作，所以就是“手动存盘”。
因此两者尽管原理一致，但用途就有所差别了：检查点主要用来做故障恢复，是容错机制的核心；
保存点则更加灵活，可以用来做有计划的手动备份和恢复，如：升级应用程序，更新flink版本等，主要用于运维。

我们通过检查点的保存来保证状态恢复后结果的正确，所以主要讨论的就是“状态的一致性”。状态一致性有三种级别：
    最多一次（AT-MOST-ONCE）
    至少一次（AT-LEAST-ONCE）
    精确一次（EXACTLY-ONCE）

完整的流处理应用，应该包括了数据源、流处理器和外部存储系统三个部分。这个完整应用的一致性，
就叫作“端到端（end-to-end）的状态一致性”，它取决于三个组件中最弱的那一环。
一般来说，能否达到 at-least-once 一致性级别，主要看数据源能够重放数据；
而能否达到 exactly-once 级别，流处理器内部、数据源、外部存储都要有相应的保证机制。

对于 Flink 内部来说，检查点机制可以保证故障恢复后数据不丢（在能够重放的前提下），并且只处理一次，所以已经可以做到 exactly-once 的一致性语义了。
所以，端到端一致性的关键点，就在于输入的数据源端和输出的外部存储端
    数据源可重放数据，或者说可重置读取数据偏移量，加上 Flink 的 Source 算子将偏移量作为状态保存进检查点，就可以保证数据不丢。
    这是达到 at-least-once 一致性语义的基本要求，当然也是实现端到端 exactly-once 的基本要求。

    为了实现端到端 exactly-once，我们还需要对外部存储系统、以及 Sink 连接器有额外的要求。
    能够保证 exactly-once 一致性的写入方式有两种：
        幂等写入: 所谓“幂等”操作，就是说一个操作可以重复执行很多次，但只导致一次结果更改
        事务写入: 如果说幂等写入对应用场景限制太多，那么事务写入可以说是更一般化的保证一致性的方式。
            在 Flink 流处理的结果写入外部系统时，如果能够构建一个事务，让写入操作可以随着检查点来提交和回滚，
            那么自然就可以解决重复写入的问题了。所以事务写入的基本思想就是：用一个事务来进行数据向外部系统的写入，
            这个事务是与检查点绑定在一起的。当 Sink 任务遇到 barrier 时，开始保存状态的同时就开启一个事务，
            接下来所有数据的写入都在这个事务中；待到当前检查点保存完毕时，将事务提交，所有写入的数据就真正可用了。
            如果中间过程出现故障，状态会回退到上一个检查点，而当前事务没有正常关闭（因为当前检查点没有保存完），
            所以也会回滚，写入到外部的数据就被撤销了。具体来说，又有两种实现方式：预写日志（WAL）和两阶段提交（2PC）

        预写日志（WAL）就是一种非常简单的方式。具体步骤是：
            1. 先把结果数据作为日志（log）状态保存起来
            2. 进行检查点保存时，也会将这些结果数据一并做持久化存储
            3. 在收到检查点完成的通知时，将所有结果一次性写入外部系统。
        我们会发现，这种方式类似于检查点完成时做一个批处理，一次性的写入会带来一些性能上的问题；而优点就是比较简单，
        由于数据提前在状态后端中做了缓存，所以无论什么外部存储系统，理论上都能用这种方式一批搞定。
        在 Flink 中 DataStream API 提供了一个模板类GenericWriteAheadSink，用来实现这种事务型的写入方式。
        但是这种方式只能保存至少一次语义，不能保证精确一次语义

        两阶段提交（two-phase-commit，2PC）：先做“预提交”，等检查点完成之后再正式提交。
            1. 当第一条数据到来时，或者收到检查点的分界线时，Sink 任务都会启动一个事务。
            2. 接下来接收到的所有数据，都通过这个事务写入外部系统；这时由于事务没有提交，
                所以数据尽管写入了外部系统，但是不可用，是“预提交”的状态。
            3. 当 Sink 任务收到 JobManager 发来检查点完成的通知时，正式提交事务，写入的结果就真正可用了。
        当中间发生故障时，当前未提交的事务就会回滚，于是所有写入外部系统的数据也就实现了撤回。
        这种两阶段提交（2PC）的方式充分利用了 Flink 现有的检查点机制：分界线的到来，就标志着开始一个新事务；
        而收到来自 JobManager 的 checkpoint 成功的消息，就是提交事务的指令。每个结果数据的写入，依然是流式的，
        不再有预写日志时批处理的性能问题；最终提交时，也只需要额外发送一个确认信息。
        所以2PC协议不仅真正意义上实现了 exactly-once，而且通过搭载 Flink 的检查点机制来实现事务，只给系统增加了很少的开销。
        不过两阶段提交虽然精巧，却对外部系统有很高的要求。这里将 2PC 对外部系统的要求,列举如下：
            1. 外部系统必须提供事务支持，或者 Sink 任务必须能够模拟外部系统上的事务。
            2. 在检查点的间隔期间里，必须能够开启一个事务并接受数据写入。
            3. 在收到检查点完成的通知之前，事务必须是“等待提交”的状态。在故障恢复的情况下，
                这可能需要一些时间。如果这个时候外部系统关闭事务（例如超时了），那么未提交的数据就会丢失。
            4. Sink 任务必须能够在进程失败后恢复事务。
            5. 提交事务必须是幂等操作。也就是说，事务的重复提交应该是无效的。
        Flink提供了TwoPhaseCommitSinkFunction 接口，方便我们自定义实现两阶段提交的SinkFunction的实现，提供了真正端到端的 exactly-once 保证。

不同的source和sink的端到端一致性语义（需要开启checkpoint功能）：
                     source不可重置             source可重置

sink任意              at-most-once            at-most-once

sink幂等              at-most-once            exactly-once（故障恢复时会出现暂时的不一致）

sink预写日子（WAL)     at-most-once            at-least-once

sink两阶段提交         at-most-once            exactly-once



 */
/**
 * 检查点
 */
public class CheckpointSample {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        // 设置状态后端，默认是：HashMapStateBackend
        env.setStateBackend(new HashMapStateBackend());

        // 开启检查点
        env.enableCheckpointing(1000);
        CheckpointConfig checkpointConfig = env.getCheckpointConfig();
        // 配置存储检查点到 JobManager 堆内存, 默认策略
        //checkpointConfig.setCheckpointStorage(new JobManagerCheckpointStorage());
        // 配置存储检查点到文件系统
        checkpointConfig.setCheckpointStorage(new FileSystemCheckpointStorage("file:///D:/idea_project/study_skill/file/flink/checkpoint"));
        // 超过这个时间，就放弃保存这个检查点
        checkpointConfig.setCheckpointTimeout(60000L);
        // 下一个检查点至少要和前一个检查点完成后间隔多少时间
        checkpointConfig.setMinPauseBetweenCheckpoints(500L);
        // 同一时间只能保存多个个检查点（当不设置MinPauseBetweenCheckpoints的时候可能会出现同时保存多个检查点的情况）
        checkpointConfig.setMaxConcurrentCheckpoints(1);
        // 不再执行检查点的分界线对齐操作，启用之后可以大大减少产生背压时的检查点保存时间。
        // 这个设置要求检查点模式（CheckpointingMode）必须为 exactly-once，并且并发的检查点个数为 1。
        checkpointConfig.enableUnalignedCheckpoints();
        // 用于开启检查点的外部持久化，而且默认在作业失败的时候不会自动清理，如果想释放空间需要自己手工清理。
        checkpointConfig.enableExternalizedCheckpoints(CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);
        // 检查点异常多少次时，让整个任务失败，默认失败就停止作业
        checkpointConfig.setTolerableCheckpointFailureNumber(0);

        // 设置检查点一致性的保证级别，有“精确一次”（exactly-once）和“至少一次”（at-least-once）两个选项。
        // 默认级别为 exactly-once，而对于大多数低延迟的流处理程序，at-least-once 就够用了，而且处理效率会更高。
        checkpointConfig.setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE);

        DataStreamSource<Event> dataStreamSource = env.addSource(new CustomSource());

        dataStreamSource.print("source");

        env.execute();
    }
}
