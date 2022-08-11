package com.wang.leetcode.offer;

/*
实现 pow(x, n) ，即计算 x 的 n 次幂函数（即，xn）。不得使用库函数，同时不需要考虑大数问题。

 

示例 1：

输入：x = 2.00000, n = 10
输出：1024.00000
示例 2：

输入：x = 2.10000, n = 3
输出：9.26100
示例 3：

输入：x = 2.00000, n = -2
输出：0.25000
解释：2-2 = 1/22 = 1/4 = 0.25
 

 */
public class SolutionOffer16 {
    public double myPow(double x, int n) {
        long m = n;
        return m >= 0 ? quickMulWithLoop(x, m) : 1.0 / quickMulWithLoop(x, -m);
    }

    public double quickMul(double x, long n) {
        if (n == 0) {
            return 1;
        }

        double ans = quickMul(x, n / 2);
        return n % 2 == 0 ? ans * ans : ans * ans * x;
    }

    public double quickMulWithLoop(double x, long n) {
        double ans = 1;
        double y = ans;
        while(n > 0) {
            if (n % 2 == 1) {
                y = ans * x;
            }

            ans = ans * y;
            n = n / 2;
        }
        return ans;
    }

    public static void main(String[] args) {
        SolutionOffer16 solution = new SolutionOffer16();
        double x = 3;
        int n = 3;
        double result = solution.myPow(x, n);
        System.out.println(result);
    }
}
