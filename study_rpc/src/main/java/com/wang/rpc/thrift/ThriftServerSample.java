package com.wang.rpc.thrift;

/*
Thrift官网：https://thrift.apache.org/
    可以参考帮助文档，示例以及idl语言，以及下载相关编译程序等


Thrift特性
    1）开发速度快
    2）接口维护简单
    3）学习成本低
    4）多语言及跨语言支持
    5）稳定/广泛的应用


Thrift架构
Thrift 技术栈分层从下向上分别为：传输层，协议层，处理层和服务层
传输层（Transport Layer）： 传输层负责直接茨贝格网络中读取和写入数据，它定义了具体的网络传输协议，比如说TCP/IP传输等
协议层（Protocol Layer）：协议层定义了数据传输格式，负责网络传输数据的序列化和反序列化，比如说json，xml，二进制数据等
处理层（Processor Layer）：处理层是具体的IDL生成的，封装了具体的底层网络传输和序列化方式，并委托给用户实现的handler处理
服务层（Server Layer）：整合上述组件，提供具体的网络io模型，形成最终的服务

Thrift传输层
TIOStreamTransport：thrift中用于进行网络传输的transport，其内部基于对象构造时传入的InputStream/OutputStream进行IO操作
TSocket：thrift中用于进行网络IO的socket类，用于管理相关socket链接，该类继承自TIOStreamTransport
TNonblockingTransport:非阻塞网络IO类，用于异步网络通信
TNonblockingSocket:继承自TNonblockingTransport，用于管理异步socket，其内部的默认io实现是基于jdk提供的nio来支持的，当然，我们也可以重写一个基于netty的异步socket
TMemoryBuffer:对ByteArrayOutputStream进行了封装，用于字节输出流
TMemoryInputTransport:thrift自己实现的字节数组输入流，通过对其内部持有的字节数组进行实现
TFramedTransport：对字节IO流进行了一层封装，支持frame形式的字节IO。在thrift中，frame形式即表征数据长度（4字节）与结构化数据

Thrift网络服务模型
Thrift提供的网络服务模型：单线程，多线程，时间驱动。从另外一个角度划分:阻塞服务模型，非阻塞服务模型
阻塞服务模型： TSimpleServer,TThreadPoolServer
非阻塞服务模型：TNonblockingServer,THshaServer,TThreadedSelectorServer

TServer
定义了静态内部类Args,Args继承抽象类AbstractServerArgs,AbstractServerArgs采用建造者模式，
向TServer提供了各种工厂。TServer的三个方法，
    serve()：启动服务
    stop()：停止服务
    isServing()：用于检测服务的启停状态

TSimpleServer
    采用最简单的阻塞IO，实现方法简洁明了，但是一次只能接受和处理一个socket连接，
    效率比较低，一般用于延时Thrift的工作过程

TThreadPoolServer
    采用阻塞socket方式工作，主线程负责阻塞式监听是否有新的socket连接，具体业务处理交由一个线程池处理
    ThreadedPoolServer 解决了TSimpleServer不支持的并发和多连接问题，引入线程池，实现的模型是one thread per connection

    优点：
        拆分监听线程（Accept Thread）与处理客户端连接的工作线程（Worker Thread）,数据读取和业务处理都交给线程池处理。
        因此并发量较大的时候新的连接能被及时的接受
        线程池模式比较适合服务器端能够阈值最多有多少个客户端并发的情况，这样每个请求都能被业务线程池及时处理，性能非常高
    缺点：
        线程池模式的处理能力受限于线程池的工作能力，如果线程处理业务逻辑较久，
        那么并发线程数大于线程池中的线程数，新的请求只能排队等待
        默认线程池允许创建的最大的线程数量为Integer.MAX_VALUE,可能会创建大量的线程，导致OOM

TNonblockingServer
    单线程工作，但是采用NIO，利用IO多路复用模型处理socket就绪事件，对于有数据到来的socket进行数据读取工作，
    对于有数据发送的socket则进行数据发送操作，对于监听socket则产生一个新的socket注册到selector,使用单Reactor多线程模式。

    优点：
        相对于TSimpleServer效率提升主要体现在IO多路复用上，
        TNonblockingServer采用非阻塞IO,对accept/read/write等io事件进行监控和处理，同时监控多个socket变化
    缺点：
        在业务处理上还是单线程顺序来完成，当业务处理比较复杂，耗时的时候，例如某些接口需要读取数据库执行时间比较长，
        会导致整个服务阻塞住，此时该模式效率不高，因为多个调用请求任务依然是顺序一个一个执行

THsHaServer
    鉴于TNonblockingServer缺点，THsHaServer继承TNonblockingServer，引入线程池提高任务处理的并发能力

    优点：
        与TNonblockingServer模式相比，THsHaServer在完成数据读取后，
        将业务处理过程交给线程池去处理，主线程直接返回下一次循环操作，效率大大提升
    缺点
        主线程仍然完成所有的socket的连接监听，数据读取，数据写入操作，当并发请求数量大的时候，
        且发送数据量过多的时候，监听socket新的连接请求不能及时的接受
TThreadedSelectorServer
    TThreadedSelectorServer是对THsHaServer的一种扩充，它将selector中的读写IO事件（read/write）从主线程分离处来，
    同时引入worker工作线程池. 内部有几下几个部分组成：
        1.一个AcceptThread专门用来处理socket的连接事件
        2.若干个SelectorThread专门用来处理业务socket的网络IO操作，所有的网络的读写都是由这些线程完成
        3.一个负载均衡器 SelectorThreadLoadBalancer对象，主要用于AcceptThread线程接受到一个新的socket连接请求的时候，将这个新的连接请求分配到那个SelectorThread线程
        4.一个ExecutorService类型的工作线程池，在SelectorThread的线程中，监听到有业务的socket中有调用请求过来，则将请求的数据如读取之后，交给ExecutorSErvice线程池完成此次调用的具体操作，主要用于处理每个rpc请求的handler回调处理

 */

