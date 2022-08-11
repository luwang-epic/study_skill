package com.wang.leetcode;

/*
你是一个专业的小偷，计划偷窃沿街的房屋。每间房内都藏有一定的现金，影响你偷窃的唯一制约因素就是相邻的房屋装有相互连通的防盗系统，如果两间相邻的房屋在同一晚上被小偷闯入，系统会自动报警。

给定一个代表每个房屋存放金额的非负整数数组，计算你 不触动警报装置的情况下 ，一夜之内能够偷窃到的最高金额。

 

示例 1：

输入：[1,2,3,1]
输出：4
解释：偷窃 1 号房屋 (金额 = 1) ，然后偷窃 3 号房屋 (金额 = 3)。
     偷窃到的最高金额 = 1 + 3 = 4 。
示例 2：

输入：[2,7,9,3,1]
输出：12
解释：偷窃 1 号房屋 (金额 = 2), 偷窃 3 号房屋 (金额 = 9)，接着偷窃 5 号房屋 (金额 = 1)。
     偷窃到的最高金额 = 2 + 9 + 1 = 12 。

 */

public class Solution198 {
    public int rob(int[] nums) {
        if (null == nums || nums.length == 0) {
            return 0;
        }

        if (nums.length == 1) {
            return nums[0];
        }
        int[] dp = new int[nums.length];
        dp[0] = nums[0];
        dp[1] = Math.max(nums[0], nums[1]);
        for (int i = 2; i < nums.length; i++) {
            dp[i] = Math.max(nums[i] + dp[i-2], dp[i-1]);
        }
        return dp[nums.length - 1];

//        return maxAccount(nums, 0);
    }

    public int maxAccount(int[] nums, int start) {
        if (start >= nums.length) {
            return 0;
        }
        if (start == nums.length - 1) {
            return nums[start];
        }

        return Math.max(nums[start] + maxAccount(nums, start + 2), nums[start + 1] + maxAccount(nums, start + 3));
    }

    public static void main(String[] args) {
        Solution198 solution = new Solution198();
        int[] nums = {1,2,3,1};//1 2 4 4
        System.out.println(solution.rob(nums));

        nums = new int[]{2,7,9,3,1};
        System.out.println(solution.rob(nums));

        nums = new int[]{114,117,207,117,235,82,90,67,143,146,53,108,200,91,80,223,58,170,110,236,81,90,222,160,165,195,187,199,114,235,197,187,69,129,64,214,228,78,188,67,205,94,205,169,241,202,144,240};
        System.out.println(solution.rob(nums));
    }
}
