package com.wang.io.bio;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 伪异步通讯服务端
 * 思路：服务端没接收到一个客户端socket请求对象之后都交给一个独立的线程来处理客户端的数据交互需求
 */
public class BioPseudoAsynchronousServerSample {

    public static void main(String[] args) {
        try {
            // 1.注册端口
            ServerSocket ss = new ServerSocket(9999);
            // 初始化一个线程池对象
            HandlerSocketServerPool pool = new HandlerSocketServerPool(3, 10);
            // 2.定义一个死循环，负责不断的接收客户端的Socket的连接请求
            while (true) {
                Socket socket = ss.accept();
                // 3.把socket对象交给一个线程池进行处理
                // 把socket封装成一个任务对象交给线程池处理
                Runnable target = new ServerRunnableTarget(socket);
                pool.execute(target);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class ServerRunnableTarget implements Runnable {
        private Socket socket;

        public ServerRunnableTarget(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            // 处理接收到的客户端socket通信需求
            try {
                // 1.从socket管道中得到一个字节输入流对象
                InputStream is = socket.getInputStream();
                // 2.把字节输入流包装成一个缓存字符输入流
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String msg;
                while ((msg = br.readLine()) != null) {
                    System.out.println("服务端收到：" + msg);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static class HandlerSocketServerPool {
        //1. 创建一个线程池的成员变量用于存储一个线程池对象
        private ExecutorService executorService;

        public HandlerSocketServerPool(int maxThreadNum, int queueSize) {
            executorService = new ThreadPoolExecutor(3, maxThreadNum, 120,
                    TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(queueSize));
        }

        /**
         * 3.提供一个方法来提交任务给线程池的任务队列来暂存，等待线程池来处理
         */
        public void execute(Runnable target) {
            executorService.execute(target);
        }
    }

}
