package com.wang.leetcode;

/*
给你一个字符串 s 和一个整数 repeatLimit ，用 s 中的字符构造一个新字符串 repeatLimitedString ，使任何字母 连续 出现的次数都不超过 repeatLimit 次。你不必使用 s 中的全部字符。

返回 字典序最大的 repeatLimitedString 。

如果在字符串 a 和 b 不同的第一个位置，字符串 a 中的字母在字母表中出现时间比字符串 b 对应的字母晚，则认为字符串 a 比字符串 b 字典序更大 。如果字符串中前 min(a.length, b.length) 个字符都相同，那么较长的字符串字典序更大。

 

示例 1：

输入：s = "cczazcc", repeatLimit = 3
输出："zzcccac"
解释：使用 s 中的所有字符来构造 repeatLimitedString "zzcccac"。
字母 'a' 连续出现至多 1 次。
字母 'c' 连续出现至多 3 次。
字母 'z' 连续出现至多 2 次。
因此，没有字母连续出现超过 repeatLimit 次，字符串是一个有效的 repeatLimitedString 。
该字符串是字典序最大的 repeatLimitedString ，所以返回 "zzcccac" 。
注意，尽管 "zzcccca" 字典序更大，但字母 'c' 连续出现超过 3 次，所以它不是一个有效的 repeatLimitedString 。
示例 2：

输入：s = "aababab", repeatLimit = 2
输出："bbabaa"
解释：
使用 s 中的一些字符来构造 repeatLimitedString "bbabaa"。
字母 'a' 连续出现至多 2 次。
字母 'b' 连续出现至多 2 次。
因此，没有字母连续出现超过 repeatLimit 次，字符串是一个有效的 repeatLimitedString 。
该字符串是字典序最大的 repeatLimitedString ，所以返回 "bbabaa" 。
注意，尽管 "bbabaaa" 字典序更大，但字母 'a' 连续出现超过 2 次，所以它不是一个有效的 repeatLimitedString 。

 */

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Solution2182 {

    public String repeatLimitedString(String s, int repeatLimit) {
        if (null == s || s.length() == 0) {
            return s;
        }
        if (repeatLimit < 1) {
            return "";
        }

        Map<Character, Integer> charNumMap = new TreeMap<>((o1, o2) -> 0 - Character.compare(o1, o2));
        for (int i = 0; i < s.length(); i++) {
            charNumMap.compute(s.charAt(i), (key, value) -> null == value ? 1 : value + 1);
        }

        StringBuilder sb = new StringBuilder();
        List<Map.Entry<Character, Integer>> charNumList = new ArrayList<>(charNumMap.entrySet());
        int prePos = 0;
        int preNum = 0;
        int currentPos = 0;
        int currentNum = charNumList.get(currentPos).getValue();
        while (currentPos < charNumList.size()) {
            if (preNum > 0) {
                if (currentNum < 1) {
                    if (currentPos == charNumList.size() - 1) {
                        break;
                    }
                    currentPos++;
                    currentNum = charNumList.get(currentPos).getValue();
                }
                currentNum = addChar(sb, charNumList.get(currentPos).getKey(), currentNum, 1);
                preNum = addChar(sb, charNumList.get(prePos).getKey(), preNum, repeatLimit);
            } else {
                currentNum = addChar(sb, charNumList.get(currentPos).getKey(), currentNum, repeatLimit);
                preNum = currentNum;
                prePos = currentPos;
                currentPos++;
                if (currentPos < charNumList.size()) {
                    currentNum = charNumList.get(currentPos).getValue();
                }
            }
        }
        return sb.toString();
    }

    private int addChar(StringBuilder sb, char ch, int num, int repeatLimit) {
        int repeat = Math.min(num, repeatLimit);
        addChar(sb, ch, repeat);
        return num - repeat;
    }

    private void addChar(StringBuilder sb, char ch, int repeat) {
        for (int i = 0; i < repeat; i++) {
            sb.append(ch);
        }
    }


    public static void main(String[] args) {
        Solution2182 solution = new Solution2182();
        String s = "cczazcc";
        int repeatLimit = 3;
        System.out.println(solution.repeatLimitedString(s, repeatLimit));

        s = "aababab";
        repeatLimit = 2;
        System.out.println(solution.repeatLimitedString(s, repeatLimit));

        s = "xyutfpopdynbadwtvmxiemmusevduloxwvpkjioizvanetecnuqbqqdtrwrkgt";
        repeatLimit = 1;
        System.out.println(solution.repeatLimitedString(s, repeatLimit));
    }

}
