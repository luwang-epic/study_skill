package com.wang.io.bio;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

/**
 * 客户端
 */
public class BioClientSample {

    public static void main(String[] args) {
        try {
            // 1.创建Socket对象请求服务端的连接
            Socket socket = new Socket("127.0.0.1", 9999);
            // 2.从Socket对象中获取一个字节输出流
            OutputStream os = socket.getOutputStream();
            // 3.把字节输出流包装成一个打印流
            PrintStream ps = new PrintStream(os);

            // 发送一行数据
            ps.println("hello World! 服务端，你好");
            ps.flush();

            // 循环发送消息
            Scanner sc = new Scanner(System.in);
            while (true) {
                System.out.print("请说：");
                String msg = sc.nextLine();
                ps.println(msg);
                ps.flush();
            }

            // 通知服务端，我客户端这边的数据已经发送完毕了,
            // 这样服务端的读取方法将会返回结束标志，而不会一直在阻塞等待数据
            // socket.shutdownOutput();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
