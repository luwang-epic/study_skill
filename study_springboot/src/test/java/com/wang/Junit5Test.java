package com.wang;

import org.junit.jupiter.api.*;

import java.util.concurrent.TimeUnit;

public class Junit5Test {
    /**
     * 测试前置条件
     */
    @DisplayName("测试前置条件")
    @Test
    void testAssumptions(){
        Assumptions.assumeTrue(false,"结果不是true");
        System.out.println("111111");

    }



    /**
     * 断言：前面断言失败，后面的代码都不会执行
     */
    @DisplayName("测试简单断言")
    @Test
    void testSimpleAssertions() {
        int cal = cal(3, 2);
        //相等
        Assertions.assertEquals(6, cal, "业务逻辑计算失败");
        Object obj1 = new Object();
        Object obj2 = new Object();
        Assertions.assertSame(obj1, obj2, "两个对象不一样");

    }

    @Test
    @DisplayName("array assertion")
    void array() {
        Assertions.assertArrayEquals(new int[]{1, 2}, new int[]{1, 2}, "数组内容不相等");
    }

    @Test
    @DisplayName("组合断言")
    void all() {
        /**
         * 所有断言全部需要成功
         */
        Assertions.assertAll("test",
                () -> Assertions.assertTrue(true && true, "结果不为true"),
                () -> Assertions.assertEquals(1, 2, "结果不是1"));

        System.out.println("=====");
    }

    @DisplayName("异常断言")
    @Test
    void testException() {

        //断定业务逻辑一定出现异常
        Assertions.assertThrows(ArithmeticException.class, () -> {
            int i = 10 / 2;
        }, "业务逻辑居然正常运行？");
    }

    @DisplayName("快速失败")
    @Test
    void testFail(){
        //xxxxx
        if(1 == 2){
            Assertions.fail("测试失败");
        }

    }


    int cal(int i, int j) {
        return i + j;
    }


    @Disabled
    @DisplayName("测试方法2")
    @Test
    void test2() {
        System.out.println(2);
    }

    @RepeatedTest(5)
    @Test
    void test3() {
        System.out.println(5);
    }

    /**
     * 规定方法超时时间。超出时间测试出异常
     *
     * @throws InterruptedException
     */
    @Timeout(value = 500, unit = TimeUnit.MILLISECONDS)
    @Test
    void testTimeout() throws InterruptedException {
        Thread.sleep(600);
    }


    @BeforeEach
    void testBeforeEach() {
        System.out.println("测试就要开始了...");
    }

    @AfterEach
    void testAfterEach() {
        System.out.println("测试结束了...");
    }

    @BeforeAll
    static void testBeforeAll() {
        System.out.println("所有测试就要开始了...");
    }

    @AfterAll
    static void testAfterAll() {
        System.out.println("所有测试以及结束了...");

    }
}
