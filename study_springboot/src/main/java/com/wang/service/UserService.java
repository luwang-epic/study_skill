package com.wang.service;

import com.wang.model.User;
import com.wang.mapper.UserMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;

@Service
public class UserService {
    @Resource
    private MeterRegistry meterRegistry;
    /**
     * 获取StudyConfig中注入到容器中的user
     */
    @Resource
    private User user;

    private Counter counter;


    @Resource
    private UserMapper userMapper;

    @PostConstruct
    public void init() {
        // action = getSpecUser
        counter = meterRegistry.counter("com.wang.count", "action", "getSpecUser");
    }

    public User getSpecUser() {
        counter.increment();
        return user;
    }

    public List<User> getAllUsers(int age) {
        return userMapper.getAllUsers(age);
    }

}
