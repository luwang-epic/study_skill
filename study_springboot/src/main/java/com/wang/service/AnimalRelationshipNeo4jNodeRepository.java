package com.wang.service;

import com.wang.model.AnimalRelationshipNeo4jNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;

/**
 * spring boot会自动加载实现并加载这个类，通过这个接口来操作neo4j关系
 */
public interface AnimalRelationshipNeo4jNodeRepository extends Neo4jRepository<AnimalRelationshipNeo4jNode, Long> {
}
