package com.wang.baidu.java.reg;

import java.util.regex.Pattern;

/**
 * @Author: wanglu51
 * @Date: 2023/7/14 14:46
 */
public class RegSample {

    public static void main(String[] args) {
        String str = "abc___def___ghi___aa";
        str = "abc___def___aaa";
        str = "abc___def";
        // Pattern pattern = Pattern.compile("^(?!.*___.*___.*___.*$).*___.*___.*$");

        // Pattern pattern = Pattern.compile("^(?!.*___.*___.*$).*___.*$");

        Pattern pattern = Pattern.compile("^[^\\._]+___[^\\._]+$");

        if (pattern.matcher(str).find()) {
            System.out.println("match");
        } else {
            System.out.println("no match");
        }
    }


}
