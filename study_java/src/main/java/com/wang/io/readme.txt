
BIO：同步并阻塞（传统阻塞型），服务器实现模式为一个连接一个线程，即客户端有连接请求时服务器 端就
需要启动一个线程进行处理，如果这个连接不做任何事情会造成不必要的线程开销

NIO：同步非阻塞，服务器实现模式为一个线程处理多个请求（连接），即客户端发送的连接请求
都会注 册到多路复用器上，多路复用器轮询到连接有I/O请求就进行处理

AIO(NIO.2)：异步非阻塞，服务器实现模式为一个有效请求一个线程，客户端的I/O请求都是由
OS先完 成了再通知服务器应用去启动线程进行处理，一般适用于连接数较多且连接时间较长的应用

BIO、NIO、AIO适用场景分析
    1、 BIO方式适用于连接数目比小且固定的架构，这种方式对服务器资源要求比较高，并发局限于应用
        中， jDK1.4以前的唯一选择，但程序简单易理解。
    2、 NIO方式适用于连接数目多且连接比较短（轻操作）的架构，比如聊天服务器，弹幕系统，服务器
        间通讯等。 编程比较复杂，jDK1 .4开始支持。
    3、 AIO方式使用于连接数目多且连接比较长（重操作）的架构，比如相册服务器，充分调用OS参与并
        发操作， 编程比较复杂，JDK7开始支持。


