package com.wang.startup;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

public class MyApplicationListener implements ApplicationListener {
    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        // 防止干扰，先注释了
//        System.out.println("MyApplicationListener.....onApplicationEvent...");
    }
}
