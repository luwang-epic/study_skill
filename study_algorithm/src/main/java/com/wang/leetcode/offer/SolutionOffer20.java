package com.wang.leetcode.offer;


/*
请实现一个函数用来判断字符串是否表示数值（包括整数和小数）。

数值（按顺序）可以分成以下几个部分：

若干空格
一个 小数 或者 整数
（可选）一个 'e' 或 'E' ，后面跟着一个 整数
若干空格
小数（按顺序）可以分成以下几个部分：

（可选）一个符号字符（'+' 或 '-'）
下述格式之一：
至少一位数字，后面跟着一个点 '.'
至少一位数字，后面跟着一个点 '.' ，后面再跟着至少一位数字
一个点 '.' ，后面跟着至少一位数字
整数（按顺序）可以分成以下几个部分：

（可选）一个符号字符（'+' 或 '-'）
至少一位数字
部分数值列举如下：

["+100", "5e2", "-123", "3.1416", "-1E-16", "0123"]
部分非数值列举如下：

["12e", "1a3.14", "1.2.3", "+-5", "12e+5.4"]
 

示例 1：

输入：s = "0"
输出：true
示例 2：

输入：s = "e"
输出：false
示例 3：

输入：s = "."
输出：false
示例 4：

输入：s = "    .1  "
输出：true
 
 */

public class SolutionOffer20 {

    public boolean isNumber(String s) {
        if (null == s || s.length() == 0) {
            return false;
        }

        char[] chs = s.toCharArray();
        int start = 0;
        int end = chs.length - 1;
        while (start <= end && chs[start] == ' ') {
            start++;
        }

        while (end >= start && chs[end] == ' ') {
            end--;
        }
        if (start > end) {
            return false;
        }

        // '+.'等情况返回false
        if (end - start == 1 && (chs[start] < '0' || chs[start] > '9') && (chs[end] < '0' || chs[end] > '9')) {
            return false;
        }

        int dotIndex = -1;
        int eIndex = -1;
        for (int i = start; i <= end; i++) {
            if (chs[i] == '.') {
                dotIndex = i;
            }
            if (chs[i] == 'e' || chs[i] == 'E') {
                eIndex = i;
            }
        }

        if (eIndex >= 0) {
            if (!isInteger(chs, eIndex + 1, end, true, false)) {
                return false;
            }
            end = eIndex - 1;
        }

        if (dotIndex >= 0) {
            // 只有.
            if (start == dotIndex  && dotIndex == end) {
                return false;
            }

            // 点后面是整数 或者 没有任何数字
            if (dotIndex != end && !isInteger(chs, dotIndex + 1, end, false, false)) {
                return false;
            }

            // 第一个就是. 那么通过， 否则第一个不是，将end移动到.前面一个位置，后续检查是否是整数
            if (dotIndex != start) {
                end = dotIndex - 1;
            } else {
                return true;
            }
        }

        // 不含.或者e的前面部分必须是整数
        if (!isInteger(chs, start, end, true, dotIndex >= 0)) {
            return false;
        }

        return true;
    }

    boolean isInteger(char[] chs, int start, int end, boolean isSign, boolean hasDot) {
        if (start > end || end >= chs.length) {
            return false;
        }
        if (isSign && (chs[start] == '+' || chs[start] == '-')) {
            if (hasDot && start == end) {
                return true;
            }
            return start + 1 <= end ? isInteger(chs, start + 1, end,false, false) : false;
        }

        for (int i = start ; i <= end; i++) {
            if (chs[i] < '0' || chs[i] > '9') {
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) {
        SolutionOffer20 solution = new SolutionOffer20();
        String s = "";
        System.out.println(s + " -----> " + solution.isNumber(s));

        s = " ";
        System.out.println(s + " -----> " + solution.isNumber(s));

        s = "0";
        System.out.println(s + " -----> " + solution.isNumber(s));

        s = "e";
        System.out.println(s + " -----> " + solution.isNumber(s));

        s = ".";
        System.out.println(s + " -----> " + solution.isNumber(s));

        s = "    .1  ";
        System.out.println(s + " -----> " + solution.isNumber(s));

        s = "   3.1416";
        System.out.println(s + " -----> " + solution.isNumber(s));

        s = "5e2";
        System.out.println(s + " -----> " + solution.isNumber(s));

        s = "-123";
        System.out.println(s + " -----> " + solution.isNumber(s));

        s = "-1E-16";
        System.out.println(s + " -----> " + solution.isNumber(s));

        s = "--5";
        System.out.println(s + " -----> " + solution.isNumber(s));

        s = "1a3.14";
        System.out.println(s + " -----> " + solution.isNumber(s));

        s = "12e";
        System.out.println(s + " -----> " + solution.isNumber(s));

        s = "12e";
        System.out.println(s + " -----> " + solution.isNumber(s));

        s = "1.2.3";
        System.out.println(s + " -----> " + solution.isNumber(s));

        s = "12e+5.4";
        System.out.println(s + " -----> " + solution.isNumber(s));

        // 3. 是 true ?
        s = "3.";
        System.out.println(s + " -----> " + solution.isNumber(s));

        s = "+.8";
        System.out.println(s + " -----> " + solution.isNumber(s));

        s = "-.";
        System.out.println(s + " -----> " + solution.isNumber(s));
    }
}
