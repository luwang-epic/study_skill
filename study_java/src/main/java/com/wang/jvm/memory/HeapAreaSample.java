package com.wang.jvm.memory;

import lombok.AllArgsConstructor;
import org.junit.jupiter.api.Test;

/*
“-Xms" 用于表示堆区的起始内存，等价于 -XX:InitialHeapSize
“-Xmx" 则用于表示堆区的最大内存，等价于 -XX:MaxHeapSize
默认-XX:NewRatio=2，表示新生代占1，老年代占2，新生代占整个堆的1/3
Eden 空间和另外两个 Survivor 空间缺省所占的比例是8 : 1 : 1;
由于存在自适应机制即-XX:-UseAdaptiveSizePolicy(+启用，-禁用)，实际当中不一定是8:1:1的比例，
但UseAdaptiveSizePolicy这种方法一般不能生效，所以一般采用-XX:SurvivorRatio=8来设置比例为8:1:1

特别注意，在Eden区满了的时候，才会触发Minor GC，而Survivor区满了后，不会触发Minor GC操作
如果Survivor区满了后，将会触发一些特殊的规则，也就是可能直接晋升老年代

针对幸存者 S0，S1 区的总结：复制之后有交换，谁空谁是 To
关于垃圾回收：频繁在新生区收集，很少在老年代收集，几乎不再永久代和元空间进行收集
新生代采用复制算法的目的：是为了减少内碎片

其实不分代完全可以，分代的唯一理由就是优化GC性能。如果没有分代，那所有的对象都在一块，GC的时候要找到哪些对象没用，
这样就会对堆的所有区域进行扫描。而很多对象都是朝生夕死的，如果分代的话，把新创建的对象放到某一地方，
当GC的时候先把这块存储“朝生夕死”对象的区域进行回收，这样就会腾出很大的空间出来。

TLAB：Thread Local Allocation Buffer，也就是为每个线程单独分配了一个缓冲区
    由于对象实例的创建在 JVM 中非常频繁，因此在并发环境下从堆区中划分内存空间是线程不安全的
    为避免多个线程操作同一地址，需要使用加锁等机制，进而影响分配速度。
从内存模型而不是垃圾收集的角度，对Eden区域继续进行划分，JVM为每个线程分配了一个私有缓存区域，它包含在Eden空间内。
    多线程同时分配内存时，使用TLAB可以避免一系列的非线程安全问题，
    同时还能够提升内存分配的吞吐量，因此我们可以将这种内存分配方式称之为快速分配策略。
尽管不是所有的对象实例都能够在 TLAB 中成功分配内存，但JVM确实是将TLAB作为内存分配的首选。
在程序中，开发人员可以通过选项“-XX:UseTLAB”设置是否开启 TLAB 空间。
默认情况下，TLAB 空间的内存非常小，仅占有整个 Eden 空间的1%，
当然我们可以通过选项“-XX:TLABWasteTargetPercent”设置TLAB空间所占用Eden空间的百分比大小。
一旦对象在TLAB空间分配内存失败时，JVM就会尝试着通过使用加锁机制确保数据操作的原子性，从而直接在Eden空间中分配内存。

随着JIT编译期的发展与逃逸分析技术逐渐成熟，栈上分配、标量替换优化技术将会导致一些微妙的变化，
所有的对象都分配到堆上也渐渐变得不那么“绝对”了，有些特殊情况下，对象不会被分配到堆上了
    1. 如果经过逃逸分析（Escape Analysis）后发现，一个对象并没有逃逸出方法的话，那么就可能被优化成栈上分配。
        分配完成后（一般栈上分配时间也会更快一点），继续在调用栈内执行，最后线程结束，栈空间被回收，局部变量对象也被回收。
        这样就无须进行垃圾回收了。这也是最常见的堆外存储技术。
    2. 此外，前面提到的基于OpenJDK深度定制的TaoBao VM ，其中创新的GCIH（GC Invisible Heap）技术实现Off-Heap，
        将生命周期较长的Java对象从Heap中移至Heap外，并且GC不能管理GCIH内部的Java对象，
        以此达到降低GC的回收频率和提升GC的回收效率的目的。

通过逃逸分析， Java HotSpot 编译器能够分析出一个新的对象的引用的使用范围从而决定是否要将这个对象分配到堆上。
    当一个对象在方法中被定义后，对象只在方法内部使用，则认为没有发生逃逸。
    当一个对象在方法中被定义后，它被外部方法所引用，则认为发生逃逸。例如作为调用参数传递到其他地方中

开发中能使用局部变量的（变量作用域尽量小），就不要使用在方法外定义。使用逃逸分析，编译器可以对代码做如下优化：
    1. 栈上分配：将堆分配转化为栈分配。如果一个对象在子程序中被分配，要使指向该对象的指针永远不会发生逃逸，
        对象可能是栈上分配的候选，而不是堆上分配
    2. 同步省略：如果一个对象被发现只有一个线程被访问到，那么对于这个对象的操作可以不考虑同步。
    3. 分离对象或标量替换：有的对象可能不需要作为一个连续的内存结构存在也可以被访问到，
        那么对象的部分（或全部）可以不存储在内存，而是存储在CPU寄存器中。

关于逃逸分析的论文在1999年就已经发表了，但直到JDK 1.6才有实现，而且这项技术到如今也并不是十分成熟的。
其根本原因就是无法保证逃逸分析的性能消耗一定能高于他的消耗。虽然经过逃逸分析可以做标量替换、栈上分配、和锁消除。
但是逃逸分析自身也是需要进行一系列复杂的分析的，这其实也是一个相对耗时的过程。
一个极端的例子，就是经过逃逸分析之后，发现没有一个对象是不逃逸的。那这个逃逸分析的过程就白白浪费掉了。
虽然这项技术并不十分成熟，但是它也是即时编译器优化技术中一个十分重要的手段。


直接内存是在Java堆外的、直接向系统申请的内存区间。来源于NIO，通过存在堆中的DirectByteBuffer操作Native内存
通常，访问直接内存的速度会优于Java堆。即读写性能高。因此出于性能考虑，读写频繁的场合可能会考虑使用直接内存。
直接内存的缺点：1. 分配回收成本较高； 2. 不受JVM内存回收管理
Java的NIO库允许Java程序使用直接内存，用于数据缓冲区：ByteBuffer byteBuffer = ByteBuffer.allocateDirect(size);

直接内存也可能导致OutOfMemoryError异常；直接内存大小可以通过MaxDirectMemorySize设置
由于直接内存在Java堆外，因此它的大小不会直接受限于-Xmx指定的最大堆大小，默认与堆的最大值-Xmx参数值一致
但是系统内存是有限的，Java堆和直接内存的总和依然受限于操作系统能给出的最大内存。




 */
