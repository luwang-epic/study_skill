package com.wang.algorithm;

/*
二分法
 */

public class Dichotomy {

    boolean binarySearch(int[] nums, int target) {
        int start = 0;
        int end = nums.length - 1;
        int mid = (start + end) / 2;
        while (start <= end) {
            if (nums[mid] == target) {
                return true;
            } else if (nums[mid] > target) {
                end = mid - 1;
            } else {
                start = mid + 1;
            }
            mid = (start + end) / 2;
        }
        return false;
    }

    public static void main(String[] args) {
        Dichotomy dichotomy = new Dichotomy();
        int[] nums = new int[] {1, 3, 8, 11, 15, 20, 83};
        int target = 10;
        boolean isHit = dichotomy.binarySearch(nums, target);
        System.out.println(isHit);

        target = 1;
        isHit = dichotomy.binarySearch(nums, target);
        System.out.println(isHit);
    }

}
