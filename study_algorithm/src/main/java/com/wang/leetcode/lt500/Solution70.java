package com.wang.leetcode.lt500;

/*
假设你正在爬楼梯。需要 n 阶你才能到达楼顶。

每次你可以爬 1 或 2 个台阶。你有多少种不同的方法可以爬到楼顶呢？

 

示例 1：

输入：n = 2
输出：2
解释：有两种方法可以爬到楼顶。
1. 1 阶 + 1 阶
2. 2 阶
示例 2：

输入：n = 3
输出：3
解释：有三种方法可以爬到楼顶。
1. 1 阶 + 1 阶 + 1 阶
2. 1 阶 + 2 阶
3. 2 阶 + 1 阶
 */
public class Solution70 {

    public int climbStairs(int n) {
        if (n == 0 || n == 1) {
            return 1;
        }

        int result = 0;
        int preFirst = 1;
        int preSecond = 1;
        for (int i = 2; i <= n; i++) {
            result = preFirst + preSecond;
            preFirst = preSecond;
            preSecond = result;
        }
        return result;
    }

    public static void main(String[] args) {
        Solution70 solution = new Solution70();
        System.out.println(solution.climbStairs(2));
        System.out.println(solution.climbStairs(3));
        System.out.println(solution.climbStairs(4));
        System.out.println(solution.climbStairs(5));
    }
}
