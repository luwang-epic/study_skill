package com.wang.leetcode.lt500;

/**
 * 给你链表的头节点 head ，每 k 个节点一组进行翻转，请你返回修改后的链表。
 * <p>
 * k 是一个正整数，它的值小于或等于链表的长度。如果节点总数不是 k 的整数倍，那么请将最后剩余的节点保持原有顺序。
 * <p>
 * 你不能只是单纯的改变节点内部的值，而是需要实际进行节点交换。
 * <p>
 * <p>
 * 输入：head = [1,2,3,4,5], k = 2
 * 输出：[2,1,4,3,5]
 * <p>
 * 输入：head = [1,2,3,4,5], k = 3
 * 输出：[3,2,1,4,5]
*/

public class Solution25 {
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

        public static void println(ListNode root) {
            ListNode tempRoot = root;
            System.out.println("\n");
            while (null != tempRoot) {
                System.out.print(tempRoot.val + " -> ");
                tempRoot = tempRoot.next;
            }
        }
    }

    public ListNode reverseKGroup(ListNode head, int k) {
        ListNode preEnd = null;
        ListNode first = head;
        ListNode end = head;
        ListNode root = head;
        boolean isFirst = true;

        int count = 0;
        while(null != head) {
            if (count == 0) {
                first = head;
            }
            if (count == k - 1) {
                end = head;
            }
            head = head.next;
            count++;

            if (count == k) {
                if (isFirst) {
                    root = end;
                    isFirst = false;
                } else {
                    preEnd.next = end;
                }

                preEnd = first;
                ListNode pre = null;
                ListNode cur = null;
                while(first != head) {
                    cur = first;
                    first = first.next;
                    cur.next = pre;
                    pre = cur;
                }

                count = 0;
            }
        }

        if (count > 0 && !isFirst) {
            preEnd.next = first;
        }

        return root;
    }

    public static void main(String[] args) {
        Solution25 solution = new Solution25();
        ListNode listNode1 = new ListNode(1);
        ListNode listNode2 = new ListNode(2);
        listNode1.next = listNode2;
        ListNode listNode3 = new ListNode(3);
        listNode2.next = listNode3;
        ListNode listNode4 = new ListNode(4);
        listNode3.next = listNode4;
        ListNode listNode5 = new ListNode(5);
        listNode4.next = listNode5;
        int k = 1;
        ListNode reverseNode = solution.reverseKGroup(listNode1, k);
        ListNode.println(reverseNode);
    }
}
