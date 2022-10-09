package com.wang.jdk.concurrency;

/*
Thread和ThreadLocal的联系（看ThreadLocal源码也可以看出来）
    Thread和ThreadLocal是绑定的， ThreadLocal依赖于Thread去执行，
    Thread将需要隔离的数据存放到ThreadLocal(准确的讲是ThreadLocalMap)中, 来实现多线程处理。

为什么key要设置成弱引用：
    要知道，ThreadlocalMap是和线程绑定在一起的，如果这样线程没有被销毁，而我们又已经不会再某个threadlocal引用，
    那么key-value的键值对就会一直在map中存在，这对于程序来说，就出现了内存泄漏。
    为了避免这种情况，只要将key设置为弱引用，那么当发生GC的时候，就会自动将弱引用给清理掉，
    也就是说：假如某个用户A执行方法时产生了一份threadlocalA，然后在很长一段时间都用不到threadlocalA时，
    作为弱引用，它会在下次垃圾回收时被清理掉。
    而且ThreadLocalMap在内部的set，get和扩容时都会清理掉泄漏的Entry，内存泄漏完全没必要过于担心。

为什么value不设置成弱引用：
    因此value设置成弱引用，gc时会被回收掉，导致获取不到value了
    因此不适用某个value的时候，需要将其移除了，否则会造成内存泄漏

 */
public class ThreadLocalDemo {

    public static void main(String[] args) {
        ThreadLocal<String> threadLocal = new ThreadLocal<>();
        // 需要看源码是如何实现的
        threadLocal.set("aaa");
        System.out.println(threadLocal.get());
    }
}
