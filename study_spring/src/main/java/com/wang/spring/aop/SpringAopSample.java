package com.wang.spring.aop;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/*
AOP的几个概念：
1. 切面（Aspect）：一个关注点的模块化，这个关注点可能会横切多个对象。
    事务管理是J2EE应用中一个关于横切关注点的很好的例子。在Spring AOP中，切面可以使用基于模式或者基于@Aspect注解的方式来实现。
2. 连接点（Joinpoint）: 在程序执行过程中某个特定的点，比如某方法调用的时候或者处理异常的时候。
    在Spring AOP中，一个连接点总是表示一个方法的执行。
3. 通知/增强（Advice）: 在特定的连接点，AOP框架执行的动作。
    各种类型的通知包括“around”、“before”和“throws”通知。通知类型将在下面讨论。
    许多AOP框架包括Spring都是以拦截器做通知模型，维护一个“围绕”连接点的拦截器链。spring aop advice的类型：
        1、前置通知（before advice），在目标方法执行之前执行。
        2、返回后通知（after returning advice），在方法正常执行结束之后的通知，可以访问到方法的返回值。
        3、抛出异常后通知（after throwing advice），在目标方法出现异常时执行的代码，可以访问到异常对象，且可以指定出现特定异常执行此方法。
        4、后置通知：（after[finally] advice），在目标方法执行之后执行（无论是否发生异常）。
        5、环绕通知：（around advice），可以实现上述所有功能。
4. 切入点（Pointcut）: 指定一个通知将被引发的一系列连接点的集合。AOP框架必须允许开发者指定切入点：
    例如，使用正则表达式。 Spring定义了Pointcut接口，用来组合MethodMatcher和ClassFilter，
    可以通过名字很清楚的理解， MethodMatcher是用来检查目标类的方法是否可以被应用此通知，
    而ClassFilter是用来检查Pointcut是否应该应用到目标类上
5. 引入（Introduction）: 添加方法或字段到被通知的类。 Spring允许引入新的接口到任何被通知的对象。
    例如，你可以使用一个引入使任何对象实现 IsModified接口，来简化缓存。
    Spring中要使用Introduction, 可有通过DelegatingIntroductionInterceptor来实现通知，
    通过DefaultIntroductionAdvisor来配置Advice和代理类要实现的接口
6. 目标对象（Target Object）: 包含连接点的对象。也被称作被通知或被代理对象
7. AOP代理（AOP Proxy）: AOP框架创建的对象，包含通知。在Spring中，AOP代理可以是JDK动态代理或者CGLIB代理。
8. 织入（Weaving）: 组装方面来创建一个被通知对象。这可以在编译时完成（例如使用AspectJ编译器），
    也可以在运行时完成。Spring和其他纯Java AOP框架一样，在运行时完成织入。

AOP用来封装横切关注点，具体可以在下面的场景中使用:
    Authentication 权限
    Caching 缓存
    Context passing 内容传递
    Error handling 错误处理
    Lazy loading 懒加载
    Debugging 调试
    logging, tracing, profiling and monitoring 记录跟踪　优化　校准
    Performance optimization 性能优化
    Persistence 持久化
    Resource pooling 资源池
    Synchronization 同步
    Transactions 事务
 */

/*
EnableAspectJAutoProxy注解作用
    主要是把AnnotationAwareAspectJAutoProxyCreator.class这个类注册到容器。
    这个是一个Bean后置处理器(BeanPostProcessor)，在bean初始化完成前后做一些aop相关的工作
 */

/**
 * 演示aop功能
 */
@ComponentScan
// 在Spring中，如果不在配置类中添加@EnableAspectJAutoProxy，那么所有切面注解是不生效的
// spring boot因为有自动配置，所以不需要开发人员手工配置@EnableAspectJAutoProxy
@EnableAspectJAutoProxy // 开启aop功能
public class SpringAopSample {

    public static void main(String[] args) {
        ApplicationContext context = new AnnotationConfigApplicationContext(SpringAopSample.class);

        HelloWorldService helloWorldService = context.getBean(HelloWorldService.class);
        helloWorldService.sayHello();
    }

    // 使用代理时，该类必须是public的，否则报错
    @Service
    public static class HelloWorldService {
        public void sayHello() {
            System.out.println("hello world");
        }
    }

    /*
    当Spring初始化所有单例bean时，容器中存在AbstractAutoProxyCreator的实现类作为BeanPostProcessor时，
    bean才会被代理，这才是aop代理生成根本
    AnnotationAwareAspectJAutoProxyCreator是AbstractAutoProxyCreator的子类，它提供了更强的查找切面的能力（支持AspectJ注解）
     */
    @Component
    @Aspect // 定义切面
    private static class HelloWorldAspect {

        // 定义切入点，id为：pointCut()
        @Pointcut("execution(* com.wang.spring.aop..*.*(..))")
        public void pointCut() {

        }

        // 声明前置通知（在切入点方法被执行前调用）
        @Before("pointCut()")
        public void before() {
            System.out.println("before...");
        }

        // 声明后置通知（在切入点方法被执行后调用）
        @After("pointCut()")
        public void after() {
            System.out.println("after...");
        }
    }

}
