package com.wang.leetcode.offer;

public class SolutionOffer24 {

    private class ListNode {
        int val;
        ListNode next;

        ListNode(int x) {
            val = x;
        }
    }

    public ListNode reverseList(ListNode head) {
        ListNode cur = head, pre = null;
        while(cur != null) {
            ListNode tmp = cur.next; // 暂存后继节点 cur.next
            cur.next = pre;          // 修改 next 引用指向
            pre = cur;               // pre 暂存 cur
            cur = tmp;               // cur 访问下一节点
        }
        return pre;
    }

//    public ListNode reverseList(ListNode head) {
//        if (null == head || null == head.next) {
//            return head;
//        }
//
//        ListNode next = head.next;
//        ListNode root = head;
//        root.next = null;
//        while (null != next) {
//            ListNode temp = next.next;
//            next.next = root;
//            root = next;
//            next = temp;
//        }
//        return root;
//    }
}
