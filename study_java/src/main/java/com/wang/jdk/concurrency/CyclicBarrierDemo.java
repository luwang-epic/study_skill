package com.wang.jdk.concurrency;

import java.util.Random;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 一个同步辅助类，它允许一组线程互相等待，直到到达某个公共屏障点 (common barrier point)。
 * 在涉及一组固定大小的线程的程序中，这些线程必须不时地互相等待，此时 CyclicBarrier 很有用。
 * 因为该 barrier 在释放等待线程后可以重用，所以称它为循环 的 barrier。
 *
 * CountDownLatch： 一个或者是一部分线程，等待另外一部线程都完成操作。
 * CyclicBarrier： 所有线程互相等待完成。
 *
 * 赛跑时，等待所有人都准备好时，才起跑：
 *
 */
public class CyclicBarrierDemo {


	static class Runner implements Runnable{

		// 一个同步辅助类，它允许一组线程互相等待，直到到达某个公共屏障点 (common barrier point)
		private CyclicBarrier barrier;
		private String name;

		public Runner(CyclicBarrier barrier, String name) {
			this.barrier = barrier;
			this.name = name;
		}

		@Override
		public void run() {
			try {
				Thread.sleep(1000 * (new Random()).nextInt(8));
				System.out.println(name + " 准备好了...");

				// barrier的await方法，在所有参与者都已经在此 barrier 上调用 await 方法之前，将一直等待。
				barrier.await();

			} catch (Exception e) {
				e.printStackTrace();
			}

			//当所有方法都到达公共屏障点时，开始执行后续事情   如：起跑
			System.out.println(name + " 起跑！");

		}

	}


	public static void main(String[] args) {

		CyclicBarrier barrier = new CyclicBarrier(3);

		ExecutorService executor = Executors.newFixedThreadPool(3);

		//提交任务，运动员到场
		executor.submit(new Runner(barrier, "1号选手"));
		executor.submit(new Runner(barrier, "2号选手"));
		executor.submit(new Runner(barrier, "3号选手"));

		//关闭
		executor.shutdown();

	}

}
