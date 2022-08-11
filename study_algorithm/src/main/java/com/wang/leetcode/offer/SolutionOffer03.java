package com.wang.leetcode.offer;

import java.util.HashSet;
import java.util.Set;

/**

在一个长度为 n 的数组 nums 里的所有数字都在 0～n-1 的范围内。数组中某些数字是重复的，但不知道有几个数字重复了，也不知道每个数字重复了几次。请找出数组中任意一个重复的数字。

示例 1：
输入：
[2, 3, 1, 0, 2, 5, 3]
输出：2 或 3

 */


public class SolutionOffer03 {

    public int findRepeatNumber(int[] nums) {
        Set<Integer> notRepeatNums = new HashSet<>();
        for (int num : nums) {
            if (notRepeatNums.contains(num)) {
                return num;
            }
            notRepeatNums.add(num);
        }
        return 0;
    }

    public static void main(String[] args) {
        SolutionOffer03 solution = new SolutionOffer03();

        int[] nums = new int[] {2, 3, 1, 0, 2, 5, 3};
        int repeatNumber = solution.findRepeatNumber(nums);
        System.out.println(repeatNumber);
    }
}
