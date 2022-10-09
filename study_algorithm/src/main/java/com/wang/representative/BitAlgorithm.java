package com.wang.representative;

/**
 * 位运算
 */
public class BitAlgorithm {

    /**
     * 是否是2的次方
     *  只有一位是1的是2的次方，此时与n-1（1的位之后都是0）为 0
     * @param n 数字
     */
    public boolean is2Power(int n) {
        return (n & (n - 1)) == 0;
    }

    /**
     * 是否是4的次方
     *  先判断是2的次方，4的次方只有一个位是1，且在第1,3,5位置上，那么与010101..010101这个数不为0
     */
    public boolean is4Power(int n) {
        // 0x55555555的位信息为：010101..010101
        return is2Power(n) && (n & 0x55555555) != 0;
    }
}
