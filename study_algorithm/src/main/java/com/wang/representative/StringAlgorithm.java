package com.wang.representative;

import org.junit.jupiter.api.Test;

public class StringAlgorithm {

    /**
     * 求一个字符串的最大回文子串
     */
    @Test
    public void maxPalindromeSubStringDemo() {
        String str = "abccbade";
        System.out.println(maxPalindromeSubString(str));
        System.out.println(manacher(str));
    }

    @Test
    public void kmpDemo() {
        String s = "abacbcdhijk";
        String p = "abad";
        System.out.println(kmp(s, p));

        s = "abcabcdef";
        p = "abcd";
        System.out.println(kmp(s, p));
    }

    /*
    回文的经典求法，从某一个字符开始向左右扩充，但是这样只能求出奇数个回文的字符串，
    求不出偶数个对称的字符串，因此需要加入相同的字符(随便一个字符)插入到原始串中，在进行扩充，
    最后再去除（长度/2就是原来的长度），时间复杂度为O(N^2)
     */
    public int maxPalindromeSubString(String str) {
        // 这里就不些经典求法了
        return 0;
    }

    /**
     * manacher算法可以在O(N)的时间复杂度中求出最大回文子串
     * 定义扩充到的最左边为L，最右边为R，其中心为 C，第i的回文半径为ir,需要记录下来
     * 如果现在遍历到第j个
     *  1. 如果j > R暴力往后阔
     *      根据情况更新LR和C的值
     *  2. 如果j<=R，又分为3种情况
     *      a. 如果j根据C对称的j'的半径 r 的左边界在L内，那么j的回文半径和j'一样
     *      b. 如果j根据C对称的j'的半径 r 的左边界在L外，那么j的回文半径为R-j
     *      c. 如果j根据C对称的j‘的半径 r 的左边界在L上（和L相等），那么j的回文半径需要从R往后阔，
     *          根据阔后是否满足回文来确定半径，并更新LR和C的值
     * @param str 字符串
     * @return 回文串的长度
     */
    public int manacher(String str) {
        if (null == str || str.length() == 0) {
            return 0;
        }

        // 特殊处理之后的字符串，加入#这个字符，解决偶数个回文问题
        char[] chars = new char[2 * str.length() + 1];
        chars[chars.length - 1] = '#';
        int index = 0;
        for (int i = 0; i < str.length(); i++) {
            chars[index++] = '#';
            chars[index++] = str.charAt(i);
        }

        int[] radius = new int[chars.length];
        // 生成r的中心
        int center = -1;
        // 回文右边界的位置  // + 1， right-1为回文右边界，方便后面计算
        int right = -1;
        // 每个位置求回文半径，然后再求出最大值
        for (int i = 0; i < str.length(); i++) {
            // 第一种 > right
            if (i > right) {
                // 扩充
                int k = 1;
                while (i + k < chars.length && i - k > -1) {
                    // 如果有不相等的，那么跳出
                    if (chars[i + k] == chars[i - k]) {
                        k++;
                    } else {
                        break;
                    }
                }
                if (i + k - 1 > right) {
                    radius[i] = k;
                    right = i + k - 1;
                    center = i;
                } else {
                    // 不能阔，只有自己，半径为1
                    radius[i] = 1;
                }
            } else {
                // 分为3种情况

                // i关于center对称的点i'
                int li = 2 * center - i;
                // 在LR的里面
                if (li - radius[li] + 1 > 2 * center - right) {
                    radius[i] = radius[li];
                }
                // 在LR的外面
                else if (li - radius[li] + 1 < 2 * center - right) {
                    radius[i] = right - i + 1;
                }
                // 在LR的上面
                else {
                    // 扩充
                    int k = right - i + 1;
                    while (i + k < chars.length && i - k > -1) {
                        // 如果有不相等的，那么跳出
                        if (chars[i + k] == chars[i - k]) {
                            k++;
                        } else {
                            break;
                        }
                    }
                    if (i + k > right) {
                        radius[i] = k;
                        right = i + k - 1;
                        center = i;
                    } else {
                        // 不能阔，那么i的右边界就是到right了
                        radius[i] = right - i;
                    }
                }
            }
        }

        int max = 0;
        for (int i = 0; i < radius.length; i++) {
            max = Math.max(max, radius[i]);
        }
        // 因为填充了字符，所以原始字符串的回文子串长度为 回文半径 - 1 （也就是回文直径/2）
        return max - 1;
    }


