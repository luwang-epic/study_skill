package com.wang.io.nio;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * nio客户端
 */
public class NioClientSample {

    public static void main(String[] args) throws Exception {
        // SocketChannel是基于tcp的，而DatagramChannel是基于udp的
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        InetSocketAddress inetSocketAddress = new InetSocketAddress("127.0.0.1", 5555);

        if (!socketChannel.connect(inetSocketAddress)) {
            while (!socketChannel.finishConnect()) {
                System.out.println("client: 客户端正在连接中，请耐心等待......");
                Thread.sleep(1000);
            }
        }

        ByteBuffer byteBuffer = ByteBuffer.wrap("test".getBytes("UTF-8"));
        socketChannel.write(byteBuffer);
        byteBuffer.clear();
        socketChannel.close();
    }
}
