package com.wang.jvm.garbage;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.lang.ref.SoftReference;

/*
软引用是用来描述一些还有用，但非必需的对象。只被软引用关联着的对象，在系统将要发生内存溢出异常前，
会把这些对象列进回收范围之中进行第二次回收，如果这次回收还没有足够的内存，才会抛出内存溢出异常。

软引用通常用来实现内存敏感的缓存。比如：高速缓存就有用到软引用。如果还有空闲内存，
就可以暂时保留缓存，当内存不足时清理掉，这样就保证了使用缓存的同时，不会耗尽内存。
 */
public class SoftReferenceTest {
    @Data
    @AllArgsConstructor
    public static class SoftObject {
        private Integer id;
        private String name;
    }


    // 需要加上一些JVM参数：-Xms10m -Xmx10m
    public static void main(String[] args) {
        // 创建对象，建立软引用
        SoftReference<SoftObject> softReference = new SoftReference<>(new SoftObject(1, "zhangsan"));

        // 从软引用中重新获得对象
        System.out.println(softReference.get());

        System.gc();
        System.out.println("After GC: ");
        // 由于堆空间内存充足，所以不会回收软引用的可达对象
        System.out.println(softReference.get());

        try {
            // 让系统认为资源紧张了
            byte[] bytes = new byte[1024 *1024 * 7];
        } catch (Exception e) {
            // 在出现oom之前会进行垃圾回收，支持会将软引用对象回收掉，那么回收后将是null
            e.printStackTrace();
        } finally {
            // 再次从软引用中获取数据
            System.out.println(softReference.get()); // 在报OOM之前，垃圾回收器会回收软引用的可达对象
        }

    }
}
