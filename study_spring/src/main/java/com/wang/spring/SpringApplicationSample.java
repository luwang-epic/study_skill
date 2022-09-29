package com.wang.spring;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Service;

/*
Spring容器是Spring的核心，Spring容器是管理bean对象的地方，其通过IoC技术管理。
Spring容器也就是一个bean工厂（BeanFactory）。应用中bean的实例化，获取，销毁等都是由这个bean工厂管理的

Spring提供了两种容器类型：BeanFactory和ApplicationContext
    BeanFactory: 基础类型IoC容器，提供完整的IoC服务支持。如果没有特殊指定，默认采用延迟初始化策略（lazy-load）。
        只有当客户端对象需要访问容器中的某个受管对象的时候，才对该受管对象进行初始化以及依赖注入操作。
        所以，相对来说，容器启动初期速度较快，所需要的资源有限。
    ApplicationContext: 在BeanFactory的基础上构建，是相对比较高级的容器实现，除了拥有BeanFactory的所有支持，
        ApplicationContext还提供了其他高级特性，比如事件发布、国际化信息支持等。
        ApplicationContext所管理的对象，在该类型容器启动之后，默认全部初始化并绑定完成。
        所以，相对于BeanFactory来说，ApplicationContext要求更多的系统资源，
        同时，因为在启动时就完成所有初始化，容器启动时间较之BeanFactory也会长一些。



启动流程
    一. 创建BeanFactory
        BeanFactory即是各种缓存的集合. 存储创建Bean的各种过程和结果数据.
        对于AnnotationConfigApplicationContext,是通过其父类GenericApplicationContext构造器创建的.
        new AnnotationConfigApplicationContext(xx)时首先就会调用自己的无参数构造器this()，
            这个构造函数首先会调用父类GenericApplicationContext构造函数（类初始化先调用父类构造器），this()构造器中的代码

    二. BeanDefinitionReader
        Spring源码的基础设施之一. 是类到BeanDefinition的转换适配器. 不同的容器有不同的读取器.
        AnnotationConfigApplicationContext 对应AnnotatedBeanDefinitionReader,里边包含解析各种注解的处理器.
        ClassPathXmlApplicationContext 对应 XmlBeanDefinitionReader,里边包含解析各种xml标签的处理器

        创建AnnotatedBeanDefinitionReader类，
            构造方法中BeanDefinitionRegistry就是Bean工厂DefaultListableBeanFactory(实现了BeanDefinitionRegistry接口).
            构造方法中核心是创建基础注解配置处理器,并注册到容器中,用于后续解析各类注解.
            AnnotationConfigUtils.registerAnnotationConfigProcessors方法中,初始化了三个重要的根处理器BeanDefinition.
                // 用于解析配置类BeanFactoryPostProcessor
                RootBeanDefinition def = new RootBeanDefinition(ConfigurationClassPostProcessor.class);
                //用于处理@Autowired、@Value
                RootBeanDefinition def = new RootBeanDefinition(AutowiredAnnotationBeanPostProcessor.class);
                //处理@Resource、@PostConstruct、 @PreDestroy等
                RootBeanDefinition def = new RootBeanDefinition(CommonAnnotationBeanPostProcessor.class);

    三. ClassPathBeanDefinitionScanner
        ClassPathBeanDefinitionScanner用来执行具体的扫描逻辑. 扫描过程支持通过定义过滤器来排除或者包含部分类

    四. 容器refresh
        狭义上说,refresh方法的执行才开始容器启动. 前边都是一些准备工作.
        AnnotationConfigApplicationContext 并没有实现refresh方法, 实现该方法的是其父类AbstractApplicationContext.
        这里用到设计模式模板方法. 容器抽象父类来规定容器refresh的步骤. 子类通过重写实现步骤中的方法来实现.

        主要流程在AbstractApplicationContext.refresh()方法中，refresh()函数主要有13个步骤:
        1. prepareRefresh: 刷新前的准备工作，设置了容器的启动时间，将容器的标识符设置为启动状态active等工作。
        2. obtainFreshBeanFactory: 刷新BeanFactory，并为容器设置了序列化ID；然后返回初始化过程中创建的DefaultListableBeanFactory对象。
            注意：如果是XML版本，则DefaultListableBeanFactory、XmlBeanDefinitionReader并不是在初始化过程中创建的，而是在这里创建的，
            初始化过程中只是设置了一下配置文件的路径。然后会在这里去解析XML配置文件，将里面的Bean信息转换成BeanDefinition添加到容器中，
            然后返回创建的DefaultListableBeanFactory。
            如果是注解形式，将在invokeBeanFactoryPostProcessors步骤中将Bean详细转为为BeanDefinition添加到容器中
        3. prepareBeanFactory: 向BeanFactory中设置一些内容，比如说设置了类加载器、默认以"#{“开头以”}"结尾的表达式解析器等内容。
        4. postProcessBeanFactory: 空方法，留待子类去实现，作用是在BeanFactory准备工作完成后做一些定制化的处理。
        5. invokeBeanFactoryPostProcessors: 在BeanFactory标准初始化之后执行每个BeanFactoryPostProcessor实现类的postProcessBeanFactory
            实例化并调用所有BeanFactoryPostProcessor. 实现解析配置类，将业务代码中Bean转化为BeanDefinition
        6. registerBeanPostProcessors: 这一步是向容器中注册BeanPostProcessor（会通过其BeanDefinition创建出Bean，然后让beanFactory统一进行管理），
            BeanPostProcessor会干预 Spring 初始化 bean 的流程，从而完成代理、自动注入等各种功能。
        7. initMessageSource: 初始化 MessageSource 组件，做国际化的处理。
        8. initApplicationEventMulticaster: 初始化事件广播器，如果用户配置了就用自定义的，如果没有就创建一个SimpleApplicationEventMulticaster。
            事件广播器管理了所有的事件监听器，当发布一个事件的时候，需要通过事件广播器来广播事件，
            即遍历所有的监听器，然后找到监听该事件的监听器（监听器需要提前注册在容器中），然后调用它们的回调函数。
        9. onRefresh: 空方法，留给子类去实现，在容器刷新的时候可以自定义逻辑，web场景下会使用。
        10. registerListeners: 将容器中所有的ApplicationListener都注册到容器中，由容器统一管理。
            然后通过事件广播器发布之前步骤产生的事件ApplicationEvent。
        11. finishBeanFactoryInitialization: 首先如果之前没有注册类似于PropertyPlaceholderConfigurer这样的解析Bean属性值的组件时，
            会向容器中添加默认的属性值解析器，具体的作用就是用于解析注解属性值，比如说我们通过@Value("#{jdbc.address}")注解为属性注入值，
            然后（在自动注入步骤处）该处理器就会将其解析为配置文件中的值，完成注入。

            初始化剩下所有的非懒加载的单例Bean对象，具体来说就是遍历所有的beanName，然后调用getBean(beanName)方法来创建Bean对象。
            值得注意的是，如果该Bean是FactoryBean类型，会调用两次getBean方法，第一次是将其当作普通Bean来创建，第二次是将其当作工厂，通过它来创建对象。
        12. finishRefresh: 首先初始化生命周期处理器，如果用户没有配置则提供一个默认的DefaultLifecycleProcessor。然后发布容器刷新完毕的事件。
        13. resetCommonCaches: 清空启动过程中产生的一些缓存，例如：反射相关的信息、注解相关的信息、类加载器相关的信息等，因为不再需要单例Bean的元数据了。

    参考：
        博客：https://blog.csdn.net/zhang_qing_yun/article/details/120157047

Bean生命周期
    主要有：实例化、属性赋值、初始化、销毁这 4 个大阶段，具体如下：
    1. Spring 启动，查找并加载需要被 Spring 管理的 Bean，进行 Bean 的实例化；
    2. Bean 实例化后，对 Bean 的引入和值注入到 Bean 的属性中；
        解析自动装配（byName、byType、constractor（1阶段）、default）DI的体现
        自动装配和循环依赖也在这个阶段（1阶段也有）
    3. 如果 Bean 实现了 BeanNameAware 接口的话，Spring 将 Bean 的 Id 传递给 setBeanName() 方法；
    4. 如果 Bean 实现了 BeanFactoryAware 接口的话，Spring 将调用 setBeanFactory() 方法，将 BeanFactory 容器实例传入；
    5. 如果 Bean 实现了 ApplicationContextAware 接口的话，Spring 将调用 Bean 的 setApplicationContext() 方法，将Bean所在应用上下文引用传入进来；
    6. 如果 Bean 实现了 BeanPostProcessor 接口，Spring 就将调用它们的postProcessBeforeInitialization() 方法；
    7. 如果 Bean 实现了 InitializingBean 接口，Spring 将调用它们的 afterPropertiesSet() 方法。类似地，如果 Bean 使用 init-method 声明了初始化方法，该方法也会被调用；
    8. 如果 Bean 实现了 BeanPostProcessor 接口，Spring 就将调用它们的 postProcessAfterInitialization() 方法；
    9. 此时，Bean 已经准备就绪，可以被应用程序使用了。它们将一直驻留在应用上下文中，直到应用上下文被销毁；
    10. 如果 Bean 实现了 DisposableBean 接口，Spring 将调用它的 destory() 接口方法，同样，如果 Bean 使用了 destory-method 声明销毁方法，该方法也会被调用。


依赖注入原理（自动装配）
    @Autowired是在Bean属性赋值阶段进行装配，通过Bean的后置处理器进行解析

    1. 在创建一个spring上下文的时候在构造函数中注册AutowiredAnnotationBeanPostProcessor
    2、在Bean的创建过程中进行解析
        在实例化后预解析（解析@Autowired标注的属性、方法，比如：把属性的类型、名称、属性所在的类...元数据缓存）
        在属性赋值阶段真正的注入（拿到上一步缓存的元数据去ioc容器进行查找，并且返回注入）
            首先根据解析的元数据拿到类型去容器种查找
            如果查询结果刚好为一个，就将该bean装配给@Autowired指定的数据；
            如果查询的结果不止一个，那么@Autowired会根据名称来查找
            如果上述查找结果为空，那么就会抛出异常。

    在容器启动，为bean属性赋值的时候，spring会用后置处理器AutowiredAnnotationBeanPostProcessor解析@Autowired注解，来创建属性的实例，
    然后从IOC容器中根据@Primary、@Order、@PriorityOrder或Spring默认规则挑选出最符合的Bean,利用反射注入到字段中完成赋值；


循环依赖问题 & spring解决方式
    Bean的生成大概步骤：
        1. Spring 扫描 class 得到 BeanDefinition；
                如果bean是存在两个id相关的bean在同一个xml中，解析时会报错
                如果bean是存在两个id在不同的xml中，不会报错，或者使用注解生成两个相同的id，也不会报错
                    此时spring IOC容器在解析的时候只会注册第一个声明bean的一个实例，后面重复名字的bean的实例就不会再注册了
                    但是在依赖注入时，如果两个相同id的bean不是一个类型，会报类型不匹配的错误（如果根据类型注入，可能会报找不到某个类型实例错误）
        2. 根据得到的 BeanDefinition去生成bean；
        3. 首先根据 class 推断构造方法；
        4. 根据推断出来的构造方法，反射，得到一个对象（暂时叫做原始对象）；
        5. 填充原始对象中的属性（依赖注入）；
        6. 如果原始对象中的某个方法被AOP了，那么则需要根据原始对象生成一个代理对象；
        7. 把最终生成的代理对象放入单例池（源码中叫做 singletonObjects）中，下次 getBean 时就直接从单例池拿即可；

    上述步骤4中，如果A构造函数需要B属性，而B构造函数也需要A属性，也会形成循环依赖，这种情况spring启动时会报错
    上述步骤5中，如果B类有属性A，而A类有属性B，那么会形成循环依赖，这种情况的调用链如下：
        A Bean创建 –> 依赖了B属性 –> 触发B Bean创建 —> B依赖了A属性 —> 需要A Bean（但A Bean还在创建过程中）
        这种情况spring通过三级缓存来解决（单例和懒加载模式没有问题，但是多例模式解决不了，启动会报错），但是bean初始化后还可以有BeanPostProcessor来处理Bean，
            如果在这期间自定义的BeanPostProcessor将这个Bean对象修改了，那么A和B类中的a属性还是可能会存在不一样的情况，这时使用过程中可能会产生问题

    三级缓存
        singletonObjects中缓存的是已经经历了完整生命周期的bean对象
        earlySingletonObjects中缓存的是早期的bean对象，早期指的是Bean的生命周期还没走完
        singletonFactories中缓存的是ObjectFactory，表示对象工厂，用来创建某个对象的代理，
            在每个 Bean 的生成过程中，都会提前暴露一个工厂，这个工厂可能用到，也可能用不到，
            如果没有出现循环依赖依赖本 bean，那么这个工厂无用，本 bean 按照自己的生命周期执行，
            执行完后直接把本 bean 放入 singletonObjects 中即可，如果出现了循环依赖依赖了本 bean，
            则另外那个 bean 执行 ObjectFactory 提交得到一个对象，
            如果有AOP的话，会获取代理对象，如果无需AOP，则直接得到一个原始对象

    可以用com.wang.spring.ioc包下面的程序跟踪源码，查看循环依赖的解决方式
    参考：
        博客：https://blog.csdn.net/weixin_44129618/article/details/122839774
        B站：https://www.bilibili.com/video/BV1Z44y1b775


Spring AOP原理
    AOP的概念介绍见：com.wang.spring.aop.SpringAopSample类

    依托Spring的IOC容器，提供了极强的AOP扩展增强能力，对项目开发提供了极大地便利。
    AOP的实现有AspectJ、JDK动态代理、CGLIB动态代理，Spring AOP不是一种新的AOP实现，其底层采用的是JDK/CGLIB动态代理。

    SpringAOP是对bean的一种扩展，是后处理器的一种处理。Spring bean在执行初始化方法前后，
    会使用所有BeanPostProcessor对bean进行特殊处理。Aop代理即是一种对bean特殊处理。
    对应的BeanPostProcessor为AbstractAutoProxyCreator的子类，执行AbstractAutoProxyCreator.postProcessAfterInitialization()。

    CGLIB动态代理与JDK动态区别
        java动态代理是利用反射机制生成一个实现代理接口的匿名类，在调用具体方法前调用InvokeHandler来处理。
        而cglib动态代理是利用asm开源包，对代理对象类的class文件加载进来，通过修改其字节码生成子类来处理。

        JDK动态代理只能对实现了接口的类生成代理，而不能针对类 。
        CGLIB是针对类实现代理，主要是对指定的类生成一个子类，覆盖其中的方法 。
        因为是继承，所以该类或方法不要声明成final, final可以阻止继承和多态。


Spring的事务管理
    spring支持两种方式的事务管理: 编程式事务管理 和 声明式事务管理

    编程式事务管理:
        通过 TransactionTemplate或者TransactionManager手动管理事务，实际应用中很少使用

    声明式事务管理:
        推荐使用（代码侵入性最小），一般使用@Transactional的注解，@Transactional的工作机制是基于AOP实现的
        如果同一个类中的其他没有@Transactional注解的方法内部调用有@Transactional注解的方法，有@Transactional 注解的方法的事务会失效。
        这是由于Spring AOP代理的原因造成的，因为只有当 @Transactional 注解的方法在类以外被调用的时候，Spring 事务管理才生效。

        @Transactional注解相关属性：
            value当在配置文件中有多个TransactionManager , 可以用该属性指定选择哪个事务管理器。
            propagation事务的传播行为，默认值为REQUIRED。
            isolation事务的隔离度，默认值采用DEFAULT。
            timeout事务的超时时间，默认值为-1。如果超过该时间限制但事务还没有完成，则自动回滚事务。
            read-only指定事务是否为只读事务，默认值为false；为了忽略那些不需要事务的方法，比如读取数据，可以设置read-only 为true。
            rollback-for用于指定能够触发事务回滚的异常类型，如果有多个异常类型需要指定，各类型之间可以通过逗号分隔。
            no-rollback-for抛出no-rollback-for 指定的异常类型，不回滚事务。

        @Transactional 的使用注意事项总结
            1）@Transactional 注解只有作用到 public 方法上事务才生效，不推荐在接口上使用；
            2）避免同一个类中调用 @Transactional 注解的方法，这样会导致事务失效；
            3）正确的设置 @Transactional 的 rollbackFor 和 propagation 属性，否则事务可能会回滚失败;
            4）被 @Transactional 注解的方法所在的类必须被 Spring 管理，否则不生效；
            5）底层使用的数据库必须支持事务机制，否则不生效。

        spring的事务是通过aop来实现的，首先要生成具体的代理对象，然后按照aop的流程来执行具体逻辑，
        正常情况下要通过通知来完成核心功能，但是事务不是通过通知来实现的，而是通过TransactionInterceptor来实现的
        然后调用invoke来实现具体的逻辑
            1. 先做准备工作，解析各个方法上事务的相关属性，根据具体的属性来判断是否开启新事物
            2. 当需要开启时，获取数据库连接，关闭自动提交功能，开启事务
            3. 执行具体的sql逻辑
            4. 在操作过程中，如果执行失败，那么会通过completeTransactionAfterThrowing来完成事务的回滚操作
                回滚的具体逻辑是通过doRollBack方法实现的，实现的时候需要先获取连接，然后通过连接对象来回滚
            5. 如果执行过程中，没有意外发生，通过commitTransactionAfterReturning来完成事务的提交操作
                提交逻辑通过doCommit方法来实现，也是需要先获取连接，在通过连接来提交
            6. 当事务执行完毕之后（不敢提交或者回滚），需要通过cleanupTransactionInfo方法清理相关的事务信息（如设置事务状态）

        事务传播行为是为了解决业务层方法之间互相调用的事务问题。当事务方法被另一个事务方法调用时，必须指定事务应该如何传播。
        例如：方法可能继续在现有事务中运行，也可能开启一个新事物，并在自己的事务中运行。
        （事务的传播行为分类以及异常处理方式在网上查看相关资料）

 */

@ComponentScan
public class SpringApplicationSample {

    public static void main(String[] args) {
        // 基于注解的方式启动spring容器
        ApplicationContext context = new AnnotationConfigApplicationContext(SpringApplicationSample.class);

        HelloWorldService helloWordService = context.getBean(HelloWorldService.class);
        System.out.println(helloWordService);
        helloWordService.sayHello();
    }

    @Service
    private static class HelloWorldService {
        public void sayHello() {
            System.out.println("hello world");
        }
    }
}
