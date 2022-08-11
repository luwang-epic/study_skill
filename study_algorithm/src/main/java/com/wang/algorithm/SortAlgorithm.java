package com.wang.algorithm;

import java.util.Arrays;
import java.util.stream.Collectors;

public class SortAlgorithm {

    void quickSort(int[] nums, int start, int end) {
        if (start < end) {
            int base = nums[start];
            int low = start;
            int high = end;
            boolean isLeft = false;
            while (low < high) {
//                if (!isLeft && nums[high] < base) {
//                    nums[low] = nums[high];
//                    low++;
//                    isLeft = true;
//                } else if (!isLeft) {
//                    high--;
//                }

                // 直到右边比基准小，交换
                while (low < high && nums[high] > base) {
                    high--;
                }
                nums[low] = nums[high];

//                if (isLeft && low < high && nums[low] > base) {
//                    nums[high] = nums[low];
//                    high--;
//                    isLeft = false;
//                } else if (isLeft && low < high) {
//                    low++;
//                }

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
