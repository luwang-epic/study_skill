package com.wang.zookeeper;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;

/**
 * Curator分布式锁示例
 */
public class CuratorLockSample {
    public static final String ZK_HOST = "localhost:2181";

    private static CuratorFramework getCuratorFramework() {
        ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(3000, 3);
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString(ZK_HOST)
                .connectionTimeoutMs(2000)
                .sessionTimeoutMs(2000)
                .retryPolicy(retryPolicy)
                .build();
        // 启动客户端
        client.start();

        System.out.println("zookeeper客户端获取成功...");
        return client;
    }

    public static void main(String[] args) {
        // 获取zk客户端
        CuratorFramework client = getCuratorFramework();

        // 创建分布式锁
        InterProcessMutex lock1 = new InterProcessMutex(client, "/test");
        InterProcessMutex lock2 = new InterProcessMutex(client, "/test");

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    lock1.acquire();
                    System.out.println("线程1 获取到锁");

                    lock1.acquire();
                    System.out.println("线程1 再次获取到锁");

                    Thread.sleep(5 * 1000);

                    lock1.release();
                    System.out.println("线程1 释放锁");

                    lock1.release();
                    System.out.println("线程1 再次释放锁");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    lock2.acquire();
                    System.out.println("线程2 获取到锁");

                    lock2.acquire();
                    System.out.println("线程2 再次获取到锁");

                    Thread.sleep(5 * 1000);

                    lock2.release();
                    System.out.println("线程2 释放锁");

                    lock2.release();
                    System.out.println("线程2 再次释放锁");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }
}
