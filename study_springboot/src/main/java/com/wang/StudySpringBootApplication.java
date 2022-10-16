package com.wang;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

/* spring相关概念
    启动流程（Bean什么周期）
    依赖注入原理（自动装配）
    循环依赖
    AOP和事务管理
具体内容见study_spring项目中的相关类上面的说明，如：SpringApplicationSample类等
 */

/* spring boot 相关概念

自动配置（auto config 或者 starter 是如何加载生效的）
    1. 启动过程中，会在SpringApplication.prepareContext方法中初始化AnnotatedBeanDefinitionReader，
        并注册相关的路径信息等，该类主要用于读取项目中类的注解，将其转化为BeanDefinition
    2. 在spring的启动过程中，在BeanFactory初始化后，会调用每个BeanFactoryPostProcessors，
        这里会调用ConfigurationClassPostProcessor这个处理器，这个处理器主要负责对注解的处理
    3. spring boot的启动类中会有@SpringBootApplication注解，该注解中包含了@EnableAutoConfiguration注解
        3.1 @EnableAutoConfiguration注解中包含AutoConfigurationPackage注解
            该注解利用@Import导入了AutoConfigurationPackages.Registrar类
            该类的功能就是把这个注解所在包下面的所有组件都导入到容器中，这就解释了我们之前在搭建SpringBoot工程的时候，
            要求所有组件必须定义在启动类的包下面了，只有这样才能被扫描的到
        3.2 @EnableAutoConfiguration注解利用@Import导入了AutoConfigurationImportSelector类，
            该类最重要的功能是加入自动配置的配置类，里面有一个方法selectImports，就是给给容器批量导入一些组件的方法
            selectImports方法内部的getAutoConfigurationEntry方法才是真正执行导入组件的操作，
            getAutoConfigurationEntry方法里面的getCandidateConfigurations方法导入候选配置信息，
            候选配置信息的加载是从spring.facories文件中加载的，这些包含了全部的自动配置类
            但是所有的配置类不可能全部都是必须的，需要进行筛选，在步骤在getCandidateConfigurations方法中调用filter方法
            在filter过滤方法中利用@Conditional注解以及根据Conditional扩展出来的组合注解来进行筛选，得到最终的自动配置类
    4. 处理这些自动配置类，包括：加入到容器中和实例化等，只有这些自动配置类进入到容器中以后，接下来这个自动配置类才开始进行启动
        在spring的启动过程中，在Bean的创建和初始化（自动装备）中，将这些配置类和配置类中配置的Bean加载到容器中

    参考：
        博客：https://www.cnblogs.com/daihang2366/p/15763111.html


Filter & Interceptor & AOP 区别和联系
    这三种拦截方式的拦截顺序是：filter—>Interceptor-->ControllerAdvice-->@Aspect -->Controller；

    过滤器拦截web访问url地址。 严格意义上讲，filter只是适用于web中，依赖于Servlet容器，利用Java的回调机制进行实现。
    Filter过滤器：和框架无关，可以控制最初的http请求，但是更细一点的类和方法控制不了。
    过滤器可以拦截到方法的请求和响应(ServletRequest request, ServletResponse response)，
        并对请求响应做出像响应的过滤操作，比如设置字符编码，鉴权操作等

    Interceptor是基于Java的反射机制（AOP思想）进行实现，不依赖Servlet容器。因此拦截器既可以用于Web程序，也可以用于Application、Swing程序中。
    拦截器可以在方法执行之前(preHandle)和方法执行之后(afterCompletion)进行操作，回调操作(postHandle)，可以获取执行的方法的名称，请求(HttpServletRequest)
    Interceptor可以通过IoC容器来管理，因此可以通过注入等方式来获取其他Bean的实例，因此使用会更方便。
    Interceptor可以控制请求的控制器和方法，但控制不了请求方法里的参数(只能获取参数的名称，不能获取到参数的值)

    AOP操作可以对操作进行横向的拦截，只能拦截Spring容器中管理的Bean的访问
    最大的优势在于他可以获取执行方法的参数(ProceedingJoinPoint.getArgs())，对方法进行统一的处理。
    实际开发中，AOP常和事务结合：Spring的事务管理:声明式事务管理(切面)


 */

/**
 * 由于redis已经关闭了，所以排除这个自动配置，否则由于连不上redis会导致服务启动失败
 */
@SpringBootApplication (exclude = {RedisAutoConfiguration.class})
@EnableAdminServer
@EnableScheduling
public class StudySpringBootApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(StudySpringBootApplication.class, args);

        String[] beanDefinitionNames = context.getBeanDefinitionNames();
        for (String beanName : beanDefinitionNames) {
            System.out.println(beanName);
        }

    }

}