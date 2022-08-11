package com.wang.leetcode.offer;

/*
输入一个整数数组，实现一个函数来调整该数组中数字的顺序，使得所有奇数在数组的前半部分，所有偶数在数组的后半部分。
 */


public class SolutionOffer21 {

    public int[] exchange(int[] nums) {
        if (null == nums || nums.length == 0) {
            return nums;
        }

        int headIndex = 0;
        int tailIndex = nums.length - 1;
        while(headIndex < tailIndex) {
            while(headIndex < tailIndex && nums[headIndex] % 2 == 1) {
                headIndex++;
            }

            while (headIndex < tailIndex && nums[tailIndex] % 2 == 0) {
                tailIndex--;
            }

            if (headIndex < tailIndex) {
                int temp = nums[headIndex];
                nums[headIndex] = nums[tailIndex];
                nums[tailIndex] = temp;
            }
        }
        return nums;
    }
}
