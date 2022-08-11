package com.wang.controller;

import com.wang.model.DbConnectionInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class TestController {

    @Value("${MAVEN_HOME}")
    private String mavenHome;

    @Resource
    private DbConnectionInfo dbConnectionInfo;

    @ResponseBody
    @GetMapping("/test")
    public String test() {
        return "springboot2 test with " + mavenHome;
    }

    @ResponseBody
    @GetMapping("/db")
    public DbConnectionInfo getDbConnectionInfo() {
        return dbConnectionInfo;
    }

    /**
     * 欢迎页，当找不到index.html时或者加了静态页前缀，会使用该方法作为欢迎页
     * @return
     */
    @ResponseBody
    @GetMapping("/index")
    public String index(){
        return "欢迎您";
    }

}
