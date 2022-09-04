package com.wang.leetcode.lt500;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**

给你两个 非空 的链表，表示两个非负的整数。它们每位数字都是按照 逆序 的方式存储的，并且每个节点只能存储 一位 数字。

请你将两个数相加，并以相同形式返回一个表示和的链表。

你可以假设除了数字 0 之外，这两个数都不会以 0 开头。

 */

public class Solution2 {


      private static class ListNode {
          int val;
          ListNode next;
          ListNode() {}
          ListNode(int val) { this.val = val; }
          ListNode(int val, ListNode next) { this.val = val; this.next = next; }
     }


    public ListNode addTwoNumbers(ListNode l1, ListNode l2) {
        ListNode root = new ListNode();
        ListNode current = new ListNode();
        int posVal = 0;
        int v1 = 0, v2 = 0;
        boolean isFirst = true;
        ListNode t1 = l1;
        ListNode t2 = l2;
        while(true) {
            if (Objects.isNull(t1) && Objects.isNull(t2)) {
                break;
            }
            v1 = Objects.nonNull(t1) ? t1.val : 0;
            v2 = Objects.nonNull(t2) ? t2.val : 0;

            int total = v1 + v2 + posVal;
            if (total >= 10) {
              posVal = 1;
              total = total - 10 ;
            } else {
              posVal = 0;
            }
            ListNode next = new ListNode(total);
            if (isFirst) {
                current = next;
                root = current;
                isFirst = false;
            } else {
                current.next = next;
                current = next;
            }
            t1 = Objects.nonNull(t1) ? t1.next : null;
            t2 = Objects.nonNull(t2) ? t2.next : null;
        }

        if (posVal == 1) {
            current.next = new ListNode(1);
        }

        return root;
    }

    public static void main(String[] args) {
        Solution2 solution = new Solution2();
        ListNode l11 = new ListNode(2);
        ListNode l12 = new ListNode(4);
        ListNode l13 = new ListNode(3);
        l11.next = l12;
        l12.next = l13;
        ListNode l21 = new ListNode(5);
        ListNode l22 = new ListNode(6);
//        ListNode l23 = new ListNode(4);
        l21.next = l22;
//        l22.next = l23;

        ListNode result = solution.addTwoNumbers(l11, l21);
        List<Integer> nums = new ArrayList<>();
        ListNode current = result;
        do {
            nums.add(current.val);
            current = current.next;
        } while (Objects.nonNull(current));
        System.out.println(nums);
    }
}