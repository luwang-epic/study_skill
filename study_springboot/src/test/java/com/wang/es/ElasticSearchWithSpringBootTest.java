package com.wang.es;

import com.wang.model.UserEsDocument;
import com.wang.service.UserElasticsearchRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.Optional;

@SpringBootTest
public class ElasticSearchWithSpringBootTest {

    @Resource
    private UserElasticsearchRepository userElasticsearchRepository;


    @Test
    public void addDocument() {
        UserEsDocument user = new UserEsDocument();
        user.setUsername("lisi");
        user.setAge(22);
        userElasticsearchRepository.save(user);
    }

    @Test
    public void getDocument() {
        Optional<UserEsDocument> user = userElasticsearchRepository.findById("lisi");
        System.out.println("document ---->" + user);
    }

}
