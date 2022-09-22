package com.wang.algorithm;

import java.util.HashMap;
import java.util.Map;

/**
 * 一个字符串，求这个字符串中不包含重复字符的最长子串的长度，
 * 如abba返回2，aaaaabc返回3，bbbbbbb返回1，等等上面是测试用例
 */
public class LongestDistinctSubString {

    /*
    思路是这样的这次要以一个hashmap作为辅助，map的key存储的是字符，value存储的是该字符当前的位置，
    首先设置一个头指针，指向字符串开头，那么从开始遍历字符串，如果map当中不包含这个字符，
    那么用这个字符当前所在的位置减去头指针的位置，然后与最大长度做比较，选打的成为最大长度，
    然后把当前字符的以及位置放入map
     */

    public static int longestDistinctSubString(String s) {
        Map<Character, Integer> charIndexMap = new HashMap<>();
        int maxLength = 0;
        int now = 0;
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (charIndexMap.containsKey(ch)) {
                now = Math.max(charIndexMap.get(ch) + 1, now);
            }
            maxLength = Math.max(i - now + 1, maxLength);
            charIndexMap.put(ch, i);
        }
        return maxLength;
    }

    public static void main(String[] args) {
        String s = "abba";
        System.out.println(longestDistinctSubString(s));

        s = "abbad";
        System.out.println(longestDistinctSubString(s));
    }
}
