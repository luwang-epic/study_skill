package com.wang.representative;


/*
先暴力try  -> 记忆化搜索（一般不需要管状态的） -> 表结果的动态规划（需要理清状态转换）  ->  严格动态规划（是否可以优化存储）
使用暴力迭代尝试的方法：
    1. 尽量让可变的尝试是整数，而不是数组（这个改为动态规划会比较麻烦，一般比较少）
    2. 可变参数的个数尽量少
 */

import org.junit.jupiter.api.Test;

/**
 * 动态规划相关的
 */
public class DynamicProgrammingProblem {

    /*
     给定四个参数N、P、M、K。表示：
        N : 一共有1～N个位置
        P : 机器人想要去的位置是P
        K : 一共有P步要走
        M : 机器人初始停留在M位置上
     题目：已知，如果机器人来到 1 位置，那么下一步一定会走到 2 位置。如果机器人来到 N 位置，
     那么下一步一定会走到 N - 1 位置；如果机器人在中间的位置，那么下一步既可以走向左，也可以走向右。
     请返回，机器人如果初始停留在 M 位置，经过 K 步之后，机器人来到 P 位置的走法有多少种。
     */
    @Test
    public void botWalkDemo() {
        int n = 5;
        int p = 4; // 终点位置
        int k = 4; // 一共需要走多少步
        int m = 2; // 初始位置
        System.out.println(botWalkWithViolenceSolve(n, p, k, m));
        System.out.println(botWalkWithMemorySearchSolve(n, p, k, m));
        System.out.println(botWalkWithDynamicProgrammingSolve(n, p, k, m));
    }

    /*
    一个有硬币面值（可以是任意正整数面值）组成的正数数组，可以有重复，和一个数，
    要求从数组中拿出若干个（不重复），和是否可以等于这个数，最少需要多少硬币
     */
    @Test
    public void minCoinCountDemo() {
        int[] coins = new int[] {1, 2, 3, 1, 4};
        System.out.println(minCoinCountWithViolenceSolve(coins, 0, 7));
        System.out.println(minCoinCountWithMemorySearchSolve(coins, 0, 7));
        System.out.println(minCoinCountWithDynamicProgrammingSolve(coins, 7));
    }

    /*
    一个有硬币面值（可以是任意正整数面值）组成的正数数组，可以有重复，和一个数，
    要求从数组中拿出若干个（可以重复），和是否可以等于这个数，有多少中方案
     */
    @Test
    public void totalCoinCountDemo() {
        int[] coins = new int[] {1, 2, 3, 1, 4};
        System.out.println(totalCoinCountWithViolenceSolve(coins, 0, 7));
        System.out.println(totalCoinCountWithMemorySearchSolve(coins, 0, 7));
        System.out.println(totalCoinCountWithDynamicProgrammingSolve(coins, 7));

        coins = new int[] {1, 2, 3, 1, 4, 2};
        System.out.println(totalCoinCountWithViolenceSolve(coins, 0, 7));
        System.out.println(totalCoinCountWithMemorySearchSolve(coins, 0, 7));
        System.out.println(totalCoinCountWithDynamicProgrammingSolve(coins, 7));
    }

    /**
     * 暴力求解
     * @param n 位置总数
     * @param p 终点位置
     * @param remaining 剩余多少步
     * @param cur 当前位置
     * @return 总共有多少中走法
     */
    public int botWalkWithViolenceSolve(int n, int p, int remaining, int cur) {
        // 如果走完了，判断是否在p位置，如果在则是一种有效方法
        if (remaining == 0) {
            return cur == p ? 1 : 0;
        }

        // 如果还剩余步，需要继续走

        // 当前走到1位置，下一步只能到2的位置
        if (cur == 1) {
            return botWalkWithViolenceSolve(n, p, remaining - 1, 2);
        }
        // 当前走的n位置，下一步只能到n-1的位置
        if (cur == n) {
            return botWalkWithViolenceSolve(n, p, remaining - 1, n - 1);
        }

        // 中间位置，左右都可以走，统计两个走法的总和
        return botWalkWithViolenceSolve(n, p, remaining - 1, cur + 1) + botWalkWithViolenceSolve(n, p, remaining - 1, cur - 1);
    }

