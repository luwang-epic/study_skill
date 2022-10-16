package com.wang.jdk.grammar;

import org.junit.jupiter.api.Test;

import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/**
 * 基本语法的使用
 */
/*
一个Java源文件中可以定义多个类，但是最多只有一个类被public修饰，并且这个类的类名与文件名必须相同。
若这个文件中没有public的类，则文件名可随便命名(前提是符合规范)。
要注意的是，当用javac指令编译有多个类的Java源文件时，它会给该源文件中的每一个类生成一个对应的.class文件。
 */
public class BaseGrammar {
    // object方法
    // getClass, hashCode, equals, clone, toString, notify, notifyAll, wait, finalize, registerNatives

    /*
    equals()与 == 的区别：
        （1）== 号在比较基本数据类型时比较的是数据的值，而比较引用类型时比较的是两个对象的地址值；
        （2）equals()不能用于基本的数据类型，对于基本的数据类型要用其包装类。
        （3）默认情况下，从Object继承而来的 equals 方法与 “==” 是完全等价的，比较的都是对象的内存地址，
            因为底层调用的是 “==” 号，但我们可以重写equals方法，使其按照我们的需求方式进行比较，
            如String类重写equals()方法，使其比较的是字符的内容，而不再是内存地址。

    hashCoed 的特性：
        （1）HashCode的存在主要是用于查找的快捷性，如Hashtable，HashMap等，
            HashCode经常用于确定对象的存储地址；
        （2）如果两个对象相同， equals方法一定返回true，并且这两个对象的HashCode一定相同；
        （3）两个对象的HashCode相同，并不一定表示两个对象就相同，即equals()不一定为true，
            只能够说明这两个对象在一个散列存储结构中。
        （4）如果对象的equals方法被重写，那么对象的HashCode也尽量重写；
            否则集合中（特别是Set）可能存在两个相同的对象，会导致一些不符合预期的行为

    hashCode是用于查找使用的，而equals是用于比较两个对象是否相等的。
     */

    public interface InterfaceMethod {
        /*
        JDK 1.8允许给接口添加非抽象的方法实现，但必须使用default关键字修饰；
        定义了default的方法可以不被实现子类所实现，但只能被实现子类的对象调用；

        如果子类实现了多个接口，并且这些接口包含一样的默认方法，则子类必须重写默认方法；
         */
        default void testDefault() {
            System.out.println("有默认实现的接口方法");
        }

        /*
        JDK 1.8中允许使用static关键字修饰一个方法，并提供实现，称为接口静态方法。
        接口静态方法只能通过接口调用（接口名.静态方法名）。
         */
        static void testStaticMethod() {
            System.out.println("接口的静态方法");
        }
    }
    @Test
    public void interfaceMethodGrammar() {
        InterfaceMethod.testStaticMethod();

        InterfaceMethod interfaceMethod = new InterfaceMethod() { };
        interfaceMethod.testDefault();
    }

    /*
    Java创建对象的4中方式（对象分配参考：https://www.cnblogs.com/jianwei-dai/p/15402683.html）：
        new对象
        通过反射创建对象
        通过clone方法创建对象
        通过序列化方式创建对象

    Java为了避免产生大量的String对象，设计了一个字符串常量池。工作原理是这样的，创建一个字符串时，
    JVM首先为检查字符串常量池中是否有值相等的字符串，如果有，则不再创建，直接返回该字符串的引用地址，
    若没有，则创建，然后放到字符串常量池中，并返回新创建的字符串的引用地址。所以下面s1与s2引用地址相同。

    那为什么s3与s1、s2引用的不是同一个字符串地址呢？ String s3=new String(“Hello”);
    JVM首先是在字符串常量池中找"Hello” 字符串，如果没有创建字符串常量，然后放到常量池中，
    若已存在，则不需要创建；当遇到 new 时，
    还会在内存（不是字符串常量池中，而是在堆里面）上创建一个新的String对象，存储"Hello"，
    并将内存上的String对象引用地址返回，所以s3与s1、s2引用的不是同一个字符串地址。

    s1与s2指向的都是常量池中的字符串常量，所以它们比较的是同一块内存地址，
    而s3指向的是堆里面的一块地址，说的具体点应该是堆里面的Eden区域，
    s1跟s3，s2跟s3比较都是不相等的，都不是同一块地址。
     */
    @Test
    public void stringGrammar() {
        String s1 = "a";
        String s2 = "a";
        String s3 = new String("a");
        String s4 = new String(s1);
        // true，
        System.out.println(s1 == s2);
        // false
        System.out.println(s1 == s3);
        // false
        System.out.println(s1 == s4);
        // false
        System.out.println(s3 == s4);

        // Object的hashCode()默认是返回内存地址的，但是hashCode()可以重写，
        // 所以hashCode()不能代表内存地址的不同。
        System.out.println(s1.hashCode());
        System.out.println(s3.hashCode());
        // System.identityHashCode(Object)方法可以返回对象的内存地址,
        // 不管该对象的类是否重写了hashCode()方法
        System.out.println(System.identityHashCode(s1));
        System.out.println(System.identityHashCode(s3));

    }

    @Test
    public void shortGrammar() {
        short s1 = 1;
        // 相当于 s1 = (short)(s1 + 1)
        s1 += 1;
        // 会报错，因为右边会转换为int类型，需要强转
        //s1 = s1 + 1;
        s1 = (short) (s1 + 1);
        System.out.println(s1);

        char ch = 1;
        int i = ch;
        double d = ch;
        double e = i;
        // 不可以将char int double直接赋值给string
        String s2 = ch + "";
    }

    @Test
    public void mathGrammar() {
        System.out.println(Math.round(-1.5));
        System.out.println(Math.floor(-1.5));

        System.out.println(Math.round(1.5));
        System.out.println(Math.floor(1.5));
    }

