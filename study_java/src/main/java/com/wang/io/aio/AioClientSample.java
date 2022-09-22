package com.wang.io.aio;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.Future;

/**
 * AIO客户端
 */
public class AioClientSample {

    public static void main(String[] args) throws Exception {
        AsynchronousSocketChannel asynchronousSocketChannel = AsynchronousSocketChannel.open();
        Future<Void> connectResult = asynchronousSocketChannel.connect(new InetSocketAddress("127.0.0.1", 9999));
        connectResult.get();

        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        byteBuffer.put("helle aio end".getBytes());
        byteBuffer.flip();
        asynchronousSocketChannel.write(byteBuffer);
    }

}
