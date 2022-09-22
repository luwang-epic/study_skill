package com.wang.algorithm;

/**
 * 背包问题
 *
 * 背包问题具体例子：假设现有容量10kg的背包，另外有3个物品，分别为a1，a2，a3。
 * 物品a1重量为3kg，价值为4；物品a2重量为4kg，价值为5；物品a3重量为5kg，价值为6。
 * 将哪些物品放入背包可使得背包中的总价值最大？
 *
 * 如果限定每种物品只能选择0 个或1 个，则问题称为0-1 背包问题；
 * 如果不限定每种物品的数量，则问题称为无界背包问题或完全背包问题。
 *
 * 博客：https://blog.csdn.net/qq_15711195/article/details/123455338
 */
public class BackpackProblem {
    /*
    以0-1 背包问题为例。我们可以定义一个二维数组dp存储最大价值，
    其中dp[i][j] 表示前i 件物品体积不超过j 的情况下能达到的最大价值。
    在我们遍历到第i 件物品时，在当前背包总容量为j 的情况下，
    如果我们不将物品i 放入背包，那么dp[i][j] = dp[i-1][j]，即前i 个物品的最大价值等于只取前i-1 个物品时的最大价值；
    如果我们将物品i 放入背包，假设第i 件物品体积为w，价值为v，那么我们得到dp[i][j] = dp[i-1][j-w] + v。
    我们只需在遍历过程中对这两种情况取最大值即可，总时间复杂度和空间复杂度都为O(NW)。
     */

    /**
     * 01背包问题
     * @param m 表示背包的最大容量
     * @param n 表示商品个数
     * @param w 表示商品重量数组
     * @param p 表示商品价值数组
     * @return 返回背包可以存放物品的最大的价值
     */
    public static int solutionOf01Backpack(int m, int n, int[] w, int[] p) {
        //c[i][v]表示前i件物品恰放入一个重量为m的背包可以获得的最大价值
        int c[][] = new int[n + 1][m + 1];
        for (int i = 0; i < n + 1; i++) {
            c[i][0] = 0;
        }
        for (int j = 0; j < m + 1; j++) {
            c[0][j] = 0;
        }

        for (int i = 1; i < n + 1; i++) {
            for (int j = 1; j < m + 1; j++) {
                //当物品为i件重量为j时，如果第i件的重量(w[i-1])小于重量j时，c[i][j]为下列两种情况之一：
                //(1)物品i不放入背包中，所以c[i][j]为c[i-1][j]的值
                //(2)物品i放入背包中，则背包剩余重量为j-w[i-1],所以c[i][j]为c[i-1][j-w[i-1]]的值加上当前物品i的价值
                if (w[i - 1] <= j) {
                    if (c[i - 1][j] < (c[i - 1][j - w[i - 1]] + p[i - 1])) {
                        c[i][j] = c[i - 1][j - w[i - 1]] + p[i - 1];
                    } else {
                        c[i][j] = c[i - 1][j];
                    }
                } else {
                    c[i][j] = c[i - 1][j];
                }
            }
        }

        return c[n][m];
    }

    /*
    在完全背包问题中，一个物品可以拿多次。如果仍采用这种方法，假设背包容量无穷大而物体的体积无穷小，
    我们这里的比较次数也会趋近于无穷大，远超O(NW) 的时间复杂度。因此，对于拿多个物品的情况，
    我们就得到了完全背包问题的状态转移方程：dp[i][j] = max(dp[i-1][j], dp[i][j-w] + v)，
    其与0-1 背包问题的差别仅仅是把状态转移方程中的第二个i-1 变成了i。
     */

    /**
     * 完全背包问题
     * @param m 表示背包的最大容量
     * @param n 表示商品个数
     * @param w 表示商品重量数组
     * @param p 表示商品价值数组
     * @return 返回背包可以存放物品的最大的价值
     */
    public static int solutionOfFullBackpack(int m, int n, int[] w, int[] p) {
        //c[i][v]表示前i件物品恰放入一个重量为m的背包可以获得的最大价值
        int c[][] = new int[n + 1][m + 1];
        for (int i = 0; i < n + 1; i++) {
            c[i][0] = 0;
        }
        for (int j = 0; j < m + 1; j++) {
            c[0][j] = 0;
        }

        for (int i = 1; i < n + 1; i++) {
            for (int j = 1; j < m + 1; j++) {
                //当物品为i件重量为j时，如果第i件的重量(w[i-1])小于重量j时，c[i][j]为下列两种情况之一：
                //(1)物品i不放入背包中，所以c[i][j]为c[i-1][j]的值
                //(2)物品i放入背包中，则背包剩余重量为j-w[i-1],
                // 所以c[i][j]为c[i][j-w[i-1]]的值(因为可以多次放i的物品)加上当前物品i的价值
                if (w[i - 1] <= j) {
                    if (c[i - 1][j] < (c[i][j - w[i - 1]] + p[i - 1])) {
                        c[i][j] = c[i][j - w[i - 1]] + p[i - 1];
                    } else {
                        c[i][j] = c[i - 1][j];
                    }
                } else {
                    c[i][j] = c[i - 1][j];
                }
            }
        }

        return c[n][m];
    }


    public static void main(String[] args) {
        int m = 10;
        int n = 3;
        int w[] = {3, 4, 5};
        int p[] = {4, 5, 6};
        System.out.println(solutionOf01Backpack(m, n, w, p));
        System.out.println(solutionOfFullBackpack(m, n, w, p));
    }
}
