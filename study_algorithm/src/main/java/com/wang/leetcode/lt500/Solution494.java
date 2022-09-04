package com.wang.leetcode.lt500;

/*
给你一个整数数组 nums 和一个整数 target 。

向数组中的每个整数前添加 '+' 或 '-' ，然后串联起所有整数，可以构造一个 表达式 ：

例如，nums = [2, 1] ，可以在 2 之前添加 '+' ，在 1 之前添加 '-' ，然后串联起来得到表达式 "+2-1" 。
返回可以通过上述方法构造的、运算结果等于 target 的不同 表达式 的数目。

 */

public class Solution494 {
    public int findTargetSumWays(int[] nums, int target) {
        if (null == nums || nums.length == 0) {
            return 0;
        }

        int[] totals = new int[(int)Math.pow(2, nums.length)];
        int index = 1;
        for (int i = 0; i < nums.length; i++) {
            for (int j = 0; j < index; j++) {
                totals[index + j] = totals[j] - nums[i];
            }
            for (int j = 0; j < index; j++) {
                totals[j] = totals[j] + nums[i];
            }
            index = 2 * index;
        }

        int result = 0;
        for (int i = 0; i < totals.length; i++) {
            if (totals[i] == target) {
                result++;
            }
        }
        return result;
    }

    public int findTargetSumWays2(int[] nums, int target) {
        int sum = 0;
        for (int num : nums) {
            sum += num;
        }
        int diff = sum - target;
        if (diff < 0 || diff % 2 != 0) {
            return 0;
        }

        int[][] dp = new int[nums.length + 1][diff / 2 + 1];
        dp[0][0] = 1;
        for (int i = 1; i <= nums.length; i++) {
            for (int j = 0; j <= diff/2; j++) {
                if (nums[i-1] > j) {
                    dp[i][j] = dp[i-1][j];
                } else {
                    dp[i][j] = dp[i-1][j] + dp[i-1][j-nums[i-1]];
                }
            }
        }
        return dp[nums.length][diff/2];
    }

    public static void main(String[] args) {
        Solution494 solution = new Solution494();
        int target = 3;
        int[] nums = new int[] {1,1,1,1,1};
        System.out.println(solution.findTargetSumWays2(nums, target));
    }
}
