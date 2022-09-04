package com.wang.leetcode.lt1500;

/*
给定两个字符串 text1 和 text2，返回这两个字符串的最长 公共子序列 的长度。如果不存在 公共子序列 ，返回 0 。

一个字符串的 子序列 是指这样一个新的字符串：它是由原字符串在不改变字符的相对顺序的情况下删除某些字符（也可以不删除任何字符）后组成的新字符串。

例如，"ace" 是 "abcde" 的子序列，但 "aec" 不是 "abcde" 的子序列。
两个字符串的 公共子序列 是这两个字符串所共同拥有的子序列。

 

示例 1：

输入：text1 = "abcde", text2 = "ace"
输出：3
解释：最长公共子序列是 "ace" ，它的长度为 3 。
示例 2：

输入：text1 = "abc", text2 = "abc"
输出：3
解释：最长公共子序列是 "abc" ，它的长度为 3 。
示例 3：

输入：text1 = "abc", text2 = "def"
输出：0
解释：两个字符串没有公共子序列，返回 0 。

 */

public class Solution1143 {
    public int longestCommonSubsequence(String text1, String text2) {
        if (null == text1 || null == text2 || text1.length() == 0 || text2.length() == 0) {
            return 0;
        }

        int n = text1.length();
        int m = text2.length();
        int[][] dp = new int[n][m];
        for (int i = 0; i < n; i++) {
            if (text1.substring(0, i+1).indexOf(text2.charAt(0)) >= 0) {
                dp[i][0] = 1;
            }
        }
        for (int i = 0; i < m; i++) {
            if (text2.substring(0, i+1).indexOf(text1.charAt(0)) >= 0) {
                dp[0][i] = 1;
            }
        }

        for (int i = 0; i < n; i++) {
            for (int j = 1; j < m; j++) {
                char ch = text2.charAt(j);
                for (int k = 0; k < n; k++) {
                    if (text1.charAt(k) == ch) {
                        dp[i][j] = Math.max(dp[i][j], dp[k][j-1] + 1);
                    } else {
                        dp[i][j] = Math.max(dp[i][j], dp[k][j]);
                    }
                }
            }
        }

        return dp[n-1][m-1];
    }

    public static void main(String[] args) {
        Solution1143 solution = new Solution1143();
        String text1 = "abcde";
        String text2 = "ace";
        System.out.println(solution.longestCommonSubsequence(text1, text2));

        text1 = "bl";
        text2 = "yby";
        System.out.println(solution.longestCommonSubsequence(text1, text2));

    }
}
