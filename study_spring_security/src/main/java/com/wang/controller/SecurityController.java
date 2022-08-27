package com.wang.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * spring security 演示
 *      为了方便浏览器请求，都用get方法
 *      可以使用chrome的无痕模式
 */
@RestController
@RequestMapping("/security")
public class SecurityController {

    @ResponseBody
    @GetMapping("/get")
    public String getMember() {
        return "getMember";
    }

    @ResponseBody
    @GetMapping("/add")
    public String addMember() {
        return "addMember";
    }

    @ResponseBody
    @GetMapping("/update")
    public String updateMember() {
        return "updateMember";
    }

    @ResponseBody
    @GetMapping("/delete")
    public String deleteMember() {
        return "deleteMember";
    }
}
