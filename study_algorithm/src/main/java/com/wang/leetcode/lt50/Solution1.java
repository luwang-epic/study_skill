package com.wang.leetcode.lt50;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 给定一个整数数组 nums 和一个整数目标值 target，请你在该数组中找出 和为目标值 target  的那 两个 整数，并返回它们的数组下标。

 你可以假设每种输入只会对应一个答案。但是，数组中同一个元素在答案里不能重复出现。

 你可以按任意顺序返回答案。

  

 示例 1：

 输入：nums = [2,7,11,15], target = 9
 输出：[0,1]
 解释：因为 nums[0] + nums[1] == 9 ，返回 [0, 1] 。
 示例 2：

 输入：nums = [3,2,4], target = 6
 输出：[1,2]
 示例 3：

 输入：nums = [3,3], target = 6
 输出：[0,1]

 */
public class Solution1 {

    public int[] twoSum(int[] nums, int target) {
        if (null == nums || nums.length < 2) {
            return new int[0];
        }

        int[] copyNums = Arrays.copyOf(nums, nums.length);
        Arrays.sort(copyNums);
        int start = 0;
        int end = copyNums.length - 1;
        while (start < end) {
            if (copyNums[start] + copyNums[end] < target) {
                start++;
            } else if (copyNums[start] + copyNums[end] > target) {
                end--;
            } else {
                int[] hits = new int[] {-1, -1};
                for (int i = 0; i < nums.length; i++) {
                    if (nums[i] == copyNums[start] && hits[0] < 0) {
                        hits[0] = i;
                        continue;
                    }
                    if (nums[i] == copyNums[end]) {
                        hits[1] = i;
                    }
                }
                return hits;
            }
        }
        return new int[0];
    }

    public static void main(String[] args) {
        Solution1 solution = new Solution1();
        int[] nums = new int[] {2,5,5,11};
        int target = 10;
        System.out.println(Arrays.stream(solution.twoSum(nums, target)).boxed().collect(Collectors.toList()));
    }
}
