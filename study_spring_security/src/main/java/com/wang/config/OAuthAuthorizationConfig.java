package com.wang.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;
import org.springframework.security.oauth2.provider.code.InMemoryAuthorizationCodeServices;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

import javax.annotation.Resource;
import java.util.Arrays;

/** 实现openapi的oauth认证配置  security已经封装好了认证相关api地址
 授权码模式：
     1. 访问地址（获取code的地址）：http://localhost:8081/oauth/authorize?client_id=test&scope=all&response_type=code&redirect_url=http://localhost:8081/security/callback
            response_type=code表示授权码模式
            需要输入用户名和密码 (这里是该系统的账号密码，admin:admin)，在回调接口中可以获取code值
     2. 获取token地址(Post请求)：http://localhost:8081/oauth/token?code=xxx&client_id=test&client_secret=123456&grant_type=authorization_code
            grant_type=authorization_code表示授权码模式
            返回格式如下：
                 {
                     "access_token": "c244f90a-dcea-4c21-b971-0465d5c44c6e",
                     "token_type": "bearer",
                     "refresh_token": "39c71787-8a2c-47f9-be97-5f9110f4c3f8",
                     "expires_in": 7199,
                     "scope": "all"
                 }
     3. 通过返回的token调用相关opean api， 如：/oauth/test
 简化模式：
    1. 访问地址：http://localhost:8081/oauth/authorize?client_id=test&scope=all&response_type=token&redirect_url=http://localhost:8081/security/callback
        response_type=token表示简化模式
        结果url为：http://localhost:8081/oauth/callback#access_token=91f6ad41-256c-4c3e-af81-ef3998b251c7&token_type=bearer&expires_in=7199
        可以直接在url中获得access_token，由于一般适用于午客户端模式，因此结果放在#后面，而不是参数的形式?后面
 密码模式：
    1. 直接访问token地址，通过传入用户账号(admin:admin，注意不是客户端账号)的形式获取token
        地址：http://localhost:8081/oauth/token?client_id=test&client_secret=123456&grant_type=password&username=admin&password=admin
        grant_type=password表示密码模式
            返回格式如下：
                 {
                     "access_token": "73ccab01-a506-4463-aec0-4a9240906916",
                     "token_type": "bearer",
                     "refresh_token": "0efd355d-93fe-4c41-93f5-b4cc9b5a162e",
                     "expires_in": 7199,
                     "scope": "all"
                 }
 客户端模式：
    1. 直接访问token地址，通过传入用户账号(admin:admin，注意不是客户端账号)的形式获取token
       地址：http://localhost:8081/oauth/token?client_id=test&client_secret=123456&grant_type=client_credentials
            grant_type=client_credentials表示客户端模式
            返回格式如下：
             {
                 "access_token": "326f2754-98ce-4d84-91a3-dfe9bc9c0f14",
                 "token_type": "bearer",
                 "expires_in": 7199,
                 "scope": "all"
             }
 */
/**
 * oauth认证服务器
 */
@Configuration
@EnableAuthorizationServer
public class OAuthAuthorizationConfig extends AuthorizationServerConfigurerAdapter {

    /**
     * 配置令牌存储策略，生成环境一般放到数据库中
     */
    @Bean
    public TokenStore tokenStore() {
        // 将令牌存放到内存， 生成普通令牌
//        return new InMemoryTokenStore();

        // 使用jwt生成token
        return new JwtTokenStore(accessTokenConverter());

    }

    @Bean
    public JwtAccessTokenConverter accessTokenConverter() {
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        // 随机的一个key，保证令牌不被篡改，这个key需要和资源端保持一直
        converter.setSigningKey("aaa"); // 对称秘钥，资源服务器使用这个秘钥来验证
        return converter;
    }


