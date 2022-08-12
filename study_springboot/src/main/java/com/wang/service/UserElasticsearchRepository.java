package com.wang.service;

import com.wang.model.UserEsDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface UserElasticsearchRepository extends ElasticsearchRepository<UserEsDocument, String> {

}
