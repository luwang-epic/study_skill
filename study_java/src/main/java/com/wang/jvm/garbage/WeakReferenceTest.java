package com.wang.jvm.garbage;

/*
弱引用也是用来描述那些非必需对象，只被弱引用关联的对象只能生存到下一次垃圾收集发生为止。
在系统 GC时，只要发现弱引用，不管系统堆空间使用是否充足，都会回收掉只被弱引用关联的对象。

但是，由于垃圾回收器的线程通常优先级很低，因此，并不一定能很快地发现持有弱引用的对象。
在这种情况下，弱引用对象可以存在较长的时间。
 */

import lombok.AllArgsConstructor;
import lombok.Data;

import java.lang.ref.WeakReference;

public class WeakReferenceTest {
    @Data
    @AllArgsConstructor
    public static class WeakObject {
        private Integer id;
        private String name;
    }

    public static void main(String[] args) {
        // 构造弱引用对象
        WeakReference<WeakObject> weakReference = new WeakReference<>(new WeakObject(1, "lisi"));
        // 从弱引用中重新获取对象
        System.out.println(weakReference.get());

        System.gc();
        // 不管当前内存空间是否足够，都会回收弱引用对象
        System.out.println("After GC: ");
        // 重新尝试从弱引用中获取对象
        System.out.println(weakReference.get());
    }
}
