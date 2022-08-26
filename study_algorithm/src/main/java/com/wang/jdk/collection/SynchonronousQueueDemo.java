package com.wang.jdk.collection;

import java.util.concurrent.SynchronousQueue;

/**
 * SynchronousQueue 也是一个队列来的，但它的特别之处在于它内部没有容器，
 * 一个生产线程，当它生产产品（即put的时候），如果当前没有人想要消费产品(即当前没有线程执行take)，
 * 此生产线程必须阻塞，等待一个消费线程调用take操作，take操作将会唤醒该生产线程，
 * 同时消费线程会获取生产线程的产品（即数据传递），这样的一个过程称为一次配对过程(当然也可以先take后put,原理是一样的)。
 *
 */
public class SynchonronousQueueDemo {

	public static void main(String[] args) throws InterruptedException{
		final SynchronousQueue<Integer> queue = new SynchronousQueue<>();

		Thread putThread = new Thread(new Runnable() {

			@Override
			public void run() {
				System.out.println("put thread start");
				try {
					queue.put(1);
				} catch (InterruptedException e) {
				}
				System.out.println("put thread end");
			}
		});

		Thread takeThread = new Thread(new Runnable() {

			@Override
			public void run() {
				System.out.println("take thread start");
				try {
					System.out.println("take from putThread: "+ queue.take());
				} catch (InterruptedException e) {

				}
				System.out.println("take thread end");
			}
		});


		putThread.start();
		Thread.sleep(1000);
		takeThread.start();

		/*
		 * 从结果可以看出，put线程执行queue.put(1) 后就被阻塞了，只有take线程进行了消费，put线程才可以返回。
		 * 可以认为这是一种线程与线程间一对一传递消息的模型。
		 */

	}

}
