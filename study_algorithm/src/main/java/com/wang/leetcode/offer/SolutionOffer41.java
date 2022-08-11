package com.wang.leetcode.offer;

/*

如何得到一个数据流中的中位数？如果从数据流中读出奇数个数值，那么中位数就是所有数值排序之后位于中间的数值。如果从数据流中读出偶数个数值，那么中位数就是所有数值排序之后中间两个数的平均值。

例如，

[2,3,4] 的中位数是 3

[2,3] 的中位数是 (2 + 3) / 2 = 2.5

设计一个支持以下两种操作的数据结构：

void addNum(int num) - 从数据流中添加一个整数到数据结构中。
double findMedian() - 返回目前所有元素的中位数。
示例 1：

输入：
["MedianFinder","addNum","addNum","findMedian","addNum","findMedian"]
[[],[1],[2],[],[3],[]]
输出：[null,null,null,1.50000,null,2.00000]
示例 2：

输入：
["MedianFinder","addNum","findMedian","addNum","findMedian"]
[[],[2],[],[3],[]]
输出：[null,null,2.00000,null,2.50000]



 */
public class SolutionOffer41 {
    int[] nums = new int[100000];
    int size = 0;

    public void addNum(int num) {
        int insertIndex = 0;
        if (size > 0) {
            if (num >= nums[size - 1]) {
                insertIndex = size;
            } else if (num > nums[0]) {
                int start = 0;
                int end = size - 1;
                int mid = 0;
                while (start <= end) {
                    mid = (start + end) / 2;
                    if (nums[mid] == num) {
                        start = mid;
                        break;
                    }
                    if (nums[mid] > num) {
                        end = mid - 1;
                    } else if (nums[mid] < num) {
                        start = mid + 1;
                    }
                }
                insertIndex = start;
            }
        }

        // 移动
        if (insertIndex >= size) {
            nums[size] = num;
        } else {
            for (int i = size; i > insertIndex; i--) {
                nums[i] = nums[i-1];
            }
            nums[insertIndex] = num;
        }
        size++;
    }

    public double findMedian() {
        if (size == 0) {
            return 0;
        }
        if (size % 2 == 0) {
            return (nums[size/2] + nums[size/2 -1]) / 2.0;
        }
        return nums[size/2];
    }
}
