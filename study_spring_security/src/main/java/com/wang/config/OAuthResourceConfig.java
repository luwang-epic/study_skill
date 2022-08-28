package com.wang.config;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;

import javax.annotation.Resource;

/**
 * 资源服务配置，一般认证和资源服务是分开的，这里放到一个服务中
 *      可以先获取到token，然后访问/oauth/test方法来验证，
 *      放在同一个服务器中（即加上这个类后），只有/oauth/token, /oauth/check_token等方法可以使用，
 *          需要使用密码模式获取token，因为code模式需要登录，但是由于放了该类，导致登录不可用
 *          其他基于session的功能将会失效，如：/security/路径下的方法，login等方法
 *          因为session和token是两种不同的认证方式，会有一些冲突，
 *          因此这里先注释这类，如果只想演示这个，可以打开，然后获取token以及访问/oauth/test验证
 */
//@Configuration
//@EnableResourceServer
public class OAuthResourceConfig extends ResourceServerConfigurerAdapter {

    // 如果资源服务和认证服务在一个server，可以通过本地令牌服务来验证，如果不在一个server，可以通过远程令牌服务来验证
//    @Bean
//    public ResourceServerTokenServices resourceServerTokenServices() {
        // 使用远程服务请求授权服务器验证token，必须指定token的url,client_id,client_secret
//        RemoteTokenServices tokenServices = new RemoteTokenServices();
//        tokenServices.setCheckTokenEndpointUrl("http://localhost:8081/oauth/check_token");
//        tokenServices.setClientId("test");
//        tokenServices.setClientSecret("123456");
//        return tokenServices;
//    }

    /**
     如果资源服务和认证服务在一个项目
         在OauthAuthorizationConfig中实现，通过new DefaultTokenServices来生成，因此可以直接强转为该类型
           可以看源码中的这个类，即实现了AuthorizationServerTokenServices接口，也实现了ResourceServerTokenServices接口
           因此在一个项目中，直接使用这个即可，否则会报有两个ResourceServerTokenServices的问题
     如果资源服务和认证服务不在一个项目，那么使用resourceServerTokenServices的方法来实现远程认证
     */
    @Resource
    private AuthorizationServerTokenServices authorizationServerTokenServices;

    @Override
    public void configure(ResourceServerSecurityConfigurer resources) throws Exception {

        resources
                .stateless(true)
                // 配置资源id
                .resourceId("resource1")
                // 验证令牌的服务
                .tokenServices((DefaultTokenServices)authorizationServerTokenServices);

    }

    /**
     * 配置策略
     * @param http the current http filter configuration
     * @throws Exception
     */
    @Override
    public void configure(HttpSecurity http) throws Exception {
        System.out.println("OAuthResourceConfig HttpSecurity configure......");

        http.authorizeRequests()
                .antMatchers("/oauth/test").access("#oauth2.hasScope('all')")
                .antMatchers("/**").permitAll()
                .and().csrf().disable();

        // 一般通过令牌访问，会设置为无状态的
//        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }
}