    /**
     * 记忆化搜索，上面暴力求解会有很多重复的步骤，比如 f(remaining, cur): f(2,2) 需要计算多次
     *   因为这个f(2,2)的方法和到达f(2,2)的前一个状态没有关系，都是f(2,2)的结果
     *   那么我们是否可以通过缓存的方式，避免重复计算了，因此需要加一个缓存，缓存中间的结果
     *   可以定义一个dp数组来缓存，因为有两个变量，因此需要一个二维数组，第一个位置的剩余步数，第二个维度为当前位置
     *   因此可以一定以为dp[p][n]，在计算过程中来缓存计算好的结果
     * @param n 位置总数
     * @param p 终点位置
     * @param remaining 剩余多少步
     * @param cur 当前位置
     * @return 总共有多少中走法
     */
    public int botWalkWithMemorySearchSolve(int n, int p, int remaining, int cur) {
        // 为了方便，从1开始
        int[][] dp = new int[p + 1][n + 1];
        for (int i = 0; i <= p; i++) {
            for (int j = 0; j <= n; j++) {
                // -1表示没有缓存
                dp[i][j] = -1;
            }
        }
        return botWalkWithMemorySearchSolve(n, p, remaining, cur, dp);
    }

    private int botWalkWithMemorySearchSolve(int n, int p, int remaining, int cur, int[][] dp) {
        // 有缓存直接走缓存
        if (dp[remaining][cur] != - 1) {
            return dp[remaining][cur];
        }

        // 没有缓存，计算

        if (remaining == 0) {
            // 返回之前保存结果到缓存
            dp[remaining][cur] = cur == p ? 1 : 0;
            return dp[remaining][cur];
        }

        // 当前走到1位置，下一步只能到2的位置
        if (cur == 1) {
            // 返回之前保存结果到缓存
            dp[remaining][cur] = botWalkWithViolenceSolve(n, p, remaining - 1, 2);
            return dp[remaining][cur];
        }
        // 当前走的n位置，下一步只能到n-1的位置
        if (cur == n) {
            // 返回之前保存结果到缓存
            dp[remaining][cur] = botWalkWithViolenceSolve(n, p, remaining - 1, n - 1);
            return dp[remaining][cur];
        }

        // 中间位置，左右都可以走，统计两个走法的总和
        // 返回之前保存结果到缓存
        dp[remaining][cur] = botWalkWithViolenceSolve(n, p, remaining - 1, cur + 1) + botWalkWithViolenceSolve(n, p, remaining - 1, cur - 1);
        return dp[remaining][cur];
    }
    /**
     * 动态规划，从记忆化搜索可以看出，remaining的求解只和remaining-1有关，和其他没有关系
     *  因此我们可以定义一个严格的表格（数组），从第一行开始计算，从而求出最终结果
     *  也就是先从 remaining = 0 行计算，这样只有 cur = p才会为1，否则都是0，然后再计算后续行数
     *  因为第1行只和0行有关，i只和i-1有关，因此可以计算出所有的值，而remaining=4, p=2就是我们需要的中走发
     * @param n 位置总数
     * @param p 终点位置
     * @param k 需要走多少步
     * @param m 初始位置
     * @return 总共有多少中走法
     */
    public int botWalkWithDynamicProgrammingSolve(int n, int p, int k, int m) {
        int[][] dp = new int[p + 1][n + 1];
        for (int i = 0; i <= n; i++) {
            // 只有剩余0不，得到目标p时，才是有效走法，其他都不是
            if (i == p) {
                dp[0][i] = 1;
            } else {
                dp[0][i] = 0;
            }
        }

        // 第一行（0行）已经计算好了，只需要计算后续的行数
        for (int i = 1; i <= p; i++) {
            // 不可能走到0位置
            for (int j = 1; j <= n; j++) {
                // 左边界只和上一行的第一个位置有关
                if (j == 1) {
                    dp[i][j] = dp[i - 1][2];
                }
                // 右边界只和上一行的第n-1位置有关
                else if (j == n) {
                    dp[i][j] = dp[i - 1][n - 1];
                }
                // 否则，中间位置，和上一行的第i-1和第i+1位置有关
                else {
                    dp[i][j] = dp[i - 1][j - 1] + dp[i - 1][j + 1];
                }
            }
        }
        return dp[k][m];
    }


