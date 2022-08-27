package com.wang;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/*
Oauth2协议是一个授权协议，它允许软件应用代表（而不是充当）资源拥有者去访问资源拥有者的资源。
应用向资源拥有者请求授权，然后获得令牌（token），并用它来访问资源，并且资源拥有者不同向应用提供用户名和密码等敏感数据

授权流程：
    1. 在开放接口平台申请一个appId（应用id）和appPassword（秘钥）
    2. 通过appId和appPassword调用开放接口平台接口获取token （临时且唯一的，具有过期时间，过期可以刷新token）
    3. 再使用token调用接口

主要应用场景：
    第三方联合登录（使用第三方账号登录），比如QQ联合登录
    开发接口， 比如开发openapi，腾讯的一些开发接口认证

    如：使用微信登录
        1. 在需要用户登录时，生成一个微信连接，传入appId和一个回调地址 （地址格式是开放平台指定的，这里是微信）
        2. 当用户微信打开该地址时（对于微信，一般将地址生成二维码，用户扫描即可打开），会出现是否同意授权页面
        3. 当用户点击确认授权，会重定向到提供的回调地址（回调地址类似http://localhost:8080?code=xxx）形式，
            应用可以拿到微信返回的code信息
        4. 使用code以及appId，appPassword调用微信接口获取访问token（同时也会返回该用户对该应用的唯一id（openid））
        5. 使用返回的token + openapi可以调用微信的接口获取用户已经授权的基本信息

 */

@SpringBootApplication
public class StudySpringSecurityApplication {

    public static void main(String[] args) {
        SpringApplication.run(StudySpringSecurityApplication.class, args);
    }

}