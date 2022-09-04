package com.wang.jdk.collection;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;

/**
 * 		Iterator和Enumeration区别
 * 1.接口内方法不同
 * 2.Iterator支持fail-fast机制，而Enumeration不支持。
 * 3.这两个是接口，性能上无比较意义。但是对于具体实现类来说，Enumeration 比 Iterator 的遍历速度更快。
 * 			为什么呢？这是因为，Hashtable中Iterator是通过Enumeration去实现的，
 * 			而且Iterator添加了对fail-fast机制的支持；所以，执行的操作自然要多一些。
 * 			（这个快和慢是不确定的，有的时候Iterator反而更快）
 * 4.Enumeration是先进后出，而Iterator是先进先出。    //下面验证这个
 *
 */
public class IteratorEnumerationDifference {


	public static void main(String[] args) {
		Hashtable<String, String> hashtable = new Hashtable<String, String>();
		HashMap<String,String> hashmap = new HashMap<String,String>();

		//放置到table中的顺序  1,2    Hashtable 实现了 Enumeration接口
		hashtable.put("1", "1");
		hashtable.put("2", "2");

		//放置到map中的顺序 1,2 HashMap 实现了Iterator接口
		hashmap.put("1", "1");
		hashmap.put("2", "2");


		System.out.println("Enumeration放入顺序为1,2 输入顺序如下：");
		Enumeration<String> enumeration = hashtable.elements();
		while (enumeration.hasMoreElements()) {
			String result = enumeration.nextElement();
			System.out.println(result);
		}


		System.out.println("Iterator放入顺序为1,2 输入顺序如下：");
		Iterator<Entry<String, String>> iterator  = hashmap.entrySet().iterator();
		while (iterator.hasNext()){
			Entry<String, String> entry = iterator.next();
			String value = entry.getValue();
			System.out.println(value);
		}



	}

}
