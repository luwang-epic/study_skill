package com.wang.jvm.garbage;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;

/*
一个对象是否有虚引用的存在，完全不会决定对象的生命周期。如果一个对象仅持有虚引用，
那么它和没有引用几乎是一样的，随时都可能被垃圾回收器回收

它不能单独使用，也无法通过虚引用来获取被引用的对象。当试图通过虚引用的 get() 方法取得对象时，总是 null

为一个对象设置虚引用关联的唯一目的在于跟踪垃圾回收过程。比如：能在这个对象被收集器回收时收到一个系统通知

虚引用必须和引用队列一起使用。虚引用在创建时必须提供一个引用队列作为参数。
当垃圾回收器准备回收一个对象时，如果发现它还有虚引用，就会在回收对象后，
将这个虚引用加入引用队列，以通知应用程序对象的回收情况
 */
public class PhantomReferenceTest {
    // 当前类对象的声明
    public static PhantomReferenceTest obj;
    // 引用队列
    static ReferenceQueue<PhantomReferenceTest> phantomQueue = null;

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        System.out.println("调用当前类的finalize方法");
        obj = this;
    }

    /*
    从上述运行结果我们知道，第一次尝试获取虚引用的值，发现无法获取的，这是因为虚引用是无法直接获取对象的值，
    然后进行第一次 GC，因为会调用 finalize() 方法，将对象复活了，
    所以对象没有被回收，但是调用第二次 GC 操作的时候，因为 finalize() 方法只能执行一次，
    所以就触发了 GC 操作，将对象回收了，同时将会触发第二个操作就是 将回收的值存入到引用队列中。
     */
    public static void main(String[] args) {
        Thread thread = new Thread(() -> {
            while(true) {
                if (phantomQueue != null) {
                    PhantomReference<PhantomReferenceTest> objt = null;
                    try {
                        objt = (PhantomReference<PhantomReferenceTest>) phantomQueue.remove();
                    } catch (Exception e) {
                        e.getStackTrace();
                    }
                    if (objt != null) {
                        System.out.println("追踪垃圾回收过程：PhantomReferenceTest实例被GC了");
                    }
                }
            }
        }, "t1");
        thread.setDaemon(true);
        thread.start();

        phantomQueue = new ReferenceQueue<>();
        obj = new PhantomReferenceTest();
        // 构造了PhantomReferenceTest对象的虚引用，并指定了引用队列
        PhantomReference<PhantomReferenceTest> phantomReference = new PhantomReference<>(obj, phantomQueue);
        try {
            System.out.println(phantomReference.get());
            // 去除强引用
            obj = null;
            // 第一次进行GC，由于对象可复活，GC无法回收该对象
            System.out.println("第一次GC操作");
            System.gc();
            Thread.sleep(1000);
            if (obj == null) {
                System.out.println("obj 是 null");
            } else {
                System.out.println("obj 不是 null");
            }
            System.out.println("第二次GC操作");
            obj = null;
            System.gc();
            Thread.sleep(1000);
            if (obj == null) {
                System.out.println("obj 是 null");
            } else {
                System.out.println("obj 不是 null");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
    }
}
