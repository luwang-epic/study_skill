package com.wang.leetcode.lt50;

public class Solution11 {
    public int maxArea(int[] height) {
        if (null == height || height.length < 2) {
            return 0;
        }

        return 0;
    }

    public static void main(String[] args) {
        Solution11 solution = new Solution11();
        int[] height = new int[]{1, 8, 6, 2, 5, 4, 8, 3, 7};
        System.out.println(solution.maxArea(height));
    }
}
