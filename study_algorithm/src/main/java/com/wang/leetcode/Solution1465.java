package com.wang.leetcode;

import java.util.Arrays;

public class Solution1465 {
    public int maxArea(int h, int w, int[] horizontalCuts, int[] verticalCuts) {
        Arrays.sort(horizontalCuts);
        Arrays.sort(verticalCuts);

        long maxH = 0;
        int high = 0;
        for (int i = 1; i < horizontalCuts.length; i++) {
            high = horizontalCuts[i] - horizontalCuts[i - 1];
            if (high > maxH) {
                maxH = high;
            }
        }
        if (horizontalCuts.length == 0) {
            maxH = h;
        } else {
            maxH = Math.max(Math.max(maxH, horizontalCuts[0]), h - horizontalCuts[horizontalCuts.length - 1]);
        }

        long maxW = 0;
        int width = 0;
        for (int i = 1; i < verticalCuts.length; i++) {
            width = verticalCuts[i] - verticalCuts[i - 1];
            if (width > maxW) {
                maxW = width;
            }
        }
        if (verticalCuts.length == 0) {
            maxW = w;
        } else {
            maxW = Math.max(Math.max(maxW, verticalCuts[0]), w - verticalCuts[verticalCuts.length - 1]);
        }

        // int * int 超出int结果会出错（负数），因此，需要讲maxH和maxW定义为long类型
        long s = maxH * maxW;
        return (int)(s % (int)(1e9 + 7));
    }

    public int maxArea2(int h, int w, int[] horizontalCuts, int[] verticalCuts) {
        long horizonMax = 0, verticalMax = 0;
        int mod = 1000000007;
        Arrays.sort(horizontalCuts);
        Arrays.sort(verticalCuts);

        for (int i = 1; i < horizontalCuts.length; i++) {
            horizonMax = Math.max(horizonMax, horizontalCuts[i] - horizontalCuts[i - 1]);
        }
        // 补充验证边界切割位置
        horizonMax = Math.max(horizonMax, horizontalCuts[0] - 0);
        horizonMax = Math.max(horizonMax, h - horizontalCuts[horizontalCuts.length - 1]);

        for (int i = 1; i < verticalCuts.length; i++) {
            verticalMax = Math.max(verticalMax, verticalCuts[i] - verticalCuts[i - 1]);
        }
        // 补充验证边界切割位置
        verticalMax = Math.max(verticalMax, verticalCuts[0] - 0);
        verticalMax = Math.max(verticalMax, w - verticalCuts[verticalCuts.length - 1]);

        return (int) ((horizonMax * verticalMax) % mod);
    }

    public static void main(String[] args) {
        Solution1465 solution = new Solution1465();
        int h = 1000000000;
        int w = 1000000000;
        int[] horizontalCuts = new int[]{2};
        int[] verticalCuts = new int[]{2};
        System.out.println(solution.maxArea(h, w, horizontalCuts, verticalCuts));

        System.out.println(solution.maxArea2(h, w, horizontalCuts, verticalCuts));
    }
}
