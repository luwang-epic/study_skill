package com.wang.jvm.garbage;

/*
在默认情况下，通过 System.gc() 者 Runtime.getRuntime().gc() 的调用，会显式触发 Full GC，
同时对老年代和新生代进行回收，尝试释放被丢弃对象占用的内存。

然而 System.gc() 调用附带一个免责声明，无法保证对垃圾收集器的调用。(不能确保立即生效)

JVM 实现者可以通过 System.gc() 调用来决定 JVM 的 GC 行为。
而一般情况下，垃圾回收应该是自动进行的，无须手动触发，否则就太过于麻烦了。
在一些特殊情况下，如我们正在编写一个性能基准，我们可以在运行之间调用 System.gc()

-XX:+PrintCommandLineFlags：查看命令行相关参数（包含使用的垃圾收集器）
-XX:+PrintGCDetails：打印gc情况
 */
public class SystemGCTest {

    public static void main(String[] args) {
        new SystemGCTest();
        // 提醒 JVM 进行垃圾回收，但是不确定是否立马执行gc，内部调用Runtime.getRuntime().gc();来实现
        System.gc();

        // 强制调用失去引用对象的finalize()方法
        //System.runFinalization();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        System.out.println("SystemGCTest 执行了 finalize方法");
    }

}