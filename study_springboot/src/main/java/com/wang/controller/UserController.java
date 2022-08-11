package com.wang.controller;

import com.wang.model.User;
import com.wang.user.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @ResponseBody
    @GetMapping("/spec")
    public User getUser() {
        return userService.getSpecUser();
    }

    @ResponseBody
    @GetMapping("/all")
    public List<User> getAllUsers() {
        return userService.getAllUsers(10);
    }

    @ResponseBody
    @PostMapping("/saveUser")
    public String saveUser(User user) {
        return "save user success, with " + user;
    }
}
