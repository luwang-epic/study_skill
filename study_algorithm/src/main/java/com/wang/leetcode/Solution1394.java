package com.wang.leetcode;
/*
在整数数组中，如果一个整数的出现频次和它的数值大小相等，我们就称这个整数为「幸运数」。

给你一个整数数组 arr，请你从中找出并返回一个幸运数。

如果数组中存在多个幸运数，只需返回 最大 的那个。
如果数组中不含幸运数，则返回 -1 。

 */


import java.util.HashMap;
import java.util.Map;

public class Solution1394 {
    public int findLucky(int[] arr) {
        if (null == arr || arr.length == 0) {
            return -1;
        }

        Map<Integer, Integer> numCountMap = new HashMap<>();
        for (int i = 0; i < arr.length; i++) {
            if (numCountMap.containsKey(arr[i])) {
                numCountMap.put(arr[i], numCountMap.get(arr[i]) + 1);
            } else {
                numCountMap.put(arr[i], 1);
            }
        }

        int max = -1;
        for (Map.Entry<Integer, Integer> entry : numCountMap.entrySet()) {
            if (entry.getKey() == entry.getValue() && entry.getKey() > max) {
                max = entry.getKey();
            }
        }
        return max;
    }

    public static void main(String[] args) {
        Solution1394 solution = new Solution1394();
        int[] arr = new int[] {2,2,3,4};
        System.out.println(solution.findLucky(arr));

        arr = new int[] {1,2,2,3,3,3};
        System.out.println(solution.findLucky(arr));

        arr = new int[] {5};
        System.out.println(solution.findLucky(arr));
    }
}
