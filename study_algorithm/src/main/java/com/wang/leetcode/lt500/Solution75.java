package com.wang.leetcode.lt500;

import java.util.Arrays;
import java.util.stream.Collectors;

/*
给定一个包含红色、白色和蓝色、共 n 个元素的数组 nums ，原地对它们进行排序，使得相同颜色的元素相邻，并按照红色、白色、蓝色顺序排列。

我们使用整数 0、 1 和 2 分别表示红色、白色和蓝色。

必须在不使用库的sort函数的情况下解决这个问题。

 

示例 1：

输入：nums = [2,0,2,1,1,0]
输出：[0,0,1,1,2,2]
示例 2：

输入：nums = [2,0,1]
输出：[0,1,2]
 

 */
public class Solution75 {

    public void sortColors(int[] nums) {
        if (null == nums || nums.length == 0) {
            return;
        }

        int redCount = 0;
        for (int i = 0; i < nums.length; i++) {
            if (nums[i] == 0) {
                redCount++;
            }
        }
        int index = 0;
        int end = nums.length - 1;
        int mid = redCount;
        while(index < redCount) {
            if (nums[index] == 1) {
                while (nums[mid] == 1) {
                    mid++;
                }
                swap(nums, index, mid);
            } else if (nums[index] == 2) {
                while (nums[end] == 2) {
                    end--;
                }
                swap(nums, index, end);
            } else {
                index++;
            }
        }
        while(index < end) {
            if (nums[index] == 2) {
                while (end >= 0 && nums[end] == 2) {
                    end--;
                }
                if (end <= index) {
                    break;
                }
                swap(nums, index, end);
            } else {
                index++;
            }
        }
    }

    private void swap(int[] nums, int src, int dest) {
        int temp = nums[src];
        nums[src] = nums[dest];
        nums[dest] = temp;
    }

    public static void main(String[] args) {
        Solution75 solution = new Solution75();
        int[] nums = new int[]{2,0,1};
        solution.sortColors(nums);
        System.out.println(Arrays.stream(nums).boxed().collect(Collectors.toList()));

        nums = new int[]{2,0,2,1,1,0};
        solution.sortColors(nums);
        System.out.println(Arrays.stream(nums).boxed().collect(Collectors.toList()));

        nums = new int[]{2,2};
        solution.sortColors(nums);
        System.out.println(Arrays.stream(nums).boxed().collect(Collectors.toList()));

        nums = new int[]{2,2,0};
        solution.sortColors(nums);
        System.out.println(Arrays.stream(nums).boxed().collect(Collectors.toList()));
    }
}
