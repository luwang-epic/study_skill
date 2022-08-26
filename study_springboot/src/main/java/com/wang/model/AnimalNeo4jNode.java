package com.wang.model;

import lombok.Data;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;

import java.io.Serializable;

@Data
// 表示该对象是一个neo4j节点，指定为animal类型
@NodeEntity(label = "animal")
public class AnimalNeo4jNode implements Serializable {
    // 节点id，由neo4j来生产对应的值
    @Id
    @GeneratedValue
    private Long id;
    // 节点属性
    @Property
    private String name;
    // 节点属性
    @Property
    private String color;
}
