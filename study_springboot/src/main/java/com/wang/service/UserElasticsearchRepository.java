package com.wang.service;

import com.wang.model.UserEsDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * spring boot会自动加载实现并加载这个类，通过这个接口来操作es
 */
public interface UserElasticsearchRepository extends ElasticsearchRepository<UserEsDocument, String> {

}
