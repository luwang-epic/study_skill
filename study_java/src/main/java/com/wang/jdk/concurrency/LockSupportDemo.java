package com.wang.jdk.concurrency;

import java.util.concurrent.locks.LockSupport;

/*
LockSupport类，是JUC包中的一个工具类，是用来创建锁和其他同步类的基本线程阻塞原语。

LockSupport类的核心方法有两个：park()和unpark()，其中park()方法用来阻塞当前调用线程，unpark()方法用于唤醒指定线程。
这其实和Object类的wait()和singal()方法有些类似，
但是LockSupport的这两种方法从语义上将比Object类的方法更清晰，而且可以针对指定线程进行阻塞和唤醒。

LockSupport类使用了一种名为Permit（许可证）的概念来做到阻塞和唤醒线程的功能，
可以把许可看成是一种(0,1)信号量(Semaphore)，但与Semaphore不同的是，许可的累加上限是1。
初始时，permit为0，当调用unpark()方法时，线程的permit加1，当调用park()方法时，如果permit为0，则调用线程进入阻塞状态。


与Object类的wait()、notify()、notifyAll()相比，park()和unpark()又有哪些特点：
    1. wait、notify、notifyAll必须配合Object Monitor一起来使用，而park、unpark不需要
    2. park & unpark是以线程为单位来阻塞和唤醒线程，
        而notify只能随机唤醒一个等待线程，notifyAll是唤醒所有等待线程，就不那么精确
    3. park & unpark可以先unpark，而wait & notify不能先notify
    4. 多次unpark不能叠加，多次unpark只需一次park就可抵消掉

 */
public class LockSupportDemo {

    public static void main(String[] args) throws Exception {
        Thread packThread = new Thread(() -> {
            System.out.println("在packThread线程中执行");
            /*
            如果调用park方法的线程已经拿到了与LockSupport关联的许可证，则调用LockSupport.park()时会马上返回，
            否则调用线程会被禁止参与线程的调度，也就是会被阻塞挂起。

            在其他线程调用unpark(Thread thread)方法并且将当前线程作为参数时，调用park方法而被阻塞的线程会返回。
            另外，如果其他线程调用了阻塞线程的interrupt()方法，设置了中断标志或者线程被虚假唤醒，则阻塞线程也会返回。
            所以在调用park方法时最好也是用循环条件判断方式。

            需要注意的是，因调用方法而被阻塞的线程被其他线程中断而返回时并不会抛出InterruptedException异常。
             */
            // 底层调用sun.misc.Unsafe的pack方法来实现，LockSupport是对其的封装
            LockSupport.park();
            System.out.println("park后继续执行packThread线程后续工作");
        });
        packThread.start();

        Thread.sleep(1000);
        System.out.println("主线程执行");

        // LockSupport.park()方法可以响应中断，会被唤醒继续执行后续方法，但是不会抛异常
        //System.out.println("准备中断packThread线程");
        //packThread.interrupt();
        //Thread.sleep(1000);
        //System.out.println("中断packThread线程睡眠1s后");
        //Thread.sleep(1000);

        /*
        当一个线程调用unpark时，如果参数thread线程没有持有thread与LockSupport类关联的许可证，
        则让thread线程持有。如果thread之前因调用park()而被挂起，则调用unpark后，该线程被唤醒。
        如果thread之前没有调用park，则调用unpark方法后，再调用park方法，其会立刻返回。
         */
        // 底层调用sun.misc.Unsafe的unpark方法来实现，LockSupport是对其的封装
        LockSupport.unpark(packThread);
    }


}