import com.wang.rpc.thrift.api.User;
import com.wang.rpc.thrift.api.UserService;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.server.THsHaServer;
import org.apache.thrift.server.TNonblockingServer;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.server.TThreadedSelectorServer;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportException;
import org.apache.thrift.transport.layered.TFramedTransport;

/**
 * 服务端代码
 */
public class ThriftServerSample {

    public static void main(String[] args) {
        try {
            //simpleServerDemo();
            //threadPoolServerDemo();
            //nonBlockingServerDemo();
            //hsHaServerDemo();
            threadedSelectorServerDemo();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 基本的非阻塞服务端，非阻塞IO，多线程处理所有客户端的连接以及读取和写入数据，类似ZK的服务器连接管理
     * @throws TTransportException
     */
    private static void threadedSelectorServerDemo() throws TTransportException {
        // 指定传输层通信方式，socket通信
        TNonblockingServerSocket serverTransport = new TNonblockingServerSocket(9999);
        // 指定二进制编码格式
        //TBinaryProtocol.Factory protocolFactory = new TBinaryProtocol.Factory();
        // 指定压缩的二进制格式
        TCompactProtocol.Factory protocolFactory = new TCompactProtocol.Factory();
        // 指定TFramedTransport, 非阻塞必须使用这个数据传输方式，否则会报错，客户端也需要指定这个数据传输方式
        TFramedTransport.Factory tFramedTransportFactory = new TFramedTransport.Factory();

        // 生成对应的处理类
        UserService.Processor processor = new UserService.Processor(new UserServiceImpl());

        TThreadedSelectorServer.Args serverArgs = new TThreadedSelectorServer.Args(serverTransport);
        // 设置处理类
        serverArgs.processor(processor);
        // 设置协议工厂
        serverArgs.protocolFactory(protocolFactory);
        // 设置传输层协议工厂
        serverArgs.transportFactory(tFramedTransportFactory);

        // 基本的非阻塞的服务端，react模型
        TServer server = new TThreadedSelectorServer(serverArgs);
        System.out.println("starting the thread selector server...");
        // 启动服务
        server.serve();
    }

    /**
     * 基本的非阻塞服务端，非阻塞IO，单线程处理所有客户端的连接写入数据，多线程处理客户端的读取操作
     * @throws TTransportException
     */
    private static void hsHaServerDemo() throws TTransportException {
        // 指定传输层通信方式，socket通信
        TNonblockingServerSocket serverTransport = new TNonblockingServerSocket(9999);
        // 指定二进制编码格式
        //TBinaryProtocol.Factory protocolFactory = new TBinaryProtocol.Factory();
        // 指定压缩的二进制格式
        TCompactProtocol.Factory protocolFactory = new TCompactProtocol.Factory();
        // 指定TFramedTransport, 非阻塞必须使用这个数据传输方式，否则会报错，客户端也需要指定这个数据传输方式
        TFramedTransport.Factory tFramedTransportFactory = new TFramedTransport.Factory();

        // 生成对应的处理类
        UserService.Processor processor = new UserService.Processor(new UserServiceImpl());

        THsHaServer.Args serverArgs = new THsHaServer.Args(serverTransport);
        // 设置处理类
        serverArgs.processor(processor);
        // 设置协议工厂
        serverArgs.protocolFactory(protocolFactory);
        // 设置传输层协议工厂
        serverArgs.transportFactory(tFramedTransportFactory);

        // 基本的非阻塞的服务端，使用NIO服务端和客户端需要指定TFramedTransport数据传输方式
        TServer server = new THsHaServer(serverArgs);
        System.out.println("starting the hs ha server...");
        // 启动服务
        server.serve();
    }


    /**
     * 基本的非阻塞服务端，非阻塞IO，单线程处理所有客户端的连接读取和写入数据
     * @throws TTransportException
     */
    private static void nonBlockingServerDemo() throws TTransportException {
        // 指定传输层通信方式，socket通信
        TNonblockingServerSocket serverTransport = new TNonblockingServerSocket(9999);
        // 指定二进制编码格式
        //TBinaryProtocol.Factory protocolFactory = new TBinaryProtocol.Factory();
        // 指定压缩的二进制格式
        TCompactProtocol.Factory protocolFactory = new TCompactProtocol.Factory();
        // 指定TFramedTransport, 非阻塞必须使用这个数据传输方式，否则会报错，客户端也需要指定这个数据传输方式
        TFramedTransport.Factory tFramedTransportFactory = new TFramedTransport.Factory();

        // 生成对应的处理类
        UserService.Processor processor = new UserService.Processor(new UserServiceImpl());

        TNonblockingServer.Args serverArgs = new TNonblockingServer.Args(serverTransport);
        // 设置处理类
        serverArgs.processor(processor);
        // 设置协议工厂
        serverArgs.protocolFactory(protocolFactory);
        // 设置传输层协议工厂
        serverArgs.transportFactory(tFramedTransportFactory);

        // 基本的非阻塞的服务端，使用NIO服务端和客户端需要指定TFramedTransport数据传输方式
        TServer server = new TNonblockingServer(serverArgs);
        System.out.println("starting the non blocking server...");
        // 启动服务
        server.serve();
    }

    /**
     * 简单的服务端，阻塞IO，多线程，一个程处理一个客户端的请求，客户端之间会不影响
     * @throws TTransportException
     */
    private static void threadPoolServerDemo() throws TTransportException {
        // 指定传输层通信方式，socket通信
        TServerTransport serverTransport = new TServerSocket(9999);
        // 指定二进制编码格式
        TBinaryProtocol.Factory protocolFactory = new TBinaryProtocol.Factory();
        // 生成对应的处理类
        UserService.Processor processor = new UserService.Processor(new UserServiceImpl());

        TThreadPoolServer.Args serverArgs = new TThreadPoolServer.Args(serverTransport);
        // 设置处理类
        serverArgs.processor(processor);
        // 设置协议工厂
        serverArgs.protocolFactory(protocolFactory);

        // 多线程服务端
        TServer server = new TThreadPoolServer(serverArgs);
        System.out.println("starting the thread pool server...");
        // 启动服务
        server.serve();
    }

    /**
     * 简单的服务端，阻塞IO，单线程处理所有客户端的请求，一个客户端阻塞会影响其他客户端
     * @throws TTransportException
     */
    private static void simpleServerDemo() throws TTransportException {
        // 指定传输层通信方式，socket通信
        TServerTransport serverTransport = new TServerSocket(9999);
        // 指定二进制编码格式
        TBinaryProtocol.Factory protocolFactory = new TBinaryProtocol.Factory();
        // 生成对应的处理类
        UserService.Processor processor = new UserService.Processor(new UserServiceImpl());

        TSimpleServer.Args serverArgs = new TSimpleServer.Args(serverTransport);
        // 设置处理类
        serverArgs.processor(processor);
        // 设置协议工厂
        serverArgs.protocolFactory(protocolFactory);

        // 简单服务端，一般用于测试
        TServer server = new TSimpleServer(serverArgs);
        System.out.println("starting the simple server...");
        // 启动服务
        server.serve();
    }

    /**
     * 实现具体的服务接口
     * 对于开发人员来说，使用原生的Thrift框架，仅需要关注以下四个核心内部接口/类：Iface，AsyncIface，Client,AsyncClient
     *   Iface: 服务端通过实现UserService.Iface接口，向客户端提供具体的同步业务逻辑
     *   AsyncIface: 服务端通过实现UserService.AsyncIface接口，向客户端提供具体的异步业务逻辑
     *   Client: 客户端通过UserService.Client实例对象，以同步的方式访问服务器提供的服务方法
     *   AsyncClient: 客户端通过UserService.AsyncClient实例对象，以异步的方式访问服务器提供的服务方法
     */
    private static class UserServiceImpl implements UserService.Iface {
        @Override
        public User getById(int id) throws TException {
            System.out.println("invoke UserServiceImpl getById with " + id);
            System.out.println("thread name: " + Thread.currentThread());

            // mock一个用户
            User user = new User();
            user.setName("zhangsan");
            user.setAge(10);
            user.setId(1);
            return user;
        }

        @Override
        public boolean isExist(String name) throws TException {
            System.out.println("invoke UserServiceImpl isExist with " + name);
            return false;
        }
    }
}
