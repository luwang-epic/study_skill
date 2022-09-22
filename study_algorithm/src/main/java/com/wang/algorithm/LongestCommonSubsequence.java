package com.wang.algorithm;

/**
 * 最长公共子序列问题:
 *  给定两个字符串，求解这两个字符串的最长公共子序列（Longest Common Sequence）。比如字符串1：BDCABA；字符串2：ABCBDAB
 *  则这两个字符串的最长公共子序列长度为4，最长公共子序列是：BCBA
 *
 * 参考：
 *  力扣：https://leetcode.cn/problems/qJnOS7/solution/zui-chang-gong-gong-zi-xu-lie-by-leetcod-ugg7/
 *  博客：https://www.cnblogs.com/wkfvawl/p/9362287.html
 */
public class LongestCommonSubsequence {

    /*
    假定两个序列为X={x1, x2, ..., xm}和Y={y1, y2, ..., yn)，并设Z={z1, z2, ..., zk}为X和Y的任意一个LCS。
        1）如果xm = yn，则zk = xm=yn，且Zk-1是Xm-1和Yn-1的一个LCS。
        2）如果xm != yn, 则zk != xm蕴含Z是Xm-1和Y得一个LCS。
        3）如果xm != yn, 则zk != yn蕴含Z是X和Yn-1的一个LCS。
     */

    public static int longestCommonSubsequence(String text1, String text2) {
        int m = text1.length(), n = text2.length();
        int[][] dp = new int[m + 1][n + 1];
        for (int i = 1; i <= m; i++) {
            char c1 = text1.charAt(i - 1);
            for (int j = 1; j <= n; j++) {
                char c2 = text2.charAt(j - 1);
                if (c1 == c2) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                }
            }
        }
        return dp[m][n];
    }

    public static void main(String[] args) {
        String text1 = "abcdef";
        String text2 = "ace";
        System.out.println(LongestCommonSubsequence.longestCommonSubsequence(text1, text2));
    }
}
