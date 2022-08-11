package com.wang.leetcode;

/*
编写一个函数来查找字符串数组中的最长公共前缀。

如果不存在公共前缀，返回空字符串 ""。

 

示例 1：

输入：strs = ["flower","flow","flight"]
输出："fl"
示例 2：

输入：strs = ["dog","racecar","car"]
输出：""
解释：输入不存在公共前缀。

 */

public class Solution14 {
    public String longestCommonPrefix(String[] strs) {
        String prefix = "";
        if (null == strs || strs.length == 0) {
            return prefix;
        }

        int minLen = strs[0].length();
        for (int i = 0; i < strs.length; i++) {
            minLen = Math.min(minLen, strs[i].length());
        }

        for (int i = 0; i < minLen; i++) {
            char ch = strs[0].charAt(i);
            boolean isPrefix = true;
            for (int j = 1; j < strs.length; j++) {
                if (strs[j].charAt(i) != ch) {
                    isPrefix = false;
                    break;
                }
            }
            if (!isPrefix) {
                break;
            }
            prefix += ch;
        }
        return prefix;
    }

    public static void main(String[] args) {
        Solution14 solution = new Solution14();
        String[] strs = new String[]{"flower","flow","flight"};
        System.out.println(solution.longestCommonPrefix(strs));
    }
}