    /**
     * 暴力递归求最少的硬币数
     * @param coins 硬币数组
     * @param index 到了哪个位置
     * @param remaining 到了这个位置后，前面的所有位置，即0到index-1的位置还剩余多少才可以组成规定的面值
     *                  即：target = remaining + sum(0~index-1) (每个位置可以选择或者不选择这个位置的硬币)
     * @return 最少的方案数
     */
    public int minCoinCountWithViolenceSolve(int[] coins, int index, int remaining) {
        // 不可以达成目标
        if (remaining < 0) {
            return -1;
        }
        // 不需要选择就达成了目标
        if (remaining == 0) {
            return 0;
        }
        // remaining > 0, 而且硬币都决定完了，所以也没有方案可以达成目标
        if (index == coins.length) {
            return -1;
        }

        // 否则有两种方案，选择最小个数的
        // 1. 不需要index位置的硬币， 2. 选择index位置的硬币，那么硬币数+1
        int noSelected = minCoinCountWithViolenceSolve(coins, index + 1, remaining);
        int selected = minCoinCountWithViolenceSolve(coins, index + 1, remaining - coins[index]);

        // 对无效解-1特殊处理，否则加-1会有干扰正常数量的选择
        if (noSelected == -1 && selected == -1) {
            return -1;
        } else if (noSelected == -1) {
            return selected + 1;
        } else if (selected == -1) {
            return noSelected;
        } else {
            return Math.min(noSelected, selected + 1);
        }
    }

    /**
     * 记忆化搜索的方式求解最少的硬币数
     * @param coins 硬币数组
     * @param index 到了哪个位置
     * @param remaining 到了这个位置后，前面的所有位置，即0到index-1的位置还剩余多少才可以组成规定的面值
     *                  即：target = remaining + sum(0~index-1) (每个位置可以选择或者不选择这个位置的硬币)
     * @return 最少的方案数
     */
    public int minCoinCountWithMemorySearchSolve(int[] coins, int index, int remaining) {
        // 缓存结果
        int[][] dp = new int[coins.length + 1][remaining + 1];
        for (int i = 0; i <= coins.length; i++) {
            for (int j = 0; j <= remaining; j++) {
                dp[i][j] = -2;
            }
        }
        return minCoinCountWithMemorySearchSolve(coins, index, remaining, dp);
    }

    public int minCoinCountWithMemorySearchSolve(int[] coins, int index, int remaining, int[][] dp) {
        // 不可以达成目标
        if (remaining < 0) {
            return -1;
        }

        // 从缓存中获取
        if (dp[index][remaining] != -2) {
            return dp[index][remaining];
        }

        // 不需要选择就达成了目标
        if (remaining == 0) {
            dp[index][0] = 0;
            return 0;
        }
        // remaining > 0, 而且硬币都决定完了，所以也没有方案可以达成目标
        if (index == coins.length) {
            dp[index][remaining] = -1;
            return -1;
        }

        // 否则有两种方案，选择最小个数的
        // 1. 不需要index位置的硬币， 2. 选择index位置的硬币，那么硬币数+1
        int noSelected = minCoinCountWithViolenceSolve(coins, index + 1, remaining);
        int selected = minCoinCountWithViolenceSolve(coins, index + 1, remaining - coins[index]);

        // 对无效解-1特殊处理，否则加-1会有干扰正常数量的选择
        if (noSelected == -1 && selected == -1) {
            dp[index][remaining] = -1;
            return -1;
        } else if (noSelected == -1) {
            dp[index][remaining] = selected + 1;
            return selected + 1;
        } else if (selected == -1) {
            dp[index][remaining] = noSelected;
            return noSelected;
        } else {
            dp[index][remaining] = Math.min(noSelected, selected + 1);
            return Math.min(noSelected, selected + 1);
        }
    }


    /**
     * 动态规划的方式求解最少的硬币数
     * @param coins 硬币数组
     * @param target 目标值
     * @return 最少的方案数
     */
    public int minCoinCountWithDynamicProgrammingSolve(int[] coins, int target) {
        int[][] dp = new int[coins.length + 1][target + 1];
        // 越界都是-1
        for (int i = 0; i <= target; i++) {
             dp[coins.length][i] = -1;
        }
        // 目标为0，都是0
        for (int i = 0; i <= coins.length; i++) {
            dp[i][0] = 0;
        }

        // 只依赖于上一个选还是不选，依此求出所有的解
        for (int i = coins.length - 1; i >= 0; i--) {
            for (int j = 1; j <= target; j++) {
                int noSelected = dp[i + 1][j];
                int selected = -1;
                // 下标越界为-1
                if (j - coins[i] >= 0) {
                    selected = dp[i + 1][j - coins[i]];
                }

                if (selected == -1 && noSelected == -1) {
                    dp[i][j] = -1;
                } else if (selected == -1) {
                    dp[i][j] = noSelected;
                } else if (noSelected == -1) {
                    dp[i][j] = selected + 1;
                } else {
                    dp[i][j] = Math.min(selected + 1, noSelected);
                }
            }
        }

        return dp[0][target];
    }

