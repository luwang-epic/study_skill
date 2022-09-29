package com.wang.jdk.concurrency;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 比较锁性能：synchronized关键字，基于cas实现的原子锁，ReentrantLock锁
 */
public class LockPerformanceDemo {

    // 运行程序，观看输出
    public static void main(String[] args) throws Exception {
        // 两个线程的测试
        performanceWithTwoThread();

        // 多个线程的测试
        performanceWithMultiThread(10);
    }

    private static void performanceWithTwoThread() throws Exception {
        Counter counter = new Counter();

        // 使用synchronized关键字加锁
        long synchronizedStart = System.currentTimeMillis();
        Thread synchronizedThread = new Thread(() -> {
            for (int i = 0; i < 10000000; i++) {
                counter.increaseBySynchronized();
            }
        });
        synchronizedThread.start();
        for (int i = 0; i < 10000000; i++) {
            counter.increaseBySynchronized();
        }
        synchronizedThread.join();
        long synchronizedEnd = System.currentTimeMillis();
        System.out.println("累加到: " + counter.getSynchronizedLockCount()
                + ", synchronized关键字锁耗时: " + (synchronizedEnd - synchronizedStart) + "ms");

        // 使用ReentrantLock加锁
        long reentrantLockStart = System.currentTimeMillis();
        Thread reentrantLockThread = new Thread(() -> {
            for (int i = 0; i < 10000000; i++) {
                counter.increaseByReentrantLock();
            }
        });
        reentrantLockThread.start();
        for (int i = 0; i < 10000000; i++) {
            counter.increaseByReentrantLock();
        }
        reentrantLockThread.join();
        long reentrantLockEnd = System.currentTimeMillis();
        System.out.println("累加到: " + counter.getReentrantCount()
                + ", ReentrantLock锁耗时: " + (reentrantLockEnd - reentrantLockStart) + "ms");

        // 使用AtomicInteger(CAS)加锁
        long atomicIntegerStart = System.currentTimeMillis();
        Thread atomicIntegerThread = new Thread(() -> {
            for (int i = 0; i < 10000000; i++) {
                counter.increaseByAtomicInteger();
            }
        });
        atomicIntegerThread.start();
        for (int i = 0; i < 10000000; i++) {
            counter.increaseByAtomicInteger();
        }
        atomicIntegerThread.join();
        long atomicIntegerEnd = System.currentTimeMillis();
        System.out.println("累加到: " + counter.getAtomicCount()
                + ", AtomicInteger(CAS)锁耗时: " + (atomicIntegerEnd - atomicIntegerStart)+ "ms");
    }

    private static void performanceWithMultiThread(int threadCount) throws Exception {
        Counter counter = new Counter();

        // 使用synchronized关键字加锁
        long synchronizedStart = System.currentTimeMillis();
        List<Thread> synchronizedThreads = new ArrayList<>(16);
        CountDownLatch synchronizedCountDownLatch = new CountDownLatch(threadCount);
        for (int k = 0; k < threadCount; k++) {
            synchronizedThreads.add(new Thread(() -> {
                for (int i = 0; i < 10000000; i++) {
                    counter.increaseBySynchronized();
                }
                synchronizedCountDownLatch.countDown();
            }));
        }
        synchronizedThreads.forEach(Thread::start);
        synchronizedCountDownLatch.await();
        long synchronizedEnd = System.currentTimeMillis();
        System.out.println("累加到: " + counter.getSynchronizedLockCount()
                + ", synchronized关键字锁耗时: " + (synchronizedEnd - synchronizedStart) + "ms");

        // 使用ReentrantLock加锁
        long reentrantLockStart = System.currentTimeMillis();
        List<Thread> reentrantLockThreads = new ArrayList<>(16);
        CountDownLatch reentrantLockCountDownLatch = new CountDownLatch(threadCount);
        for (int k = 0; k < threadCount; k++) {
            reentrantLockThreads.add(new Thread(() -> {
                for (int i = 0; i < 10000000; i++) {
                    counter.increaseByReentrantLock();
                }
                reentrantLockCountDownLatch.countDown();
            }));
        }
        reentrantLockThreads.forEach(Thread::start);
        reentrantLockCountDownLatch.await();
        long reentrantLockEnd = System.currentTimeMillis();
        System.out.println("累加到: " + counter.getReentrantCount()
                + ", ReentrantLock锁耗时: " + (reentrantLockEnd - reentrantLockStart) + "ms");

        // 使用AtomicInteger(CAS)加锁
        long atomicIntegerStart = System.currentTimeMillis();
        List<Thread> atomicIntegerThreads = new ArrayList<>(16);
        CountDownLatch atomicIntegerLockCountDownLatch = new CountDownLatch(threadCount);
        for (int k = 0; k < threadCount; k++) {
            atomicIntegerThreads.add(new Thread(() -> {
                for (int i = 0; i < 10000000; i++) {
                    counter.increaseByAtomicInteger();
                }
                atomicIntegerLockCountDownLatch.countDown();
            }));
        }
        atomicIntegerThreads.forEach(Thread::start);
        atomicIntegerLockCountDownLatch.await();
        long atomicIntegerEnd = System.currentTimeMillis();
        System.out.println("累加到: " + counter.getAtomicCount()
                + ", AtomicInteger(CAS)锁耗时: " + (atomicIntegerEnd - atomicIntegerStart)+ "ms");
    }

    private static class Counter {
        private int synchronizedLockCount;
        private int reentrantLockCount;
        private AtomicInteger atomicCount = new AtomicInteger();

        private Object synchronizedLock = new Object();
        private ReentrantLock reentrantLock = new ReentrantLock();

        public long getSynchronizedLockCount() {
            return synchronizedLockCount;
        }
        public long getReentrantCount() {
            return reentrantLockCount;
        }
        public long getAtomicCount() {
            return atomicCount.get();
        }

        // 通过synchronized关键字实现计数
        public void increaseBySynchronized() {
            synchronized (synchronizedLock) {
                synchronizedLockCount++;
            }
        }

        // 通过ReentrantLock实现计数
        public void increaseByReentrantLock() {
            try {
                reentrantLock.lock();
                reentrantLockCount++;
            } finally {
                reentrantLock.unlock();
            }
        }

        // 通过AtomicInteger(CAS))实现计数
        public void increaseByAtomicInteger() {
            atomicCount.incrementAndGet();
        }
    }

}
