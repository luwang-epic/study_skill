package com.wang.algorithm;

import java.util.Arrays;
import java.util.stream.Collectors;

/*
           时间复杂度       空间复杂度       稳定性
选择排序      O(N^2)          O(1)          否
冒泡排序      O(N^2)          O(1)          是
插入排序      O(N^2)          O(1)          是
归并排序      O(N*logN)       O(N)          是
快速排序      O(N*logN)       O(logN)       否
堆排序       O(N*logN)       O(1)          否
 */

/**    一些常见的排序算法
 *   主要包括 如下几类  ：
 *     1. 插入排序  ：    直接插入排序
 *     				希尔排序
 *     2. 选择排序  ：     简单选择排序
 *     				堆排序
 *     3.交换排序     ：    冒泡排序
 *     			  ：     快速排序
 *     4.归并排序
 *
 *     5.基排序
 *
 */
public class SortAlgorithm {

    void quickSort(int[] nums, int start, int end) {
        if (start < end) {
            int base = nums[start];
            int low = start;
            int high = end;
            while (low < high) {
                // 直到右边比基准小，交换
                while (low < high && nums[high] > base) {
                    high--;
                }
                nums[low] = nums[high];
                // 直到左边比基准大，交换
                while (low < high && nums[low] < base) {
                    low++;
                }
                nums[high] = nums[low];
            }

            nums[low] = base;
            quickSort(nums, start, low - 1);
            quickSort(nums, low + 1, end);
        }
    }


    void insertSort(int[] nums) {
        for (int i = 1; i < nums.length; i++) {
            if (nums[i-1] > nums[i]) {
                int temp = nums[i];
                int j = i - 1;
                for (;j >= 0 && nums[j] > temp; j--) {
                    nums[j+1] = nums[j];
                }
                nums[j+1] = temp;
            }
        }
    }

    void shellSort(int[] nums) {
        for ( int step = nums.length/2; step > 0; step = step / 2) {
            for (int i = step; i < nums.length; i++) {
                if (nums[i] < nums[i - step]) {
                    int temp = nums[i];
                    int j = i - step;
                    for (; j >= 0 && nums[j] > temp; j = j - step) {
                        nums[j + step] = nums[j];
                    }
                    nums[j + step] = temp;
                }
            }

        }
    }

    void merge(int[] nums, int low, int mid, int high) {
        int[] tempNums = new int[high - low + 1];

        int index = 0;
        int left = low;
        int right = mid + 1;
        while (left <= mid && right <= high) {
            if (nums[left] <= nums[right]) {
                tempNums[index] = nums[left];
                left++;
            } else {
                tempNums[index] = nums[right];
                right++;
            }
            index++;
        }
        while(left <= mid) {
            tempNums[index] = nums[left];
            left++;
            index++;
        }
        while (right <= high) {
            tempNums[index] = nums[right];
            right++;
            index++;
        }

        index = 0;
        for (int i = low; i <= high; i++) {
            nums[i] = tempNums[index++];
        }
    }

    void mergeSort(int[] nums, int low, int high) {
        int mid = (low + high) / 2;
        if (low < high) {
            // 左边
            mergeSort(nums, low, mid);
            // 右边
            mergeSort(nums, mid + 1, high);
            // 归并左右两个序列
            merge(nums, low, mid, high);
        }
    }

    public static void main(String[] args) {
        SortAlgorithm sortAlgorithm = new SortAlgorithm();
        int[] nums = new int[] {6, 1, 2, 7, 9, 3, 4, 5, 10, 8};
        sortAlgorithm.quickSort(nums, 0, nums.length - 1);
        System.out.println(Arrays.stream(nums).boxed().collect(Collectors.toList()));

        nums = new int[] {6, 1, 2, 7, 9, 3, 4, 5, 10, 8};
        sortAlgorithm.insertSort(nums);
        System.out.println(Arrays.stream(nums).boxed().collect(Collectors.toList()));

        nums = new int[] {6, 1, 2, 7, 9, 3, 4, 5, 10, 8};
        sortAlgorithm.shellSort(nums);
        System.out.println(Arrays.stream(nums).boxed().collect(Collectors.toList()));

        nums = new int[] {6, 1, 2, 7, 9, 3, 4, 5, 10, 8};
        sortAlgorithm.mergeSort(nums, 0, nums.length - 1);
        System.out.println(Arrays.stream(nums).boxed().collect(Collectors.toList()));
    }
}
