package com.wang.leetcode.lt500;

/*
给你一个字符串表达式 s ，请你实现一个基本计算器来计算并返回它的值。

注意:不允许使用任何将字符串作为数学表达式计算的内置函数，比如 eval() 。

 

示例 1：

输入：s = "1 + 1"
输出：2
示例 2：

输入：s = " 2-1 + 2 "
输出：3
示例 3：

输入：s = "(1+(4+5+2)-3)+(6+8)"
输出：23
 

提示：

1 <= s.length <= 3 * 105
s 由数字、'+'、'-'、'('、')'、和 ' ' 组成
s 表示一个有效的表达式
'+' 不能用作一元运算(例如， "+1" 和 "+(2 + 3)" 无效)
'-' 可以用作一元运算(即 "-1" 和 "-(2 + 3)" 是有效的)
输入中不存在两个连续的操作符
每个数字和运行的计算将适合于一个有符号的 32位 整数

 */

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

public class Solution224 {
    public int calculate(String s) {
        Deque<Integer> ops = new LinkedList<Integer>();
        ops.push(1);
        int sign = 1;

        int ret = 0;
        int n = s.length();
        int i = 0;
        while (i < n) {
            if (s.charAt(i) == ' ') {
                i++;
            } else if (s.charAt(i) == '+') {
                sign = ops.peek();
                i++;
            } else if (s.charAt(i) == '-') {
                sign = -ops.peek();
                i++;
            } else if (s.charAt(i) == '(') {
                ops.push(sign);
                i++;
            } else if (s.charAt(i) == ')') {
                ops.pop();
                i++;
            } else {
                long num = 0;
                while (i < n && Character.isDigit(s.charAt(i))) {
                    num = num * 10 + s.charAt(i) - '0';
                    i++;
                }
                ret += sign * num;
            }
        }
        return ret;
    }

    public int calculate2(String s) {
        Stack<String> stack = new Stack<>();
        char[] chars = s.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == ' ') {
                continue;
            }

            if (chars[i] == ')') {
                // 计算中间值，并入栈
                int result = calNum(stack);
                stack.push(String.valueOf(result));
            }else {
                stack.push(String.valueOf(chars[i]));
            }
        }
        return calNum(stack);
    }

    private int calNum(Stack<String> stack) {
        List<Integer> nums = new ArrayList<>();
        List<String> symbols = new ArrayList<>();
        boolean isNegative = false;

        String ch;
        int ten = 1;
        int num = 0;
        while (true) {
            if (stack.isEmpty()) {
                break;
            }
            ch = stack.pop();
            if ("(".equals(ch)) {
                break;
            }

            if ("+".equals(ch) || "-".equals(ch)) {
                symbols.add(ch);
            } else {
                num += ten * Integer.parseInt(String.valueOf(ch));
                ten *= 10;
                if (stack.isEmpty() || stack.peek().equals("+") || stack.peek().equals("-") || stack.peek().equals("(")) {
                    nums.add(num);
                    num = 0;
                    ten = 1;
                }
            }
        }

        if (nums.size() == symbols.size()) {
            if ("-".equals(symbols.remove(symbols.size() - 1))) {
                isNegative = true;
            }
        }

        int result = nums.get(symbols.size());
        for (int i = symbols.size() - 1; i >= 0; i--) {
            if ("+".equals(symbols.get(i))) {
                result = result + nums.get(i);
            } else {
                result = result - nums.get(i);
            }
        }
        return isNegative ? -result : result;
    }

    public static void main(String[] args) {
        Solution224 solution = new Solution224();
        String s = "1 + 1";
        System.out.println(solution.calculate(s));

        s = " 2-1 + 2 ";
        System.out.println(solution.calculate(s));

        s = "(1+(4+5+2)-3)+(6+8)";
        System.out.println(solution.calculate(s));

        s = "-2+ 1";
        System.out.println(solution.calculate(s));
    }
}
