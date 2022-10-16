package com.wang.state;

import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.state.AggregatingState;
import org.apache.flink.api.common.state.AggregatingStateDescriptor;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.state.MapState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.state.ReducingState;
import org.apache.flink.api.common.state.ReducingStateDescriptor;
import org.apache.flink.api.common.state.StateTtlConfig;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.runtime.state.FunctionInitializationContext;
import org.apache.flink.runtime.state.FunctionSnapshotContext;
import org.apache.flink.streaming.api.checkpoint.CheckpointedFunction;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/*
在流处理中，数据是连续不断到来和处理的。每个任务进行计算处理时，可以基于当前数据直接转换得到输出结果；
也可以依赖一些其他数据。这些由一个任务维护，并且用来计算输出结果的所有数据，就叫作这个任务的状态。

在 Flink 中，算子任务可以分为无状态和有状态两种情况。
    如：map、filter、flatMap，计算时不依赖其他数据，就都属于无状态的算子。
    如：聚合算子、窗口算子都属于有状态的算子。
Flink的解决方案是，将状态直接保存在内存中来保证性能，并通过分布式扩展来提高吞吐量。
在低延迟、高吞吐的基础上还要保证容错性，一系列复杂的问题就会随之而来了。
    1. 状态的访问权限。我们知道Flink上的聚合和窗口操作，一般都是基于KeyedStream的，数据会按照key的哈希值进行分区，
        聚合处理的结果也应该是只对当前key有效。然而同一个分区（也就是slot）上执行的任务实例，
        可能会包含多个key的数据，它们同时访问和更改本地变量，就会导致计算结果错误。所以这时状态并不是单纯的本地变量。
    2. 容错性，也就是故障后的恢复。状态只保存在内存中显然是不够稳定的，我们需要将它持久化保存，
        做一个备份；在发生故障后可以从这个备份中恢复状态。
    3. 我们还应该考虑到分布式应用的横向扩展性。比如处理的数据量增大时，
        我们应该相应地对计算资源扩容，调大并行度。这时就涉及到了状态的重组调整。

Flink的状态有两种：托管状态（Managed State）和原始状态（Raw State）。
    1. 托管状态：由Flink 统一管理的，状态的存储访问、故障恢复和重组等一系列问题都由 Flink 实现，我们只要调接口就可以；
        Flink 也提供了值状态（ValueState）、列表状态（ListState）、映射状态（MapState）、归约状态（ReducingState）、
        聚合状态（AggregateState）等多种结构，内部支持各种数据类型。聚合、窗口等算子中内置的状态，就都是托管状态；
        我们也可以在富函数类（RichFunction）中通过上下文来自定义状态，这些也都是托管状态。

        托管状态分为两类：算子状态（一个分区（并行度）维护一个状态）和按键分区状态（一个key维护一个状态）。
            1. 算子状态作用范围限定为当前的算子任务实例，也就是只对当前并行子任务实例有效。
                列表状态（ListState）
                联合列表状态（UnionListState）
                广播状态（BroadcastState）
            2. 按键分区状态是根据输入流中定义的键（key）来维护和访问的，所以只能定义在按键分区流（KeyedStream）中，也就keyBy之后才可以使用
                值状态（ValueState）
                列表状态（ListState）
                映射状态（MapState）
                归约状态（ReducingState）
                聚合状态（AggregateState）
    2. 原始状态：自定义的，相当于就是开辟了一块内存，需要我们自己管理，实现状态的序列化和故障恢复。

即使是 map、filter 这样无状态的基本转换算子，我们也可以通过富函数类给它们“追加”Keyed State，
或者实现CheckpointedFunction 接口来定义 Operator State；从这个角度讲，Flink中所有的算子都可以是有状态的

配置一个状态的“生存时间”（time-to-live，TTL），当状态在内存中存在的时间超出这个值时，就将它清除。
设置 失效时间 = 当前时间 + TTL；之后如果有对状态的访问和修改，我们可以再对失效时间进行更新；
当设置的清除条件被触发时（比如，状态被访问的时候，或者每隔一段时间扫描一次失效状态），就可以判断状态是否失效、从而进行清除了。
可以通过StateTtlConfig类来配置状态的ttl


我们已经知道，状态从本质上来说就是算子并行子任务实例上的一个特殊本地变量。它的特殊之处就在于 Flink 会提供完整的管理机制，
来保证它的持久化保存，以便发生故障时进行状态恢复；另外还可以针对不同的 key 保存独立的状态实例。
按键分区状态（Keyed State）对这两个功能都要考虑；而算子状态（Operator State）并不考虑 key 的影响，
所以主要任务就是要让 Flink 了解状态的信息、将状态数据持久化后保存到外部存储空间。看起来算子状态的使用应该更加简单才对。
不过仔细思考又会发现一个问题：我们对状态进行持久化保存的目的是为了故障恢复；在发生故障、重启应用后，
数据还会被发往之前分配的分区吗？显然不是，因为并行度可能发生了调整，不论是按键（key）的哈希值分区，
还是直接轮询（round-robin）分区，数据分配到的分区都会发生变化。这很好理解，当打牌的人数从 3 个增加到 4 个时，
即使牌的次序不变，轮流发到每个人手里的牌也会不同。数据分区发生变化，带来的问题就是，怎么保证原先的状态跟故障恢复后数据的对应关系呢？
对于 Keyed State 这个问题很好解决：状态都是跟 key 相关的，而相同 key 的数据不管发往哪个分区，
总是会全部进入一个分区的；于是只要将状态也按照 key 的哈希值计算出对应的分区，进行重组分配就可以了。
恢复状态后继续处理数据，就总能按照 key 找到对应之前的状态，就保证了结果的一致性。
所以 Flink 对 Keyed State 进行了非常完善的包装，我们不需实现任何接口就可以直接使用。
而对于 Operator State 来说就会有所不同。因为不存在 key，所有数据发往哪个分区是不可预测的；
也就是说，当发生故障重启之后，我们不能保证某个数据跟之前一样，进入到同一个并行子任务、访问同一个状态。
所以 Flink 无法直接判断该怎样保存和恢复状态，而是提供了接口，让我们根据业务需求自行设计状态的快照保存（snapshot）和恢复（restore）逻辑。


 */

