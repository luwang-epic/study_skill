package com.wang.jdk.concurrency;

/*
Future接口可以构建异步应用，但依然有其局限性。它很难直接表述多个Future 结果之间的依赖性。
实际开发中，我们经常需要达成以下目的：
    将两个异步计算合并为一个——这两个异步计算之间相互独立，同时第二个又依赖于第一个的结果。
    等待 Future 集合中的所有任务都完成。
    仅等待 Future集合中最快结束的任务完成（有可能因为它们试图通过不同的方式计算同一个值），并返回它的结果。
    通过编程方式完成一个Future任务的执行（即以手工设定异步操作结果的方式）。
    应对 Future 的完成事件（即当 Future 的完成事件发生时会收到通知，并能使用 Future 计算的结果进行下一步的操作，不只是简单地阻塞等待操作的结果）
新的CompletableFuture类将使得这些成为可能。

CompletableFuture类实现了CompletionStage 接口，CompletionStage接口实际上提供了同步或异步运行计算的舞台，
CompletionStage代表某个同步或异步计算的一个阶段。你可以把它理解为是一个为了产生有价值最终结果的计算的流水线上的一个单元。
这意味着多个CompletionStage指令可以链接起来从而一个阶段的完成可以触发下一个阶段的执行。

笔者的理解CompletableFuture相当于一个Task编排工具。
CompletableFuture#completedFuture、CompletableFuture#whenComplete这些方法都是对某一个阶段Task计算完成然后进行下一步的动作。
将下一个一个Task和前一个Task进行编排。CompletableFuture#handle将Task串连起来。这些动作其实就是Task编排。

CompletableFuture.xxx()：表示该方法将继续在当前执行CompletableFuture的方法线程中执行
CompletableFuture.xxxAsync()：表示异步，在线程池中执行。
    这些线程都是Daemon线程，主线程结束Daemon线程不结束，只有JVM关闭时，生命周期终止。


 */

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class CompletableFutureDemo {

    public static void main(String[] args) throws Exception {
        // 同步运行
        syncCompletableFuture();

        // 异步运行
        asyncCompletableFuture();
    }


    /**
     * 提供了函数式编程的能力，可以通过回调的方式处理计算结果，
     * 并且提供了转换和组合CompletableFuture的方法。
     */
    private static void syncCompletableFuture() throws Exception {
        CompletableFuture<String> completableFutureOne = new CompletableFuture<>();
        completableFutureOne.thenRun(() -> {
            System.out.println("completableFutureOne threadName: " +Thread.currentThread().getName());
        });

        // WhenComplete 方法返回的 CompletableFuture 仍然是原来的 CompletableFuture 计算结果(类型都是String).
        CompletableFuture<String> completableFutureTwo = completableFutureOne.whenComplete((s, t) -> {
            System.out.println("completableFutureTwo threadName: " +Thread.currentThread().getName());
            System.out.println("params---> " + s + " ----- " + t);
        });

        // ThenApply 方法返回的是一个新的 completeFuture（类型不再一致）
        CompletableFuture<Integer> completableFutureThree = completableFutureTwo.thenApply(s -> {
            System.out.println("completableFutureThree threadName: " + Thread.currentThread().getName());
            System.out.println("params---> " + s);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return s.length();
        });

        // 整个程序开始执行
        completableFutureOne.complete("start tasks");
        System.out.println("completableFutureOne result: " + completableFutureOne.get());
        System.out.println("completableFutureTwo result: " + completableFutureTwo.get());
        System.out.println("completableFutureThree result: " + completableFutureThree.get());
    }

    private static void asyncCompletableFuture() throws Exception {
        List<String> names = Arrays.asList("a", "b", "c");
        List<CompletableFuture<String>> completableFutures = names.stream().map(name -> getCompletableFuture(name)).collect(Collectors.toList());
        // 使用allOf方法封装所有的并行任务
        CompletableFuture<Void> allOfCompletableFutures = CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[completableFutures.size()]));
        // 获得所有子任务的处理结果
        CompletableFuture<List<String>> thenApplyCompletableFuture = allOfCompletableFutures.thenApply(v -> completableFutures.stream().map(future -> future.join()).collect(Collectors.toList()));

        // 开始异步执行
        List<String> strings = thenApplyCompletableFuture.get();
        System.out.println("当前线程: " + Thread.currentThread().getName());
        System.out.println("thenApplyCompletableFuture result: " + strings);
    }

    private static CompletableFuture<String> getCompletableFuture(String name){
        return CompletableFuture.supplyAsync(() -> {
            System.out.println(name + "threadName: " + Thread.currentThread().getName());
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return name;
        });
    }
}
