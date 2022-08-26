package com.wang.algorithm;


/**
 * 问题：求数组中最长递增子序列
 * 写一个时间复杂度尽可能低的程序，求一个一维数组（N个元素）中的最长递增子序列的长度。
 * 例如：在序列1，-1,2，-3,4，-5,6，-7中，其最长的递增子序列为1,2,4,6,最长递增子序列
 * 的长度为4。
 * 求解思路：使用动态规划算法 时间复杂度O(n^2)
 */
public class MaxSubsequence {

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

        int[] result = MaxSubsequence.solve(array, subseq);
        for (int temp : result) {
            System.out.print("  " + temp);
        }
    }

}

