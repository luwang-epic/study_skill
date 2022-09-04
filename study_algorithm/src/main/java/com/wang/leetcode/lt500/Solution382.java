package com.wang.leetcode.lt500;

import java.util.Random;

/**
 * 给你一个单链表，随机选择链表的一个节点，并返回相应的节点值。每个节点 被选中的概率一样 。
 * <p>
 * 实现 Solution 类：
 * <p>
 * Solution(ListNode head) 使用整数数组初始化对象。
 * int getRandom() 从链表中随机选择一个节点并返回该节点的值。链表中所有节点被选中的概率相等。
 */
public class Solution382 {
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


    private int[] nums;
    private Random random;

    public Solution382(ListNode head) {
        int length = 1;
        ListNode node = head;
        while(null != node.next) {
            length++;
            node = node.next;
        }

        nums = new int[length];
        node = head;
        for (int i = 0; i < nums.length; i++) {
            nums[i] = node.val;
            node = node.next;
        }
        random = new Random();
    }

    public int getRandom() {
        return nums[random.nextInt(nums.length)];
    }

}
