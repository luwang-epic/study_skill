package com.wang.leetcode.offer;

public class SolutionOffer2_22 {

    class ListNode {
        int val;
        ListNode next;

        ListNode(int x) {
            val = x;
            next = null;
        }
    }

    public class Solution {
        public ListNode detectCycle(ListNode head) {
            if (null == head || null == head.next) {
                return null;
            }

            ListNode first = head;
            ListNode second = head;
            do {
                first = first.next;
                second = second.next.next;
            } while (first != second && null != second && null != second.next);

            if (null == second || null == second.next) {
                return null ;
            }
            second = head;
            while(second != first) {
                first = first.next;
                second = second.next;
            }
            return second;
        }
    }
}
