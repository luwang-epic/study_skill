package com.wang.leetcode.lt50;

/*
给你一个链表数组，每个链表都已经按升序排列。

请你将所有链表合并到一个升序链表中，返回合并后的链表。

 

示例 1：

输入：lists = [[1,4,5],[1,3,4],[2,6]]
输出：[1,1,2,3,4,4,5,6]
解释：链表数组如下：
[
  1->4->5,
  1->3->4,
  2->6
]
将它们合并到一个有序链表中得到。
1->1->2->3->4->4->5->6
示例 2：

输入：lists = []
输出：[]
示例 3：

输入：lists = [[]]
输出：[]
 */

public class Solution23 {

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

    public ListNode mergeKLists(ListNode[] lists) {
        if (null == lists || lists.length == 0) {
            return null;
        }

        ListNode root = new ListNode();
        ListNode cur = root;
        int min = Integer.MAX_VALUE;
        int index = 0;
        while (true) {
            min = Integer.MAX_VALUE;
            for (int i = 0; i < lists.length; i++) {
                if (null != lists[i] && lists[i].val < min) {
                    min = lists[i].val;
                    index = i;
                }
            }

            if (min == Integer.MAX_VALUE) {
                break;
            }
            cur.next = lists[index];
            cur = cur.next;
            lists[index] = lists[index].next;
        }

        return root.next;
    }
}
