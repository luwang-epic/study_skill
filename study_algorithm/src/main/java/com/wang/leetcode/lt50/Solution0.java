package com.wang.leetcode.lt50;


import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**

 给定一个整数数组 nums 和一个整数目标值 target，请你在该数组中找出 和为目标值 target  的那 两个 整数，并返回它们的数组下标。

 你可以假设每种输入只会对应一个答案。但是，数组中同一个元素在答案里不能重复出现。

 你可以按任意顺序返回答案。

 */

class Solution0 {
    public int[] twoSum(int[] nums, int target) {
        for (int i = 0; i < nums.length - 1; i++) {
            for (int j = i + 1; j < nums.length; j++) {
                if (nums[i] + nums[j] == target) {
                    return new int[] {i, j};
                }
            }
        }
        return new int[0];
    }


    public static void main(String[] args) {
        Solution0 solution = new Solution0();
        int[] nums = new int[] {3, 3};
        int[] indexes = solution.twoSum(nums, 6);
        List<Integer> results = Arrays.stream(indexes).boxed().collect(Collectors.toList());
        System.out.println(results);

        nums = new int[] {3, 2, 4};
        indexes = solution.twoSum(nums, 6);
        System.out.println(Arrays.stream(indexes).boxed().collect(Collectors.toList()));
    }
}