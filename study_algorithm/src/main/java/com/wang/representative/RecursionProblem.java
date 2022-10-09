package com.wang.representative;

import org.junit.jupiter.api.Test;

/**
 * 递归问题
 */
public class RecursionProblem {

    /*
    一个矩阵中只有0和1两种值， 每个位置都可以和自己的上、 下、 左、 右
    四个位置相连， 如果有一片1连在一起， 这个部分叫做一个岛， 求一个
    矩阵中有多少个岛？
    举例：
        0 0 1 0 1 0
        1 1 1 0 1 0
        1 0 0 1 0 0
        0 0 0 1 0 0
    这个矩阵中有三个岛。
     */
    @Test
    public void islandCountDemo() {
        int[][] matrix = new int[][]{
                {0, 0, 1, 0, 1, 0},
                {1, 1, 1, 0, 1, 0},
                {1, 0, 0, 1, 0, 0},
                {0, 0, 0, 1, 0, 0}
        };
        System.out.println(islandCount(matrix));
    }

    /**
     * 岛的个数
     * @param matrix 0,1矩阵
     */
    public int islandCount(int[][] matrix) {
        if (null == matrix || null == matrix[0]) {
            return 0 ;
        }

        int n = matrix.length;
        int m = matrix[0].length;
        int count = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                if (matrix[i][j] == 1) {
                    count++;
                    infect(matrix, i, j, n, m);
                }
            }
        }

        return count;
    }

    /**
     * 感染过程，将连着的1都改为2，这样下次就不会重新计算进去了
     */
    private void infect(int[][] matrix, int i, int j, int n, int m) {
        // 越界或者不为1，跳出，不是1说明到了海岸线了，不能继续感染了
        if (i < 0 || i >= n || j < 0 || j >= m || matrix[i][j] != 1) {
            return;
        }

        // 没有越界, 那么当前位置就是1，改为2
        matrix[i][j] = 2;
        // 继续感染后面的连续区域
        infect(matrix, i + 1, j, n, m);
        infect(matrix, i - 1, j, n, m);
        infect(matrix, i, j + 1, n, m);
        infect(matrix, i, j - 1, n, m);
    }
}
