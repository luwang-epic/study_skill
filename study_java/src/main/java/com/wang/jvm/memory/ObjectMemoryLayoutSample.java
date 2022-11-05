package com.wang.jvm.memory;

import org.openjdk.jol.info.ClassLayout;

/*
对象创建方式
1. new：最常见的方式、单例类中调用 getInstance 的静态类方法，XXXFactory 的静态方法
2. Class的newInstance方法：在JDK9里面被标记为过时的方法，因为只能调用空参构造器，权限必须是public
3. Constructor的newInstance(XXX)：反射的方式，可以调用空参的，或者带参的构造器，权限没有要求
4. 使用 clone()：不调用任何的构造器，要求当前的类需要实现 Cloneable 接口中的 clone() 方法
5. 使用反序列化：序列化一般用于Socket的网络传输，从文件、网络中获取文件二进制流
6. 第三方库 Objenesis

对象实例化的过程
    1. 判断对象对应的类是否加载、链接、初始化
        虚拟机遇到一条new指令，首先去检查这个指令的参数能否在Metaspace的常量池中定位到一个类的符号引用，
        并且检查这个符号引用代表的类是否已经被加载、解析和初始化（即判断类元信息是否存在）。
        如果没有，那么在双亲委派模式下，使用当前类加载器以ClassLoader + 包名 + 类名为Key进行查找对应的.class文件，
        如果没有找到文件，则抛出ClassNotFoundException异常，如果找到，则进行类加载，并生成对应的Class对象。
    2. 为对象分配内存
        首先计算对象占用空间的大小，接着在堆中划分一块内存给新对象。
        选择哪种分配方式由Java堆是否规整所决定，而Java堆是否规整又由所采用的垃圾收集器是否带有压缩整理功能决定。
            如果内存规整：使用指针碰撞
                意思是所有用过的内存在一边，空闲的内存放另外一边，中间放着一个指针作为分界点的指示器，
                分配内存就仅仅是把指针指向空闲那边挪动一段与对象大小相等的距离罢了。
            如果内存不规整：虚拟机需要维护一个列表：空闲列表分配；
                如果内存不是规整的，已使用的内存和未使用的内存相互交错，那么虚拟机将采用的是空闲列表来为对象分配内存
    3. 处理并发问题
        在分配内存空间时，另外一个问题是及时保证new对象时候的线程安全性；
        创建对象是非常频繁的操作，虚拟机需要解决并发问题。虚拟机采用了两种方式解决并发问题
            1. CAS（Compare And Swap）失败重试或者区域加锁：保证指针更新操作的原子性
            2. TLAB把内存分配的动作按照线程划分在不同的空间之中进行，即每个线程在Java堆中预先分配一个小块内存，
            称为本地线程分配缓冲区，虚拟机是否使用TLAB，可以通过 -XX:+/-UseTLAB参数来设定，默认开启
    4. 属性的默认初始化（零值初始化）
        内存分配结束，虚拟机将分配到的内存空间都初始化为零值（不包括对象头）
    5. 设置对象头信息
        将对象的所属类（即类的元数据信息）、对象的HashCode和对象的GC信息、锁信息等数据存储在对象的对象头中。
        这个过程的具体设置方式取决于JVM实现。jdk中hashcode采用的是懒加载模式，即使用的时候才放到对象头中
    6. 属性的显示初始化、代码块中初始化、构造器中初始化
        初始化成员变量，执行实例化代码块，调用类的构造方法，并把堆内对象的首地址赋值给引用变量


对象的结构包括：
    1. 对象头：对象头包含三部分，运行时元数据（Mark Word）、类型指针（class point）、数组长度。如果对象不是数组，没有该数组长度部分。
        运行时元数据（Mark Word）包括如下：（可能是交叉的，某个时刻只包含部分信息）
            哈希值（HashCode）
            GC分代年龄
            锁状态标志
            线程持有的锁
            偏向线程ID
            偏向时间戳
        类型指针：指向类元数据 InstanceKlass ，确定该对象所属的类型。指向的其实是方法区中存放的类元信息
    2. 对象体：存放具体的数据或者引用，如果空对象，将没有该部分（也就是这部分占用0）
        相同宽度的字段总是被分配在一起
        父类中定义的变量会出现在子类之前
        如果CompactFields参数为 true（默认为 true），子类的窄变量可能插入到父类变量的空隙
    3. 对齐字节（可有可无，若对象头加上对象体是8的倍数时，则不存在字节对齐）。
        因为在64位机器上面，以64字节为单位寻址时效率比较高，所以对象需要是8的倍数

对象访问的两种方式
    1. 句柄访问：句柄访问就是说栈的局部变量表中，记录的对象的引用，然后在堆空间中开辟了一块空间，也就是句柄池
        优点：reference中存储稳定句柄地址，对象被移动（垃圾收集时移动对象很普遍）时，
            只会改变句柄中实例数据指针即可，reference本身不需要被修改
    2. 直接指针（HotSpot采用）：局部变量表中的引用，直接指向堆中的实例，在对象实例中有类型指针，指向的是方法区中的对象类型数据
        优点：空间更少，访问速度更快了（句柄访问需要两次，而直接指针只需要一次就可以访问到）


 */
/**
 * 展示java对象的内存布局，使用openjdk JOL工具
 *  参考：https://blog.csdn.net/weixin_40482816/article/details/126161765
 */
public class ObjectMemoryLayoutSample {

    private static class NullObjectLayout {

    }

    private static class IntObjectLayout {
        private int a;
    }

    private static class StringObjectLayout {
        private String str = "hello";
    }

    public static void main(String[] args) {
        // 空对象，共16字节 4(mark word) + 4(mark word) + 4(类指针) + 4(对齐8字节)
        NullObjectLayout nullObjectLayout = new NullObjectLayout();
        System.out.println(ClassLayout.parseInstance(nullObjectLayout).toPrintable());

        IntObjectLayout intObjectLayout = new IntObjectLayout();
        System.out.println(ClassLayout.parseInstance(intObjectLayout).toPrintable());

        StringObjectLayout stringObjectLayout = new StringObjectLayout();
        System.out.println(ClassLayout.parseInstance(stringObjectLayout).toPrintable());

        // 空对象数组，共56字节 4(mark word) + 4(mark word) + 4(数组长度) + 4(类指针) + 10 * 4(10个对象引用)
        NullObjectLayout[] nullObjectLayouts = new NullObjectLayout[10];
        System.out.println(ClassLayout.parseInstance(nullObjectLayouts).toPrintable());
    }

}
