package com.wang.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/oauth")
public class OAuthController {

    @ResponseBody
    @GetMapping("/test")
    public String test() {
        return "test oauth success";
    }

    @ResponseBody
    @GetMapping("/callback")
    public String callback(@RequestParam(value = "code", required = false) String code) {
        return "get callback success with code: " + code;
    }
}
