package com.wang.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;


/**
 * 这个配置用于控制哪些用户可以访问哪些资源
 */
@Configuration
@EnableWebSecurity //开启权限验证
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private PasswordEncoder passwordEncoder;

    @Bean
    public PasswordEncoder passwordEncoder() {
        passwordEncoder= new BCryptPasswordEncoder();
        return passwordEncoder;
    }

    /**
     * 提供用户密码已经对应的权限信息等
     */
    @Bean
    @Override
    protected UserDetailsService userDetailsService() {
        // 内存存放用户认证信息
//        InMemoryUserDetailsManager userDetailsService = new InMemoryUserDetailsManager();
//        userDetailsService.createUser(User.withUsername("admin").password(passwordEncoder.encode("admin")).authorities("get", "add", "update", "delete", "oauth").build());
//        userDetailsService.createUser(User.withUsername("get").password(passwordEncoder.encode("get")).authorities("get").build());
//        return userDetailsService;

        // 实际使用下面的方式从数据库获取或者redis缓存获取（单独定义一个类注入到spring中，不需要重载userDetailsService这个方法了）
        return new UserDetailsService() {
            @Override
            public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

                // 这里使用username从数据库或者缓存获取用户数据

                if ("admin".equals(username)) {
                    return User.withUsername("admin").password(passwordEncoder.encode("admin")).authorities("get", "add", "update", "delete", "oauth").build();
                }

                if ("get".equals(username)) {
                    return User.withUsername("get").password(passwordEncoder.encode("get")).authorities("get").build();
                }

                return null;
            }
        };
    }

    /**
     * 注入到spring中，后续oauth认证使用密码模式的时候需要使用
     */
    @Bean
    @Override
    protected AuthenticationManager authenticationManager() throws Exception {
        return super.authenticationManager();
    }

    /**
     * 认证账户信息
     * @param auth the {@link AuthenticationManagerBuilder} to use
     * @throws Exception
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        // 可以实现userDetailsService方法来代替，即提供UserDetailsService的实现类；那么也可以自定义一个继承UserDetailsService的类，加载到spring容器中
//        auth.inMemoryAuthentication().withUser("admin").password(passwordEncoder.encode("admin")).authorities("get", "add", "update", "delete")
//                .and().withUser("get").password(passwordEncoder.encode("get")).authorities("get")
//                .and().passwordEncoder(passwordEncoder);

        // 如果通过@Bean注入到spring容器了，AuthenticationManagerBuilder也可以自动注入，否则需要如下的方式手动注入
        // 设置到AuthenticationManagerBuilder类中
//        auth.userDetailsService(userDetailsService());
        super.configure(auth);
    }


    /* 几种认证模式（认证原理见下面）：
    HttpBasic模式
        HttpBasic登录验证模式是Spring Security实现登录验证最简单的一种方式，也可以说是最简陋的一种方式。
        它的目的并不是保障登录验证的绝对安全，而是提供一种“防君子不防小人”的登录验证。
        如果有心人破解了，真想看看这里面的数据，其实也无妨。这就是HttpBasic模式的典型应用场景。
    HttpBasic认证原理：
            1.首先，HttpBasic模式要求传输的用户名密码使用Base64模式进行加密。如果用户名是 "admin"  ，密码是“ admin”，则将字符串"admin:admin" 使用Base64编码算法加密。加密结果可能是：YWtaW46YWRtaW4=。
            2.然后，在Http请求中使用Authorization作为一个Header，“Basic YWtaW46YWRtaW4=“作为Header的值，发送给服务端。（注意这里使用Basic+空格+加密串）
            3.服务器在收到这样的请求时，到达BasicAuthenticationFilter过滤器，将提取“ Authorization”的Header值，并使用用于验证用户身份的相同算法Base64进行解码。
            4.解码结果与登录验证的用户名密码匹配，匹配成功则可以继续过滤器后续的访问。
        所以，HttpBasic模式真的是非常简单又简陋的验证模式，Base64的加密算法是可逆的，
        你知道上面的原理，分分钟就破解掉。我们完全可以使用PostMan工具，发送Http请求进行登录验证。

    FormLogin登录认证模式
        Spring Security的HttpBasic模式，该模式比较简单，只是进行了通过携带Http的Header进行简单的登录验证，
        而且没有定制的登录页面，所以使用场景比较窄。对于一个完整的应用系统，与登录验证相关的页面都是高度定制化的，
        非常美观而且提供多种登录方式。这就需要SpringSecurity支持我们自己定制登录页面,
        spring boot2.0以上版本（依赖Security 5.X版本）默认会生成一个登录页面.
     */
    /**
     * 配置授权信息
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        System.out.println("SecurityConfig HttpSecurity configure......");

        // 配置认证方式为 token form 表单 设置为httpBasic模式(这种模式没有登录，浏览器胡自动弹出登录框)
//        http.authorizeRequests().antMatchers("/**").fullyAuthenticated().and().httpBasic();
        // formLogin模式，spring security会自动生成登录页面
//        http.authorizeRequests().antMatchers("/**").fullyAuthenticated().and().formLogin();

        // 这里的检查是否有权限是有顺序的，从上到下的，因此如果url匹配有多个，那么会以第一个规则为准
        http.authorizeRequests().antMatchers("/security/get").hasAnyAuthority("get")
                .antMatchers("/security/add").hasAnyAuthority("add")
                .antMatchers("/security/update").hasAnyAuthority("update")
                .antMatchers("/security/delete").hasAnyAuthority("delete")
                .antMatchers("/oauth/test").hasAnyAuthority("oauth")
                .antMatchers("/login", "/oauth/**", "/static/**", "/css/**", "/js/**", "/images/**", "/plugins/**", "**/favicon.ico").permitAll()
                .antMatchers("/**").fullyAuthenticated()
                // 登录设置， /security/success为post方法  如果设置了successForwardUrl，调用授权接口/oauth/authorize时需要登录后重新调用一次，不会直接到授权页面
                .and().formLogin()//.successForwardUrl("/security/success")
                .and().logout().logoutUrl("/logout").logoutSuccessUrl("/login")
                // 关闭跨域请求防护
                .and().csrf().disable();

        // 默认也是这个策略，需要时创建一个session，登录时会创建session保证回话状态；
        // 如果是使用token等方式认证，可以设置为不需要创建session
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED);
    }
}
