package com.wang.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@DisplayName("StudyConfigTest")
public class StudyConfigTest {

    @Test
    void applicationArgumentsPopulated(@Autowired StudyConfig studyConfig) {
        Assertions.assertEquals("zhangsan", studyConfig.user().getName());
    }

}