/**
 * 堆区域和对象的分配
 */
public class HeapAreaSample {
    /*
    常用的参数设置：
        -XX：+PrintFlagsInitial：查看所有的参数的默认初始值
        -XX：+PrintFlagsFinal：查看所有的参数的最终值（可能会存在修改，不再是初始值）
        -Xms：初始堆空间内存（默认为物理内存的1/64）
        -Xmx：最大堆空间内存（默认为物理内存的1/4）
        -Xmn：设置新生代的大小。（初始值及最大值）
        -XX:NewRatio：配置新生代与老年代在堆结构的占比
        -XX:SurvivorRatio：设置新生代中Eden和S0/S1空间的比例
        -XX:MaxTenuringThreshold：设置新生代垃圾的最大年龄
        -XX:+PrintGCDetails：输出详细的GC处理日志
        打印gc简要信息：①-Xx：+PrintGC ② - verbose:gc
        -XX:HandlePromotionFalilure：是否设置空间分配担保
            在发生 Minor GC 之前，虚拟机会检查老年代最大可用的连续空间是否大于新生代所有对象的总空间。
                如果大于，则此次 Minor GC 是安全的
                如果小于，则虚拟机会查看 -XX:HandlePromotionFailure 设置值是否允担保失败。
                    如果 HandlePromotionFailure=true ，那么会继续检查老年代最大可用连续空间是否大于历次晋升到老年代的对象的平均大小。
                    如果大于，则尝试进行一次 Minor GC ，但这次 Minor GC 依然是有风险的；
                    如果小于，则改为进行一次 Full GC 。
           JDK6 Update24之后的规则变为只要老年代的连续空间大于新生代对象总大小或者历次晋升的平均大小就会进行Minor GC，
           否则将进行 Full GC；也就是HandlePromotionFailure这个参数无用了（或者都是true了，修改不生效了）
     */

    @Test
    public void headSpaceSize() {
        // 返回Java虚拟机中的堆内存总量
        long initialMemory = Runtime.getRuntime().totalMemory() / 1024 / 1024;
        // 返回Java虚拟机试图使用的最大堆内存
        long maxMemory = Runtime.getRuntime().maxMemory() / 1024 / 1024;
        System.out.println("-Xms:" + initialMemory + "M");
        System.out.println("-Xmx:" + maxMemory + "M");
    }

    /*
    标量（Scalar）是指一个无法再分解成更小的数据的数据。 Java中的原始数据类型就是标量。
    相对的，那些还可以分解的数据叫做聚合量（Aggregate），Java中的对象就是聚合量，因为他可以分解成其他聚合量和标量。

    在JIT阶段，如果经过逃逸分析，发现一个对象不会被外界访问的话，那么经过JIT优化，
    就会把这个对象拆解成若干个其中包含的若干个成员变量来代替。这个过程就是标量替换。
     */
    @Test
    public void allocInStack() {
        Point point = new Point(1,2);
        System.out.println("point.x" + point.x + ";point.y" + point.y);

        // 以上代码，经过标量替换后，就会变成下面的代码；这个就是变量替换的一个例子
        //int x = 1;
        //int y = 2;
        //System.out.println("point.x = " + x + "; point.y=" + y);

        /*
        Point 这个聚合量经过逃逸分析后，发现他并没有逃逸，就被替换成两个标量了。
        那么标量替换有什么好处呢？就是可以大大减少堆内存的占用。
        因为一旦不需要创建对象了，那么就不再需要分配堆内存了。标量替换为栈上分配提供了很好的基础。
         */
    }
    @AllArgsConstructor
    private static class Point {
        private int x;
        private int y;
    }
}
