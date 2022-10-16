package com.wang.jdk.collection;

import lombok.AllArgsConstructor;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TransferQueue;

/**
 * TransferQueue的实现类LinkedTransferQueue的演示
 */
public class TransferQueueDemo {

    /*
    从类的源码可以看到TransferQueue同时也是一个阻塞队列，它具备阻塞队列的所有特性。
    生产者会一直阻塞直到所添加到队列的元素被某一个消费者所消费（不仅仅是添加到队列里就完事）。
    提供的方法：
        1. put ，take，offer ，poll等阻塞队列都有的方法
        2. transfer(E e)若当前存在一个正在等待获取的消费者线程，即立刻将e移交之；
            否则将元素e插入到队列尾部，并且当前线程进入阻塞状态，直到有消费者线程取走该元素。
        3. tryTransfer(E e)若当前存在一个正在等待获取的消费者线程，则该方法会即刻转移e，并返回true;
            若不存在则返回false，但是并不会将e插入到队列中。这个方法不会阻塞当前线程，要么快速返回true，要么快速返回false。
        4. hasWaitingConsumer()和getWaitingConsumerCount()用来判断当前正在等待消费的消费者线程个数。
        5. tryTransfer(E e, long timeout, TimeUnit unit) 若当前存在一个正在等待获取的消费者线程，
            会立即传输给它; 否则将元素e插入到队列尾部，并且等待被消费者线程获取消费掉。
            若在指定的时间内元素e无法被消费者线程获取，则返回false，同时该元素从队列中移除。

    可以控制线程的执行，如让两个线程交替执行
    更重要的，当我们不想生产者过度生产消息时，TransferQueue可能非常有用，可避免发生OutOfMemory错误。
    在这样的设计中，消费者的消费能力将决定生产者产生消息的速度。
     */

    /**
     * 线程交替执行
     */
    @Test
    public void threadInTurnExecute() {
        TransferQueue<Integer> transferQueue = new LinkedTransferQueue<>();

        Thread t1 = new Thread(() -> {
            try {
                int num = 0;
                while (num <= 100) {
                    transferQueue.add(num + 1);
                    num = transferQueue.take();
                    System.out.println(Thread.currentThread() + " : " + num);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        Thread t2 = new Thread(() -> {
            try {
                int num = 0;
                while (num <= 100) {
                    num = transferQueue.take();
                    System.out.println(Thread.currentThread() + " : " + num);
                    transferQueue.add(num + 1);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        t1.start();
        t2.start();
    }

    /**
     * 实现生产者和消费者模式
     */
    @Test
    public void producerConsumerDemo() {
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        TransferQueue<String> transQueue = new LinkedTransferQueue<>();
        Producer producer = new Producer(transQueue);
        Consumer consumer = new Consumer(transQueue);

        executorService.execute(producer);
        executorService.execute(consumer);
        executorService.shutdown();
    }

    @AllArgsConstructor
    public static class Producer implements Runnable {
        private TransferQueue<String> transferQueue;

        @Override
        public void run() {
            for (int i = 0; i < 3; i++) {
                try {
                    System.out.println("Producer is waiting to transfer...");
                    transferQueue.transfer("A" + i);
                    System.out.println("producer transferred element: A" + i);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    @AllArgsConstructor
    private static class Consumer implements Runnable {
        private TransferQueue<String> transferQueue;

        @Override
        public void run() {
            for (int i = 0; i < 3; i++) {
                try {
                    System.out.println("Consumer is waiting to take element...");
                    String s = transferQueue.take();
                    System.out.println("Consumer received Element: " + s);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
