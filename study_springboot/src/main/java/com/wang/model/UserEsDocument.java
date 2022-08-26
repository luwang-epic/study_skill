package com.wang.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

@Data
// 表示该对象是一个es文档，指定user索引
@Document(indexName = "user")
public class UserEsDocument {
    // 表示文档的id
    @Id
    private String username;
    private Integer age;
}
