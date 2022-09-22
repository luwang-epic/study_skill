package com.wang.io.bio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 多线程服务端
 */
public class BioMultiThreadServerSample {

    public static void main(String[] args) {
        try {
            // 1.注册端口
            ServerSocket ss = new ServerSocket(9999);
            // 2.定义一个死循环，负责不断的接收客户端的Socket的连接请求
            while (true) {
                Socket socket = ss.accept();
                // 3.创建一个独立的线程来处理与这个客户端的socket通信需求
                new ServerThreadReader(socket).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class ServerThreadReader extends Thread {
        private Socket socket;

        public ServerThreadReader(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                // 从socket对象中得到一个字节输入流
                InputStream is = socket.getInputStream();
                // 使用缓存字符输入流包装字节输入流
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String msg;
                while ((msg = br.readLine()) != null) {
                    System.out.println(msg);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
