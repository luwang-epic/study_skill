package com.wang.jvm.classloader;

/*
加载:
    通过一个类的全限定名获取定义此类的二进制字节流
    将这个字节流所代表的静态存储结构转化为方法区的运行时数据结构
    在内存中生成一个代表这个类的 java.lang.Class 对象，作为方法区这个类的各种数据的访问入口

补充：加载 .class 文件的方式
    从本地系统中直接加载
    通过网络获取，典型场景：Web Applet
    从 zip 压缩包中读取，成为日后 jar、war 格式的基础
    运行时计算生成，使用最多的是：动态代理技术
    由其他文件生成，典型场景：JSP 应用从专有数据库中提取 .class 文件，比较少见
    从加密文件中获取，典型的防 class 文件被反编译的保护措施

验证 Verify
    目的在于确保 class 文件的字节流中包含信息符合当前虚拟机要求，保证被加载类的正确性，不会危害虚拟机自身安全。
    主要包括四种验证：文件格式验证，元数据验证，字节码验证，符号引用验证。

准备 Prepare
    为类变量分配内存并且设置该类变量的默认初始值，即零值；
    这里不包含用final修饰的static的常量，因为基本类型常量和字面量String类型的常量在编译的时候就会分配了，
    准备阶段会显式赋值；而对于其他引用类型的常量和非字面量String类型，会在类的初始化阶段赋值；（常量不需要默认值，直接赋最终值）
    这里不会为实例变量分配初始化，类变量会分配在方法区中，而实例变量是会随着对象一起分配到 Java 堆中。

解析 Resolve
    将常量池内的符号引用转换为直接引用的过程。
    事实上，解析操作往往会伴随着 JVM 在执行完初始化之后再执行。
    符号引用就是一组符号来描述所引用的目标。符号引用的字面量形式明确定义在《Java虚拟机规范》的 class文件格式中。
        直接引用就是直接指向目标的指针、相对偏移量或一个间接定位到目标的句柄。
    解析动作主要针对类或接口、字段、类方法、接口方法、方法类型等。
        对应常量池中的 CONSTANT_Class_info、CONSTANT_Fieldref_info、CONSTANT_Methodref_info 等

验证，准备，解析合在一起的大阶段叫做链接

初始化
    初始化阶段就是执行类构造器法cinit方法的过程，此方法不需定义，
    是javac编译器自动收集类中的所有类变量的赋值动作和静态代码块中的语句合并而来。cinit不同于类的构造器。
    也就是说，当我们代码中包含static变量的时候，就会有clinit方法，构造器方法中指令按语句在源文件中出现的顺序执行。
    若该类具有父类，JVM 会保证子类的cinit方法执行前，父类的cinit方法已经执行完毕。
    虚拟机必须保证一个类的cinit方法在多线程下被同步加锁。

Java 程序对类的使用方式分为：主动使用和被动使用。 主动使用，又分为七种情况：
    1. 创建类的实例
    2. 访问某个类或接口的静态变量，或者对该静态变量赋值
    3. 调用类的静态方法
    4. 反射（比如：Class.forName（"com.atguigu.Test"））
    5. 初始化一个类的子类
    6. Java 虚拟机启动时被标明为启动类的类
    7. JDK 7 开始提供的动态语言支持：java.lang.invoke.MethodHandle实例的解析结果
        REF getStatic、REF putStatic、REF invokeStatic 句柄对应的类没有初始化，则初始化
除了以上七种情况，其他使用Java类的方式都被看作是对类的被动使用，都不会导致类的初始化。例如下面的情况：
    1. 当访问一个静态字段时，只有真正声明这个字段的类才会被初始化
    2. 当通过子类引用父类的静态变量，不会导致子类初始化
    3. 通过数组定义类引用，不会触发此类的初始化
    4. 引用基本类型常量或者字面量的常量不会触发此类或接口的初始化。因为常量在链接阶段就已经被显式赋值了
    5. 调用ClassLoader类的loadClass()方法加载一个类，并不是对类的主动使用，不会导致类的初始化


 */
/**
 * 类的加载过程
 */
public class ClassInitSample {

    private static int num = 1;
    static {
        num = 2;
        number = 20;
        System.out.println(num);
//        System.out.println(number);  //报错，非法的前向引用（static只能赋值，不能调用）
    }

    private static int number = 10;

    public static void main(String[] args) {
        System.out.println(ClassInitSample.num); // 2
        System.out.println(ClassInitSample.number); // 10
    }

    /*
    说明：使用 static + final 修饰的字段的显式赋值的操作，到底是在哪个阶段进行的赋值？
        情况1：在链接阶段的准备环节赋值
        情况2：在初始化阶段<clinit>()中赋值

    结论：
        在链接阶段的准备环节赋值的情况：
            1. 对于基本数据类型的字段来说，如果使用 static final 修饰，则显式赋值(直接赋值常量，而非调用方法)通常是在链接阶段的准备环节进行
            2. 对于 String 来说，如果使用字面量的方式赋值，使用 static final 修饰的话，则显式赋值通常是在链接阶段的准备环节进行
        在初始化阶段<clinit>()中赋值的情况
            排除上述的在准备环节赋值的情况之外的情况

    最终结论：使用 static + final 修饰，且显示赋值中不涉及到方法或构造器调用的基本数据类型或String类型的显式赋值，是在链接阶段的准备环节进行
     */
    public static int a = 1; //在初始化阶段<clinit>()中赋值
    public static final int INT_CONSTANT = 10;  //在链接阶段的准备环节赋值

    public static final Integer INTEGER_CONSTANT1 = Integer.valueOf(100);   //在初始化阶段<clinit>()中赋值
    public static Integer INTEGER_CONSTANT2 = Integer.valueOf(1000); //在初始化阶段<clinit>()中赋值

    public static final String s0 = "helloworld0"; //在链接阶段的准备环节赋值
    public static final String s1 = new String("helloworld1"); //在初始化阶段<clinit>()中赋值
}
