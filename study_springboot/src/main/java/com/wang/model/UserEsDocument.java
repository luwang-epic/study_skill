package com.wang.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

@Data
@Document(indexName = "user")
public class UserEsDocument {
    @Id
    private String username;
    private Integer age;
}
