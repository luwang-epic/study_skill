package com.wang.jdk.concurrency;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

/**
 * 一个同步辅助类，在完成一组正在其他线程中执行的操作之前，它允许一个或多个线程一直等待。
 * 用给定的计数 初始化 CountDownLatch。由于调用了 countDown() 方法，
 * 所以在当前计数到达零之前，await 方法会一直受阻塞。之后，会释放所有等待的线程，await 的所有后续调用都将立即返回。
 * 这种现象只出现一次——计数无法被重置。 一个线程(或者多个)， 等待另外N个线程完成某个事情之后才能执行
 *
 * CountDownLatch的计数器不能重复使用，CyclicBarrier的计数器是可以重置的，可以重复使用
 *
 * CountDownLatch这个类能够使一个线程等待其他线程完成各自的工作后再执行。
 * 例如，应用程序的主线程希望在负责启动框架服务的线程已经启动所有的框架服务之后再执行。
 *
 */
public class CountDownLatchDemo {

	final static SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	static class Worker extends Thread{
		String workerName;
		int workTime;
		CountDownLatch latch;

		public Worker(String workerName, int workTime, CountDownLatch latch) {
			this.workerName = workerName;
			this.workTime = workTime;
			this.latch = latch;
		}

		@Override
		public void run() {
			System.out.println("Worker "+workerName
					+" do work begin at "+sdf.format(new Date()));

			doWork(); // 开始工作

			System.out.println("Worker "+workerName
					+" do work complete at "+sdf.format(new Date()));

			latch.countDown(); //人完成工作，计数器减一

		}


		private void doWork(){
			try {
				Thread.sleep(workTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}


	public static void main(String[] args) throws Exception{

		CountDownLatch latch = new CountDownLatch(2); //两个工人的协作

		Worker work1 = new Worker("zhangsan", 5000, latch);
		Worker work2 = new Worker("lisi", 8000, latch);

		work1.start();
		work2.start();

		latch.await();//等待所有工人完成工作  完成以后才可以执行下面的语句

		System.out.println("all work done at "+sdf.format(new Date()));

	}


}
