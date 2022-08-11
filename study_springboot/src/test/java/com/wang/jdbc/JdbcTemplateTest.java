package com.wang.jdbc;

import com.wang.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@SpringBootTest
public class JdbcTemplateTest {

    @Resource
    private JdbcTemplate jdbcTemplate;


    @Test
    public void insert() {
        jdbcTemplate.update("insert into user(name, age) values('zhangsan', 22)");
    }

    @Test
    public void query() {
        List<Map<String, Object>> data = jdbcTemplate.queryForList("select * from user where age > 0");
        System.out.println(data);

        List<User> users = jdbcTemplate.query("select * from user where age > 0", BeanPropertyRowMapper.newInstance(User.class));
        System.out.println(users);
    }

}
