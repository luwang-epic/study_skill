package com.wang.leetcode.offer;

/*

输入两个整数序列，第一个序列表示栈的压入顺序，请判断第二个序列是否为该栈的弹出顺序。假设压入栈的所有数字均不相等。例如，序列 {1,2,3,4,5} 是某栈的压栈序列，序列 {4,5,3,2,1} 是该压栈序列对应的一个弹出序列，但 {4,3,5,1,2} 就不可能是该压栈序列的弹出序列。

 

示例 1：

输入：pushed = [1,2,3,4,5], popped = [4,5,3,2,1]
输出：true
解释：我们可以按以下顺序执行：
push(1), push(2), push(3), push(4), pop() -> 4,
push(5), pop() -> 5, pop() -> 3, pop() -> 2, pop() -> 1
示例 2：

输入：pushed = [1,2,3,4,5], popped = [4,3,5,1,2]
输出：false
解释：1 不能在 2 之前弹出。

 */

import java.util.HashMap;
import java.util.Map;

public class SolutionOffer31 {

    public boolean validateStackSequences(int[] pushed, int[] popped) {
        if (null == pushed || null == popped || popped.length != pushed.length) {
            return false;
        }
        Map<Integer, Integer> valueIndex = new HashMap<>();
        for (int i = 0; i < pushed.length; i++) {
            valueIndex.put(pushed[i], i);
        }

        int[] postValues = new int[pushed.length];
        for (int i = 0; i < popped.length; i++) {
            Integer index = valueIndex.get(popped[i]);
            int size = 0;
            for (int j = i; j < popped.length; j++) {
                Integer postIndex = valueIndex.get(popped[j]);
                if (postIndex < index) {
                    postValues[size++] = popped[j];
                }
            }

            // post是倒叙的
            for (int k = 0; k < size - 1; k++) {
                Integer pre = valueIndex.get(postValues[k]);
                Integer post = valueIndex.get(postValues[k + 1]);
                if (pre < post) {
                    return false;
                }
            }
        }
        return true;
    }

    public static void main(String[] args) {
        SolutionOffer31 solution = new SolutionOffer31();

        int[] pushed = {};
        int[] popped = {};
        System.out.println(solution.validateStackSequences(pushed, popped));

        pushed = new int[]{1,2,3,4,5};
        popped = new int[]{4,5,3,2,1};
        System.out.println(solution.validateStackSequences(pushed, popped));
    }
}
