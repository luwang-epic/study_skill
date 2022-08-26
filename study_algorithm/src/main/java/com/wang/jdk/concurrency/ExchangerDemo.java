package com.wang.concurrency;

import java.util.concurrent.Exchanger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Exchanger可以在两个线程之间交换数据，只能是2个线程，他不支持更多的线程之间互换数据。
 * 
 * 当线程A调用Exchange对象的exchange()方法后，他会陷入阻塞状态，直到线程B也调用了exchange()方法，
 * 然后以线程安全的方式交换数据，之后线程A和B继续运行
 * 
 */
public class ExchangerDemo {
	
	private static volatile boolean isDone = false;
	
	
	static class ExchangerProducer implements Runnable{
		
		private Exchanger<Integer> exchanger;
		private static int data = 1;  // 需要交换的数据
		
		public ExchangerProducer(Exchanger<Integer> exchanger) {
			this.exchanger = exchanger;
		}
		
		@Override
		public void run() {
			while(!Thread.interrupted() && !isDone){
				for(int i=1; i<=3; i++){
					try {
						TimeUnit.SECONDS.sleep(1);
						data = i;
						//交换之前的数据为多少
						System.out.println("producer before：" + data);
						
						//交换数据
						data = exchanger.exchange(data);
						//交换之后的数据为多少
						System.out.println("producer after: " + data);
						
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				
				//结束
				isDone = true;
			}
		}
	}
	
	
	static class ExchangerConsumer implements Runnable{

		private Exchanger<Integer> exchanger;
		private static int data = 0;  // 需要交换的数据
		
		public ExchangerConsumer(Exchanger<Integer> exchanger) {
			this.exchanger = exchanger;
		}
		
		@Override
		public void run() {
			while(!Thread.interrupted() && !isDone){
				data = 0;
				System.out.println("consumer before: " + data);
				
				try {
					TimeUnit.SECONDS.sleep(1);
					
					//交换数据
					data = exchanger.exchange(data);
					
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				//交换之后的数据
				System.out.println("consumer after: " + data);
				
			}
		}
		
	}
	
	
	
	public static void main(String[] args) {
		
		//交换类
		Exchanger<Integer> exchanger = new Exchanger<>();
		
		ExecutorService executor = Executors.newCachedThreadPool();
		
		ExchangerProducer producer = new ExchangerProducer(exchanger);
		ExchangerConsumer consumer = new ExchangerConsumer(exchanger);
		executor.execute(producer);
		executor.execute(consumer);
		
		//关闭
		executor.shutdown();
		
		
		try {
            executor.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
		
	}

}
