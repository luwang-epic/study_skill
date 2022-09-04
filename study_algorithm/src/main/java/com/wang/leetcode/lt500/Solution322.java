package com.wang.leetcode.lt500;

/*
给你一个整数数组 coins ，表示不同面额的硬币；以及一个整数 amount ，表示总金额。

计算并返回可以凑成总金额所需的 最少的硬币个数 。如果没有任何一种硬币组合能组成总金额，返回 -1 。

你可以认为每种硬币的数量是无限的。

 

示例 1：

输入：coins = [1, 2, 5], amount = 11
输出：3
解释：11 = 5 + 5 + 1
示例 2：

输入：coins = [2], amount = 3
输出：-1
示例 3：

输入：coins = [1], amount = 0
输出：0
 
 */

public class Solution322 {
    public int coinChange(int[] coins, int amount) {
        if (null == coins || coins.length == 0) {
            return -1;
        }
        if (amount <= 0) {
            return 0;
        }

        int[] dp = new int[amount + 1];
        dp[0] = 0;
        int temp = 0;
        for (int i = 1; i < dp.length; i++) {
            for (int j = 0; j < coins.length; j++) {
                if (coins[j] < i) {
                    temp = dp[i - coins[j]] > 0 ? dp[i - coins[j]] + 1 : 0;
                    dp[i] = dp[i] == 0 ? temp : (temp == 0 ? dp[i] : Math.min(dp[i], temp));
                } else if (coins[j] == i) {
                    dp[i] = 1;
                }
            }

        }

        return dp[amount] > 0 ? dp[amount] : -1;
    }

    public static void main(String[] args) {
        Solution322 solution = new Solution322();
        int[] coins = new int[]{1, 2, 5};
        int amount = 11;
        System.out.println(solution.coinChange(coins, amount));

        coins = new int[]{2};
        amount = 3;
        System.out.println(solution.coinChange(coins, amount));

        coins = new int[]{1};
        amount = 0;
        System.out.println(solution.coinChange(coins, amount));

    }
}
