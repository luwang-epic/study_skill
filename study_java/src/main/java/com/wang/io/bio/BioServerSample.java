package com.wang.io.bio;

/*
传统的同步阻塞模型开发中，服务端ServerSocket负责绑定IP地址，启动监听端口；客户端Socket负责
发起 连接操作。连接成功后，双方通过输入和输出流进行同步阻塞式通信。
基于BIO模式下的通信，客户端-服务端是完全同步，完全藕合的。
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 服务端
 */
public class BioServerSample {

    public static void main(String[] args) {
        try {
            System.out.println("===服务端启动===");
            // 1.定义一个ServerSocket对象进行服务端的端口注册
            ServerSocket ss = new ServerSocket(9999);
            // 2. 监听客户端的Socket连接请求
            // 如果没有客户端连接，将会一直阻塞，直到有连接
            Socket socket = ss.accept();
            // 3.从socket管道中得到一个字节输入流对象
            InputStream is = socket.getInputStream();
            // 4.把字节输入流包装成一个缓存字符输入流
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String msg;
            // 如果没有数据，一直阻塞等待，直到有一行数据；
            // 如果只发一行，使用if就可以，如果循环发送，需要使用while
            if ((msg = br.readLine()) != null) {
                System.out.println("服务端接收到：" + msg);
            }

            // 循环接受消息
            while ((msg = br.readLine()) != null) {
                System.out.println("服务端接收到：" + msg);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