    @Test
    public void assertGrammar() {
        // 如果表达式的值为false，那么系统会报告一个AssertionError；如：java.lang.AssertionError
        assert 1 < 2;
        // 如果断言为false，会将后面的字符串作为断言错误的提示信息; 如：java.lang.AssertionError: 断言失败
        assert 1 > 2 : "断言失败";
        System.out.println("这行代码不会执行......");
    }

    public static void main(String[] args) {
        BaseGrammar baseGrammar = new BaseGrammar();
        /*
        要在运行时启用断言， 可以在启动JVM 时使用-enableassertions 或者-ea 标记。
        要在运行时选择禁用断言，可以在启动JVM 时使用-da 或者-disableassertions标记。
        要在系统类中启用或禁用断言，可使用-esa 或-dsa 标记。还可以在包的基础上启用或者禁用断言。

        注意：断言不应该以任何方式改变程序的状态。
        简单的说，如果希望在不满足某些条件时阻止代码的执行，就可以考虑用断言来阻止它。
         */
        // 默认不开启断言，因此不会出错，会输出最后一行代码；
        // 但是在单元测试中是开启的，因此运行assertGrammar的单侧会报错
        baseGrammar.assertGrammar();
    }


    private int tryCatchFinallyGrammar() {
        /*
        finally 是在 return 后面的表达式运算后执行的（此时并没有返回运算后的值，
        而是先把要返回的值保存起来，不管finally中的代码怎么样，返回的值都不会改变，
        仍然是之前保存的值），所以函数返回值是在 finally 执行前确定的；
         */
        int result = -1;
        try {
            System.out.println("try......");
            result = 1/0;
            return result;
        } catch (Exception e) {
            // 异常处理
            System.out.println("exception......");
            result = 10;
            return result;
        } finally {
            System.out.println("finally......");
            result = 1;
            // 如果这里没有return，result的值并不会改变；没有异常情况下返回的还是-1，有异常返回还是10
            // 如果这里使用了return，不管是否发生异常，都返回1
            //return result;
        }
    }

    @Test
    public void runTryCatchFinallyGrammar() {
        System.out.println(tryCatchFinallyGrammar());

        // 没有异常信息打印
        try {
            int a = 1/0;
        } finally {
            return;
        }
    }

    @Test
    public void ioGrammar() throws Exception {
        /*
        在java.io包中操作文件内容的主要有两大类：字节流、字符流，两类都分为输入和输出操作。
        在字节流中输出数据主要是使用OutputStream完成，输入使的是InputStream，
        在字符流中输出主要是使用Writer类完成，输入流主要使用Reader类完成。（这四个都是抽象类）

        Reader类的read()方法（不带参数的方法）返回类型为int,作为整数读取的字符（占两个字节共16位）,
        范围在 0 到 65535 之间 (0x00-0xffff)，如果已到达流的末尾，则返回 -1
        inputStream的read()方法（不带参数的方法）虽然也返回int，但由于此类是面向字节流的，一个字节占8个位，
        所以返回 0 到 255 范围内的 int 字节值。如果因为已经到达流末尾而没有可用的字节，则返回值 -1。
        因此对于不能用0-255来表示的值就得用字符流来读取！比如说汉字，否则将会是乱码.

        字符流处理的单元为2个字节的Unicode字符，分别操作字符、字符数组或字符串，
        而字节流处理单元为1个字节，操作字节和字节数组。
        所以字符流是由Java虚拟机将字节转化为2个字节的Unicode字符为单位的字符而成的，
        所以它对多国语言支持性比较好！如果是音频文件、图片、歌曲，就用字节流好点，
        如果是关系到中文（文本）的，用字符流好点。
        所有文件的储存是都是字节（byte）的储存，在磁盘上保留的并不是文件的字符而是先把字符编码成字节，
        再储存这些字节到磁盘。在读取文件（特别是文本文件）时，也是一个字节一个字节地读取以形成字节序列

        字节流是最基本的，采用ASCII编码,所有的InputStream和OutputStream的子类都是,主要用在处理二进制数据，它是按字节来处理的
        但实际中很多的数据是文本，又提出了字符流的概念(一个字符character占两个字节)，采用Unicode编码.
        它是按虚拟机的encode来处理，也就是要进行字符集的转化
        这两个之间通过 InputStreamReader,OutputStreamWriter来关联，实际上是通过byte[]和String来关联

        字节流在操作的时候本身是不会用到缓冲区（内存）的，是与文件本身直接操作的，
        而字符流在操作的时候是使用到缓冲区的
        字节流在操作文件时，即使不关闭资源（close方法），文件也能输出，
        但是如果字符流不使用close方法的话，则不会输出任何内容，
        说明字符流用的是缓冲区，并且可以使用flush方法强制进行刷新缓冲区，这时才能在不close的情况下输出内容
         */
        // 字节流和字符流的转换
        // 将字节流InputStream转换为字符流InputStreamReader
        // new InputStreamReader(System.in);
        // 将字节流OutputStream转换为字符流OutputStreamWriter
        // new OutputStreamWriter(System.out);

        FileWriter fileWriter = new FileWriter("D:\\idea_project\\study_skill\\file\\java\\io_read_file.txt");
        fileWriter.write("io test / 文件操作");
        // 如果不加上close方法 或者 flush方法 将会读取不到任何内容
        // fileWriter.close();
        // fileWriter.flush();

        FileReader fileReader = new FileReader("D:\\idea_project\\study_skill\\file\\java\\io_read_file.txt");
        char[] chs = new char[1024];
        int characterNumber = fileReader.read(chs);
        fileReader.close();
        if (characterNumber > 0) {
            System.out.println(new String(chs, 0, characterNumber));
        } else {
            System.out.println("没有读取到任何内容......");
        }

        // 上面都是阻塞的BIO，对于NIO和AIO见com.wang.io包下的实例
    }

