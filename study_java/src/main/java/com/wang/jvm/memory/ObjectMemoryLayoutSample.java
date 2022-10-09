package com.wang.jvm.memory;

import org.openjdk.jol.info.ClassLayout;

/*
对象的结构包括：
    1. 对象头：对象头包含三部分，Mark Word、class point、数组长度。如果对象不是数组，数组长度可以忽略。
    2. 对象体：存放具体的数据或者引用，如果空对象，将没有该部分（也就是这部分占用0）
    3. 对齐字节（可有可无，若对象头加上对象体是8的倍数时，则不存在字节对齐）。
        因为在64位机器上面，以64字节为单位寻址时效率比较高，所以对象需要是8的倍数
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
