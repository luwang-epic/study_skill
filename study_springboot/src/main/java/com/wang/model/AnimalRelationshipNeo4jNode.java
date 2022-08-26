package com.wang.model;

import lombok.Data;
import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

@Data
// 表示一个neo4j关系对象
@RelationshipEntity(type = "animalRelationship")
public class AnimalRelationshipNeo4jNode {
    @Id
    @GeneratedValue
    private Long id;
    // 关系开始节点
    @StartNode
    private AnimalNeo4jNode parent;
    // 关系结束节点
    @EndNode
    private AnimalNeo4jNode child;
    // 关系属性
    @Property
    private String type;
}
