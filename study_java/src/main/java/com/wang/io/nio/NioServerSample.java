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

     selector的几点说明：
        每次向选择器注册通道时，都会创建一个选择键SelectionKey，在通过调用其cancel方法取消键、
            关闭其通道或关闭其选择器之前，键一直有效。cancel方法取消键不会立即将其从选择器中移除，
            而是将其添加到选择器的取消键集合中， 以便在下一次选择操作中删除，
            可以通过调用其isValid方法来测试密钥SelectionKey的有效性
        SelectionKey包含两个表示为整数值的操作集，一个是兴趣集，一个是就绪操作集，
            兴趣集表示SelectionKey对对应通道的某类操作感兴趣，当选择器Selector监听到该通道已经准备好该操作时候，
            会将该操作放入就绪操作集，其实就是将表示就绪操作集的整数的某些位设置为1，
            而一开始设置兴趣集的时候也是将表示兴趣集的整数的某些位设置为1
        选择键的就绪集表示其通道已为某个操作类别做好准备，这是一种提示，但不能保证线程可以执行此类类别中的操作，
            而不会导致线程阻塞。就绪集最有可能在选择操作select()完成后立即准确，
            外部事件和在相应通道上调用的I/O操作可能会使其不准确，因为当调用I/O操作后会把就绪集的对应位设为置为0，
            但可能在置为0后，该通道又准备好了该操作，只有等待下一次select()重新将就绪操作集设置为准确状态
        通常需要将某些特定于应用程序的数据与选择键SelectionKey相关联
            因此，选择键支持将单个任意对象附着到键。对象可以通过attach方法附加，然后通过attachment()方法检索
        多个并发线程可以安全地使用选择键SelectionKey，选择器的选择操作select()将始终使用操作开始时的当前兴趣集值


    buffer重要概念：
        1. 容量（capacity)：作为一个内存块，Buffer具有一定的固定大小，也称为”容量”，缓冲区容量不能
            为负，并且 创建后不能更改。
        2. 限制（limit)：表示缓冲区中可以操作数据的大小（limit后数据不能进行读写）。缓冲区的限制不
            能为负，并且不能大于其容量。写入模式，限制等于buffer的容量。读取模式下，limit等于写入的数据量。
        3. 位置(position)：下一个要读取或写入的数据的索引。缓’中区的位置不能为负，并且不能大于其限制
        4. 标记（mark）与重置（reset)：标记是一个索弓l，通过Buffer中的mark(）方法指定Buffer中一
            个特定的 position，之后可以通过调用reset(）方法恢复到这个position。标记、位置、限制、容
            量遵守以T不变式：0<=mark<=position<=limit<=capacity

    byte buffer。可以是两种类型，一种是基于直接内存（也就是非堆内存）；另一种是非直接内存（也就是堆内存）。
    对于直接内存来说，JVM将会在IO操作上具有更高的性能，因为它直接作用于本地系统的IO操作。
    而非直接 内存，也就是堆内存中的数据，如果要作IO操作，会先从本进程内存复制到直接内存，再利用本地IO处理。
    从数据流的角度，非直接内存是下面这样的作用链：本地IO--->直接内存--->非直接内存--->直接内存--->本地IO
    而直接内存是：本地IO--->直接内存--->本地IO
    很明显，在做IO处理时，比如网络发送大量数据时，直接内存会具有更高的效率。直接内存使用allocateDirect创建，
    但是它比申请普通的堆内存需要耗费更高的性能。不过，这部分的数据是在JVM之外的，因此它不会占用应用的内存。
    所以呢，当你有很大的数据要缓存，并且它的生命周期又很长，那么就比较适合使用直接内存。
    只是一般 来说，如果不是能带来很明显的性能提升，还是推荐直接使用堆内存。
    字节缓冲区是直接缓冲区还是非直接缓冲区 可通过调用其isDirect()方法来确定。

    Channel表示IO源与目标打开的连接。Channel类似于传统的“流”。只不过Channel本身不能直接访问数据，Channel只能与Buffer进行交互。
    BIO中的stream是单向的，例如FileInputStream对象只能进行读取数据的操作，而NIO中的通道（Channel)是双向的，可以读操作，也可以写操作

    常用的Channel实现类
        FileChannel：用于读取、写入、映射和操作文件的通道
        DatagramChannel：通过UDP读写网络中的数据通道
        SocketChannel：通过TCP读写网络中额数据
        ServerSocketChannel：可以监听新进来的TCP连接，对每一个新进来的连接都会创建一个
        SocketChannel。【ServerSocketChannel类似ServerSocket，SocketChannel类似Socket】
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
                        // 外部事件和在相应通道上调用的I/O操作可能会使其不准确，因为当调用I/O操作后会把就绪集的对应位置为0
                        key.interestOps(0);
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
                            System.out.println("server: key是否有效：" + key.isValid());
                            throw new IOException("server: 地址为：" + socketChannel.getLocalAddress() + "的客户端已经关闭了...");
                        }
                        byteBuffer.flip();
                        System.out.println("server: 接受到地址为：" + socketChannel.getLocalAddress() + "发送来的数据，具体内容为：" + new String(byteBuffer.array(), 0, byteBuffer.limit()));
                        byteBuffer.clear();

                        // 一般会在读取数据之前将key设置为不感兴趣的集合，然后读取完再设置回去
                        key.interestOps(SelectionKey.OP_READ);
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