    /**
     * 暴力递归求总共有多少总方案
     * @param coins 硬币数组
     * @param index 到了哪个位置
     * @param remaining 到了这个位置后，前面的所有位置，即0到index-1的位置还剩余多少才可以组成规定的面值
     *                  即：target = remaining + sum(0~index-1) (每个位置可以选择0或者多次硬币)
     * @return 总共有多少中方案
     */
    public int totalCoinCountWithViolenceSolve(int[] coins, int index, int remaining) {
        // 当没有硬币可以选择时，如果remaining为0，说明找到一种方案，否则找不到方案
        if (index == coins.length) {
            return remaining == 0 ? 1 : 0;
        }

        int total = 0;
        // index对应的硬币，可以选择0或者多次，但是不能超过remaining面值，因为超过了方案数为0
        for (int i = 0; i * coins[index] <= remaining; i++) {
            total += totalCoinCountWithViolenceSolve(coins, index + 1, remaining - i * coins[index]);
        }
        return total;
    }

    /**
     * 记忆化搜索求总共有多少总方案
     * @param coins 硬币数组
     * @param index 到了哪个位置
     * @param remaining 到了这个位置后，前面的所有位置，即0到index-1的位置还剩余多少才可以组成规定的面值
     *                  即：target = remaining + sum(0~index-1) (每个位置可以选择0或者多次硬币)
     * @return 总共有多少中方案
     */
    public int totalCoinCountWithMemorySearchSolve(int[] coins, int index, int remaining) {
        int[][] dp = new int[coins.length + 1][remaining + 1];
        for (int i = 0; i <= coins.length; i++) {
            for (int j = 0; j <= remaining; j++) {
                dp[i][j] = -1;
            }
        }
        return totalCoinCountWithMemorySearchSolve(coins, index, remaining, dp);
    }

    public int totalCoinCountWithMemorySearchSolve(int[] coins, int index, int remaining, int[][] dp) {
        // 从缓存中获取
        if (dp[index][remaining] != -1) {
            return dp[index][remaining];
        }

        // 当没有硬币可以选择时，如果remaining为0，说明找到一种方案，否则找不到方案
        if (index == coins.length) {
            dp[index][remaining] = remaining == 0 ? 1 : 0;
            return dp[index][remaining];
        }

        int total = 0;
        // index对应的硬币，可以选择0或者多次，但是不能超过remaining面值，因为超过了方案数为0
        for (int i = 0; i * coins[index] <= remaining; i++) {
            total += totalCoinCountWithViolenceSolve(coins, index + 1, remaining - i * coins[index]);
        }
        dp[index][remaining] = total;
        return dp[index][remaining];
    }

    /**
     * 动态规划的方式求总共有多少总方案
     * @param coins 硬币数组
     * @param target 目标值
     * @return 最少的方案数
     */
    public int totalCoinCountWithDynamicProgrammingSolve(int[] coins, int target) {
        int[][] dp = new int[coins.length + 1][target + 1];
        for (int i = 0; i <= target; i++) {
            dp[coins.length][i] = 0;
        }
        for (int i = 0; i <= coins.length; i++) {
            dp[i][0] = 1;
        }

        for (int i = coins.length - 1; i >= 0; i--) {
            for (int remaining = 1; remaining <= target; remaining++) {
                // 第一种，根据迭代获取公式，是O(N * target * target)的复杂度
                int total = 0;
                for (int k = 0; k * coins[i] <= remaining; k++) {
                    total += dp[i + 1][remaining - k * coins[i]];
                }
                dp[i][remaining] = total;

                // 第二种，下面进行优化，去除for循环，得到O(N * target)的复杂度

                // 上面的这个循环真的有必要吗？看第i行remaining-coins[i]列是怎么来的，是用前面的累加得到来的
                // 这个前面的累加和remaining的累加一样，只是remaining多了一个dp[i-1][remaining]
                // 那么这个循环可以替换为下面的公式  (这个是观察出来的，可以优化的，没必要找具体的意义)
                if (remaining - coins[i] >= 0) {
                    dp[i][remaining] = dp[i][remaining - coins[i]] + dp[i + 1][remaining];
                } else {
                    dp[i][remaining] = dp[i + 1][remaining];
                }
            }
        }

        return dp[0][target];
    }
}
