package com.wang.jvm.classloader;

/*
双亲委派模型。但不是所有类加载都遵守这个模型，例如下面的例子：
    由于双亲委派模型在 JDK 1.2 之后才被引入，但是类加载器的概念和抽象类 java.lang.ClassLoader
        则在 Java 的第一个版本中就已经存在，面对已经存在的用户自定义类加载器的代码，
        Java 设计者们引入双亲委派模型时不得不做出一些妥协，为了兼容这些已有的代码，
        无法再以技术手段避免 loadClass() 被子类覆盖的可能性
    双亲委派模型的第二次"被破坏"是由这个模型自身的缺陷导致的，
        双亲委派很好地解决了各个类加载器协作时基础类型的一致性问题(越基础的类由越上层的加载器进行加载)，
        基础类型之所以被称为"基础"，是因为它们总是作为被用户代码继承、调用的 API 存在，
        但程序设计往往没有绝对不变的完美规则，如果有基础类型又要调用回用户代码，那该怎么办？
        这并非是不可能出现的事情，一个典型的例子便是 JNDI 服务，JNDI 现在已经是 Java 的标准服务，
        它的代码由启动类加载器来完成加载(在 JDK 1.3 时加入到 rt.jar)，肯定属于 Java 中很基础的类型了。
        但 JNDI 存在的目的就是对资源进行查找和集中管理，
        它需要调用由其它厂商实现并部署在应用程序的 ClassPath 下的 JNDI 服务提供者接口(Service Provider Interface. SPI) 的代码，
        现在问题来了，启动类加载器时绝对不可能认识、加载这些代码的，那该怎么办？(SPI：在 Java 平台中，
        通常把核心类 rt.jar 中提供外部服务、可由应用层自行实现的接口称为 SPI)
        为了解决这个困境，Java 的设计团队只好引入了一个不太优雅的设计：
        线程上下文类加载器(Thread Context ClassLoader)。
        这个类加载器可以通过 java.lang.Thread 类的 setContextClassLoader() 方法进行设置，
        如果创建线程时还未设置，它将会从父线程中继承一个，如果在应用程序的全局范围内都没有设置过的话，
        那这个类加载器默认就是应用程序类加载器
        有了线程上下文类加载器，程序就可以做一些"舞弊"的事情了。
        JNDI 服务使用这个线程上下文类加载器去加载所需的 SPI 服务代码。
        这是一种父类加载器去请求子类加载器完成类加载的行为，
        这种行为实际上是打通了双亲委派模型的层次结构来逆向使用类加载器，已经违背了双亲委派模型的一般性原则，
        但也是无可奈何的事情。Java 中涉及 SPI 的加载基本上都采用这种方式来完成，
        例如 JNDI、JDBC、JCE、JAXB 和 JBI 等。
    双亲委派模型的第三次"被破坏"是由于用户对程序动态性的追求而导致的。如：代码热替换(Hot Swap)、模块热部署(Hot Deployment)等
双亲委派模型的优势：
    避免类的重复加载，确保一个类的全局唯一性
    保护程序安全，防止核心 API 被随意篡改
双亲委派模型的劣势：
    顶层的 ClassLoader 无法访问底层的 ClassLoader 所加载的类，在jdbc等场景下会有问题，因此这种场景下会破坏双亲委派模型

启动类加载器(引导类加载器 Bootstrap ClassLoader)
    这个类加载使用 C/C++ 语言实现的，嵌套在 JVM 内部
    它用来加载 Java 的核心库(JAVA_HOME/jre/lib/rt.jar 或 sun.boot.class.path 路径下的内容)。用于提供 JVM 自身需要的类
    并不继承自 java.lang.ClassLoader，没有父加载器
    出于安全考虑，Bootstrap 启动类加载器之加载包名为 java、javax、sun 等开头的类
    加载扩展类和应用程序类加载器，并指定为他们的父类加载器
扩展类加载器(Extension ClassLoader)
    Java 语言编写，由 sun.misc.Launcher$ExtClassLoader 实现
    继承于 ClassLoader 类
    父类加载器为启动类加载器
    从 java.ext.dirs 系统属性所指定的目录中加载类库，或从 JDK 的安装目录的 jre/lib/ext 子目录下加载类库。如果用户创建的 JAR 放在此目录下，也会自动由扩展类加载器加载
应用程序类加载器(系统类加载器，AppClassLoader)
    Java 语言编写，由 sun.misc.Launcher$AppClassLoader 实现
    继承于 ClassLoader 类
    父类加载器为扩展类加载器
    它负责加载环境变量 classpath 或系统属性 java.class.path 指定路径下的类库
    应用程序中的类加载器默认是系统类加载器
    它是用户自定义类加载器的默认父加载器
    通过 ClassLoader 的 getSystemClassLoader() 方法可以获取到该类加载器

自定义java.lang.String类，但是在加载自定义String类的时候会率先使用引导类加载器加载，
而引导类加载器在加载的过程中会先加载JDK自带的文件（rt.jar包中java\lang\String.class），
这样可以保证对Java核心源代码的保护，从而自定义的和jdk中一样的类（包名和类名都一样）无效，
不会覆盖jdk中的类，这就是沙箱安全机制。

Class.forName() 与 ClassLoader.loadClass()
    Class.forName()：是一个静态方法，最常用的是 Class.forName(String className)；
        根据传入的类的权限定名返回一个 Class 对象。该方法在将 Class 文件加载到内存的同时，会执行类的初始化。如：
        Class.forName("com.atguigu.java.HelloWorld");
    ClassLoader.loadClass() 这是一个实例方法，需要一个 ClassLoader 对象来调用该方法。
        该方法将 Class 文件加载到内存时，并不会执行类的初始化，直到这个类第一次使用时才进行初始化。
        该方法因为需要得到一个 ClassLoader 对象，所以可以根据需要指定使用哪个类加载器，
        如：ClassLoader c1 = .....; c1.loadClass("com.atguigu.java.HelloWorld");
 */

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * 通过自定义的ClassLoader实现热加载
 */
