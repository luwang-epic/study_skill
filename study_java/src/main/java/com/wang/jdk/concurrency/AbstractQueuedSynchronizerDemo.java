package com.wang.jdk.concurrency;

/*
CAS会有aba问题，通过版本号解决; 可以参考类为：AtomicStampedReference，通过版本号解决aba问题

AQS支持两种同步方式: 独占式 和 共享式，AQS为使用提供了底层支撑，如何组装实现，使用者可以自由发挥。
    独占式，如：ReentrantLock
    共享式，如：Semaphore，CountDownLatch
    组合式，如：ReentrantReadWriteLock

AQS的全称是AbstractQueuedSynchronizer（抽象的队列式的同步器），AQS定义了一套多线程访问共享资源的同步器框架，
许多同步类实现都依赖于它，如常用的ReentrantLock/Semaphore/CountDownLatch等。

核心思想：自旋等待、CAS操作，阻塞
    acquire操作是这样的：
        while (当前同步器的状态不允许获取操作) {
            如果当前线程不在队列中，则将其插入队列
            阻塞当前线程
        }
        如果线程位于队列中，则将其移出队列
    release操作是这样的：
        更新同步器的状态
        if (新的状态允许某个被阻塞的线程获取成功)
            解除队列中一个或多个线程的阻塞状态

    从这两个操作中的思想中我们可以提取出三大关键操作：同步器的状态变更、线程阻塞和释放、插入和移出队列。
    所以为了实现这两个操作，需要协调三大关键操作引申出来的三个基本组件：
        同步器状态的原子性管理；
        线程阻塞与解除阻塞；
        队列的管理；


具体需要看源码，查看内部的实现细节，可以参考相关博客或者视频后再查看

 */

import java.util.concurrent.locks.AbstractQueuedSynchronizer;

// 基于AQS实现线程安全的计数器
public class AbstractQueuedSynchronizerDemo {


    public static void main(String[] args) throws Exception {
        // 不加锁计数
        countWithoutLock();

        // 使用自己的锁计数
        countWithMyMutexLock();
    }

    // 计数器
    private static int count;

    public static void increase() {
        count++;
    }

    public static void countWithoutLock() throws Exception {
        count = 0;
        Thread other = new Thread(() -> {
            for (int i = 0; i < 10000; i++) {
                increase();
            }
        });
        other.start();
        for (int i = 0; i < 10000; i++) {
            increase();
        }

        Thread.sleep(2000);
        System.out.println("计数器的值为：" + count);
    }

    public static void countWithMyMutexLock() throws Exception {
        count = 0;
        MyMutexLock lock = new MyMutexLock();
        Thread other = new Thread(() -> {
            for (int i = 0; i < 10000; i++) {
                lock.lock();
                try {
                    increase();
                } finally {
                    lock.unlock();
                }
            }
        });
        other.start();
        for (int i = 0; i < 10000; i++) {
            lock.lock();
            try {
                increase();
            } finally {
                lock.unlock();
            }
        }

        Thread.sleep(2000);
        System.out.println("计数器的值为：" + count);
    }

    /*
    同步类在实现时一般都将自定义同步器（sync）定义为内部类，供自己使用；而同步类自己（MyMutexLock）则实现某个接口，对外服务。
    当然，接口的实现要直接依赖sync，它们在语义上也存在某种对应关系。而sync只用实现资源state的获取-释放方式tryAcquire-tryRelelase，
    至于线程的排队、等待、唤醒等，上层的AQS都已经实现好了，我们不用关心。

    ReentrantLock/CountDownLatch/Semphore这些同步类的实现方式都差不多，
    不同的地方就在获取、释放资源的方式tryAcquire-tryRelelase。
     */

    // 我们利用AQS来实现一个不可重入的互斥锁实现。
    // 锁资源（AQS里的state）只有两种状态：0表示未锁定，1表示锁定
    private static class MyMutexLock {
        /**
         * 静态内部类，自定义同步器
         */
        private static class Sync extends AbstractQueuedSynchronizer {
            @Override
            protected boolean isHeldExclusively() {
                // 是否有资源可用
                return getState() == 1;
            }

            @Override
            public boolean tryAcquire(int acquires) {
                assert acquires == 1;
                // state:0——>1，代表获取锁
                if (compareAndSetState(0, 1)) {
                    // 设置当前占用资源的线程
                    setExclusiveOwnerThread(Thread.currentThread());
                    return true;
                }
                return false;
            }

            @Override
            protected boolean tryRelease(int releases) {
                assert releases == 1;
                if (getState() == 0) {
                    throw new IllegalMonitorStateException();
                }
                setExclusiveOwnerThread(null);
                // state:1——>0，代表释放锁
                setState(0);
                return true;
            }
        }

        private final Sync sync = new Sync();

        /**
         * 获取锁，可能会阻塞
         */
        public void lock() {
            sync.acquire(1);
        }

        /**
         * 尝试获取锁，无论成功或失败，立即返回
         */
        public boolean tryLock() {
            return sync.tryAcquire(1);
        }

        /**
         * 释放锁
         */
        public void unlock() {
            sync.release(1);
        }
    }
}
