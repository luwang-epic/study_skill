package com.wang.jdk.collection;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Copy-On-Write简称COW，是一种用于程序设计中的优化策略。
 * 其基本思路是，从一开始大家都在共享同一个内容，当某个人想要修改这个内容的时候，
 * 才会真正把内容Copy出去形成一个新的内容然后再改，这是一种延时懒惰策略。
 * 从JDK1.5开始Java并发包里提供了两个使用CopyOnWrite机制实现的并发容器,
 * 它们是CopyOnWriteArrayList和CopyOnWriteArraySet。
 * CopyOnWrite容器非常有用，可以在非常多的并发场景中使用到。
 *
 * CopyOnWrite并发容器用于读多写少的并发场景。比如白名单，黑名单，商品类目的访问和更新场景
 *
 * CopyOnWrite容器只能保证数据的最终一致性，不能保证数据的实时一致性。
 * 所以如果你希望写入的的数据，马上能读到，请不要使用CopyOnWrite容器。
 *
 * CopyOnWriteArrayList中add方法的实现（向CopyOnWriteArrayList里添加元素），
 * 可以发现在添加的时候是需要加锁的，否则多线程写的时候会Copy出N个副本出来
 *
 * 读的时候不需要加锁，如果读的时候有多个线程正在向CopyOnWriteArrayList添加数据，读还是会读到旧的数据，
 * 因为写的时候不会锁住旧的CopyOnWriteArrayList。
 *
 */
public class CopyOnWriteArrayListDemo {

	public static void main(String[] args) throws Exception{

		List<String> list = new ArrayList<String>();
		list.add("a");
		list.add("b");
		list.add("c");

		final CopyOnWriteArrayList<String> cowList = new CopyOnWriteArrayList<>(list);

		Thread t = new Thread(new Runnable() {
			int count = -1;

			@Override
			public void run() {
				while(true){
					cowList.add(count++ + "");
				}
			}
		});

		t.setDaemon(true);
		t.start();

		Thread.sleep(3);

		//这段代码在for循环中遍历list的时候，同时会输出list的hashcode来看看list是不是同一个list了
		for (String s : cowList) {
			System.out.println(cowList.hashCode());
			System.out.println(s);
		}
	}

}
