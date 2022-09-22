package com.wang.algorithm;

/**
 * KMP 算法是一个十分高效的字符串查找算法，目的是在一个字符串 s 中，查询 s 是否包含子字符串 p，
 * 若包含，则返回 p 在 s 中起点的下标。
 * 参考：https://blog.csdn.net/qq_39183034/article/details/115139682
 *      https://blog.csdn.net/weixin_52622200/article/details/110563434
 */
public class KmpAlgorithm {

    /**
     *
     * @param str 主串
     * @param pattern 模式字符串
     * @return 起始下标
     */
    public int kmp(String str, String pattern) {
        char[] s = str.toCharArray();
        char[] p = pattern.toCharArray();
        int[] next = genNext(pattern);

        int i = 0;
        int j = 0;
        while (i < s.length && j < p.length) {
            if (j == -1 || s[i] == p[j]) {
                i++;
                j++;
            } else {
                j = next[j];
            }
        }

        if (j >= p.length) {
            return i - p.length;
        }
        return -1;
    }

    private int[] genNext(String pattern) {
        char[] p = pattern.toCharArray();
        int[] next = new int[p.length];
        next[0] = -1;

        int j = 0;
        int k = -1;
        while (j < p.length - 1) {
            if (k == -1 || p[j] == p[k]) {
                j++;
                k++;
                next[j] = k;
            } else {
                k = next[k];
            }
        }
        return next;
    }

    public static void main(String[] args) {
        KmpAlgorithm kmpAlgorithm = new KmpAlgorithm();
        String s = "abacbcdhijk";
        String p = "abad";
        System.out.println(kmpAlgorithm.kmp(s, p));

        s = "abcabcdef";
        p = "abcd";
        System.out.println(kmpAlgorithm.kmp(s, p));
    }
}
