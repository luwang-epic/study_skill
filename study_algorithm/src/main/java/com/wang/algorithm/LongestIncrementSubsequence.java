package com.wang.algorithm;


/**
 * 问题：求数组中最长递增子序列
 * 写一个时间复杂度尽可能低的程序，求一个一维数组（N个元素）中的最长递增子序列的长度。
 * 例如：在序列1，-1,2，-3,4，-5,6，-7中，其最长的递增子序列为1,2,4,6,最长递增子序列
 * 的长度为4。
 *
 * 参考：
 * 力扣：https://leetcode.cn/problems/longest-increasing-subsequence/solution/zui-chang-shang-sheng-zi-xu-lie-by-leetcode-soluti/
 * 博客：https://blog.csdn.net/qq_37774171/article/details/81203890
 */
public class LongestIncrementSubsequence {

    /*
    方法一：
        这个问题可以转换为最长公共子序列问题。如例子中的数组A{5，6， 7， 1， 2， 8}，
        则我们排序该数组得到数组A‘{1， 2， 5， 6， 7， 8}，然后找出数组A和A’的最长公共子序列即可。
        显然这里最长公共子序列为{5, 6, 7, 8}，也就是原数组A最长递增子序列。见：LongestCommonSubsequence类

    方法二：
        设长度为N的数组为{a0，a1, a2, ...an-1)，则假定以aj结尾的数组序列的最长递增子序列长度为L(j)，
        则L(j)={ max(L(i))+1, i<j且a[i]<a[j] }。也就是说，我们需要遍历在j之前的所有位置i(从0到j-1)，
        找出满足条件a[i]<a[j]的L(i)，求出max(L(i))+1即为L(j)的值。最后，我们遍历所有的L(j)（从0到N-1），
        找出最大值即为最大递增子序列。时间复杂度为O(N^2)。例如给定的数组为{5，6，7，1，2，8}，
        则L(0)=1, L(1)=2, L(2)=3, L(3)=1, L(4)=2, L(5)=4。
        所以该数组最长递增子序列长度为4，序列为{5，6，7，8}。

    方法三：
        贪心 + 二分查找； 具体看力扣
     */
    public int lengthOfLIS(int[] nums) {
        if (null == nums || nums.length == 0) {
            return 0;
        }

        int[] dp = new int[nums.length];
        for (int i = 0; i < dp.length; i++) {
            dp[i] = 1;
        }

        int max = dp[0];
        for (int i = 1; i < nums.length; i++) {
            for (int j = 0; j < i; j++) {
                if (nums[i] > nums[j]) {
                    dp[i] = Math.max(dp[i], dp[j] + 1);
                }
            }
            max = Math.max(max, dp[i]);
        }

        return max;
    }

    /**
     * @param array  原始数组
     * @param subseq 保存最大子序列在数组中的下标
     * @return 最大子序列
     */
    public static int[] solve(final int[] array, int[][] subseq) {
        //初始化subseq
        for (int i = 0; i < array.length; i++) {
            subseq[i][0] = i;
        }

        int[] lis = new int[array.length]; //用于记录当前各元素作为最大元素的最长递增序列长度

        for (int i = 0; i < array.length; i++) {
            lis[i] = 1; //设置当前元素array[i]作为最大元素的最长递增序列长度为1

            for (int j = 0; j < i; j++) {
                if (array[i] > array[j] && lis[j] + 1 > lis[i]) {
                    lis[i] = lis[j] + 1; //更新lis[i]的值，因为有更长的子序列
                    for (int k = 0; k < lis[j]; k++) {
                        subseq[i][k] = subseq[j][k];
                    }
                    subseq[i][lis[j]] = i;
                }
            }
        }

        int pos = max(lis);

        System.out.println("length --- >" + lis[pos]);

        int[] result = new int[lis[pos]];
        for (int i = 0; i < result.length; i++) {
            result[i] = array[subseq[pos][i]];
        }

        return result;
    }


    public static int max(int[] array) {
        int maxValue = array[0];
        int pos = 0;

        for (int i = 0; i < array.length; i++) {
            if (maxValue < array[i]) {
                maxValue = array[i];
                pos = i;
            }
        }

        return pos;
    }


    public static void main(String[] args) {
        int[] array = new int[]{1, -1, 2, -3, 4, -5, 6, -7};
        int[][] subseq = new int[array.length][array.length];

        int[] result = LongestIncrementSubsequence.solve(array, subseq);
        for (int temp : result) {
            System.out.print("  " + temp);
        }
    }

}

