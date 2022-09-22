package com.wang.leetcode.lt500;

/*
给你一个只包含 '(' 和 ')' 的字符串，找出最长有效（格式正确且连续）括号子串的长度。

 

示例 1：

输入：s = "(()"
输出：2
解释：最长有效括号子串是 "()"
示例 2：

输入：s = ")()())"
输出：4
解释：最长有效括号子串是 "()()"
示例 3：

输入：s = ""
输出：0
 

 */
public class Solution32 {
    public int longestValidParentheses(String s) {
        if (null == s || s.length() == 0) {
            return 0;
        }

        char[] chars = s.toCharArray();
        char[] matchs = new char[chars.length];
        matchs[0] = chars[0];
        int matchPosition = -1;
        for (int i = 0; i < chars.length; i++) {
            matchs[i] = chars[i];
            if (')' == chars[i]) {
                if (matchPosition >= 0 && matchs[matchPosition] == '(') {
                    matchs[matchPosition] = '=';
                    matchs[i] = '=';
                    matchPosition--;
                    while (matchPosition >= 0 && matchs[matchPosition] == '=') {
                        matchPosition--;
                    }
                } else {
                    matchPosition = i;
                }
            } else {
                matchPosition = i;
            }
        }

        int equalCount = 0;
        int maxEqualCount = 0;
        for (int i = 0; i < matchs.length; i++) {
            if (matchs[i] == '=') {
                equalCount++;
                maxEqualCount = Math.max(equalCount, maxEqualCount);
            } else {
                equalCount = 0;
            }
        }
        return maxEqualCount;
    }

    public static void main(String[] args) {
        Solution32 solution = new Solution32();
        System.out.println(solution.longestValidParentheses( "(()())"));

        System.out.println(solution.longestValidParentheses( ")()())"));

        System.out.println(solution.longestValidParentheses( "()(()"));
    }
}
