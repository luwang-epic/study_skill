package com.wang.leetcode.lt1500;

import java.util.ArrayList;
import java.util.List;

public class Solution1441 {
    public List<String> buildArray(int[] target, int n) {
        List<String> opts = new ArrayList<>();

        int pre = 0;
        int count = 0;
        for (int i = 0; i < target.length; i++) {
            for (int j = pre + 1; j < target[i]; j++) {
                opts.add("Push");
                opts.add("Pop");
                count = count + 2;
            }
            opts.add("Push");
            count++;
            pre = target[i];
        }

        for (int i = count + 1; i <= n; i = i + 2) {
            opts.add("Push");
            opts.add("Pop");
        }

        return opts;
    }

    public static void main(String[] args) {
        Solution1441 solution = new Solution1441();
        int[] target = new int[]{1, 3};
        int n = 3;
        System.out.println(solution.buildArray(target, n));
    }
}
