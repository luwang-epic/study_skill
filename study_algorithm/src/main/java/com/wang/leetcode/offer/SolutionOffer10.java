package com.wang.leetcode.offer;

/*
一只青蛙一次可以跳上1级台阶，也可以跳上2级台阶。求该青蛙跳上一个 n 级的台阶总共有多少种跳法。

答案需要取模 1e9+7（1000000007），如计算初始结果为：1000000008，请返回 1。

示例 1：

输入：n = 2
输出：2
示例 2：

输入：n = 7
输出：21
示例 3：

输入：n = 0
输出：1

 */

public class SolutionOffer10 {

    public int numWays(int n) {
        // 会有越界问题，所以先取模再进行后面的计算
//        if (n == 0 || n == 1) {
//            return 1;
//        }
//
//        long x = 1;
//        long y = 1;
//        long z = x + y;
//        long m = 1;
//        while (m < n) {
//            z = x + y;
//            x = y;
//            y = z;
//            m++;
//        }
//        return (int) (z % 1000000007);


        int a = 1, b = 1, sum;
        for(int i = 0; i < n; i++){
            sum = (a + b) % 1000000007;
            a = b;
            b = sum;
        }
        return a;
    }

    public static void main(String[] args) {
        SolutionOffer10 solution = new SolutionOffer10();
        System.out.println("n=1, ---> " + solution.numWays(1));
        System.out.println("n=2, ---> " + solution.numWays(2));
        System.out.println("n=3, ---> " + solution.numWays(3));
        System.out.println("n=4, ---> " + solution.numWays(4));
        System.out.println("n=5, ---> " + solution.numWays(5));
        System.out.println("n=6, ---> " + solution.numWays(6));
        System.out.println("n=7, ---> " + solution.numWays(7));
        System.out.println("n=46, ---> " + solution.numWays(46));
        System.out.println("n=92, ---> " + solution.numWays(92));
    }

}
