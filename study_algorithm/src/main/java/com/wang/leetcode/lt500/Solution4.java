package com.wang.leetcode.lt500;

/*
给定两个大小分别为 m 和 n 的正序（从小到大）数组 nums1 和 nums2。请你找出并返回这两个正序数组的 中位数 。

算法的时间复杂度应该为 O(log (m+n)) 。

 

示例 1：

输入：nums1 = [1,3], nums2 = [2]
输出：2.00000
解释：合并数组 = [1,2,3] ，中位数 2
示例 2：

输入：nums1 = [1,2], nums2 = [3,4]
输出：2.50000
解释：合并数组 = [1,2,3,4] ，中位数 (2 + 3) / 2 = 2.5

 */
public class Solution4 {

    public double findMedianSortedArrays(int[] nums1, int[] nums2) {
        int mid = (nums1.length + nums2.length) / 2;

        int m = nums1.length;
        int n = nums2.length;
        double avg1 = nums1.length % 2 == 0 ? (nums1[m / 2] + nums1[m / 2 + 1]) / 2.0  : nums1[m / 2];
        int index2 = dichotomy(nums2, avg1);
        double avg2 = nums2.length % 2 == 0 ? (nums2[n / 2] + nums2[n / 2 + 1]) / 2.0  : nums2[n / 2];
        int index1 = dichotomy(nums1, avg2);


        short s = 1;
        s = (short) (s +  1);

        return 0;
    }

    private int dichotomy(int[] nums, double k) {
        int low = 0;
        int high = nums.length - 1;
        int mid = (low + high) / 2;
        while (low <= high) {
            mid = (low + high) / 2;
            if (k == nums[mid]) {
                return mid;
            }
            if (k < nums[mid]) {
                high = mid - 1;
            } else {
                low = mid + 1;
            }
        }
        return k < nums[mid] ? high : low;
    }
}
