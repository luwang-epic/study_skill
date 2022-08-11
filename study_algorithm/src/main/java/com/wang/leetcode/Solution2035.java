package com.wang.leetcode;

/*
给你一个长度为 2 * n 的整数数组。你需要将 nums 分成 两个 长度为 n 的数组，分别求出两个数组的和，并 最小化 两个数组和之 差的绝对值 。nums 中每个元素都需要放入两个数组之一。

请你返回 最小 的数组和之差。

 */
public class Solution2035 {
    public int minimumDifference(int[] nums) {
        float avg = 0;
        for (int num : nums) {
            avg += num;
        }
        avg = avg / nums.length;

        return 0;
    }
}
