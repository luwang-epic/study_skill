package com.wang.jdk.concurrency;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Condition是在Java 1.5中才出现的，它用来替代传统的Object的wait()、notify()实现线程间的协作，
 * 相比使用Object的wait()、notify()，使用Condition的await()、signal()这种方式实现线程间协作更加安全和高效。
 * 因此通常来说比较推荐使用Condition，阻塞队列实际上是使用了Condition来模拟线程间协作。
 * <p>
 * Condition是个接口，基本的方法就是await()和signal()方法；(对于Object的wait()和notify()方法)
 * Condition依赖于Lock接口，生成一个Condition的基本代码是lock.newCondition()
 * 调用Condition的await()和signal()方法，都必须在lock保护之内，
 * 就是说必须在lock.lock()和lock.unlock之间才可以使用
 */
public class ConditionDemo {

    final private Lock lock = new ReentrantLock();
    final private Condition condition = lock.newCondition();

    class Consumer extends Thread {

        @Override
        public void run() {
            consume();
        }

        private void consume() {
            lock.lock();
            try {
                System.out.println("我在等一个新信号" + Thread.currentThread().getName());
                // 会释放锁，等到收到信号后再重新获取锁 (需要等待发送信号的线程释放锁之后才可以获取到)
                condition.await();
                System.out.println("我已经等到了一个信号" + Thread.currentThread().getName());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }
    }


    class Producer extends Thread {
        @Override
        public void run() {
            producer();
        }

        private void producer() {
            lock.lock();
            try {
                System.out.println("我拿到锁" + Thread.currentThread().getName());
                condition.signalAll();
                System.out.println("我发出了一个信号：" + Thread.currentThread().getName());
                Thread.sleep(2000);
                System.out.println("我等待了2s时间" + Thread.currentThread().getName());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }
    }


    public static void main(String[] args) {
        ConditionDemo demo = new ConditionDemo();

        demo.new Consumer().start();
        demo.new Producer().start();

    }


}
