package com.wang.jvm.performance;

import java.util.ArrayList;

/**
 * 用于生成GC log日志，方便用软件进行分析
 *  这个程序运行一段时间后会发送堆的OOM
 */
public class HeapOomGcLogSample {

    // 运行时需要增加如下参数：
    // -Xms60m -Xmx60m -XX:SurvivorRatio=8 -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintHeapAtGC -Xloggc:D:/idea_project/study_skill/file/java/jvm/HeapOomGcLogSample.log
    public static void main(String[] args) {
        ArrayList<byte[]> list = new ArrayList<>();

        for (int i = 0; i < 5000; i++) {
            byte[] arr = new byte[1024 * 50]; // 50KB
            list.add(arr);

            try {
                Thread.sleep(30);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
