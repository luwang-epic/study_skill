package com.wang.mapper;

import com.wang.model.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserMapper {
    /**
     * 获取所有用户
     * @return
     */
    List<User> getAllUsers(int age);
}
