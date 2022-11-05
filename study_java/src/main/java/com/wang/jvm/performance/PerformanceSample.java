package com.wang.jvm.performance;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

/*
性能优化的步骤:
第1步（发现问题）性能监控：一种以非强行或入侵方式 收集或查看 应用运营性能数据的活动。
监控通常是指一种在生产、质量评估或者开发环境下实施的带有 预防或主动性 的活动。
当应用相关干系人提出性能问题却 没有提供足够多的线索时，首先我们需要进行性能监控，随后是性能分析。
    GC 频繁
    CPU load 过高
    OOM
    内存泄漏
    死锁
    程序响应时间较长
第2步（排查问题）性能分析：一种以侵入方式 收集运行性能数据的活动，它会影响应用的吞吐量或响应性。
性能分析是针对性能问题的答复结果，关注的范围通常比性能监控更加集中。性能分析很少在生产环境下进行，
通常是在质量评估、系统测试或者开发环境下进行，是性能监控之后的步骤。
    打印GC日志，通过GCViewer或者http://gceasy.io来分析日志信息
    灵活运用命令行工具，jstack、jmap、jinfo等
    dump出堆文件，使用内存分析工具分析文件
    使用阿里 Arthas、jconsole、JVisualVM 来实时查看 JVM 状态
    jstack查看堆栈信息
第3步（解决问题）性能调优：一种为改善应用响应性或吞吐量而更改参数、源代码、属性配置的活动，
性能调优是在性能监控、性能分析之后的活动。
    适当增加内存，根据业务背景选择垃圾回收器
    优化代码，控制内存使用
    增加机器，分散节点压力
    合理设置线程池线程数量
    使用中间件提高程序效率，比如缓存，消息队列等

性能评价测试指标，主要有如下：
    停顿时间（响应时间）
    吞吐量
    并发数
    内存占用

简单命令行工具：
    jps：查看正在运行的Java进程
    jstat：查看JVM统计信息
        用于监视虚拟机各种运行状态信息的命令行工具。
        它可以显示本地或者远程虚拟机进程中的类装载、内存、垃圾收集、JIT编译等运行数据。
    jinfo：实时查看和修改JVM配置参数
    jmap：导出内存映像文件&内存使用的情况
    jhat：JDK自带堆分析工具
    jstack：打印JVM中线程快照
    jcmd：多功能命令行
        可以用来实现前面除了jstat之外所有命令的功能。比如：用它来导出堆、内存使用、
        查看Java进程、导出线程信息、执行 GC、JVM 运行时间等。

JDK自带的工具
    jconsole：JDK自带的可视化监控工具。查看Java应用程序的运行概况、监控堆信息、永久区（或元空间）使用情况、类加载情况等
    Visual VM：Visual VM是一个工具，它提供了一个可视界面，用于查看Java虚拟机上运行的基于Java技术的应用程序的详细信息。
        Visual VM是一个功能强大的多合一故障诊断和性能监控的可视化工具。它集成了多个JDK命令行工具，
        使用Visual VM可用于显示虚拟机进程及进程的配置和环境信息（jps，jinfo），
        监视应用程序的CPU、GC、堆、方法区及线程的信息（jstat、jstack）等，甚至代替JConsole。
        Visual VM便作为JDK的一部分发布（VisualVM 在JDK／bin目录下JVisualVM）即：它完全免费。
    JMC：Java Mission Control，内置Java Flight Recorder。能够以极低的性能开销收集Java虚拟机的性能数据。
第三方工具
    MAT：MAT（Memory Analyzer Tool）是基于Eclipse的内存分析工具，
        是一个快速、功能丰富的Java heap分析工具，它可以帮助我们查找内存泄漏和减少内存消耗
    JProfiler：商业软件，需要付费。功能强大。


Java的内存信息（heap dump文件）包括：
    所有的对象信息，包括对象实例、成员变量、存储于栈中的基本类型值和存储于堆中的其他对象的引用值。
    所有的类信息，包括classloader、类名称、父类、静态变量等
    GCRoot到所有的这些对象的引用路径
    线程信息，包括线程的调用栈及此线程的线程局部变量（TLS）

浅堆（Shallow Heap）
    浅堆是指一个对象所消耗的内存。对象头 + 基本数据类型 + 引用类型的指针所占空间
保留集（Retained Set）
    对象A的保留集指当对象A被垃圾回收后，可以被释放的所有的对象集合（包括对象A本身），
    即对象A的保留集可以被认为是只能通过对象A被直接或间接访问到的所有对象的集合。
    通俗地说，就是指仅被对象A所持有的对象的集合。
深堆（Retained Heap）
    深堆是指对象的保留集中所有的对象的浅堆大小之和。
    浅堆指对象本身占用的内存，不包括其内部引用对象的大小。一个对象的深堆指只能通过该对象访问到的（直接或间接）所有对象的浅堆之和，
    即对象被回收后，可以释放的真实空间。
对象的实际大小
    这里，对象的实际大小定义为一个对象所能触及的所有对象的浅堆大小之和，也就是通常意义上我们说的对象大小。

Java中内存泄露的8种情况
    静态集合类
    单例模式
    内部类持有外部类
    各种连接，如数据库连接、网络连接和IO连接等
    变量不合理的作用域
    改变哈希值
    缓存泄露
    监听器和其他回调

GC日志分析工具
GCEasy
    GCEasy是一款在线的GC日志分析器，可以通过GC日志分析进行内存泄露检测、
    GC暂停原因分析、JVM配置建议优化等功能，大多数功能是免费的。
    官网地址：https://gceasy.io/
GCViewer
    GCViewer是一款离线的GC日志分析器，用于可视化Java VM选项 -verbose:gc 和 .NET生成的数据 -Xloggc:<file>。
    还可以计算与垃圾回收相关的性能指标（吞吐量、累积的暂停、最长的暂停等）。
    当通过更改世代大小或设置初始堆大小来调整特定应用程序的垃圾回收时，此功能非常有用。



 */


