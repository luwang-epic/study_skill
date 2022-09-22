package com.wang.leetcode.lt500;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/*
给定一个只包括 '('，')'，'{'，'}'，'['，']' 的字符串 s ，判断字符串是否有效。

有效字符串需满足：

左括号必须用相同类型的右括号闭合。
左括号必须以正确的顺序闭合。
每个右括号都有一个对应的相同类型的左括号。
 

示例 1：

输入：s = "()"
输出：true
示例 2：

输入：s = "()[]{}"
输出：true
示例 3：

输入：s = "(]"
输出：false

 */
public class Solution20 {

    public boolean isValid(String s) {
        if (s == null || s.length() == 0) {
            return true;
        }

        Stack<Character> stack = new Stack<>();
        Map<Character, Character> matchBrackets = new HashMap<Character, Character>() {
            {
                put('(', ')');
                put('[', ']');
                put('{', '}');
            }
        };
        Character ch;
        for (int i = 0; i < s.length(); i++) {
            ch = s.charAt(i);
            if (matchBrackets.containsKey(ch)) {
                stack.push(ch);
            } else if (stack.isEmpty() || matchBrackets.get(stack.pop()) != ch) {
                return false;
            }
        }
        return stack.isEmpty();
    }
}
