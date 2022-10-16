package com.wang.source.function;

import com.wang.pojo.Event;
import org.apache.flink.streaming.api.functions.source.ParallelSourceFunction;
import org.apache.flink.streaming.api.watermark.Watermark;

import java.util.Calendar;
import java.util.Random;

/**
 * 自定义并行的数据源
 */
public class CustomParallelSource implements ParallelSourceFunction<Event> {
    private boolean running = true;
    private Random random = new Random();

    @Override
    public void run(SourceContext<Event> sourceContext) throws Exception {
        while (running) {
            // 毫秒时间戳
            long currentTime = Calendar.getInstance().getTimeInMillis();
            Event event = new Event("bob" + random.nextInt(10),"/url/" + random.nextInt(5), currentTime);

            // 使用 collectWithTimestamp 方法将数据发送出去，并指明数据中的时间戳的字段
            sourceContext.collectWithTimestamp(event, event.getTimestamp());
            // 发送水位线
            sourceContext.emitWatermark(new Watermark(event.getTimestamp() - 1L));

            Thread.sleep(1000);
        }
    }

    @Override
    public void cancel() {
        running = false;
    }
}
