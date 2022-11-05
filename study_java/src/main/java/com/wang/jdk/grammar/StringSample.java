package com.wang.jdk.grammar;

import org.junit.jupiter.api.Test;

/*
String 类的当前实现将字符存储在 char 数组中，每个字符使用两个字节(16位)。从许多不同的应用程序收集的数据表明，
字符串是堆使用的主要组成部分，而且，大多数字符串对象只包含拉丁字符。这些字符只需要一个字节的存储空间，
因此这些字符串对象的内部char数组中有一半的空间将不会使用。
jdk9以及之后，String再也不用char[]来存储了，改成了byte [] 加上编码标记，节约了一些空间

字符串常量池是不会存储相同内容的字符串的
String的String Pool是一个固定大小的Hashtable ，默认值大小长度是1009（jdk7以及之后为60013）。
如果放进String Pool的String非常多，就会造成Hash冲突严重，从而导致链表会很长，
而链表长了后直接会造成的影响就是当调用String.intern（如果字符串常量池没有该字符串，放入其中）时性能会大幅下降。

常量与常量的拼接结果在常量池（这里的常量包括final修饰的字符串变量），原理是编译期优化
常量池中不会存在相同内容的变量
只要其中有一个是变量，结果就在堆中。变量拼接的原理是StringBuilder
如果拼接的结果调用intern()方法，则主动将常量池中还没有的字符串对象放入池中，并返回此对象地址


 */
/**
 * 字符串相关的
 */
public class StringSample {
    // 字符串的不可变性
    @Test
    public void ImmutableStringDemo() {
        ImmutableString ex = new ImmutableString();
        ex.change(ex.str, ex.ch);
        // good
        System.out.println(ex.str);
        // best
        System.out.println(ex.ch);
    }

    private static class ImmutableString {
        String str = new String("good");
        char [] ch = {'t','e','s','t'};

        public void change(String str, char ch []) {
            str = "test ok";
            ch[0] = 'b';
        }
    }

    @Test
    public void stringConcatenationDemo() {
        String abc1 = "a" + "b" + "c";  // 得到abc的常量池，等同于abc
        String abc2 = "abc"; // abc存放在常量池，直接将常量池的地址返回
        /**
         * 最终java编译成.class，再执行.class，.class文件中就是abc了，进行了编译期优化
         */
        System.out.println(abc1 == abc2); // true，因为存放在字符串常量池

        String s1 = "javaEE";
        String s2 = "hadoop";
        String s3 = "javaEEhadoop";
        String s4 = "javaEE" + "hadoop"; // 编译期优化
        // 如果拼接符号的前后出现了变量，则相当于在堆空间中new String()，具体的内容为拼接的结果：javaEEhadoop
        String s5 = s1 + "hadoop";
        String s6 = "javaEE" + s2;

        /*
        如下的s1 + s2 的执行细节（可以通过编译后的字节码查看）：
            1. StringBuilder sb = new StringBuilder();
            2. sb.append("javaEE");
            3. sb.append("hadoop");
            4. s7 = sb.toString()  // 约等于 new String("javaEEhadoop");
         */
        String s7 = s1 + s2;

        System.out.println(s3 == s4); // true
        System.out.println(s3 == s5); // false
        System.out.println(s3 == s6); // false
        System.out.println(s3 == s7); // false
        System.out.println(s5 == s6); // false
        System.out.println(s5 == s7); // false
        System.out.println(s6 == s7); // false

        // 而调用intern()方法，则会判断字符串常量池中是否存在"javaEEhadoop"值，
        // 如果存在则返回常量池中的值，否者就在常量池中创建
        String s8 = s6.intern();
        System.out.println(s3 == s8); // true

        final String fs1 = "a";
        final String fs2 = "b";
        String fs3 = "ab";
        // 字符串拼接操作不一定使用的是StringBuilder，如果拼接符号左右两边都是字符串常量或者常量引用，
        // 则仍然使用编译期优化，即非StringBuilder的方式
        // 针对于final修饰类，方法，基本数据类型，引用数据类型的量的结构时，能使用上final的时候建议使用
        String fs4 = fs1 + fs2;
        System.out.println(fs3 == fs4); // true
    }