     /**
      * KMP 算法是一个十分高效的字符串查找算法，目的是在一个字符串 s 中，查询 s 是否包含子字符串 t
      * next数组的定义在kmpNextArray方法中，我们可以不会退s字符串的指针，根据next数组来回退t的指针
      * 回退到next数组中对应的位置，如果为-1，从0开始匹配；为什么可以这么做，有两种情况
      *     1. 为什么可以直接从next数组的位置开始匹配，
      *         因为后缀一样的，所以s指针不需要动的，next后缀和前缀最大的大小
      *     2. 为什么不能从0 - next中间的位置开始（或者 0 ~ t字符串指针位置-next的位置开始），
      *         因为中间的位置肯定不匹配，否则和next数组中记录的最大相同前缀和后缀冲突（这里画图比较好理解）
      * @param src 源字符串
      * @param target 目标字符串
      * @return 若包含，则返回 t 在 s 中起点的下标；否则返回-1
     */
    public int kmp(String src, String target) {
        if (null == src || null == target || target.length() < 1 || src.length() < target.length()) {
            return -1;
        }

        char[] srcChars = src.toCharArray();
        char[] targetChars = target.toCharArray();

        int[] next = kmpNextArray(target);
        int s = 0;
        int t = 0;
        while (s < srcChars.length && t < targetChars.length) {
            // 都相等，往后匹配
            if (srcChars[s] == targetChars[t]) {
                s++;
                t++;
            }
            // 等于-1，说明中间的都不能匹配，从下一个字符开始匹配
            // -1说明这个时候t已经来到了0位置，即下面的等价于 t == 0，
            // 此时需要src的下一个位置s+1 和 target的0位置(t)开始往后匹配
            else if (next[t] == -1) {
                s++;
            }
            // 否则t从next数组中的位置开始匹配，
            // 因为前缀从0开始，所以最大前后缀的长度，就是下一个需要匹配的位置
            else {
                t = next[t];
            }
        }

        // target匹配到最后了，说明找到了，否则没有匹配到，返回-1
        return t == targetChars.length ? s - t : -1;
    }

    /**
     * kmp算法的next数组
     *  某个字符i的前缀： 以0开始的字符串；后缀定义为：以i-1结束的字符串
     *  next数组存放target串中字符最大相同前缀和后缀相同的个数（不能重叠），
     *      例如：abcabcd，则：d字符的前缀abc和d的后缀abc一样，那么最大长度为3
     *  可以利用前一个位置，求后一个位置的最大前缀和后缀相同
     *      后一个只会比前一个最多大1（看下一个字符是否和自己相等），否则前一个不是最大相同前缀和后缀
     *          1. 相等，则为next[i-1] + 1
     *          2. 不相等，则看next[i-1]对应的值的字符串是否和i相等，
     *              依此继续，直到为0，否则前一个不是最大相同前缀和后缀
     * @return 返回next数组
     */
    public int[] kmpNextArray(String target) {
        char[] chars = target.toCharArray();
        int[] next = new int[chars.length];
        next[0] = -1;
        next[1] = 0;

        // next数组的位置
        int i = 2;
        int k = 0;
        // 从2开始依此求，
        while (i < chars.length - 1) {
            if (chars[i - 1] == chars[k]) {
                i++;
                // 相等，需要和下一个位置比较
                k++;
                next[i] = k;
            }
            // 当前跳到的k位置的字符，和i-1位置的字符匹配不上
            // 需要回退到next[k]的位置继续匹配，直到k为0
            else if (k > 0) {
                // 不相等，直接使用next数组
                k = next[k];
            }
            // k = 0 说明没有可以匹配的，此时i没有公共前缀和后缀，为0；i向后移动，继续后续的求解
            else {
                next[i] = 0;
                i++;
            }
        }
        return next;
    }
}
