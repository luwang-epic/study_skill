package com.wang.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;

import javax.annotation.Resource;

/** 实现openapi的oauth认证配置  security封装了如下地址：
 1. 访问地址（获取code的地址）：http://localhost:8081/oauth/authorize?client_id=test&response_type=code
        需要输入用户名和密码 (这里是该系统的账号密码，admin:admin)，在回调接口中可以获取code值
 2. 获取token地址(Post请求)：http://localhost:8081/oauth/token?code=xxx&grant_type=authorization_code&redirect_url=http://localhost:8081/security/callback&scope=all
        需要在postman设置basic auth，输入账号和密码（这里的账号和密码，是appId和appPassword, test:123456）
        返回格式如下：
             {
                 "access_token": "259cebf8-c1eb-4169-845e-ed10e96cde98",
                 "token_type": "bearer",
                 "expires_in": 43199,
                 "scope": "all"
             }
 3. 通过返回的token调用相关opean api， 如：/oauth/test
 */
@Configuration
@EnableAuthorizationServer
public class OAuthAuthorizationConfig extends AuthorizationServerConfigurerAdapter {

    @Resource
    private PasswordEncoder passwordEncoder;

    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
        // 允许表单提交
        security.allowFormAuthenticationForClients()
                // 允许访问security生成的 /oauth/check_token接口来验证token是否有效
                .checkTokenAccess("permitAll()");
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        // 配置oauth的appId和appPassword
        clients.inMemory().withClient("test").secret(passwordEncoder.encode("123456"))
                // 通过授权码形式
                .authorizedGrantTypes("authorization_code")
                // 作用域和资源id
                .scopes("all").resourceIds("aa")
                // 回调地址
                .redirectUris("http://localhost:8081/oauth/callback");
    }


}
