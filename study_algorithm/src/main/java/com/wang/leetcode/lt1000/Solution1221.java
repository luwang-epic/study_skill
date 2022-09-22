package com.wang.leetcode.lt1000;

/*
在一个 平衡字符串 中，'L' 和 'R' 字符的数量是相同的。

给你一个平衡字符串 s，请你将它分割成尽可能多的平衡字符串。

注意：分割得到的每个字符串都必须是平衡字符串，且分割得到的平衡字符串是原平衡字符串的连续子串。

返回可以通过分割得到的平衡字符串的 最大数量 。

 

示例 1：

输入：s = "RLRRLLRLRL"
输出：4
解释：s 可以分割为 "RL"、"RRLL"、"RL"、"RL" ，每个子字符串中都包含相同数量的 'L' 和 'R' 。
示例 2：

输入：s = "RLLLLRRRLR"
输出：3
解释：s 可以分割为 "RL"、"LLLRRR"、"LR" ，每个子字符串中都包含相同数量的 'L' 和 'R' 。
示例 3：

输入：s = "LLLLRRRR"
输出：1
解释：s 只能保持原样 "LLLLRRRR".
示例 4：

输入：s = "RLRRRLLRLL"
输出：2
解释：s 可以分割为 "RL"、"RRRLLRLL" ，每个子字符串中都包含相同数量的 'L' 和 'R' 。
 

 */

public class Solution1221 {

    public int balancedStringSplit(String s) {
        if (null == s || s.length() == 0) {
            return 0;
        }

        int maxCount = 0;
        int lCount = 0;
        int rCount = 0;
        char[] chars = s.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if ('L' == chars[i]) {
                lCount++;
            } else {
                rCount++;
            }

            if (lCount > 0 && lCount == rCount) {
                maxCount++;
                lCount = 0;
                rCount = 0;
            }
        }

        return maxCount;
    }

    public static void main(String[] args) {
        Solution1221 solution = new Solution1221();
        String s = "RLRRLLRLRL";
        System.out.println(solution.balancedStringSplit(s));

        s = "RLLLLRRRLR";
        System.out.println(solution.balancedStringSplit(s));

        s = "LLLLRRRR";
        System.out.println(solution.balancedStringSplit(s));

        s = "RLRRRLLRLL";
        System.out.println(solution.balancedStringSplit(s));
    }
}
