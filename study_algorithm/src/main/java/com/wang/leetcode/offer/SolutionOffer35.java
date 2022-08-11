package com.wang.leetcode.offer;

import java.util.HashMap;
import java.util.Map;

/*

请实现 copyRandomList 函数，复制一个复杂链表。在复杂链表中，每个节点除了有一个 next 指针指向下一个节点，还有一个 random 指针指向链表中的任意节点或者 null。

 */
public class SolutionOffer35 {
    class Node {
        int val;
        Node next;
        Node random;

        public Node(int val) {
            this.val = val;
            this.next = null;
            this.random = null;
        }
    }


    Map<String, Node> locationMap = new HashMap<>();
    public Node copyRandomList(Node head) {
        if (null == head) {
            return null;
        }

        Node copy = new Node(head.val);
        locationMap.put(head.toString(), copy);
        // 处理next
        if (null != head.next) {
            if (locationMap.containsKey(head.next.toString())) {
                copy.next = locationMap.get(head.next.toString());
            } else {
                Node copyNext = copyRandomList(head.next);
                copy.next = copyNext;
            }
        }

        // 处理random
        if (null != head.random) {
            if (locationMap.containsKey(head.random.toString())) {
                copy.random = locationMap.get(head.random.toString());
            } else {
                Node copyRandom = copyRandomList(head.random);
                copy.random = copyRandom;
            }
        }
        return copy;
    }
}
