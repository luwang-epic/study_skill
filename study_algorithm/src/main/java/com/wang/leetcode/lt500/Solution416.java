package com.wang.leetcode.lt500;

/*
给你一个 只包含正整数 的 非空 数组 nums 。请你判断是否可以将这个数组分割成两个子集，使得两个子集的元素和相等。

 

示例 1：

输入：nums = [1,5,11,5]
输出：true
解释：数组可以分割成 [1, 5, 5] 和 [11] 。
示例 2：

输入：nums = [1,2,3,5]
输出：false
解释：数组不能分割成两个元素和相等的子集。

 */

public class Solution416 {
    public boolean canPartition(int[] nums) {
        if (null == nums || nums.length == 0) {
            return false;
        }

        int sum = 0;
        for (int i = 0; i < nums.length; i++) {
            sum += nums[i];
        }

        if (sum % 2 != 0) {
            return false;
        }

        int[][] dp = new int[nums.length + 1][sum / 2 + 1];
        dp[0][0] = 1;
        for (int i = 1; i <= nums.length; i++) {
            for (int j = 0; j <= sum/2; j++) {
                if (nums[i-1] > j) {
                    dp[i][j] = dp[i-1][j] > 0 ? 1 : 0;
                } else {
                    dp[i][j] = dp[i-1][j-nums[i-1]] + dp[i-1][j] > 0 ? 1 : 0;
                }
            }
        }

        return dp[nums.length][sum/2] > 0;
    }

    public static void main(String[] args) {
        Solution416 solution = new Solution416();
        int[] nums = new int[]{1,5,11,5};
        System.out.println(solution.canPartition(nums));

        nums = new int[]{1,2,3,5};
        System.out.println(solution.canPartition(nums));

        nums = new int[]{100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100};
        System.out.println(solution.canPartition(nums));
    }
}
