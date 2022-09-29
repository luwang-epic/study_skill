package com.wang.spring.ioc;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 循环依赖，通过源码中的端点来查看spring通过3级缓存解决循环依赖的，特别是涉及aop的时候
 */
public class CircularDependencySample {

    public static void main(String[] args) {
        // 通过set注入
        cycleWithSetMethod();

        // 一个通过构造器注入， 另一个通过set注入
        cycleWithConstructor();
    }

    public static void cycleWithSetMethod() {
        // 如果不是单例的，无法解决循环依赖
        //String path = "file:D:\\idea_project\\study_skill\\study_spring\\src\\main\\java\\com\\wang\\spring\\ioc\\cycle_prototype.xml";
        // 如果是懒加载，可以解决
        //String path = "file:D:\\idea_project\\study_skill\\study_spring\\src\\main\\java\\com\\wang\\spring\\ioc\\cycle_lazy.xml";
        // 如果是单例的，可以解决
        String path = "file:D:\\idea_project\\study_skill\\study_spring\\src\\main\\java\\com\\wang\\spring\\ioc\\cycle_singleton.xml";
        ApplicationContext context = new ClassPathXmlApplicationContext(path);

        CircularDependencyA a = context.getBean(CircularDependencyA.class);
        a.sayHello();
        System.out.println(a);

        CircularDependencyB b = context.getBean(CircularDependencyB.class);
        b.sayHello();
        System.out.println(b);
    }

    public static void cycleWithConstructor() {
        // 如果c不是懒加载，会报错
        //String path = "file:D:\\idea_project\\study_skill\\study_spring\\src\\main\\java\\com\\wang\\spring\\ioc\\cycle_constructor.xml";
        // c设置为懒加载模式，不会报错
        String path = "file:D:\\idea_project\\study_skill\\study_spring\\src\\main\\java\\com\\wang\\spring\\ioc\\cycle_constructor_lazy.xml";
        ApplicationContext context = new ClassPathXmlApplicationContext(path);

        CircularDependencyC c = context.getBean(CircularDependencyC.class);
        c.sayHello();
        System.out.println(c);

        CircularDependencyD d = context.getBean(CircularDependencyD.class);
        d.sayHello();
        System.out.println(d);
    }

    public static class CircularDependencyA {
        private CircularDependencyB b;

        public void setB(CircularDependencyB b) {
            this.b = b;
        }

        public void sayHello() {
            System.out.println("a say hello...");
        }
    }


    public static class CircularDependencyB {
        private CircularDependencyA a;

        public void setA(CircularDependencyA a) {
            this.a = a;
        }

        public void sayHello() {
            System.out.println("b say hello...");
        }
    }

    public static class CircularDependencyAspect {
        public void before() {
            System.out.println("before");
        }

        public void after() {
            System.out.println("after");
        }
    }


    public static class CircularDependencyC {
        private CircularDependencyD d;

        public CircularDependencyC(CircularDependencyD d) {
            this.d = d;
        }

        public void sayHello() {
            System.out.println("c say hello...");
        }
    }

    public static class CircularDependencyD {
        private CircularDependencyC c;

        public void setC(CircularDependencyC c) {
            this.c = c;
        }

        public void sayHello() {
            System.out.println("d say hello...");
        }
    }
}
