package com.wang.leetcode.offer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/*
输入一个字符串，打印出该字符串中字符的所有排列。

 

你可以以任意顺序返回这个字符串数组，但里面不能有重复元素。

 

示例:

输入：s = "abc"
输出：["abc","acb","bac","bca","cab","cba"]

 */
public class SolutionOffer38 {

    public String[] permutation(String s) {
        if (null == s || s.length() == 0) {
            return new String[0];
        }

        return permutationWithList(s).toArray(new String[0]);
    }

    Set<String> permutationWithList(String s) {
        Set<String> results = new HashSet<>();
        if (s.length() == 1) {
            results.add(s);
            return results;
        }

        Set<String> preResults = permutationWithList(s.substring(1));
        char ch = s.charAt(0);
        for (String temp : preResults) {
            results.add(ch + temp);
            for (int i = 0; i < temp.length() - 1; i++) {
                results.add(temp.substring(0, i + 1) + ch + temp.substring(i + 1));
            }
            results.add(temp + ch);
        }
        return results;
    }

    public static void main(String[] args) {
        SolutionOffer38 solution = new SolutionOffer38();
        String s = "abc";
        System.out.println(Arrays.stream(solution.permutation(s)).collect(Collectors.toList()));
    }

}
