package com.wang.jdk.grammar;

import java.util.Arrays;
import java.util.List;

/**
 * @Author: wanglu51
 * @Date: 2023/7/24 20:22
 */
public class LambdaSample {

    public static void main(String[] args) {
        List<String> strs = Arrays.asList("one", "two", "three", "four");

        strs.forEach(str -> {
            if (str.equals("three")) {
                return;
            }
            System.out.println(str);
        });

    }

}