    /*
    通过StringBuilder的append()方式添加字符串的效率，要远远高于String的字符串拼接方法
        1. StringBuilder的append的方式，自始至终只创建一个StringBuilder的对象
        2. 对于字符串拼接的方式，还需要创建很多StringBuilder对象和调用toString时候创建的String对象
        3. 内存中由于创建了较多的StringBuilder和String对象，内存占用过大，如果进行GC那么将会耗费更多的时间

    改进的空间:
        我们使用的是StringBuilder的空参构造器，默认的字符串容量是16，然后将原来的字符串拷贝到新的字符串中，
        我们也可以默认初始化更大的长度，减少扩容的次数。因此在实际开发中，我们能够确定，
        前前后后需要添加的字符串不高于某个限定值，那么建议使用构造器创建一个阈值的长度
     */
    @Test
    public void stringPerformanceDemo() {
        long start = System.currentTimeMillis();
        strWithStringBuilder(100000);
        long end = System.currentTimeMillis();
        System.out.println("strWithStringBuilder花费的时间为：" + (end - start));

        start = System.currentTimeMillis();
        strWithoutStringBuilder(100000);
        end = System.currentTimeMillis();
        System.out.println("strWithoutStringBuilder花费的时间为：" + (end - start));
    }
    public void strWithoutStringBuilder(int highLevel) {
        String src = "";
        for (int i = 0; i < highLevel; i++) {
            src += "a"; // 每次循环都会创建一个StringBuilder对象
        }
    }
    public void strWithStringBuilder(int highLevel) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < highLevel; i++) {
            sb.append("a");
        }
    }

    @Test
    public void newStringDemo() {
        /*
        new String("ab") 会创建几个对象？ 看字节码就知道是2个对象
            1. 常量池中的字符串"ab"
            2. 堆空间中的字符串str1，内容指向常量池中的"ab"
         */
        String str1 = new String("ab");

        /*
        我们创建了6个对象
            对象1：new StringBuilder()
            对象2：new String("a")
            对象3：常量池的 a
            对象4：new String("b")
            对象5：常量池的 b
            对象6：StringBuilder.toString中会创建一个new String("ab")
                调用StringBuilder.toString方法，不会在常量池中生成 ab
         */
        String str2 = new String("a") + new String("b");
    }

    /*
     如何保证变量s指向的是字符串常量池中的数据呢？有两种方式：
         方式一： String s = "shkstart";//字面量定义的方式
         方式二： 调用intern()
             String s = new String("shkstart").intern();
             String s = new StringBuilder("shkstart").toString().intern();
     */
    @Test
    public void stringInternDemo() {
        String s = new String("1");
        s.intern();// 调用此方法之前，字符串常量池中已经存在了"1"
        String s2 = "1";
        System.out.println(s == s2); // jdk6：false  jdk7/8：false


        String s3 = new String("1") + new String("1");// s3变量记录的地址为：new String("11")
        // 执行完上一行代码以后，字符串常量池中，是否存在"11"呢？答案：不存在！！
        s3.intern();// 在字符串常量池中生成"11"。如何理解：jdk6:创建了一个新的对象"11",也就有新的地址。
        //         jdk7:此时常量中并没有创建"11",而是创建一个指向堆空间中new String("11")的地址
        String s4 = "11";//s4变量记录的地址：使用的是上一行代码代码执行时，在常量池中生成的"11"的地址
        // 这个需要在main方法中测试（下面），在单元测试中不正确
        System.out.println(s3 == s4);// jdk6：false  jdk7/8：true
    }

    public static void main(String[] args) {
        String s3 = new String("1") + new String("1");
        s3.intern();
        String s4 = "11";
        System.out.println(s3 == s4);// jdk6：false  jdk7/8：true

        // 注意intern和s6的顺序
        String s5 = new String("1") + new String("1");
        String s6 = "11";
        s5.intern();
        System.out.println(s5 == s6);// jdk6：false  jdk7/8：false
    }
}
