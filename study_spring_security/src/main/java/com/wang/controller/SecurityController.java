package com.wang.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (Objects.nonNull(authentication.getPrincipal()) && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            return "getMember" + "---->"  + userDetails.getUsername() + " " + userDetails.getAuthorities();
        }
        return "getMember" + "---->" + authentication;
    }

    @ResponseBody
    @GetMapping("/add")
    public String addMember(Authentication authentication) {
        if (Objects.nonNull(authentication.getPrincipal()) && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            return "addMember" + "---->"  + userDetails.getUsername() + " " + userDetails.getAuthorities();
        }
        return "addMember" + "---->" + authentication;
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

    @ResponseBody
    @PostMapping("/success")
    public String success() {
        return "login success";
    }
}
