package com.wang.jdk.concurrency;

import java.util.PriorityQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

//通过Condition和PriorityQueue实现生产者、消费者模式：
public class ConsumerProducerPattern {

	private int queueSize = 10;
	private PriorityQueue<Integer> queue = new PriorityQueue<Integer>(queueSize);

	private Lock lock = new ReentrantLock();
	private Condition notFull = lock.newCondition();
	private Condition notEmpty = lock.newCondition();


	class Producer extends Thread{
		private volatile boolean flag = true;

		@Override
		public void run() {
			produce();
		}

		private void produce() {
			while(flag){
				lock.lock();
				try {
					while(queue.size() == queueSize){
						System.out.println("队列满，等待有空余空间");
						try {
							notFull.await();
						} catch (InterruptedException e) {
							flag = false;
							//e.printStackTrace();
						}
					}

					queue.offer(1); //每次插入一个元素
					notEmpty.signal();  //插入后队列不为空，发出一个信号
					System.out.println("向队列取中插入一个元素，队列剩余空间："+(queueSize-queue.size()));

				} finally {
					lock.unlock();
				}
			}
		}
	}

	class Consumer extends Thread{
		private volatile boolean flag = true;

		@Override
		public void run() {
			consume();
		}

		private void consume() {
			while(flag){
				lock.lock();
				try {

					while(queue.isEmpty()){
						System.out.println("队列空，等待数据");
						try {
							notEmpty.await();
						} catch (InterruptedException e) {
							flag = false;
							//e.printStackTrace();
						}
					}

					queue.poll();  //每次移走队首元素
					notFull.signal(); //消费一个后队列不为空，发出一个信号
					System.out.println("从队列取走一个元素，队列剩余"+queue.size()+"个元素");

				} finally {
					lock.unlock();
				}
			}
		}
	}


	public static void main(String[] args) throws Exception{

		ConsumerProducerPattern demo = new ConsumerProducerPattern();

		Producer producer = demo.new Producer();
		Consumer consumer = demo.new Consumer();

		producer.start();
		consumer.start();

		Thread.sleep(0);

		producer.interrupt();
		consumer.interrupt();
	}




}
