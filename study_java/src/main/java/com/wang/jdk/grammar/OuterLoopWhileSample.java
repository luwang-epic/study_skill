package com.wang.jdk.grammar;

/**
 * 演示outerLoop和while配合使用
 */
public class OuterLoopWhileSample {

    public static void main(String[] args) {
        String caseString = "a";

        outerLoop:
        while (true) {
            switch (caseString) {
                case "a":
                    caseString = "b";
                    break;
                case "b":
                    System.out.println("跳出到outerLoop处，然后结束循环，继续往下执行");
                    break outerLoop;
            }
        }

        System.out.println("程序可以运行到这里......");
    }

}
