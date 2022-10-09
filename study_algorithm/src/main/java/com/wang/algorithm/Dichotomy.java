package com.wang.algorithm;

import org.junit.jupiter.api.Test;

/**
 * 二分法，主要3中应用场景
 *  1. 在有序数组中找到某个数
 *  2. 在有序数组中找到所有大于或者等于某个数num的位置
 *      需要一直用二分查找判断，直到临界点大于或者等于num，且减一的位置小于num
 *  3. 寻找局部最小，例如：在一个无序数组中，每个位置的数字都不相同，找到一个局部最小的数字
 *     局部最小为：该位置的数小于左边的数，也小于右边的数，0位置只需要小于1位置，n-1位置只需要小于n-2位置即可
 *       a. 判断0位置是否符合条件，如果不符合继续，符合返回
 *       b. 判断n-1位置是否符合条件，如果不符合继续，符合返回
 *       c. 都不符合的话，那么必定是arr[0]>arr[1], arr[n-1]>arr[n-2]
 *             由于各个位置的数字不一样，那么中间必定存在一个局部最小
 *       d. 取中间的位置mid=(n-1)/2，如果arr[mid]>arr[mid-1]，则在0到mid-1位置存在局部最小
 *          如果arr[mid]<arr[mid+1]，则在mid+1到n-1位置存在局部最小；
 *          否则，如果arr[mid]<arr[mid-1] & arr[mid] < arr[mid+1]，那么mid符合条件
 *          以此类推，用二分查找可以找到局部最小
 */
public class Dichotomy {

    @Test
    public void prepareSkills() {
        // 求中点
        int l = 0; int r = 100;
        // 这种写法可能会存在l+r溢出的情况
        int mid = (l + r) / 2;

        // 因此可以使用这种写法，左边加上一般的距离就是中间了，右移一位表示除2
        mid = l + ((r - l) >> 1);
    }

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
