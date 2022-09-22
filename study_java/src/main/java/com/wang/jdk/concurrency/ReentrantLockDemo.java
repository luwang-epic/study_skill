package com.wang.jdk.concurrency;

import java.util.concurrent.locks.ReentrantLock;

/*
ReentrantLock和Synchronized关键字的区别：
区别一：API层面
synchronized既可以修饰方法，也可以修饰代码块。
synchronized修饰代码块时，包含两部分：锁对象的引用和这个锁保护的代码块。

区别二：等待可中断
等待可中断是指当持有锁的线程长期不释放锁的时候，正在等待的线程可以选择放弃等待，改为处理其他事情。
可等待特性对处理执行时间非常长的同步快很有帮助。
如果使用 synchronized ，如果A不释放，B将一直等下去，不能被中断
如果使用ReentrantLock，如果A不释放，可以使B在等待了足够长的时间以后，中断等待，而干别的事情
ReentrantLock获取锁定与三种方式：
    a) lock(), 如果获取了锁立即返回，如果别的线程持有锁，当前线程则一直处于休眠状态，直到获取锁
    b) tryLock(), 如果获取了锁立即返回true，如果别的线程正持有锁，立即返回false；
    c) tryLock(long timeout,TimeUnit unit)， 如果获取了锁定立即返回true，如果别的线程正持有锁，会等待参数给定的时间，在等待的过程中，如果获取了锁定，就返回true，如果等待超时，返回false；
    d) lockInterruptibly:如果获取了锁定立即返回，如果没有获取锁定，当前线程处于休眠状态，直到或者锁定，或者当前线程被别的线程中断
synchronized是在JVM层面上实现的，lock是通过代码实现的，JVM会自动释放锁定（代码执行完成或者出现异常），但是使用Lock则不行，要保证锁定一定会被释放，就必须将unLock()放到finally{}中。

区别三：公平锁
公平锁是指多个线程在等待同一个锁时，必须按照申请的时间顺序来依次获得锁；而非公平锁则不能保证这一点。
非公平锁在锁被释放时，任何一个等待锁的线程都有机会获得锁。
synchronized的锁是非公平锁，ReentrantLock默认情况下也是非公平锁，但可以通过带布尔值的构造函数要求使用公平锁。

区别四：锁绑定多个条件
ReentrantLock可以同时绑定多个Condition对象，只需多次调用newCondition方法即可。
synchronized中，锁对象的wait()和notify()或notifyAll()方法可以实现一个隐含的条件。
但如果要和多于一个的条件关联的时候，就不得不额外添加一个锁。
具体见：ConditionDemo


区别五：性能
JDK1.5，synchronized还有很大的优化余地。JDK 1.6 中加入了很多针对锁的优化措施，
synchronized与ReentrantLock性能方面基本持平。虚拟机在未来的改进中更偏向于原生的synchronized。

相同点：都是可重入的。可重入指同一个线程多次试图获取它所占的锁，请求会成功。当释放的时候，直到重入次数清零，锁才释放。
 */
public class ReentrantLockDemo {

    // 参数为true表示为公平锁，查看具体的输出顺序
    private static ReentrantLock lock = new ReentrantLock(true);

    private static class FairLockThread implements Runnable {
        @Override
        public void run() {
            for (int i = 0; i < 10; i++) {
                lock.lock();
                try {
                    System.out.println(Thread.currentThread().getName() + " ---> " + i);
                } finally {
                    lock.unlock();
                }
            }
        }
    }

    public static void main(String[] args) {
        Thread t1 = new Thread(new FairLockThread());
        Thread t2 = new Thread(new FairLockThread());
        t1.start();
        t2.start();
    }
}
