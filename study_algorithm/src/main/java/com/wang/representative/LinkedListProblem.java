package com.wang.representative;

import org.junit.jupiter.api.Test;

/**
 * 链表相关问题
 */
public class LinkedListProblem {

    /*
   判断一个单链表是否有环，返回成环的第一个节点，否则返回null
    */
    @Test
    public void hasRingDemo() {
        Node head = new Node(1);
        Node node2 = new Node(2);
        head.next = node2;
        Node node3 = new Node(3);
        node2.next = node3;
        Node node4 = new Node(4);
        node3.next = node4;
        node4.next = node2;
        System.out.println(hasRing(head).value);
    }

    /*
    给定两个可能有环的单链表，都节点为head1和head2，返回相交的第一个节点，如果不相交，返回null
        1. 如果没有环，两个节点的后面部分一定是一样的
        2. 有一个有环，另一个没有环，不可能相交
        3. 如果都有环，相交的话；有3种情况，
            a. 不相交
            b. 相交，入环节点相同
            c. 相交，入环节点不相同
    */
    @Test
    public void intersectionNodeDemo() {
        Node head1 = new Node(1);
        Node node2 = new Node(2);
        head1.next = node2;
        Node node3 = new Node(3);
        node2.next = node3;
        Node node4 = new Node(4);
        node3.next = node4;

        Node head2 = new Node(5);
        head2.next = node4;
        // 无环相交
        System.out.println(intersectionNode(head1, head2).value);

        // 有环相交
        Node node6 = new Node(6);
        node4.next = node6;
        node6.next = node4;
        System.out.println(intersectionNode(head1, head2).value);

        // 有环不相交
        Node head3 = new Node(7);
        Node node8 = new Node(8);
        head3.next = node8;
        Node node9 = new Node(9);
        node8.next = node9;
        node9.next = node8;
        System.out.println(intersectionNode(head1, head3));
    }


    public Node hasRing(Node head) {
        if (null == head || null == head.next || null == head.next.next) {
            return null;
        }

        Node slow = head.next;
        Node fast = head.next.next;
        while (slow != fast) {
            // 指向null，肯定没有环
            if (null == fast.next || null == fast.next.next) {
                return null;
            }
            slow = slow.next;
            fast = fast.next.next;
        }
        // 相等说明有环， 找到成环的节点

        // 从头开始找，
        fast = head;
        // 从交点开始找，相遇就是成环的第一个节点
        while (slow != fast) {
            fast = fast.next;
            slow = slow.next;
        }
        return slow;
    }

    public Node intersectionNode(Node head1, Node head2) {
        Node node1 = hasRing(head1);
        Node node2 = hasRing(head2);
        // 第一个场景
        if (null == node1 && null == node2) {
            return intersectionNodeWithNoRing(head1, head2);
        }

        // 第二个场景
        if (null != node1 && null != node2) {
            return intersectionNodeWithRing(head1, node1, head2, node2);
        }

        // 第三个场景，返回null
        return null;
    }

    /**
     * 无环场景
     * @param head1 第一个链表
     * @param head2 第二个链表
     * @return 返回是否相交 null：不相交  not null：相交
     */
    public Node intersectionNodeWithNoRing(Node head1, Node head2) {
        if (null == head1 || null == head2) {
            return null;
        }

        Node cur1 = head1;
        Node cur2 = head2;
        // 记录两个链表长度的差值
        int diff = 0;
        // 找到cur1的尾结点
        while (null != cur1.next) {
            diff++;
            cur1 = cur1.next;
        }
        // 找到cur2的尾结点
        while (null != cur2.next) {
            diff--;
            cur2 = cur2.next;
        }

        // 尾节点不相等，说明不相交
        if (cur1 != cur2) {
            return null;
        }

        // cur1代表更长的链表
        cur1 = diff > 0 ? head1 : head2;
        // cur2代表更短的链表
        cur2 = cur1 == head1 ? head2 : head1;
        // 场的链表先走n步
        diff = Math.abs(diff);
        while (diff > 0) {
            cur1 = cur1.next;
            diff--;
        }

        // 一直走，直到相交的地方
        while (cur1 != cur2) {
            cur1 = cur1.next;
            cur2 = cur2.next;
        }
        return cur1;
    }

    /**
     * 都有环场景
     * @param head1 第一个链表
     * @param ring1 第一个链表入环节点
     * @param head2 第二个链表
     * @param ring2 第二个链表入环节点
     * @return 返回是否相交 null：不相交  not null：相交
     */
    public Node intersectionNodeWithRing(Node head1, Node ring1, Node head2, Node ring2) {
        // 入环节点相同，肯定相交，这个找相交的节点和无环情况类似
        if (ring1 == ring2) {
            Node cur1 = head1;
            Node cur2 = head2;
            // 记录两个链表长度的差值
            int diff = 0;
            // 找到cur1的尾结点
            while ( cur1.next != ring1) {
                diff++;
                cur1 = cur1.next;
            }
            // 找到cur2的尾结点
            while (cur2.next != ring2) {
                diff--;
                cur2 = cur2.next;
            }
            // cur1代表更长的链表
            cur1 = diff > 0 ? head1 : head2;
            // cur2代表更短的链表
            cur2 = cur1 == head1 ? head2 : head1;
            // 场的链表先走n步
            diff = Math.abs(diff);
            while (diff > 0) {
                cur1 = cur1.next;
                diff--;
            }
            // 一直走，直到相交的地方
            while (cur1 != cur2) {
                cur1 = cur1.next;
                cur2 = cur2.next;
            }
            return cur1;
        }

        // 如果不相等，有两种情况


        Node cur1 = ring1.next;
        while (cur1 != ring1) {
            // 如果第二个链表的入环节点在第一个链表的环中，那么相交，返回环中的一个节点，比如：ring1
            if (cur1 == ring2) {
                return ring1;
            }
            cur1 = cur1.next;
        }

        // 如果链表1遍历了一遍环，还没有和第二个链表的环入口节点相等，
        // 说明入口节点不在环中，那么这两个链表不相交
        return null;
    }


    private static class Node {
        public int value;
        public Node next;

        public Node(int value) {
            this.value = value;
        }
    }
}
