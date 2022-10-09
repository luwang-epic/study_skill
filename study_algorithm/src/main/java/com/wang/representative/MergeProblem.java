package com.wang.representative;

import org.junit.jupiter.api.Test;

/**
 * 归并求解问题
 *  1. 数组中小和问题
 *  2. 逆序对问题：数组中前面的数比后面的大，那么这两个数就构成了一个逆序对，需要打印所有的逆序对
 */
public class MergeProblem {

    /*
    数组中某一个数的小和定义为：某个数左边比该数小的数的和，求数组中所有这样的小和的总和
        1. 简单的想法是，遍历这个数据，求查看左边所有比其小的数的和，时间复杂度O(N^2)
        2. 将问题转换为右边比a这个数大的个数count,即：a * count, 在加上所有的，即为最终的和，这个也是O(N^2)
            但是可以利用归并的思想，这样就不需要遍历判断右边大的个数了，可以直接直到个数
            利用归并的算法，时间复杂度为O(NlogN)，代码如下；
    */
    @Test
    public void sumLessInArrayDemo() {
        int[] nums = new int[] {1, 3, 4, 2, 5};
        System.out.println(sumLessInArray(nums, 0, nums.length - 1));
    }


    public int sumLessInArray(int[] nums, int low, int high) {
        if (low == high) {
            return 0;
        }

        int mid = low + ((high - low) / 2 >> 1);
        return sumLessInArray(nums, low, mid)
                + sumLessInArray(nums, mid + 1, high) + merge(nums, low, mid, high);
    }

    private int merge(int[] nums, int low, int mid, int high) {
        int[] tempNums = new int[high - low + 1];
        int index = 0;
        int left = low;
        int right = mid + 1;
        int total = 0;
        while (left <= mid && right <= high) {
            // 可以直接获取右边大于该数的个数，为；high - right + 1; 因为是右边数组是有序的
            total += nums[left] < nums[right] ? (high - right + 1) * nums[left] : 0;
            // 注意这里不能待=号，当相等时，需要先将第二个数组中的数放到临时数组中
            tempNums[index++] = nums[left] < nums[right] ? nums[left++] : nums[right++];
        }
        while(left <= mid) {
            tempNums[index++] = nums[left++];
        }
        while (right <= high) {
            tempNums[index++] = nums[right++];
        }

        // copy到原数组中，表示这部分排序好了
        for (int i = 0; i < tempNums.length; i++) {
            nums[low + i] = tempNums[i];
        }
        return total;
    }

}
