package com.wang.leetcode.lt50;

/*
给定一个字符串 s ，请你找出其中不含有重复字符的 最长子串 的长度。

 

示例 1:

输入: s = "abcabcbb"
输出: 3
解释: 因为无重复字符的最长子串是 "abc"，所以其长度为 3。
示例 2:

输入: s = "bbbbb"
输出: 1
解释: 因为无重复字符的最长子串是 "b"，所以其长度为 1。
示例 3:

输入: s = "pwwkew"
输出: 3
解释: 因为无重复字符的最长子串是 "wke"，所以其长度为 3。
     请注意，你的答案必须是 子串 的长度，"pwke" 是一个子序列，不是子串。

 */
public class Solution3 {
    public int lengthOfLongestSubstring(String s) {
        if (null == s || s.length() == 0) {
            return 0;
        }

        int maxSubLength = 0;//" "
        String subStr = "";
        for (int i = 0; i < s.length(); i++) {
            int index = subStr.indexOf(s.charAt(i));
            if (index > -1) {
                if (maxSubLength < subStr.length()) {
                    maxSubLength =subStr.length();
                }
                subStr = index == subStr.length() - 1 ? "" : subStr.substring(index + 1);
            }
            subStr += s.charAt(i);
        }
        return Math.max(maxSubLength, subStr.length());
    }

    public static void main(String[] args) {
        Solution3 solution = new Solution3();
        System.out.println(solution.lengthOfLongestSubstring(" "));
    }
}