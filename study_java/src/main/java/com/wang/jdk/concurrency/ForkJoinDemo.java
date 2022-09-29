package com.wang.jdk.concurrency;

/*
从JDK1.7开始，Java提供Fork/Join框架用于并行执行任务，它的思想就是讲一个大任务分割成若干小任务，
最终汇总每个小任务的结果得到这个大任务的结果。这种思想和MapReduce很像（input --> split --> map --> reduce --> output）
主要有两步：第一、任务切分；第二、结果合并
它的模型大致是这样的：线程池中的每个线程都有自己的工作队列
 (这一点和ThreadPoolExecutor不同，ThreadPoolExecutor是所有线程公用一个工作队列，所有线程都从这个工作队列中取任务)
当自己队列中的任务都完成以后，会从其它线程的工作队列中偷一个任务执行，这样可以充分利用资源。

为了减少窃取任务线程和被窃取任务线程之间的竞争，通常会使用双端队列，
被窃取任务线程永远从双端队列的头部拿任务执行，而窃取任务的线程永远从双端队列的尾部拿任务执行。
工作窃取算法的优点是充分利用线程进行并行计算，并减少了线程间的竞争，其缺点是在某些情况下还是存在竞争，
比如双端队列里只有一个任务时。并且消耗了更多的系统资源，比如创建多个线程和多个双端队列。


Fork/Join框架的核心类:
ForkJoinPool：它实现ExecutorService接口和工作窃取算法。管理工作线程和提供关于任务的状态和它们执行的信息。
    ForkJoinPool与其它的ExecutorService区别主要在于它使用“工作窃取”，
    线程池中的所有线程都企图找到并执行提交给线程池的任务。当大量的任务产生子任务的时候，
    或者同时当有许多小任务被提交到线程池中的时候，这种处理是非常高效的。
    特别的，当在构造方法中设置asyncMode为true的时候这种处理更加高效。

ForkJoinWorkerThread: ForkJoinWorkerThread代表ForkJoinPool线程池中的一个执行任务的线程。
    ForkJoinWorkThread持有ForkJoinPool和ForkJoinPool.WorkQueue的引用，以表明该线程属于哪个线程池，它的工作队列是哪个

ForkJoinTask： 它是将在ForkJoinPool中执行的任务的基类。它提供在任务中执行fork()和join()操作的机制，并且这两个方法控制任务的状态。
    RecursiveAction：没有返回结果的任务。
    RecursiveTask：有返回结果的任务。
    一般用法：
        if (当前任务工作量足够小)
            直接完成这个任务
        else
            将这个任务拆分成两个部分
            分别触发（invoke）两个子任务的执行，并等待结果


Fork/Join框架的使用场景：
    适用于通过分治算法来提高性能的场景。
    适用于执行耗时短的任务，也就是CPU密集型任务（计算型任务）


 */

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;

public class ForkJoinDemo {

    public static void main(String[] args) throws Exception {
        // 无返回结果的fork/join任务
        forkJoinWithoutReturnResult();

        // 有返回结果的fork/join任务
        forkJoinWithReturnResult();
    }

    private static void forkJoinWithoutReturnResult() throws Exception {
        List<String> tasks = new ArrayList<>();
        for (int i = 0; i < 123; i++) {
            tasks.add(String.valueOf(i + 1));
        }

        ForkJoinPool pool = new ForkJoinPool();
        ForkJoinTask<Void> future = pool.submit(new SendMsgTask(0, tasks.size(), tasks));
        future.get();
        pool.shutdown();
    }

    private static void forkJoinWithReturnResult() throws Exception {
        ForkJoinPool pool = new ForkJoinPool();
        Future<Integer> future = pool.submit(new FibonacciTask(10));
        System.out.println(future.get());
        pool.shutdown();
    }

    private static class FibonacciTask extends RecursiveTask<Integer> {
        final int n;

        public FibonacciTask(int n) {
            this.n = n;
        }

        @Override
        protected Integer compute() {
            if (n <= 1) {
                return n;
            } else {
                FibonacciTask f1 = new FibonacciTask(n - 1);
                f1.fork();
                FibonacciTask f2 = new FibonacciTask(n - 1);
                return f2.compute() + f1.join();
            }
        }
    }


    private static class SendMsgTask extends RecursiveAction {
        private final int THRESHOLD = 10;

        private int start;
        private int end;
        private List<String> tasks;

        public SendMsgTask(int start, int end, List<String> tasks) {
            this.start = start;
            this.end = end;
            this.tasks = tasks;
        }

        @Override
        protected void compute() {
            if ((end - start) <= THRESHOLD) {
                for (int i = start; i < end; i++) {
                    System.out.println(Thread.currentThread().getName() + ": 执行任务--->" + tasks.get(i));
                }
            } else {
                int middle = (start + end) / 2;
                invokeAll(new SendMsgTask(start, middle, tasks), new SendMsgTask(middle, end, tasks));
            }
        }
    }

}
