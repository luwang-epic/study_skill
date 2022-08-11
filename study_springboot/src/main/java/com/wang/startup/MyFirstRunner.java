package com.wang.startup;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * SpringBoot提供了两个Runner启动器——CommandLineRunner、ApplicationRunner接口。
 * 二者实际上并无太大区别，只是前者是通过数组接收启动参数而后者则是将启动参数封装到ApplicationArguments对象中。
 * 实际开发过程中只需在接口的run方法中实现我们的初始化操作即可。当然不要忘了在启动器类上添加@Component注解
 */
@Order(1)
@Component
public class MyFirstRunner implements CommandLineRunner {
    @Override
    public void run(String... args) throws Exception {
        System.out.println("MyFirstRunner...");

        // 获取参数 方式1
        if (null != args) {
            System.out.println("args---> " + Arrays.stream(args).collect(Collectors.toList()));
        }
    }
}
