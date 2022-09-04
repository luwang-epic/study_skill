package com.wang.io.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/*
nio的数据流向是这样的：
    client -> buffer -> channel -> buffer -> server
        client: 可以是一个资源（如文件），也可以是另一个机器发送的数据
        buffer: 类似于内存缓冲区，是机器中的一个内存区域的抽象
        channel：client和server的通道，同一个client和server可以有多个channel通道
        server：服务端
    selector：可以通过selector来管理这些通道，将这些通道注册到selector中，每个通道只能注册一次
                    在注册的时候可以指定每个通道感兴趣的操作（即需要selector处理的操作）
                    不是所有的通道都可以被注册，只有集成了SelectableChannel的通道可以被注册
                    注册到selector中的通道必须是非阻塞模式，否则注册会抛异常
            感兴趣的操作有如下四种：
                可读，可写，连接，接受 （多个可以用位运算符，如：可读|可写）
            使用选择器的select方法可以查看注册到selector中的通道就绪状态查询，
                    selector会将器结果封装到SelectionKey对象中，该对象类似于事件
 */
/**
 * nio服务端
 */
public class NioServerSample {

    public static void main(String[] args) throws Exception {
        // 创建ServerSocketChannel，-->> ServerSocket
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        // 设置成非阻塞
        serverSocketChannel.configureBlocking(false);
        InetSocketAddress inetSocketAddress = new InetSocketAddress(5555);
        // 绑定连接
        serverSocketChannel.socket().bind(inetSocketAddress);

        // 开启selector,并注册accept事件
        Selector selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        while (true) {
            System.out.println("server: 等待注册到selector中的channel感兴趣事件就绪......");
            // 监听所有通道，直到有一个感兴趣的键产生，否则将会阻塞2000ms，
            // 返回所有注册的通道感兴趣键的个数，可能被唤醒，所以返回的感兴趣键个数可能为0
            // 调用wakeup方法，将会使处于阻塞状态的select方法立刻返回
            selector.select(2000);

            //遍历selectionKeys
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                // 处理连接事件 serverSocketChannel注册了感兴趣的事件为：SelectionKey.OP_ACCEPT
                if (key.isAcceptable()) {
                    SocketChannel socketChannel = serverSocketChannel.accept();
                    socketChannel.configureBlocking(false);
                    System.out.println("server: 地址为：" + socketChannel.getLocalAddress() + "的客户端 is connected");
                    socketChannel.register(selector, SelectionKey.OP_READ); //注册客户端读取事件到selector

                // 处理读取事件， 当有客户端连接后，将会注册指定的SocketChannel到selector中，
                // socketChannel感兴趣的事件为SelectionKey.OP_READ
                } else if (key.isReadable()) {
                    ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                    SocketChannel socketChannel = (SocketChannel) key.channel();

                    try {
                        /*
                        可是这样服务端怎么知道客户端已经关闭了呢？显然服务端会收到客户端的关闭信号(可读数据)，
                        而网络上绝大多数代码并无根据这个关闭信号来结束channel。那么关闭信号是什么？

                        这个语句是有返回值的，大多数状况是返回一个大于等于0的值，表示将多少数据读入byteBuffer缓冲区。
                        然而，当客户端正常断开链接的时候，它就会返回-1。虽然这个断开链接信号也是可读数据(会使得isReadable()为true)，
                        可是这个信号没法被读入byteBuffer，也就是说一旦返回-1，那么不管再继续读多少次都是-1，而且会引起可读事件isReadable()。

                        这里我根据返回值-1来抛出异常，使得下面的catch语句块捕捉并关闭链接，也能够不抛出异常，直接在try{}里处理。
                        还要注意一点的是，假如说bytebuffer已经满了，也就是channel.read(byteBuffer)返回0，
                        那么即便客户端正常关闭，也没法收到-1。所以当bytebuffer满的时候须要及时清空，
                        或者一开始就开一个大一点的bytebuffer。s
                         */
                        int num = socketChannel.read(byteBuffer);
                        if(num == -1) {
                            throw new IOException("server: 地址为：" + socketChannel.getLocalAddress() + "的客户端已经关闭了...");
                        }
                        byteBuffer.flip();
                        System.out.println("server: 接受到地址为：" + socketChannel.getLocalAddress() + "发送来的数据，具体内容为：" + new String(byteBuffer.array(), 0, byteBuffer.limit()));
                        byteBuffer.clear();
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                        key.cancel();
                        if (socketChannel != null) {
                            socketChannel.close();
                        }
                    }
                }

                // 事件处理完毕，要记得清除，防止重复处理
                iterator.remove();
            }
        }

        // 是的任何一个在select方法上阻塞的线程被唤醒（类似wakeup方法），
        // 同时注销注册到selector上的所有channel和感兴趣的键，但是不会关闭channel本身
//        selector.close();
    }
}
