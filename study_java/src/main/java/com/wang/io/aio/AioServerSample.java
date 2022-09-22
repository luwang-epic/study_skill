package com.wang.io.aio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

/*
    Java AIO(NIO.2)：异步非阻塞，服务器实现模式为一个有效请求一个线程，客户端的I/O请求都是由OS先完成了再通知服务器应用去启动线程进行处理。
    AIO是异步非阻塞，基于NIO，可以称之为NIO2.0

    BIO                 NIO                         AIO
    Socket          SocketChannel           AsynchronousSocketChannel
    ServerSocket    ServerSocketChannel     AsynchronousServerSocketChannel

    异步IO则是采用“订阅-通知”模式: 即应用程序向操作系统注册IO监听，然后继续做自己的事情。
    当操作系统发生IO事件，并且准备好数据后，在主动通知应用程序，触发相应的函数

    和同步IO一样，异步IO也是由操作系统进行支持的。微软的windows系统提供了一种异步IO技术: IOCP(I/O Completion Port，I/O完成端口)；
    Linux下由于没有这种异步IO技术，所以使用的是epoll(上文介绍过的一种多路复用IO技术的实现)对异步IO进行模拟。

    与NIO不同，当进行读写操作时，只须直接调用API的read或write方法即可，这两种方法均为异步的，
    对于读操作而言，当有流可读时，操作系统会将可读的流传入read方法的缓冲区，
    对于写操作而言，当操作系统将 write方法传递的流写入完毕时，操作系统主动通知应用程序 。
    即可以理解为，read/write方法都是异步的，完成后会主动调用回调函数。在JDK1.7中，这部分内容被称作NIO.2，
    主要在java.nio.channel包下增加了下面四个异步通道：
        AsynchronousSocketChannel
        AsynchronousServerSocketChannel
        AsynchronousFileChannel
        AsynchronousDatagramChannel

 */
public class AioServerSample {

    public static void main(String[] args) throws Exception {
        AsynchronousServerSocketChannel asynchronousServerSocketChannel = AsynchronousServerSocketChannel.open();
        asynchronousServerSocketChannel.bind(new InetSocketAddress(9999));

        /*
        在JAVA AIO框架中，由于应用程序不是“轮询”方式，而是订阅-通知方式，
        所以不再需要“selector”(选择器)了，改由channel通道直接到操作系统注册监听。
         */
        // 为AsynchronousServerSocketChannel注册监听，注意只是为AsynchronousServerSocketChannel通道注册监听
        // 并不包括为随后客户端和服务器SocketChannel通道注册的监听，因此在方法调用中还需要再次注册监听
        asynchronousServerSocketChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Object>() {

            // 当数据准备就绪时，AIO框架会调用completed方法
            // asynchronousSocketChannel为客户端和服务端的通道，可以从中读取或者向其发送数据
            @Override
            public void completed(AsynchronousSocketChannel asynchronousSocketChannel, Object attachment) {
                // 每次都要重新注册监听(一次注册，一次响应)，
                // 但是由于“文件状态标示符”是独享的，所以不需要担心有“漏掉的”事件
                asynchronousServerSocketChannel.accept(attachment, this);

                // 下面开始处理客户端发送的数据
                // 为这个新的socketChannel注册“read”事件，以便操作系统在收到数据并准备好后，主动通知应用程序

                ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

                // 可以通过异步的方式进行读取
                asynchronousSocketChannel.read(byteBuffer, attachment, new SocketChannelReadHandle(asynchronousSocketChannel, byteBuffer, attachment));

                // 也可以通过下面的方式在本方法线程进行处理（不推荐）
                //try {
                //    Future<Integer> readResult = asynchronousSocketChannel.read(byteBuffer);
                //    // 返回读取到的字节数
                //    Integer count = readResult.get();
                //    byteBuffer.flip();
                //    byte[] bytes = byteBuffer.array();
                //    System.out.println("bytes length: " + bytes.length + " ----- buffer limit: " + byteBuffer.limit() + " ---- count: " + count);
                //    System.out.println("服务端接受的数据为：" + new String(bytes, 0, count));
                //} catch (Exception e) {
                //    e.printStackTrace();
                //}
            }

            @Override
            public void failed(Throwable exc, Object attachment) {
                System.out.println("aio failed with " + exc);
            }
        });

        Thread.sleep(10 * 60 * 1000);
    }

    /*
     负责对每一个SocketChannel的数据获取事件进行监听。
     每一个SocketChannel都会有一个独立工作的SocketChannelReadHandle对象(CompletionHandler接口的实现)，
     其中又都将独享一个“文件状态标示”对象FileDescriptor、一个独立的由程序员定义的Buffer缓存(这里我们使用的是ByteBuffer)、
     所以不用担心在服务器端会出现“窜对象”这种情况，因为JAVA AIO框架已经帮您组织好了

     但是最重要的，用于生成channel的对象: AsynchronousChannelProvider是单例模式，
     无论在哪组SocketChannel，都是一个对象引用(但这没关系，因为您不会直接操作这个AsynchronousChannelProvider对象)。
     */
    private static class SocketChannelReadHandle implements CompletionHandler<Integer, Object> {
        // 异步socket通道
        private AsynchronousSocketChannel socketChannel;
        // 数据缓存区
        private ByteBuffer byteBuffer;
        // attachment对象
        private Object attachment;


        // 数据对象
        private StringBuilder messages = new StringBuilder();

        public SocketChannelReadHandle(AsynchronousSocketChannel socketChannel, ByteBuffer byteBuffer, Object attachment) {
            this.socketChannel = socketChannel;
            this.byteBuffer = byteBuffer;
            this.attachment = attachment;
        }

        @Override
        public void completed(Integer result, Object attachment) {
            //如果条件成立，说明客户端主动终止了TCP套接字，这时服务端终止就可以了
            if (result == -1) {
                try {
                    this.socketChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            }

            /*
            实际上，由于我们从Integer result知道了本次channel从操作系统获取数据总长度
            所以实际上，我们不需要切换成“读模式”的，但是为了保证编码的规范性，还是建议进行切换。
            另外，无论是JAVA AIO框架还是JAVA NIO框架，都会出现“buffer的总容量”小于“当前从操作系统获取到的总数据量”，
            但区别是，JAVA AIO框架中，我们不需要专门考虑处理这样的情况，因为JAVA AIO框架已经帮我们做了处理(做成了多次通知)
            */
            this.byteBuffer.flip();
            byte[] contexts = new byte[1024];
            this.byteBuffer.get(contexts, 0, result);
            this.byteBuffer.clear();
            // 每次读取的消息
            messages.append(new String(contexts, 0, result));
            // 判断消息是否结束
            if (messages.toString().endsWith("end")) {
                System.out.println("服务端接受到的完整消息：" + messages.toString());
                return;
            }

            // 还要继续监听(一次监听一次通知)
            this.socketChannel.read(this.byteBuffer, attachment, this);
        }

        @Override
        public void failed(Throwable exc, Object attachment) {
            System.out.println("aio failed, close client, msg: " + exc.getMessage());
            try {
                socketChannel.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
