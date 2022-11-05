package com.wang.rpc.thrift;

import com.wang.rpc.thrift.api.User;
import com.wang.rpc.thrift.api.UserService;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.layered.TFramedTransport;

/**
 * thrift客户端
 */
public class ThriftClientSample {

    public static void main(String[] args) {
        try {
            //bioClientDemo();
            nioClientDemo();

            Thread.sleep(10000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 非阻塞的IO客户端
     */
    private static void nioClientDemo() throws TException {
        // 设置传输通道，对于非阻塞的IO需要使用TFramedTransport(用于将数据分块发送)
        TTransport transport = new TFramedTransport(new TSocket("localhost", 9999));
        // 指定二进制编码格式，需要和服务端保持一致
        //TProtocol protocol = new TBinaryProtocol(transport);
        // 设置thrift协议，协议需要和服务端一致
        TProtocol protocol = new TCompactProtocol(transport);

        // 建立连接
        transport.open();

        // 发起rpc调用
        UserService.Client client = new UserService.Client(protocol);
        User user = client.getById(1);
        System.out.println(user);

        // 关闭连接
        transport.close();
    }

    /**
     * 阻塞IO的客户端
     */
    private static void bioClientDemo() throws TException {
        // 使用阻塞的IO
        TTransport transport = new TSocket("localhost", 9999);
        // 指定二进制编码格式，需要和服务端保持一致
        TProtocol protocol = new TBinaryProtocol(transport);

        // 建立连接
        transport.open();

        // 发起rpc调用
        UserService.Client client = new UserService.Client(protocol);
        User user = client.getById(1);
        System.out.println(user);

        // 关闭连接
        transport.close();
    }
}
