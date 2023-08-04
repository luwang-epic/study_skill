package com.wang.springboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * 启动类
 *
 * @Author: wanglu51
 * @Date: 2022/11/30 20:10
 */
@SpringBootApplication
public class StudySpringbootApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(StudySpringbootApplication.class, args);
    }

}
