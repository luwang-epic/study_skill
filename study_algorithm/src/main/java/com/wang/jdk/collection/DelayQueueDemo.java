package com.wang.jdk.collection;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * DelayQueue是一个BlockingQueue，其特化的参数是Delayed。
 * Delayed扩展了Comparable接口，比较的基准为延时的时间值，Delayed接口的实现类getDelay的返回值应为固定值（final）。
 * DelayQueue内部是使用PriorityQueue实现的。
 *
 * DelayQueue = BlockingQueue + PriorityQueue + Delayed
 *
 * DelayQueue的关键元素BlockingQueue、PriorityQueue、Delayed。
 * 可以这么说，DelayQueue是一个使用优先队列（PriorityQueue）实现的BlockingQueue，优先队列的比较基准值是时间。
 *
 *
 *
 * 场景：
 * a) 关闭空闲连接。服务器中，有很多客户端的连接，空闲一段时间之后需要关闭之。
 * b) 缓存。缓存中的对象，超过了空闲时间，需要从缓存中移出。
 * c) 任务超时处理。在网络协议滑动窗口请求应答式交互时，处理超时未响应的请求。
 *
 * 一种笨笨的办法就是，使用一个后台线程，遍历所有对象，挨个检查。这种笨笨的办法简单好用，但是对象数量过多时，
 * 可能存在性能问题，检查间隔时间不好设置，间隔时间过大，影响精确度，多小则存在效率问题。而且做不到按超时的时间顺序处理。
 *
 * 这场景，使用DelayQueue最适合了。
 *
 */
public class DelayQueueDemo {

	//以下是Sample，是一个缓存的简单实现。共包括三个类Pair、DelayItem、Cache。如下：


	static class Pair<K,V>{

		public K first;

		public V second;

		public Pair(){};

		public Pair(K first, V second){
			this.first = first;
			this.second = second;
		}

	}

	//DelayQueue中的元素     必须实现Delayed接口
	static class DelayItem<T> implements Delayed{

		//Base of nanosecond timings, to avoid wrapping
		private static final long NANO_ORIGIN = System.nanoTime();

		// Sequence number to break scheduling ties, and in turn to guarantee FIFO order among tiedentries.
		private static final AtomicLong sequencer = new AtomicLong(0);

		//Sequence number to break ties FIFO
		private final long sequenceNumber;

		//The time the task is enabled to execute in nanoTime units
		private final long time;

		private final T item;


		public DelayItem(T submit, long timeout) {
			this.time = now() + timeout;
			this.item = submit;
			this.sequenceNumber = sequencer.getAndIncrement();
		}


		//Returns nanosecond time offset by origin
		final static long now(){
			return System.nanoTime() - NANO_ORIGIN;
		}


		public T getItem() {
			return this.item;
		}


		@Override
		public int compareTo(Delayed o) {
			if(o == this){
				return 0;
			}
			if(o instanceof DelayItem){
				DelayItem<?> x = (DelayItem<?>) o;
				long diff = time - x.time;
				if(diff <0)
					return -1;
				else if(diff>0)
					return 1;
				else if(sequenceNumber < x.sequenceNumber)
					return -1;
				else
					return 1;
			}

			long d = (getDelay(TimeUnit.NANOSECONDS) - o.getDelay(TimeUnit.NANOSECONDS));
			return (d == 0) ? 0 : ((d < 0) ? -1 : 1);
		}

		@Override
		public long getDelay(TimeUnit unit) {
			long d = unit.convert(time-now(), TimeUnit.NANOSECONDS);
			return d;
		}

	}



	static class Cache<K,V>{

		private ConcurrentMap<K, V> cacheMap = new ConcurrentHashMap<>();

		private DelayQueue<DelayItem<Pair<K, V>>> queue = new DelayQueue<>();

		private Thread daemonThread;

		public Cache() {

			Runnable daemonTask = new Runnable() {
				public void run() {
					daemonCheck();
				}

			};

			daemonThread = new Thread(daemonTask);
			daemonThread.setDaemon(true);
			daemonThread.setName("Cache Daemon");
			daemonThread.start();
		}

		private void daemonCheck() {
			System.out.println("cache service started.");

			for(;;){
				try {
					DelayItem<Pair<K,V>> delayItem = queue.take();
					if(delayItem !=null){
						//超时对象处理
						Pair<K,V> pair = delayItem.getItem();
						cacheMap.remove(pair.first, pair.second); // compare and remove
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
					break;
				}
			}

			System.out.println("cache service stopped.");
		}


		// 添加对象到缓存中
		public void put(K key, V value, long time, TimeUnit unit){
			V oldValue = cacheMap.put(key, value);

			/* 下面的if语句没有必要的    原因如下：
			 *  即使cacheMap中原来的元素已经别改变，
			 *  但是当queue中该元素过期时，执行的cacheMap.remove(pair.first, pair.second)操作
			 *  由于找不到元素而不执行任何操作，故没有必要从queue中移除旧的元素
			 *
			 *  注意：下面的移除语句有问题，我们无法确定该Pair（key value）对应的DelayItem是哪一个。
			 */
			if(oldValue != null){
				//		queue.remove(key);  //？？ 这里貌似有问题   应该移除的是DelayItem元素才对啊
			}

			long nanoTime = TimeUnit.NANOSECONDS.convert(time, unit);
			queue.put(new DelayItem<Pair<K,V>>(new Pair<K,V>(key, value), nanoTime));

		}


		public V get(K key){
			return cacheMap.get(key);
		}

	}



	//运行Sample，main函数执行的结果是输出两行，第一行为aaa，第二行为null。
	public static void main(String[] args) throws Exception{
		Cache<Integer, String> cache = new Cache<Integer, String>();
		cache.put(1, "aaaa", 3, TimeUnit.SECONDS);

		Thread.sleep(1000 * 2);

		{
			String str = cache.get(1);
			System.out.println(str);
		}

		Thread.sleep(1000 * 2);

		{
			String str = cache.get(1);
			System.out.println(str);
		}
	}




}
