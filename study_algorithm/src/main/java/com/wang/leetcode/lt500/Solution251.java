package com.wang.leetcode.lt500;

/*
给定整数数组 nums 和整数 k，请返回数组中第 k 个最大的元素。

请注意，你需要找的是数组排序后的第 k 个最大的元素，而不是第 k 个不同的元素。

你必须设计并实现时间复杂度为 O(n) 的算法解决此问题。

 

示例 1:

输入: [3,2,1,5,6,4], k = 2
输出: 5
示例 2:

输入: [3,2,3,1,2,4,5,5,6], k = 4
输出: 4

 */

import java.util.Comparator;
import java.util.PriorityQueue;

public class Solution251 {
    public int findKthLargest(int[] nums, int k) {
        PriorityQueue<Integer> priorityQueue = new PriorityQueue<>(Comparator.naturalOrder());

        for (int i = 0; i < nums.length; i++) {
            priorityQueue.add(nums[i]);
            if (priorityQueue.size() > k) {
                priorityQueue.poll();
            }
        }

        return priorityQueue.poll();
    }

    public static void main(String[] args) {
        Solution251 solution = new Solution251();
        int[] nums = new int[] {3,2,1,5,6,4};
        int k = 2;
        System.out.println(solution.findKthLargest(nums, k));

        nums = new int[] {3,2,3,1,2,4,5,5,6};
        k = 4;
        System.out.println(solution.findKthLargest(nums, k));

    }
}
