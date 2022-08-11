package com.wang.leetcode.offer;

/*
输入一个矩阵，按照从外向里以顺时针的顺序依次打印出每一个数字。

 

示例 1：

输入：matrix = [[1,2,3],[4,5,6],[7,8,9]]
输出：[1,2,3,6,9,8,7,4,5]
示例 2：

输入：matrix = [[1,2,3,4],[5,6,7,8],[9,10,11,12]]
输出：[1,2,3,4,8,12,11,10,9,5,6,7]
 

 */

import java.util.Arrays;
import java.util.stream.Collectors;

public class SolutionOffer29 {
    public int[] spiralOrder(int[][] matrix) {
        if (null == matrix || matrix.length == 0) {
            return new int[0];
        }

        int row = matrix.length;
        int column = matrix[0].length;

        int rowStart = 0;
        int rowEnd = row-1;
        int columnStart = 0;
        int columnEnd = column - 1;

        int status = 0;
        int[] result = new int[row*column];
        int index = 0;
        while(index < result.length) {
            if (index < result.length && status == 0) {
                for(int i = columnStart; i <= columnEnd; i++) {
                    result[index++] = matrix[rowStart][i];
                }
                status = 1;
                rowStart++;
            }

            if (index < result.length && status == 1) {
                for(int i = rowStart; i <= rowEnd; i++) {
                    result[index++] = matrix[i][columnEnd];
                }
                status = 2;
                columnEnd--;
            }

            if (index < result.length && status == 2) {
                for(int i = columnEnd; i >= columnStart; i--) {
                    result[index++] = matrix[rowEnd][i];
                }
                status = 3;
                rowEnd--;
            }

            if (index < result.length && status == 3) {
                for(int i = rowEnd; i >= rowStart; i--) {
                    result[index++] = matrix[i][columnStart];
                }
                status = 0;
                columnStart++;
            }
        }

        return result;
    }

    public static void main(String[] args) {
        SolutionOffer29 solution = new SolutionOffer29();
        int[][] matrix = {{1,2,3},{4,5,6},{7,8,9}};
        System.out.println(Arrays.stream(solution.spiralOrder(matrix)).boxed().collect(Collectors.toList()));

        matrix = new int[][] {{1,2,3,4},{5,6,7,8},{9,10,11,12}};
        System.out.println(Arrays.stream(solution.spiralOrder(matrix)).boxed().collect(Collectors.toList()));
    }
}
