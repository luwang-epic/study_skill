package com.wang.leetcode.lt500;

/*
实现 pow(x, n) ，即计算 x 的整数 n 次幂函数（即，xn ）。

 

示例 1：

输入：x = 2.00000, n = 10
输出：1024.00000
示例 2：

输入：x = 2.10000, n = 3
输出：9.26100
示例 3：

输入：x = 2.00000, n = -2
输出：0.25000

 */
public class Solution50 {
    public double myPow(double x, int n) {
        if (n == 0) {
            return 1.0;
        }

        boolean isPositive = n > 0;
        double result = 1.0;
        double contribute = x;
        n = Math.abs(n);
        while (n > 0) {
            if (n % 2 == 1) {
                result = result * contribute;
            }
            contribute *= contribute;
            n = n / 2;
        }
        return isPositive ? result : 1 / result;
    }

    public double myPow2(double x, int n) {
        long N = n;
        return N >= 0 ? quickMul(x, N) : 1.0 / quickMul(x, -N);
    }

    public double quickMul(double x, long N) {
        if (N == 0) {
            return 1.0;
        }
        double y = quickMul(x, N / 2);
        return N % 2 == 0 ? y * y : y * y * x;
    }

    public static void main(String[] args) {
        Solution50 solution = new Solution50();
        System.out.println(solution.myPow(2, 2));

        System.out.println(solution.myPow(10, -5));

        System.out.println(solution.myPow(8.84372, -5));
        System.out.println(solution.myPow2(8.84372, -5));
    }
}
