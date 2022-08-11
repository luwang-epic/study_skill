package com.wang.leetcode.offer;

/*
输入一个整型数组，数组中的一个或连续多个整数组成一个子数组。求所有子数组的和的最大值。

要求时间复杂度为O(n)。

 

示例1:

输入: nums = [-2,1,-3,4,-1,2,1,-5,4]
输出: 6
解释: 连续子数组 [4,-1,2,1] 的和最大，为 6。
 

 */

public class SolutionOffer42 {

    public int maxSubArray(int[] nums) {
        if (null == nums || nums.length == 0) {
            return 0;
        }

        int pre = 0;
        int ans = nums[0];
        for (int i = 0; i < nums.length; i++) {
            pre = Math.max(nums[i], pre + nums[i]);
            ans = Math.max(ans, pre);
        }
        return ans;
    }
}
