package com.wang.leetcode.offer;

public class SolutionOffer24 {

    public class ListNode {
        int val;
        ListNode next;

        ListNode(int x) {
            val = x;
        }
    }

    public ListNode reverseList(ListNode head) {
        if (null == head || null == head.next) {
            return head;
        }

        ListNode next = head.next;
        ListNode root = head;
        root.next = null;
        while (null != next) {
            ListNode temp = next.next;
            next.next = root;
            root = next;
            next = temp;
        }
        return root;
    }
}
