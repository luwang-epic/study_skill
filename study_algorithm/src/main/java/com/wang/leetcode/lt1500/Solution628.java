package com.wang.leetcode.lt1500;

import java.util.Comparator;
import java.util.PriorityQueue;

/*
给你一个整型数组 nums ，在数组中找出由三个数组成的最大乘积，并输出这个乘积。

 

示例 1：

输入：nums = [1,2,3]
输出：6
示例 2：

输入：nums = [1,2,3,4]
输出：24
示例 3：

输入：nums = [-1,-2,-3]
输出：-6
 */
public class Solution628 {

    public int maximumProduct(int[] nums) {
        if (null == nums || nums.length < 3) {
            return 0;
        }

        PriorityQueue<Integer> maxHeap = new PriorityQueue<>(3, Comparator.reverseOrder());
        PriorityQueue<Integer> minHeap = new PriorityQueue<>(3);
        int greaterZeroCount = 0;
        for (int i = 0; i < nums.length; i++) {
            if (nums[i] > 0) {
                greaterZeroCount++;
            }
            maxHeap.add(nums[i]);
            minHeap.add(nums[i]);
        }

        if (greaterZeroCount == 0) {
            // 3个最大的数相乘，负数最大数相乘结果最大
            return maxHeap.poll() * maxHeap.poll() * maxHeap.poll();
        } else if (greaterZeroCount == 1) {
            return maxHeap.poll() * minHeap.poll() * minHeap.poll();
        } else {
            // 选两个正数，加一个最大的负数（在maxHeap的最后一个元素中）和 一个正数，两个最小的数相乘 取大的
            // 因为两个最小的数都是负数的话，就得到正数了，这个情况和>3个正数是类似，这里放在一起就可以
            // 比较3个正数相乘 和 一个正数，两个负数相乘的结果 取大的
            int maxNum = maxHeap.poll();
            int positiveNumProduct = maxNum * maxHeap.poll() * maxHeap.poll();
            int twoNegativeNumProduct = maxNum * minHeap.poll() * minHeap.poll();
            return Math.max(positiveNumProduct, twoNegativeNumProduct);
        }

        // 实际上所有情况可以直接简化为比较positiveNumProduct和twoNegativeNumProduct的大小
        // 因为返回最大的乘积，上面3种情况，都是返回这两个结果中的一个，那么返回最大的就可以了
    }

    public static void main(String[] args) {
        Solution628 solution = new Solution628();
        int[] nums = new int[]{-8,-7,-2,10,20};
        System.out.println(solution.maximumProduct(nums));
    }
}
