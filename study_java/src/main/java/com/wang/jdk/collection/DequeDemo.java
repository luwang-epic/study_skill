package com.wang.jdk.collection;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * 双向队列(Deque),是Queue的一个子接口，双向队列是指该队列两端的元素既能入队(offer)也能出队(poll),
 * 如果将Deque限制为只能从一端入队和出队，则可实现栈的数据结构。
 * 对于栈而言，有入栈(push)和出栈(pop)，遵循先进后出原则
 *
 * Java里有一个叫做Stack的类，却没有叫做Queue的类（它是个接口名字）。
 * 当需要使用栈时，Java已不推荐使用Stack，而是推荐使用更高效的ArrayDeque；
 * 既然Queue只是一个接口，当需要使用队列时也就首选ArrayDeque了（次选是LinkedList）。
 *
 */
public class DequeDemo {

    public static void main(String[] args) {
        //Deque<String> deque = new LinkedList<String>();
        Deque<String> deque = new ArrayDeque<>();

        deque.push("a");
        deque.push("b");
        deque.push("c");
        System.out.println(deque);

        //获取栈首元素后，元素不会出栈
        String str = deque.peek();
        System.out.println(str);
        System.out.println(deque);

        while(deque.size() > 0) {
            //获取栈首元素后，元素将会出栈
            System.out.println(deque.pop());
        }
        System.out.println(deque);
    }


}
