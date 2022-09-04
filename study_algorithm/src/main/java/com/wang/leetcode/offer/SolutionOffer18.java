package com.wang.leetcode.offer;

public class SolutionOffer18 {

    private class ListNode {
        int val;
        ListNode next;

        ListNode(int x) {
            val = x;
        }
    }


    public ListNode deleteNode(ListNode head, int val) {
        if (null == head) {
            return null;
        }
        if (head.val == val) {
            return head.next;
        }

        ListNode next = head;
        ListNode pre = head;
        while (null != next) {
            if (next.val == val) {
                pre.next = next.next;
                break;
            }

            pre = next;
            next = next.next;
        }
        return head;
    }

}
