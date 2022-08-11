package com.wang.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpSession;
import java.util.Objects;

@Slf4j
@Controller
public class ThymeleafController {


    /**
     * 来登录页
     *
     * @return
     */
    @GetMapping(value = {"/login"})
    public String loginPage() {

        return "first";
    }


    @PostMapping("/login")
    public String main(String username, String password, HttpSession session, Model model) { //RedirectAttributes
        if (StringUtils.hasLength(username) && StringUtils.hasLength(password)) {
            //把登陆成功的用户保存起来
            session.setAttribute("username", username);
            session.setAttribute("password", password);
            //登录成功重定向到main.html;  重定向防止表单重复提交
            return "redirect:/main.html";
        } else {
            model.addAttribute("msg", "账号密码错误");
            //回到登录页面
            return "first";
        }

    }

    /**
     * 去main页面
     *
     * @return
     */
    @GetMapping("/main.html")
    public String mainPage(HttpSession session, Model model) {

        log.info("当前方法是：{}", "mainPage");
        //是否登录。  一般使用拦截器，过滤器来实现，这里简单模拟下
        Object loginUser = session.getAttribute("username");
        if (Objects.nonNull(loginUser)) {
            return "main";
        } else {
            //回到登录页面
            model.addAttribute("msg", "请重新登录");
            return "first";
        }
    }
}
