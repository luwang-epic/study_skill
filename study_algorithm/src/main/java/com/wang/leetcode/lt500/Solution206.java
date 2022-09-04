package com.wang.leetcode.lt500;

/*
给你单链表的头节点 head ，请你反转链表，并返回反转后的链表。
 */
public class Solution206 {
    private class ListNode {
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

    public ListNode reverseList(ListNode head) {
        ListNode pre = null;
        ListNode cur = null;
        while (null != head) {
            cur = head;
            head = head.next;
            cur.next = pre;
            pre = cur;
        }
        return cur;
    }
}