/**
 * 有状态的计算
 */
public class StateSample {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        DataStreamSource<String> dataStreamSource = env.fromCollection(
                Arrays.asList("java", "java", "java", "flink"));

        // 算子状态
        dataStreamSource.map(new OperatorStateMapper());

        // 注意观察结果的输出，会按照key来保存状态的
        dataStreamSource.keyBy(word -> word).map(new KeyStateMapper());

        env.execute();
    }

    /**
     * 算子状态
     */
    private static class OperatorStateMapper implements MapFunction<String, String>, CheckpointedFunction {
        private ListState<String> listState;

        @Override
        public String map(String value) throws Exception {
            listState.add(value);
            System.out.println("operator state ----> " + StreamSupport.stream(listState.get().spliterator(), false).collect(Collectors.toList()));
            System.out.println("----------------------------------------");
            return value;
        }

        @Override
        public void snapshotState(FunctionSnapshotContext context) throws Exception {
            System.out.println("---------snapshotState---------");
        }

        @Override
        public void initializeState(FunctionInitializationContext context) throws Exception {
            ListStateDescriptor<String> descriptor = new ListStateDescriptor<>("list-state", String.class);
            listState = context.getOperatorStateStore().getListState(descriptor);

            // 如果是从故障中恢复，就将ListState中的所有元素添加到局部变量中
            if (context.isRestored()) {
                System.out.println("-----------restore list state-------");
            } else {
                System.out.println("-----------init list state-------");
            }
        }
    }

    /**
     * 各种KeyState状态的使用
     */
    private static class KeyStateMapper extends RichMapFunction<String, Map<String, Long>> {
        //值状态（ValueState）, 保存总的单词个数（也是针对某一个key的）
        private ValueState<Long> valueState;
        // 列表状态（ListState），保存所有的单词
        private ListState<String> listState;
        // 映射状态（MapState），保存单词和个数的映射
        private MapState<String, Long> mapState;
        // 归约状态（ReducingState），将所有单词连接成字符串
        private ReducingState<String> reducingState;
        // 聚合状态（AggregateState）, 保存单词和个数的映射
        private AggregatingState<String, Map<String, Long>> aggregationState;


        /**
         * 这些state不能在类成员变量中初始化，因为context还没有生成，需要在open方法中获取
         * 这些state不能在open方法中使用，需要在map中使用，因为getRuntimeContext中的state在之后才会生效
         * @param parameters
         * @throws Exception
         */
        @Override
        public void open(Configuration parameters) throws Exception {
            ValueStateDescriptor<Long> valueStateDescriptor = new ValueStateDescriptor<>("value-state", Long.class);
            valueState = getRuntimeContext().getState(valueStateDescriptor);
            listState = getRuntimeContext().getListState(new ListStateDescriptor<>("list-state", String.class));
            mapState = getRuntimeContext().getMapState(new MapStateDescriptor<>("map-state", String.class, Long.class));

            reducingState = getRuntimeContext().getReducingState(new ReducingStateDescriptor<>("reducing-state", new ReduceFunction<String>() {
                @Override
                public String reduce(String value1, String value2) throws Exception {
                    return value1 + value2;
                }
            }, String.class));
            aggregationState = getRuntimeContext().getAggregatingState(new AggregatingStateDescriptor<String, Map<String, Long>, Map<String, Long>>("aggregating-state", new AggregateFunction<String, Map<String, Long>, Map<String, Long>>() {
                @Override
                public Map<String, Long> createAccumulator() {
                    return new HashMap<>();
                }

                @Override
                public Map<String, Long> add(String value, Map<String, Long> accumulator) {
                    if (accumulator.containsKey(value)) {
                        accumulator.put(value, accumulator.get(value) + 1);
                    } else {
                        accumulator.put(value, 1L);
                    }
                    return accumulator;
                }

                @Override
                public Map<String, Long> getResult(Map<String, Long> accumulator) {
                    return accumulator;
                }

                @Override
                public Map<String, Long> merge(Map<String, Long> a, Map<String, Long> b) {
                    return null;
                }
            }, Types.MAP(Types.STRING, Types.LONG)));


            // 配置状态的ttl
            StateTtlConfig ttlConfig = StateTtlConfig.newBuilder(Time.hours(1))
                    // 什么时候更新状态的失效时间，比如：访问是否更新，写是否更新，创建是否更新等，默认是OnCreateAndWrite
                    .setUpdateType(StateTtlConfig.UpdateType.OnReadAndWrite)
                    // 所谓的“状态可见性”，是指因为清除操作并不是实时的，所以当状态过期之后还有可能基于存在，
                    // 这时如果对它进行访问，能否正常读取到就是一个问题了，默认是NeverReturnExpired
                    .setStateVisibility(StateTtlConfig.StateVisibility.ReturnExpiredIfNotCleanedUp)
                    .build();
            // 给valueState设置ttl
            valueStateDescriptor.enableTimeToLive(ttlConfig);

            /*
            除此之外，TTL 配置还可以设置在保存检查点（checkpoint）时触发清除操作，或者配置增量的清理（incremental cleanup），
            还可以针对 RocksDB 状态后端使用压缩过滤器（compaction filter）进行后台清理。关于检查点和状态后端的内容，
            目前的 TTL 设置只支持处理时间。另外，所有集合类型的状态（例如ListState、MapState）在设置 TTL 时，
            都是针对每一项（per-entry）元素的。也就是说，一个列表状态中的每一个元素，
            都会以自己的失效时间来进行清理，而不是整个列表一起清理。
             */
        }

        @Override
        public Map<String, Long> map(String word) throws Exception {
            // 获取并更新单词个数
            Long count = valueState.value();
            if (Objects.isNull(count)) {
                count = 0L;
            }
            count++;
            valueState.update(count);

            // 更新listState值
            listState.add(word);

            // 获取并更新mapState值
            if (mapState.contains(word)) {
                mapState.put(word, mapState.get(word) + 1);
            } else {
                mapState.put(word, 1L);
            }

            // 更新reducingState值
            reducingState.add(word);

            // 更新aggregationState值
            aggregationState.add(word);

            System.out.println("value state ----> " + valueState.value()
                    + "\nlist  state ----> " + StreamSupport.stream(listState.get().spliterator(), false).collect(Collectors.toList())
                    + "\nmap state ----> " + StreamSupport.stream(mapState.entries().spliterator(), false).collect(Collectors.toList())
                    + "\nreduce state ----> " + reducingState.get()
                    + "\naggregation state ---->" + aggregationState.get());
            System.out.println("\n===============================================================\n");

            return aggregationState.get();
        }
    }
}
