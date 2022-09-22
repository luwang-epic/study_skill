package com.wang.leetcode.lt500;

import java.util.HashMap;
import java.util.Map;

/*
给定一个单链表 L 的头节点 head ，单链表 L 表示为：

L0 → L1 → … → Ln - 1 → Ln
请将其重新排列后变为：

L0 → Ln → L1 → Ln - 1 → L2 → Ln - 2 → …
不能只是单纯的改变节点内部的值，而是需要实际的进行节点交换。

 */
public class Solution143 {

    private static class ListNode {
        int val;
        ListNode next;

        ListNode() {
        }

        ListNode(int val) {
            this.val = val;
        }

        ListNode(int val, ListNode next) {
            this.val = val;
            this.next = next;
        }
    }

    public void reorderList(ListNode head) {
        if (null == head || null == head.next) {
            return;
        }

        Map<Integer, ListNode> indexNodeMap = new HashMap<>();
        ListNode node = head;
        int index = 0;
        while (null != node) {
            indexNodeMap.put(index++, node);
            node = node.next;
        }

        int length = index;
        node = head;
        index = 0;
        ListNode preNode = null;
        ListNode postNode = null;
        while (index < length / 2) {
            preNode = node;
            node = node.next;
            postNode = indexNodeMap.get(length - 1 - index);

            preNode.next = postNode;
            postNode.next = node;
            index++;
        }
        node.next = null;
    }

    public static void main(String[] args) {
        Solution143 solution = new Solution143();
        ListNode node4 = new ListNode(4);
        ListNode node3 = new ListNode(3, node4);
        ListNode node2 = new ListNode(2, node3);
        ListNode node1 = new ListNode(1, node2);
        solution.reorderList(node1);

        ListNode node = node1;
        while (null != node) {
            System.out.print(node.val + " -> ");
            node = node.next;
        }
        System.out.println();
    }
}