public class MyClassLoader extends ClassLoader {
    private String byteCodePath;

    /**
     * 默认的父类加载器为 AppClassLoader
     * @param byteCodePath
     */
    public MyClassLoader(String byteCodePath) {
        this.byteCodePath = byteCodePath;
    }

    public MyClassLoader(ClassLoader parent, String byteCodePath) {
        super(parent);
        this.byteCodePath = byteCodePath;
    }

    @Override
    protected Class<?> findClass(String className) throws ClassNotFoundException {
        // 获取字节码文件的完整路径
        String fileName = byteCodePath + className + ".class";
        BufferedInputStream bis = null;
        ByteArrayOutputStream baos = null;

        try {
            // 获取一个输入流
            bis = new BufferedInputStream(new FileInputStream(fileName));
            // 获取一个输出流
            baos = new ByteArrayOutputStream();

            // 具体读入数据并写出数据的过程
            int len;
            byte[] data = new byte[1024];
            while ((len = bis.read(data)) != -1) {
                baos.write(data, 0, len);
            }

            // 获取内存中完整的字节数据的数据
            byte[] bytes = baos.toByteArray();
            // 调用defineClass()方法，将字节数组的数据转换为Class的实例
            return defineClass(null, bytes, 0, bytes.length);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (Objects.nonNull(baos)) {
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                if (Objects.nonNull(bis)) {
                    bis.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    /**
     * 测试方法， 先输出old MyClass 一段时间后再修改代码，然后通过javac重新编译，实现热加载，此时输出new MyClass，
     *      javac MyClass.java
     */
    public static void main(String[] args) throws Exception {
        while (true) {
            String byteCodePath = "D:\\idea_project\\study_skill\\study_java\\src\\main\\java\\com\\wang\\classloader\\";
            MyClassLoader myClassLoader = new MyClassLoader(byteCodePath);
            Class<?> clazz = myClassLoader.loadClass("MyClass");
            System.out.println("加载此类的类加载器为： " + clazz.getClassLoader().getClass().getName());

            // 调用方法，看输出
            Object instance = clazz.newInstance();
            Method method = clazz.getMethod("test");
            method.invoke(instance);
            Thread.sleep(5000);
        }
    }
}