/**
 * 性能相关
 */
public class PerformanceSample {

    /*
    Java提供了java.lang.management包用于监视和管理Java虚拟机和Java运行时中的其他组件,
    它允许本地或远程监控和管理运行的Java虚拟机。其中ManagementFactory类较为常用，
    另外Runtime类可获取内存、CPU核数等相关的数据。
    通过使用这些api，可以监控应用服务器的堆内存使用情况，设置一些阈值进行报警等处理。
     */
    public static void main(String[] args) {
        MemoryMXBean memorymbean = ManagementFactory.getMemoryMXBean();
        MemoryUsage usage = memorymbean.getHeapMemoryUsage();
        System.out.println("INIT HEAP: " + usage.getInit() / 1024 / 1024 + "m");
        System.out.println("MAX HEAP: " + usage.getMax() / 1024 / 1024 + "m");
        System.out.println("USE HEAP: " + usage.getUsed() / 1024 / 1024 + "m");
        System.out.println("\nFull Information:");
        System.out.println("Heap Memory Usage: " + memorymbean.getHeapMemoryUsage());
        System.out.println("Non-Heap Memory Usage: " + memorymbean.getNonHeapMemoryUsage());

        System.out.println("=======================通过java来获取相关系统状态============================ ");
        System.out.println("当前堆内存大小totalMemory " + (int) Runtime.getRuntime().totalMemory() / 1024 / 1024 + "m");// 当前堆内存大小
        System.out.println("空闲堆内存大小freeMemory " + (int) Runtime.getRuntime().freeMemory() / 1024 / 1024 + "m");// 空闲堆内存大小
        System.out.println("最大可用总堆内存maxMemory " + Runtime.getRuntime().maxMemory() / 1024 / 1024 + "m");// 最大可用总堆内存大小
    }
}