    /*
    Java 提供了一种对象序列化的机制，该机制中，一个对象可以被表示为一个字节序列，
    该字节序列包括该对象的数据、有关对象的类型的信息和存储在对象中数据的类型。
    将序列化对象写入文件之后，可以从文件中读取出来，并且对它进行反序列化，
    也就是说，对象的类型信息、对象的数据，还有对象中的数据类型可以用来在内存中新建对象。
    整个过程都是 Java 虚拟机（JVM）独立的，也就是说，在一个平台上序列化的对象可以在另一个完全不同的平台上反序列化该对象。
    类 ObjectInputStream 和 ObjectOutputStream 是高层次的数据流，它们包含反序列化和序列化对象的方法。

    必须注意地是， 对象序列化保存的是对象的”状态”，即它的成员变量。由此可知，对象序列化不会关注类中的静态变量
    对于成员变量，可以用Transient关键字阻止该变量被序列化到文件中
     */

    /*
    Java的反射（reflection）机制是在运行状态中，对于任意一个类，都能够知道这个类的所有属性和方法；
    对于任意一个对象，都能够动态调用它的任意方法和属性。既然能拿到，那么我们就可以修改部分类型信息；
    这种动态获取信息以及动态调用对象方法的功能称为java语言的反射（reflection）机制。

    涉及反射的基本类：
        1. Class类——反射核心类
        2. Constructor类——通过反射调用构造方法,破坏封装对象
        3. Method类——通过反射调用普通方法
        4. Field类——通过反射调用属性


     */

    /*
    注解是绑定到程序源代码元素的元数据，对运行代码的操作没有影响。他们的典型用例是：
        编译器的信息 - 使用注解，编译器可以检测错误或抑制警告
        编译时和部署时处理 - 软件工具可以处理注解并生成代码，配置文件等。
        运行时处理 - 可以在运行时检查注解以自定义程序的行为

    注解方法中的返回类型必须是基本类型，String，Class，Enum或数组类型之一。否则，编译器将抛出错误

    元注解就是标记其他注解的注解，共有4个：
        1. @Target 用来约束注解可以应用的地方（如方法、类或字段），其中ElementType是枚举类型，其定义如下，也代表可能的取值范围
            当注解未指定Target值时，则此注解可以用于任何元素之上，多个值使用{}包含并用逗号隔开
        2. @Retention用来约束注解的生命周期，分别有三个值，源码级别（source），类文件级别（class）或者运行时级别（runtime）
                SOURCE：注解将被编译器丢弃（该类型的注解信息只会保留在源码里，
                    源码经过编译后，注解信息会被丢弃，不会保留在编译好的class文件里）
                CLASS：注解在class文件中可用，但会被VM丢弃（该类型的注解信息会保留在源码里和class文件里，
                    在执行的时候，不会加载到虚拟机中），请注意，当注解未定义Retention值时，
                    默认值是CLASS，如Java内置注解，@Override、@Deprecated、@SuppressWarnning等
                RUNTIME：注解信息将在运行期(JVM)也保留，因此可以通过反射机制读取注解的信息（源码、class文件和执行的时候都有注解的信息），
                    如SpringMvc中的@Controller、@Autowired、@RequestMapping等。
        3. @Documented 被修饰的注解会生成到javadoc中
        4. @Inherited 可以让注解被继承，但这并不是真的继承，只是通过使用@Inherited，
            可以让子类Class对象使用getAnnotations()获取父类被@Inherited修饰的注解

    java.lang和java.lang.annotation包中有几个常用注解：
        @Override -标记方法是否覆盖超类中声明的元素。如果它无法正确覆盖该方法，编译器将发出错误
        @Deprecated - 表示该元素已弃用且不应使用。如果程序使用标有此批注的方法，类或字段，编译器将发出警告
        @SuppressWarnings - 告诉编译器禁止特定警告。在与泛型出现之前编写的遗留代码接口时最常用的
        @FunctionalInterface - 在Java 8中引入，表明类型声明是一个功能接口，可以使用Lambda Expression提供其实现

    Java8以前的版本使用注解有一个限制是相同的注解在同一位置只能使用一次，不能使用多次。
    Java 8 引入了重复注解机制，这样相同的注解可以在同一地方使用多次。重复注解机制本身必须用 @Repeatable 注解。
    实际上，重复注解不是一个语言上的改变，只是编译器层面的改动，技术层面仍然是一样的。
     */

    public @interface AnnotationSample {
        // 下面编译报错, 返回类型必须是基本类型，String，Class，Enum 或者 他们的数组类型之一
        // Object test();
        String type();
        String[] types();
        int count();
    }

    @Test
    public void annotationGrammar() {

    }

