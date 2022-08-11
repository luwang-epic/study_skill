package com.wang.leetcode.offer;

/*
在一个 n * m 的二维数组中，每一行都按照从左到右递增的顺序排序，每一列都按照从上到下递增的顺序排序。请完成一个高效的函数，输入这样的一个二维数组和一个整数，判断数组中是否含有该整数。

 

示例:

现有矩阵 matrix 如下：

[
  [1,   4,  7, 11, 15],
  [2,   5,  8, 12, 19],
  [3,   6,  9, 16, 22],
  [10, 13, 14, 17, 24],
  [18, 21, 23, 26, 30]
]
给定 target = 5，返回 true。

给定 target = 20，返回 false。

 

限制：

0 <= n <= 1000

0 <= m <= 1000
 */
public class SolutionOffer04 {
    public boolean findNumberIn2DArray(int[][] matrix, int target) {
        if (matrix.length == 0 || matrix[0].length == 0) {
            return false;
        }

        int startLength = 0;
        int endLength = matrix.length - 1;
        int startWidth = 0;
        int endWidth = matrix[0].length - 1;

        int j = 0;
        while (j <= endLength) {
//            int midLength = (startLength + endLength) / 2;
            int midWidth = (startWidth + endWidth) / 2;
            if (matrix[j][midWidth] == target) {
                return  true;
            }

            if(matrix[j][midWidth] > target) {
//                endLength = midLength - 1;
                endWidth = midWidth - 1;
            } else {
//                startLength = midLength + 1;
                startWidth = midWidth + 1;
            }

            if (startWidth > endWidth) {
                startWidth = 0;
                endWidth = matrix[0].length - 1;
                j++;
            }
        }
        return false;
    }


    public static void main(String[] args) {
        SolutionOffer04 solution = new SolutionOffer04();

        int[][] matrix = new int[][] {{1,   4,  7, 11, 15}, {2,   5,  8, 12, 19},
                {3,   6,  9, 16, 22}, {10, 13, 14, 17, 24}, {18, 21, 23, 26, 30}};

        int target = 5;
        boolean isHit = solution.findNumberIn2DArray(matrix, target);
        System.out.println(isHit);

        target = 10;
        isHit = solution.findNumberIn2DArray(matrix, target);
        System.out.println(isHit);

        target = 20;
        isHit = solution.findNumberIn2DArray(matrix, target);
        System.out.println(isHit);

        matrix = new int[][]{{}};
        isHit = solution.findNumberIn2DArray(matrix, target);
        System.out.println(isHit);
    }

}
