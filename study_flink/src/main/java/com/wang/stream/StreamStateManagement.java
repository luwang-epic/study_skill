package com.wang.stream;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.state.*;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.checkpoint.ListCheckpointed;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class StreamStateManagement {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SensorData {
        private String id;
        private Long timestamp;
        private Double temperature;
    }

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        DataStreamSource<String> inputStream = env.readTextFile("file:///D:\\idea_project\\study_skill\\file\\sensor.txt");
        SingleOutputStreamOperator<SensorData> dataStream = inputStream.map(line -> {
            String[] fields = line.split(",");
            return new SensorData(fields[0].trim(), new Long(fields[1].trim()), new Double(fields[2].trim()));
        });

        // 定义一个有状态的map操作，统计当前传感器分区数据个数
//        SingleOutputStreamOperator<Integer> resultSteam = dataStream.map(new SensorCountMapper());
        SingleOutputStreamOperator<Integer> resultSteam = dataStream.keyBy("id").map(new KeyStateSensorCountMapper());


        resultSteam.print();
        env.execute("sensor count");
    }

    public static class SensorCountMapper implements MapFunction<SensorData, Integer>, ListCheckpointed<Integer> {
        // 定义一个本地状态，作为算子状态
        private Integer count = 0;

        @Override
        public Integer map(SensorData sensorData) throws Exception {
            count++;
            return count;
        }

        /**
         * 堆状态左快照
         * @param l
         * @param l1
         * @return
         * @throws Exception
         */
        @Override
        public List<Integer> snapshotState(long l, long l1) throws Exception {
            return Collections.singletonList(count);
        }

        /**
         * 发送故障时，从快照修复
         * @param list
         * @throws Exception
         */
        @Override
        public void restoreState(List<Integer> list) throws Exception {
            for (Integer num : list) {
                count += num;
            }
        }
    }

    /**
     * 自定义保护KeyState状态管理的类
     */
    public static class KeyStateSensorCountMapper extends RichMapFunction<SensorData, Integer> {
        private ValueState<Integer> valueState;
        private ListState<String> listState;
        private MapState<String, Double> mapState;
        private ReducingState<SensorData> reducingState;


        /**
         * 这些state不能在open方法中使用，需要在map中使用，因为getRuntimeContext中的state在之后才会生效
         * @param parameters
         * @throws Exception
         */
        @Override
        public void open(Configuration parameters) throws Exception {
            valueState = getRuntimeContext().getState(new ValueStateDescriptor<Integer>("value-state", Integer.class));
            listState = getRuntimeContext().getListState(new ListStateDescriptor<String>("list-state", String.class));
            mapState = getRuntimeContext().getMapState(new MapStateDescriptor<String, Double>("map-state", String.class, Double.class));
            reducingState = getRuntimeContext().getReducingState(new ReducingStateDescriptor<SensorData>("reducing-state", new ReduceFunction<SensorData>() {
                @Override
                public SensorData reduce(SensorData sensorData1, SensorData sensorData2) throws Exception {
                    return sensorData1;
                }
            }, SensorData.class));
        }

        @Override
        public Integer map(SensorData sensorData) throws Exception {
            Integer count = valueState.value();
            if (Objects.isNull(count)) {
                count = 0;
            }
            count++;
            valueState.update(count);

            listState.add(sensorData.getId());

            if (mapState.contains(sensorData.getId())) {
                mapState.put(sensorData.getId(), mapState.get(sensorData.getId()) + sensorData.getTemperature());
            } else {
                mapState.put(sensorData.getId(), sensorData.getTemperature());
            }

            reducingState.add(sensorData);


            System.out.println("state ---->" + valueState.value() + "  " + printIterable(listState.get()) + "  "
                    + printIterable(mapState.keys()) + " : " + printIterable(mapState.values()) + "   " + reducingState.get());

            return count;
        }

        private String printIterable(Iterable<?> iterable) {
            StringBuffer sb = new StringBuffer();
            iterable.forEach(data -> sb.append(data).append("-"));
            return sb.toString();
        }
    }
}
