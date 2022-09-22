package com.wang.leetcode.lt500;

/*
给定一个非空的字符串 s ，检查是否可以通过由它的一个子串重复多次构成。

 

示例 1:

输入: s = "abab"
输出: true
解释: 可由子串 "ab" 重复两次构成。
示例 2:

输入: s = "aba"
输出: false
示例 3:

输入: s = "abcabcabcabc"
输出: true
解释: 可由子串 "abc" 重复四次构成。 (或子串 "abcabc" 重复两次构成。)

 */
public class Solution459 {

    public boolean repeatedSubstringPattern(String s) {
        if (null == s || s.length() == 0) {
            return false;
        }

        char[] repeatChars = new char[s.length()];
        char[] chars = s.toCharArray();
        int first = 0;
        int repeatLength = 0;
        while (first <= chars.length / 2) {
            if (repeatLength == 0 || chars[first] != repeatChars[0]) {
                repeatChars[repeatLength++] = chars[first++];
                continue;
            }

            boolean isMatch = true;
            // 开始匹配
            for (int i = 0; i < repeatLength; i++) {
                // 结束还是匹配不了，直接返回false
                if (first + i >= chars.length) {
                    return false;
                }
                // 不匹配，加入到repeatChars中，继续
                if (repeatChars[i] != chars[first + i]) {
                    isMatch = false;
                    break;
                }
            }
            if (!isMatch) {
                repeatChars[repeatLength++] = chars[first++];
                continue;
            }

            // 如果匹配，检查后续是否都匹配
            int remainingLength = chars.length - 2 * repeatLength;
            // 剩余字符串不能匹配
            if (remainingLength % repeatLength != 0) {
                repeatChars[repeatLength++] = chars[first];
                first++;
                continue;
            }

            for (int i = 2 * repeatLength; i < chars.length; i++) {
                // 不能匹配，跳出循环
                if (repeatChars[i % repeatLength] != chars[i]) {
                    isMatch = false;
                    break;
                }
            }

            // 可以匹配，直接返回
            if (isMatch) {
                return true;
            }
            repeatChars[repeatLength++] = chars[first++];
        }

        return false;
    }

    public static void main(String[] args) {
        Solution459 solution = new Solution459();
        System.out.println(solution.repeatedSubstringPattern("abab"));

        System.out.println(solution.repeatedSubstringPattern("aba"));

        System.out.println(solution.repeatedSubstringPattern("abcabcabcabc"));

        System.out.println(solution.repeatedSubstringPattern("abababaaba"));
    }
}
