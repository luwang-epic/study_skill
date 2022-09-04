package com.wang.jdk.collection;

import java.util.Random;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * PriorityBlockingQueue是一个没有边界的队列，它的排序规则和 java.util.PriorityQueue一样。
 * 需要注意，PriorityBlockingQueue中允许插入null对象。
 * 所有插入PriorityBlockingQueue的对象必须实现 java.lang.Comparable接口，
 * 队列优先级的排序规则就是按照我们对这个接口的实现来定义的。
 * 另外，我们可以从PriorityBlockingQueue获得一个迭代器Iterator，但这个迭代器并不保证按照优先级顺序进行迭代。
 *
 */
public class PriorityBlockingQueueDemo {

	public static class PriorityElement implements Comparable<PriorityElement>{

		private int priority;//定义优先级

		PriorityElement(int priority) {
			//初始化优先级
			this.priority = priority;
		}

		public int getPriority() {
			return priority;
		}
		public void setPriority(int priority) {
			this.priority = priority;
		}


		@Override
		public int compareTo(PriorityElement o) {
			//根据优先级排序
			return priority >= o.getPriority() ? 1 : -1;
		}

		@Override
		public String toString() {
			return "PriorityElement [priority=" + priority + "]";
		}

	}


	public static void main(String[] args) throws Exception{

		PriorityBlockingQueue<PriorityElement> blockingQueue =
				new PriorityBlockingQueue<PriorityElement>();

		for(int i=0; i<5; i++){
			Random random = new Random();
			PriorityElement element = new PriorityElement(random.nextInt(10));
			blockingQueue.put(element);
		}

		while(!blockingQueue.isEmpty()){
			System.out.println(blockingQueue.take());
		}
	}



}
