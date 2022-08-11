package com.wang.leetcode.offer;


public class SolutionOffer2_39 {
    public int largestRectangleArea(int[] heights) {
        if (null == heights || heights.length == 0) {
            return 0;
        }

        int max = heights[0];
        for (int i = 0; i < heights.length; i++) {
            int left = i - 1, right = i + 1;
            while (left >= 0 && heights[i] <= heights[left]) {
                left--;
            }
            while (right < heights.length && heights[i] <= heights[right]) {
                right++;
            }

            max = Math.max(max,heights[i] * (right - left - 1));
        }
        return max;
    }

    public static void main(String[] args) {
        SolutionOffer2_39 solution = new SolutionOffer2_39();
        int[] heights = new int[]{2,1,5,6,2,3};
        System.out.println(solution.largestRectangleArea(heights));
    }
}