    /*
    并发锁总共有4种状态：无锁状态、偏向锁状态、轻量级锁状态和重量级锁状态，
    每种状态在并发竞争情况下需要消耗的资源由低到高，性能由高到低。
    重量级锁需要通过操作系统在用户态与核心态之间切换，就像它的名字是一个重量级操作，
    这也是synchronized效率不高的原因，JDK1.6对synchronized进行了优化，
    引入了偏向锁与轻量级锁，提高了性能降低了资源消耗。

    无锁：  1.不存在竞争。没有对资源进行锁定，所有线程都能够访问到同一资源。
           2.存在竞争，采用乐观锁的方式，同步线程。CAS。
    偏向锁：只被一个线程获取到锁，判断锁标记位是01，是否是偏向锁，判断线程ID是否是当前线程，
        如果是，说明还依然是偏向锁。如果不是，说明不止一个线程竞争锁，则升级为轻量级锁。
    轻量级锁，当锁标志位是00是，则知道当前锁为轻量级锁，线程会在虚拟机栈中开辟一个为Lock Record的空间，
        Lock Record存放的是Mark Word副本，以及owner指针，线程通过CAS获得锁，那么将对象头中的Mark Word信息复制到Lock Record中，并且将owner指针指向该对象，并且对象头中前30个bit将生成一个指针指向Lock Record，实现线程和对象锁的绑定。其他线程将会自旋等待，自旋相当于cpu空转，长时间自旋浪费cpu资源，进了优化提出了适应性自旋，自旋时间不在固定，而是由上一个锁自旋的时间和锁状态来决定自旋时间。当自旋的线程数超过一个，轻量级锁升级为重量级锁。
    重量级锁：通过Mointor对线程进行控制，使用操作系统本身的互斥量（mutex lock）来实现的，
    这个只能通过系统调用来实现，所以需要切换到内核态。进程从用户态与内核态之间的切换，是一个开销较大的操作。

    synchronized 锁升级原理：在锁对象的对象头里面有一个 threadid 字段，
    在第一次访问的时候 threadid 为空，jvm 让其持有偏向锁，并将 threadid 设置为其线程 id，
    再次进入的时候会先判断 threadid 是否与其线程 id 一致，如果一致则可以直接使用此对象，
    如果不一致，则升级偏向锁为轻量级锁，通过自旋循环一定次数来获取锁，执行一定次数之后，
    如果还没有正常获取到要使用的对象，此时就会把锁从轻量级升级为重量级锁，
    此过程就构成了 synchronized 锁的升级。

    AbstractQueuedSynchronizer类（AQS）查看AbstractQueuedSynchronizerDemo类
     */

    /*
    读锁的作用：
        读锁自然也是为了避免原子性问题，比如一个long型参数的写操作并不是原子性的，如果允许同时读和写，
        那读到的数很可能是就是写操作的中间状态，比如刚写完前32位的中间状态。long型数都如此，
        而实际上一般读的都是复杂的对象，那中间状态的情况就更多了。

        读锁可以实现对读线程不阻塞，而对写线程阻塞；因此加读锁期间，所有读线程都可以操作，所有写线程等待
     */

