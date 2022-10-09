package com.wang.jvm.execute;

import java.util.concurrent.CountDownLatch;

/**
 * 演示jvm并不一定是顺序执行的
 */
public class ExecuteCodeOrder {
    private static int x = 0;
    private static int y = 0;
    private static int a = 0;
    private static int b = 0;

    public static void main(String[] args) throws Exception {
        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            x = 0;
            y = 0;
            a = 0;
            b = 0;
            CountDownLatch countDownLatch = new CountDownLatch(2);

            Thread one = new Thread(() -> {
               a = 1;
               x = b;

               countDownLatch.countDown();
            });

            Thread other = new Thread(() -> {
                b = 1;
                y = a;

                countDownLatch.countDown();
            });

            one.start();
            other.start();
            countDownLatch.await();

            // 如果程序执行按照代码的顺序，那么不可能出现 x=0 且 y=0的情况
            // 因此在单线程执行结果不变的情况下，JVM可能会乱序执行
            if (x == 0 && y ==0) {
                String result = String.format("第 %s 次执行结果为：x == %s, y == %s", i, x, y);
                System.out.println(result);
                break;
            }
        }
    }

}
