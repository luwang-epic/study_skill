package com.wang.representative;

/*
异或运算(^)的定义：相同为1，不同为0
具有的特点：
    1. 任何数异或0都是自己, 即：x ^ 0 = x
    2. 偶数个自己异或为0，奇数个自己异或为自己，即：x ^ x = 0  x ^ x ^ x = x
    3. 异或具有交换律，即：x ^ y = y ^ x
    4. 异或具有结合律，即：x ^ y ^ z = x ^ (y ^ z)
 */

import org.junit.jupiter.api.Test;

/**
 * 演示异或（XOR）运算的典型应用
 */
public class XorProblem {

    @Test
    public void prepareSkills() {
        // 提取出一个不为0的数的最后一位1
        int x = 10;
        int rightestOne = x & (~x + 1);
        System.out.println(rightestOne);
        // 0的话还是自己
        System.out.println(0 & (~0 + 1));

        // 交互两个数
        int a = 10, b = 11;
        a = a ^ b;
        b = a ^ b;
        a = a ^b;
        System.out.println("a = " + a + ", b = " + b);
        // 这种交互需要时两个变量，否则会出错，也就是说如果交互数据i,j位置的数，需要保证i!=j
    }

    /**
     * 给出一个数组，只有一个数是奇数个，其他的都是偶数个
     *  找出唯一的奇数个数字
     */
    @Test
    public void findOnlyOddNumberInArrayDemo() {
        int[] nums = new int[] {1, 2, 3, 3, 2, 1, 1};
        System.out.println(findOnlyOddNumberInArray(nums));
    }

    /**
     * 给出一个数组，只有两个数是奇数个，其他的都是偶数个
     *  找出唯二的奇数个数字
     */
    @Test
    public void findTwoOddNumberInArrayDemo() {
        int[] nums = new int[] {1, 2, 3, 3, 2, 1, 1, 4};
        int[] twoOddNumbers = findTwoOddNumberInArray(nums);
        System.out.println(twoOddNumbers[0] + " " + twoOddNumbers[1]);
    }

    public int findOnlyOddNumberInArray(int[] nums) {
        if (null == nums || nums.length == 0) {
            return 0;
        }

        int result = nums[0];
        for (int i = 1; i < nums.length; i++) {
            result = result ^ nums[i];
        }

        return result;
    }

    public int[] findTwoOddNumberInArray(int[] nums) {
        if (null == nums || nums.length < 2) {
            return null;
        }

        int eor = 0;
        for (int i = 0; i < nums.length; i++) {
            eor = eor ^ nums[i];
        }
        // eor = a ^ b (a, b是数组中唯二的奇数)
        // 因为 a != b，那么 eor != 0, 必然有一位是1
        // 下面的计算结果为 0000001000
        int rightOne = eor & (~eor + 1); // 找出1的位置

        int oneOdd = 0;
        for(int num : nums) {
            // 将数组分为两部分，指定位置为1，和指定位置不为1的，
            // 那么两个奇数必定有一个这个位置为0，一个这个位置不为0，否则a ^ b这个位置就一定是0了
            // 如此只需要和位置是1的再次异或，就可以得到其中一个奇数个数的数了
            if ((num & rightOne) == 0) {
                oneOdd = oneOdd ^ num;
            }
        }

        // 找出一个奇数a后，另一个奇数就是 eor ^ oneOdd = a ^ b ^ a = b
        return new int[] {oneOdd, eor ^ oneOdd};
    }

}