    /**
     * security框架根据configure(ClientDetailsServiceConfigurer clients)中配置的信息自动生成ClientDetailsService类
     */
    @Resource
    private ClientDetailsService clientDetailsService;
    /**
     * 令牌管理服务
     */
    @Bean
    public AuthorizationServerTokenServices authorizationServerTokenServices() {
        DefaultTokenServices tokenServices = new DefaultTokenServices();
        // 客户端信息服务
        tokenServices.setClientDetailsService(clientDetailsService);
        // 是否生成刷新令牌
        tokenServices.setSupportRefreshToken(true);
        // 令牌存储策略
        tokenServices.setTokenStore(tokenStore());

        // 设置令牌增强, JWT生成令牌是需要这个
        TokenEnhancerChain tokenEnhancerChain = new TokenEnhancerChain();
        tokenEnhancerChain.setTokenEnhancers(Arrays.asList(accessTokenConverter()));
        tokenServices.setTokenEnhancer(tokenEnhancerChain);

        // 令牌默认有效期 2h
        tokenServices.setAccessTokenValiditySeconds(2 * 60 * 60);
        // 刷新令牌默认有效期 3d
        tokenServices.setRefreshTokenValiditySeconds(3 * 24 * 60 * 60);
        return tokenServices;
    }



    // 需要在SecurityConfig配置类中注入spring中
    @Resource
    private AuthenticationManager authenticationManager;
    @Resource
    private UserDetailsService userDetailsService;


    /**
     * 设置授权码模式的授权码如何存取，暂时采用内存方式
     */
    @Bean
    public AuthorizationCodeServices authorizationCodeServices() {
        return new InMemoryAuthorizationCodeServices();
    }

    /**
     * 令牌访问端点
     * @param endpoints the endpoints configurer
     * @throws Exception
     */
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints
                // 密码模式需要
                .authenticationManager(authenticationManager)
                // 密码模式需要
                .userDetailsService(userDetailsService)
                // 授权码模式需要
                .authorizationCodeServices(authorizationCodeServices())
                // security提供的四种授权模式满足不了时，可以自定义类实现TokenGranter接口来授权
//                .tokenGranter()
                // 令牌管理服务， 不同模式都需要配置这个
                .tokenServices(authorizationServerTokenServices())
                // 允许post提交
                .allowedTokenEndpointRequestMethods(HttpMethod.POST);

    }


    @Resource
    private PasswordEncoder passwordEncoder;

    /**
     * 配置令牌访问端点的安全策略
     * @param security a fluent configurer for security features
     * @throws Exception
     */
    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
        // 允许通过表单提交来申请令牌
        security.allowFormAuthenticationForClients()
                // 允许访问security生成的 /oauth/check_token接口来验证token是否有效
                .checkTokenAccess("permitAll()")
                // JWT使用，允许访问公钥端口 /oauth/token_key接口获取公钥
                .tokenKeyAccess("permitAll()");
    }


    /**
     * 客户端信息配置，一般放到数据库中
     * @param clients the client details configurer
     * @throws Exception
     */
    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        // 配置oauth的appId和appPassword
        clients.inMemory().withClient("test").secret(passwordEncoder.encode("123456"))
                // 访问的资源id
                .resourceIds("resource1")
                /*
                    authorization_code授权码模式,这个是标准模式
                    implicit简单模式,这个主要是给无后台的纯前端项目用的
                    password密码模式,直接拿用户的账号密码授权,不安全
                    client_credentials客户端模式,用clientid和密码授权,和用户无关的授权方式
                    refresh_token使用有效的refresh_token去重新生成一个token,之前的会失效
                 */
                // 授权类型， 通过授权码形式， 包括：authorization_code, password, client_credentials, implicit, refresh_token
                // 这里为了演示，将几种模式都加入了，实际只一般都是使用authorization_code模式
                .authorizedGrantTypes("authorization_code", "refresh_token", "password", "client_credentials", "implicit")
                // 允许的授权范围
                .scopes("all")
                .autoApprove(false) // false： 使用授权码时，跳转到用户授权页面  true：不需要跳转页面，直接发放令牌
                // 回调地址
                .redirectUris("http://localhost:8081/oauth/callback");
    }


}
