package com.wang.jdk.grammar;

import org.junit.jupiter.api.Test;

/**
 * integer相关的
 */
public class IntegerSample {

    @Test
    public void IntegerCache() {
        Integer x = 5;
        int y = 5;
        // 自动拆箱和装箱，所以相等
        System.out.println(x == y); // true

        Integer i1 = 10;
        // 会解析成i2 = Integer.valueOf(10)
        Integer i2 = 10;
        // 通过valueOf获取对象，即从缓存IntegerCache中拿对象，
        // 该缓存会缓存-128~127之间的数字，因此都是同一个对象
        System.out.println(i1 == i2); // true

        Integer i3 = 128;
        Integer i4 = 128;
        // 超出了缓存范围，会在堆中new对象，因此是两个不同的对象
        System.out.println(i3 == i4); // false
    }


}
