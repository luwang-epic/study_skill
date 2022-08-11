package com.wang.actuator;

import com.wang.model.User;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 定制自己的短笛endpoint
 */
@Component
@Endpoint(id = "user")
public class MyUserEndpoint {

    @Resource
    private User user;

    /**
     * 可以是任意的返回类型，spring会将其json化展示，访问地址：http://localhost:8080/actuator/user
     * @return
     */
    @ReadOperation
    public User getDockerInfo(){
        return user;
    }

    @WriteOperation
    private void LogoutUser(){
        System.out.println("logout user....");
    }

}
