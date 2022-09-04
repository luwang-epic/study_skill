package com.wang.leetcode.lt1500;

public class Solution1340 {

    public int maxJumps(int[] arr, int d) {
        if (null == arr || arr.length == 0) {
            return 0;
        }

        int[] dp = new int[arr.length];
        for(int i = 0; i < arr.length; i++) {
            dp[i] = 1;
        }

        boolean isContinue = true;
        while (isContinue) {
            int[] copyDp = new int[dp.length];
            for(int i = 0; i < dp.length; i++) {
                copyDp[i] = dp[i];
            }
            for (int i = 0; i < arr.length; i++) {
                for (int j = i - d; j <= i + d; j++) {
                    if (j < 0 || j >= arr.length || i == j) {
                        continue;
                    }
                    boolean isMatch = true;
                    for (int k = Math.min(i, j); k <= Math.max(i, j); k++) {
                        if (k != i && arr[k] >= arr[i]) {
                            isMatch = false;
                            break;
                        }
                    }
                    if (isMatch && dp[i] < 1 + dp[j]) {
                        dp[i] = 1 + dp[j];
                    }
                }
            }

            isContinue = false;
            for (int i = 0; i < dp.length; i++) {
                if (copyDp[i] != dp[i]) {
                    isContinue = true;
                }
            }
        }

        int max = dp[0];
        for(int i = 0; i < dp.length; i++) {
            if (dp[i] > max) {
                max = dp[i];
            }
        }
        return max;
    }


    public static void main(String[] args) {
        Solution1340 solution = new Solution1340();
        int[] arr = {3,3,3,3,3};
        int d = 3;
        System.out.println(solution.maxJumps(arr, d));

        arr = new int[]{66};
        d = 1;
        System.out.println(solution.maxJumps(arr, d));

        arr = new int[]{7,1,7,1,7,1};
        d = 2;
        System.out.println(solution.maxJumps(arr, d));

        arr = new int[]{6,4,14,6,8,13,9,7,10,6,12};
        d = 2;
        System.out.println(solution.maxJumps(arr, d));

        arr = new int[]{7,6,5,4,3,2,1};
        d = 1;
        System.out.println(solution.maxJumps(arr, d));
    }

}
