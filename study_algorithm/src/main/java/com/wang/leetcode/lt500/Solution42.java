package com.wang.leetcode.lt500;

/*
输入一个整型数组，数组中的一个或连续多个整数组成一个子数组。求所有子数组的和的最大值。

要求时间复杂度为O(n)。

 

示例1:

输入: nums = [-2,1,-3,4,-1,2,1,-5,4]
输出: 6
解释: 连续子数组 [4,-1,2,1] 的和最大，为 6。

 */
public class Solution42 {
    public int maxSubArray(int[] nums) {
        if (null == nums || nums.length == 0) {
            return 0;
        }

        int left = 0;
        int right = 0;

        int max = nums[0];
        int subMax = nums[0];
        for (int i = 1; i < nums.length; i++) {
            right = i;
            subMax = subMax + nums[i];
            if (max < subMax) {
                max = subMax;
            }

            if (subMax < 0) {
                while (left <= right) {
                    subMax = subMax - nums[left++];
                    if (left - 1 != right) {
                        max = Math.max(max, subMax);
                    }
                    if (subMax > 0) {
                        break;
                    }
                }
            }
        }

        while (left < right) {
            subMax = subMax - nums[left++];
            max = Math.max(max, subMax);
        }

        return max;
    }

    public int maxSubArray2(int[] nums) {
        if (null == nums || nums.length == 0) {
            return 0;
        }

        int[] dp = new int[nums.length];
        dp[0] = nums[0];
        for (int i = 1; i < nums.length; i++) {
            dp[i] = Math.max(dp[i - 1] + nums[i], nums[i]);
        }

        int max = dp[0];
        for (int i = 1; i < dp.length; i++) {
            max = Math.max(max, dp[i]);
        }
        return max;
    }

    public static void main(String[] args) {
        Solution42 solution = new Solution42();
        int[] nums = new int[] {-2,1,-3,4,-1,2,1,-5,4};
        System.out.println(solution.maxSubArray(nums));
        System.out.println(solution.maxSubArray2(nums));

        nums = new int[]{-2, -3,-1};
        System.out.println(solution.maxSubArray(nums));
        System.out.println(solution.maxSubArray2(nums));
    }
}