    /*
    用户线程和守护线程几乎一样，唯一的不同之处在于如果用户线程已经全部退出运行，只剩下守护线程存在了,
    JVM也就退出了。因为当所有非守护线程结束时，没有了被守护者，守护线程也就没有工作可做，
    当然也就没有继续执行的必要了，程序就会终止，同时会杀死所有的"守护线程"，
    也就是说只要有任何非守护线程还在运行，程序就不会终止

    需要注意：守护线程和创建它的线程没有关系
     */
    @Test
    public void daemonThread() throws Exception {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        Thread outerThread = new Thread(() -> {
            // 构建一些守护线程，并运行
            Thread daemonThread = new Thread(() -> {
                try {
                    countDownLatch.await();
                    System.out.println("do something");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            daemonThread.setDaemon(true);
            daemonThread.start();

            // 父线程结束
            System.out.println("outerThread线程运行结束");
        });
        outerThread.setName("outerThread");
        outerThread.start();

        Thread.sleep(1000);
        countDownLatch.countDown();

        Thread.sleep(2000);
        System.out.println("主线程结束");
    }

    private static class InterruptThread extends Thread {
        private Boolean flag = true;
        private CountDownLatch countDownLatch;

        public InterruptThread(CountDownLatch countDownLatch) {
            this.countDownLatch = countDownLatch;
        }
        public void setFlag(boolean flag) {
            this.flag = flag;
        }

        @Override
        public void run() {
            countDownLatch.countDown();
            try {
                while (flag) {

                }
                System.out.println("线程循环结束......");
                Thread.sleep(10 * 60 * 1000);
                System.out.println("线程睡眠结束......");
            } catch (Exception e) {
                System.out.println("线程异常结束......" + e.getClass() + " : " + e.getMessage());
            }
        }
    }
    @Test
    public void threadInterruptGrammar() throws Exception {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        InterruptThread t1 = new InterruptThread(countDownLatch);
        t1.start();
        countDownLatch.await();

        /*
        如果一个线程由于等待某些事件的发生而被阻塞，又该怎样停止该线程呢？这种情况经常会发生，
        比如当一个线程由于需要等候键盘输入而被阻塞，或者调用Thread.join()方法，
        或者 Thread.sleep()方法，在网络中调用ServerSocket.accept()方法，
        或者调用了 DatagramSocket.receive()方法时，都有可能导致线程阻塞，
        使线程处于处于不可运行状态时，即使主程序中将该线程的共享变量设置为 true，
        但该线程此时根本无法检查循环标志，当然也就无法立即中断。这里我们给出的建议是，
        不要使用 stop()方法，而是使用 Thread 提供的interrupt()方法，
        因为该方法虽然不会中断一个正在运行的线程，但是它可以使一个被阻塞的线程抛出一个中断异常，
        从而使线程提前结束阻塞状态，退出堵塞代码。
         */
        // interrupt()方法的使用效果并不像for+break语句那样，马上就停止循环。
        // 调用interrupt方法是在当前线程中打了一个停止标志，并不是真的停止线程。
        // 如果是处于正在工作之中，而不是sleep等状态，是不会立马停止的，只是设置一个标志
        t1.interrupt();

        // 如果没有抛出异常，则isInterrupted方法返回true；
        // 因为上面调用了中断，会设置中断标志，但是如果异常抛出了InterruptedException异常，会将中断标志清除
        // interrupted(): 测试当前线程是否已经中断。线程的“中断状态”由该方法清除。
        //                换句话说，如果连续两次调用该方法，则第二次调用将返回 false。
        // isInterrupted(): 测试线程是否已经中断。线程的“中断状态”不受该方法的影响。
        if (t1.isInterrupted()) {
            System.out.println("线程处于中断状态......");
        } else {
            System.out.println("线程不是中断状态......");
        }
        // 调用stop方法，可以停止线程，但是该线程是戛然而止，里面的动作执行了一半，被终止了，这种方案是不可取的
        // t1.stop(); // 这个方法被废弃了，不建议使用
        // 1. 如果是sleep等，可以使用interrupt方法，否则可以使用2的方式
        // 2. 建议使用标识符的方法, 指定一个变量，想让其停止的时候，将变量重新赋值。
        System.out.println("调用中断方法结束......");

        t1.setFlag(false);
        System.out.println("设置flag为false......");

        // 让某个线程先执行后再执行另一个线程任务
        Thread first = new Thread(() -> {
                for (int i = 0; i <10; i++) {
                    System.out.println("first ----> " + i);
                }
        });
        Thread second = new Thread(() -> {
            for (int i = 0; i <10; i++) {
                System.out.println("second ----> " + i);
            }
        });
        first.start();
        // 等待first线程执行完成，再执行second线程
        first.join();
        second.start();

        Thread.sleep(10 * 1000);
    }

    @Test
    public void waitNotifyNotifyAllGrammar() throws Exception {
        /*
        首先，wait()方法内部是释放锁的，因此调用方法之前之前要先获得锁，
        而锁在同步块开始的时候获得，结束时释放，即同步块内为持有锁的阶段。

        现在我们考虑下这个潜在的竞争条件怎么解决。可以通过使用Java提供的synchronized关键字和锁来解决这个竞争条件。
        为了调用wait()，notify()和notifyAll()方法，我们必须获取调用这些方法的对象上的锁。
        由于wait()方法在等待前释放了锁并且在wait()方法返回之前重新获得了锁，
        我们必须使用这个锁来确保检查条件（缓冲区是否已满）和设置条件（从缓冲区取产品）是原子的，
        而这可以通过同步块或者同步方法实现。

        wait是让使用wait方法的对象等待，暂时先把对象锁给让出来，给其它持有该锁的对象用，
        其它对象用完后再告知（notify）等待的那个对象可以继续执行了，因此，只有在synchronized块中才有意义
        (否则，如果大家并不遵循同步机制，那还等谁呢？根本没人排队，也就谈不上等待和唤醒了)
         */

        Object lock = new Object();

        new Thread(() -> {
            synchronized (lock) {
                try {
                    // do something
                    Thread.sleep(5000);

                    System.out.println("做完某件事，条件达成，并唤醒等待的线程");
                    // 只是通知wait线程条件达成了，可以继续运行了，但是方法内部并不释放锁
                    // 因此，wait并不能立马获取到锁，然后执行，而是需要等待这个方法所在的代码块执行完成后释放锁，
                    // wait方法才能获取到锁，然后继续后续工作
                    lock.notify();
                    // lock.notifyAll();

                    System.out.println("notify线程继续后续工作");
                    Thread.sleep(3000);
                    System.out.println("notify线程完成后续工作");

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

        synchronized (lock) {
            // 没有等待对象，执行notify操作无实际影响
            lock.notify();
            System.out.println("在wait中会释放lock锁，然后等待，直到其他线程完成条件后释放锁，然后获取锁后再执行后续操作");
            lock.wait();
            System.out.println("条件满足后，wait线程做某些工作");
            Thread.sleep(5000);
            System.out.println("条件满足后，wait线程做完工作");
        }
    }

    @Test
    public void jvmGrammar() {
        /*
        可以通过 java.lang.Runtime 类中与内存相关方法来获取剩余的内存，总内存及最大堆内存。
        通过这些方法你也可以获取到堆使用的百分比及堆内存的剩余空间。
         */
        Runtime runtime = Runtime.getRuntime();
        // 方法返回剩余空间的字节数
        System.out.println(runtime.freeMemory());
        // 方法总内存的字节数
        System.out.println(runtime.totalMemory());
        // 返回最大内存的字节数
        System.out.println(runtime.maxMemory());
    }

    /*
    集合的比较，具体看集合代码或者网上的结论，如：

    SynchronizedMap 和 ConcurrentHashMap
        SynchronizedMap和Hashtable一样，实现上在调用Map的方法时对整个方法加锁
        ConcurrentHashMap的实现却更加精细，他对要操作的桶加锁，而不是整个加锁，
        所以ConcurrentHashMap在性能以及安全性方面更加有优势。
        即使在遍历map时，如果其他线程试图对map进行数据修改，也不会抛出ConcurrentModificationException

    ConcurrentHashMap 和 HashTable
        ConcurrentHashMap与HashTable都可以用于多线程的环境，但是当Hashtable的大小增加到一定的时候，
        性能会急剧下降，因为迭代时需要被锁定很长时间。因为ConcurrentHashMap引入了分割（segmentation），
        不论它变得多么大，仅仅需要锁定map的某个部分，而其他的线程不需要等到迭代完成才能访问map。
        简而言之，在迭代的过程中，ConcurrentHashMap仅仅锁定map的某个部分，而Hashtable则会锁定整个map。

        HashTable虽然性能上不如ConcurrentHashMap，单并不能完全被取代，两者的迭代器的一致性不同的，
        HashTable的迭代器是强一致性的，而ConcurrentHashMap是弱一致性的。
        ConcurrentHashMap的get，clear，iterator都是弱一致性的。

        get方法是弱一致的，是什么含义？可能你期望往ConcurrentHashMap底层数据结构中加入一个元素后，
        立马能对get可见，但ConcurrentHashMap并不能如你所愿。换句话说，put操作将一个元素加入到底层数据结构后，
        get可能在某段时间内还看不到这个元素，若不考虑内存模型，单从代码逻辑来看，却是应该可以看到的。

    HashSet 和 TreeSet 和 ListHashSet
        TreeSet 和 Hashset 和 ListHashSet 都不可放2个相同的元素，都不是线程安全的
        TreeSet底层是TreeMap实现的，很多api都是利用TreeMap来实现的，数据是自动排好序的，不允许放入null值。
        HashSet底层是HashMap实现的，很多api都是利用HashMap来实现的，数据是自动排好序的，可以放入null，但只能放入一个null
        ListHashSet底层是LinkedHashMap, 很多api都是利用HashMap来实现的，数据是无序的，可以放入null，但只能放入一个null
            根据元素的hashCode值来决定元素的存储位置，但是它同时使用链表维护元素的次序。
            也就是说，当遍历该集合时候，LinkedHashSet将会以元素的添加顺序访问集合的元素。

    线程安全的Set：
        1. Collections.synchronizedSet(new HashSet<>());
        2. 使用JUC包里面的CopyOnWriteArraySet
            CopyOnWriteArraySet使用Array而不是HashMap来存储数据。
            这意味着像contains()或remove()这样的操作有 O(n) 的复杂度，
            而当使用由ConcurrentHashMap 支持的 Set 时，复杂度是 O(1)。
        3. 通过new ConcurrentHashMap<>().keySet() 或者 通过new ConcurrentHashMap<>().keySet("默认value值") 来实现的
            但是如果通过无参数的keySet()这种方式来创建一个集合，那么这个集合是不能添加元素的（可以通过map对象来修改）
        4. 通过Collections.newSetFromMap(new ConcurrentHashMap<>())创建集合

    BlockingQueue （参考：https://blog.csdn.net/Jiangtagong/article/details/121855261）
        队列是一种常见的数据结构，Java中以Queue的形式存在，继承Collection。
        而BlockingQueue又继承Queue，是一种具有阻塞线程功能的特殊队列。

        BlockingQueue的实现是基于ReentrantLock，最常用的场景是：生产者/消费者模式，
        不需要额外的实现线程的同步和唤醒。场景的几种阻塞队列：
        1. ArrayBlockingQueue：由数组组成的有界阻塞队列
            由数组结构实现，队列的容量是固定的。存、取数据默认使用一把非公平锁，无法实现真正意义上的存、取数据的并发执行。
            由于是数组实现，容量固定不变，因此不容易出现内存占用率过高等问题，但如果容量过小，取数据比存数据的数据慢，
            这样会造成很多线程进入阻塞状态，可以使用offer()方法达到不阻塞线程，在高并发、吞吐量高的情况下，由于存、取共用一把锁，不推荐使用。
        2. LinkedBlockingQueue：由链表组成的有界阻塞队列
            由链表结构实现，队列容量默认Integer.MAX_VALUE，存、取数据的操作分别用于独立的锁，可以实现存、取的并发执行。
            基于链表实现，数据的新增和移除速度比数组快，但是每次存、取数据都会有Node对象的新建和移除，因此也会存在GC影响性能的可能。
            默认容量很大，因此存储数据的线程基本不会阻塞，但是取数据的速度过低，内存占用可能会飙升。
            存、取操作锁分离，所以使用有并发和吞吐量要求的场景。
            Executors,newFixedThreadPool()线程池的默认队列就是使用该类型的
        3. LinkedTransferQueue：由链表组成的无界队列
            加入的元素必须被消费才可以继续加入，具体看：com.wang.jdk.collection.TransferQueueDemo
        4. PriorityBlockingQueue：优先级排序的无界阻塞队列
            基于数组实现，队列容量最大值为Integer.MAX_VALUE-8（-8是因为数组的对象头）。根据传入的优先级进行排序，保证优先级来消费。
            优先级阻塞队列中存在一次排序，根据优先级来将数据放入到头部或者尾部；
            排序带来的损耗因素，由二叉树最小堆排序算法来降低。
        5. DelayQueue：优先级排序的无界阻塞队列
            延迟队列，基于优先级队列来实现存储元素，必须实现Delayed接口（Delayed接口继承了Comparable接口）
            由于基于优先级队列实现，这个优先级是根据时间排序的，比如：订单超时取消功能。用户订单未支付开始倒计时。

            一些使用见：com.wang.jdk.collection.DelayQueueDemo
        6. SynchronousQueue：不存储元素的阻塞队列
            利用双栈双队列算法的无空间队列或栈任何一个对SynchronousQueue写需要等到一个对SynchronousQueue的读操作，
            任何一个个读操作需要等待一个写操作。没有容量，是无缓冲等待队列，是一个不存储元素的阻塞队列，会直接将任务交给消费者。
            可以理解成交换通道，不存储任何元素，提供者和消费者是需要组队完成工作，缺少任何一个将会阻塞线程，直到等到配对为止。
            Executors.newCachedThreadPool()使用该类型队列

            一些使用见：com.wang.jdk.collection.DelayQueueDemo
        7. LinkedBlockingDeque：由链表组成的双端阻塞队列


    HashMap在多线程下会出现死循环和丢数据等问题
        主要是多线程同时put时，如果同时触发了rehash操作，会导致HashMap中的链表中出现循环节点（同时也可能会丢失数据），
        进而使得后面get的时候（get一个不在链表成环的桶中的元素），会死循环。
        （参考：https://blog.csdn.net/hyq413950612/article/details/122309301）


    一些集合类的使用看com.wang.jdk.collection包下的示例
     */
    @Test
    public void jdkSetGrammar() {
        ConcurrentHashMap<String, String> concurrentHashMap = new ConcurrentHashMap<>();
        concurrentHashMap.clear();
        // 这种方式创建的Set，与ConcurrentHashMap中的Set是同一个对象，因此会互相影响
        // 支持向Set中添加和删除元素，会反映到map中，通过Set添加元素时，该元素key在map中对应的value为传入的值("default")
        // 由于ConcurrentHashMap是线程安全的，所有对应的Set也是线程安全的
        Set<String> concurrentSetWithDefaultValue = concurrentHashMap.keySet("default");
        concurrentSetWithDefaultValue.add("a");
        concurrentSetWithDefaultValue.add("b");
        concurrentHashMap.put("c", "cc");
        concurrentHashMap.put("d", "dd");
        concurrentSetWithDefaultValue.remove("a");
        concurrentSetWithDefaultValue.remove("c");
        System.out.println("---------------------------");
        // 输出: [b, d]
        System.out.println(concurrentSetWithDefaultValue);
        // 输出: {b=default, d=dd}
        System.out.println(concurrentHashMap);

        concurrentHashMap.clear();
        // 这种方式创建的Set，与ConcurrentHashMap中的Set也是同一个对象，因此会互相影响
        // 不支持向Set添加新元素。因此，如果我们尝试调用add()或addAll() 之类的方法，我们将得到 UnsupportedOperationException。
        // 但是remove(object)或clear()之类的操作是支持的，可以正常工作，
        // 但我们需要注意Set上的任何更改都将反映在原始映射map中
        Set<String> concurrentSet = concurrentHashMap.keySet();
        // 报异常：UnsupportedOperationException
        // 可以看提出的KeySetView.add方法，如果没有给默认的value，value为空，而add方法当对象中value为空时会抛异常
        //concurrentSet.add("a");
        concurrentHashMap.put("b", "bb");
        concurrentHashMap.put("c", "bb");
        concurrentSet.remove("c");
        System.out.println("---------------------------");
        // 输出: [b]
        System.out.println(concurrentSet);
        // 输出: {b=bb}
        System.out.println(concurrentHashMap);

        // 通过Collections来创建线程安全的Set
        // 底层是通过内部类SetFromMap覆盖Set方法，然后调用map的方法或者该map对应的set对象的方法来实现（具体看源码）
        // 由于传入的ConcurrentHashMap是线程安全的，所以Set也是线程安全的
        Set<String> concurrentSetByCollections = Collections.newSetFromMap(new ConcurrentHashMap<>(16));
        concurrentSetByCollections.add("a");
        System.out.println("---------------------------");
        // 输出: [a]
        System.out.println(concurrentSetByCollections);

    }

    @Test
    public void jdkArrayListGrammar() {
        List<String> strs = new ArrayList<>();
        strs.add("a");
        strs.add("b");
        strs.add("c");
        // 不会抛异常，remove方法中没有检查，但是只能输出a,c;
        // 因为移除a之后大小变为2了，b,c都前移了一位，输出a后，i变成2，所以b就输出不了了
        //for (int i = 0; i < strs.size(); i++) {
        //    String str = strs.get(i);
        //    System.out.println(str);
        //    strs.remove("a");
        //}

        // 抛ConcurrentModificationException异常，因此remove会改变modCount值，
        // 但是iterator()方法返回的ArrayList.Itr迭代器对象(iterator)中的expectedModCount没有改变
        // 导致不一致，在next()方法中的checkForComodification()方法会做相关检查，从而抛出异常
        Iterator<String> iterator = strs.iterator();
        //strs.remove("a");
        while (iterator.hasNext()) {
            String next = iterator.next();
            // 不会抛出异常，因此迭代器的remove方法会修改 expectedModCount=modCount
            if ("a".equals(next)) {
                iterator.remove();
            }
        }

        // 不会抛异常，foreach是通过迭代器迭代的
        for (String str : strs) {
            strs.remove("a");
        }
    }

    @Test
    public void jdkVectorGrammar() throws Exception {
        Vector<String> vectors = new Vector();
        vectors.add("a");
        vectors.add("b");
        vectors.add("c");
        // 会抛异常，foreach是通过迭代器迭代的; 原因和ArrayList一样
        //for (String str : vectors) {
        //    vectors.remove("a");
        //}

        CountDownLatch runLatch = new CountDownLatch(1);
        CountDownLatch forLatch = new CountDownLatch(1);
        new Thread(() -> {
            try {
                runLatch.await();
                System.out.println("pre add d");
                vectors.add("d");
                System.out.println("post add d");
                forLatch.countDown();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
        // 会抛ConcurrentModificationException异常，因为上面的线程增加了元素，修改modCount的值
        for (String str : vectors) {
            runLatch.countDown();
            forLatch.await();
            System.out.println(str);
        }

        // 会出现死循环，这个会拿锁，而add方法也需要拿锁（但是拿不到，所以会停的add方法处），所以死锁了
        //vectors.forEach(str -> {
        //    try {
        //        runLatch.countDown();
        //        System.out.println("pre await");
        //        forLatch.await();
        //        System.out.println("post await");
        //        System.out.println(str);
        //    } catch (Exception e) {
        //        e.printStackTrace();
        //    }
        //});
        /* 一般有2种解决办法：
        　　1）在使用iterator迭代的时候使用synchronized或者Lock进行同步；
        　　2）使用并发容器CopyOnWriteArrayList代替ArrayList和Vector。
        */
    }


    @Test
    public void jdkConcurrentHashMapGrammar() throws Exception {
        ConcurrentHashMap<String, String> concurrentHashMap = new ConcurrentHashMap<>();
        concurrentHashMap.put("a", "b");

        CountDownLatch runLatch = new CountDownLatch(1);
        CountDownLatch forLatch = new CountDownLatch(1);
        new Thread(() -> {
            try {
                runLatch.await();
                concurrentHashMap.put("aa", "aa");
                forLatch.countDown();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
        // 不会抛ConcurrentModificationException异常
        //for (Map.Entry<String, String> entry : concurrentHashMap.entrySet()) {
        //    runLatch.countDown();
        //    forLatch.await();
        //    System.out.println(entry.getKey() + " : " + entry.getValue());
        //}

        // 不会抛ConcurrentModificationException异常
        concurrentHashMap.forEach((key, value) -> {
            try {
                runLatch.countDown();
                forLatch.await();
                System.out.println(key + " : " + value);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Test
    public void jdkArrayDequeGrammar() {
        /*
        Java里有一个叫做Stack的类，却没有叫做Queue的类（它是个接口名字）。当需要使用栈时，Java已不推荐使用Stack，
        而是推荐使用更高效的ArrayDeque；既然Queue只是一个接口，当需要使用队列时也就首选ArrayDeque了（次选是LinkedList）。
        官方更推荐使用ArrayDeque用作栈和队列

        从名字可以看出ArrayDeque底层通过数组实现，为了满足可以同时在数组两端插入或删除元素的需求，
        该数组还必须是循环的，即循环数组（circular array），也就是说数组的任何一点都可能被看作起点或者终点。
        ArrayDeque是非线程安全的（not thread-safe），当多个线程同时使用的时候，需要程序员手动同步；
        另外，该容器不允许放入null元素。
         */
        Deque<String> deque = new ArrayDeque<>();
        deque.addFirst("a");
    }

    /*
    每个 MyISAM 表格以三种格式存储在磁盘上：
        表定义文件具有“.frm”扩展名
        数据文件具有“.MYD”（MYData）扩展名
        索引文件具有“.MYI”（MYIndex）扩展名

    MySQL myisamchk实用程序主要用来获得有关数据库表的信息或检查、修复、优化他们。
    myisamchk适用MyISAM表(对应.MYI和.MYD文件的表)。myisamchk的功能类似mysqlcheck，但其工作不同。
    myisamchk和mysqlcheck主要差别是当mysqld服务器在运行时必须使用mysqlcheck，
    而myisamchk应用于服务器没有运行时。

    行级锁是 MySQL 中锁定粒度最细的一种锁，表示只针对当前操作的行进行加锁。
        行级锁能大大减少数据库操作的冲突，其加锁粒度最小，但加锁的开销也最大。行级锁分为共享锁和排他锁。
        特点：开销大，加锁慢；会出现死锁；锁定粒度最小，发生锁冲突的概率最低，并发度也最高。
    表级锁是 MySQL 中锁定粒度最大的一种锁，表示对当前操作的整张表加锁，它实现简单，资源消耗较少，
        被大部分 MySQL 引擎支持。最常使用的 MyISAM 与 InnoDB 都支持表级锁定。
        表级锁定分为表共享读锁（共享锁）与表独占写锁（排他锁）。
        特点：开销小，加锁快；不会出现死锁；锁定粒度大，发出锁冲突的概率最高，并发度最低。
    页级锁是 MySQL 中锁定粒度介于行级锁和表级锁中间的一种锁。表级锁速度快，但冲突多，行级冲突少，但速度慢。
        因此，采取了折衷的页级锁，一次锁定相邻的一组记录。BDB 支持页级锁。
        特点：开销和加锁时间界于表锁和行锁之间；会出现死锁；锁定粒度界于表锁和行锁之间，并发度一般。

    MySQL常用存储引擎的锁机制
        MyISAM 和 Memory 采用表级锁（table-level locking）
        BDB 采用页级锁（page-level locking）或表级锁，默认为页级锁；
        InnoDB 支持行级锁（row-level locking）和表级锁，默认为行级锁。

    InnoDB 行锁是通过给索引上的索引项加锁来实现的，这一点 MySQL 与 Oracle 不同，后者是通过在数据块中对相应数据行加锁来实现的。
    InnoDB 这种行锁实现的特点意味着：只有通过索引条件检索数据，InnoDB 才使用行级锁，否则，InnoDB 将使用表锁。
    在实际应用中，要特别注意 InnoDB 行锁的这一特性，不然的话，可能导致大量的锁冲突，从而影响并发性能。
    在不通过索引条件查询的时候，InnoDB 确实使用的是表锁，而不是行锁。

    即便在条件中使用了索引字段，但是否使用索引来检索数据是由 MySQL 通过判断不同的执行计划的代价来决定的。
    如果 MySQL 认为全表扫描效率更高，比如对一些很小的表，它就不会使用索引，这种情况下 InnoDB 将使用表锁，而不是行锁。
    因此，在分析锁冲突时，别忘了检查 SQL 的执行计划，以确认是否真正使用了索引。

    MyISAM 中是不会产生死锁的，因为 MyISAM 总是一次性获得所需的全部锁，要么全部满足，要么全部等待。
    而在 InnoDB 中，锁是逐步获得的，就造成了死锁的可能。

    在 MySQL 中，行级锁并不是直接锁记录，而是锁索引。
        索引分为主键索引和非主键索引两种，如果一条 SQL 语句操作了主键索引，MySQL就会锁定这条主键索引
        如果一条 SQL 语句操作了非主键索引，MySQL 就会先锁定该非主键索引，再锁定相关的主键索引。
        在进行UPDATE、DELETE操作时，MySQL不仅锁定WHERE条件扫描过的所有索引记录，而且会锁定相邻的键值，即所谓的next-key locking.

        当两个事务同时执行，一个锁住了主键索引，在等待其他相关索引；另一个锁定了非主键索引，在等待主键索引。这样就会发生死锁。

        发生死锁后，InnoDB 一般都可以检测到，并使一个事务释放锁回退，另一个获取锁完成事务。
        避免死锁的常用方法：
            如果不同程序会并发存取多个表，尽量约定以相同的顺序访问表，可以大大降低发生死锁的可能性；
            在同一个事务中，尽可能做到一次锁定所需要的所有资源，减少死锁产生概率；
            对于非常容易产生死锁的业务部分，可以尝试使用升级锁定颗粒度，通过表级锁定来减少死锁产生的概率。
     */

}

/*
一个Java源文件中可以定义多个类，但是最多只有一个类被public修饰，并且这个类的类名与文件名必须相同。
若这个文件中没有public的类，则文件名可随便命名(前提是符合规范)。
要注意的是，当用javac指令编译有多个类的Java源文件时，它会给该源文件中的每一个类生成一个对应的.class 文件。
 */
class OtherClassInSameFile {

    public static void main(String[] args) {
        System.out.println("同一个文件中的另一个类");
    }
}
