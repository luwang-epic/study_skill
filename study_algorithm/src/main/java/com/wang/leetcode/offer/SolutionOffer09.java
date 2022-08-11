package com.wang.leetcode.offer;

import java.util.Stack;

public class SolutionOffer09 {
    Stack<Integer> s1 = new Stack<>();
    Stack<Integer> s2 = new Stack<>();

    public SolutionOffer09() {

    }

    public void appendTail(int value) {
        s1.push(value);
    }

    public int deleteHead() {
        // o(n)复杂度
//        if (s1.isEmpty()) {
//            return -1;
//        }
//
//        while(!s1.isEmpty()) {
//            s2.push(s1.pop());
//        }
//        Integer head = s2.pop();
//        while (!s2.isEmpty()) {
//            s1.push(s2.pop());
//        }
//        return head;

        // 不需要每次都这样操作，当s2为空时再压入  这样的复杂度为o(1)
        if (s2.isEmpty()) {
            if (s1.isEmpty()) {
                return -1;
            }
            while (!s1.isEmpty()) {
                s2.push(s1.pop());
            }
        }
        return s2.pop();
    }
}
