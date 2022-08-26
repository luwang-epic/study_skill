package com.wang.concurrency;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 *  Semaphore是一种基于计数的信号量。它可以设定一个阈值，基于此，多个线程竞争获取许可信号，
 *  做完自己的申请后归还，超过阈值后，线程申请许可信号将会被阻塞。
 *  Semaphore可以用来构建一些对象池，资源池之类的，比如数据库连接池，
 *  我们也可以创建计数为1的Semaphore，将其作为一种类似互斥锁的机制，这也叫二元信号量，表示两种互斥状态。
 *
 */
public class SemaphoreDemo {
	
	public static class MyThread extends Thread{
		private volatile Semaphore sem; // 信号量
		private int count; //申请信号量的大小
		
		public MyThread(Semaphore sem, int count) {
			this.sem = sem;
			this.count = count;
		}
		
		@Override
		public void run() {
			try {
				sem.acquire(count);
				Thread.sleep(200);
				System.out.println(Thread.currentThread().getName() + " acquire count="+count);
			} catch (Exception e) {
				e.printStackTrace();
			}finally {
				//释放给定数据的许可，将其返回到信号量
				sem.release(count);
				System.out.println(Thread.currentThread().getName() + " release " + count + "");
			}	
		}
	}
	
	private Semaphore semaphore;
	
	public SemaphoreDemo(int parallelism){
		semaphore = new Semaphore(parallelism);
	}
	
	
	// Semaphore的默认实现是非公平性
	public SemaphoreDemo(int parallelism,boolean fair){
		semaphore = new Semaphore(parallelism,fair);
	}
	
	
	public Semaphore getSemaphore(){
		return semaphore;
	}
	
	public static void main(String[] args) {
		
		SemaphoreDemo demo = new SemaphoreDemo(10);
		
		//创建线程池
		ExecutorService threadPool = Executors.newFixedThreadPool(3);
		
		//在线程中执行任务
		threadPool.execute(new MyThread(demo.getSemaphore(), 5));
		threadPool.execute(new MyThread(demo.getSemaphore(), 4));
		threadPool.execute(new MyThread(demo.getSemaphore(), 7));
		
		//关闭线程池
		threadPool.shutdown();
		
		
	}

}
