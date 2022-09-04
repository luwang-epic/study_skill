package com.wang.neo4j;

import com.wang.model.AnimalRelationshipNeo4jNode;
import com.wang.service.AnimalRelationshipNeo4jNodeRepository;
import com.wang.model.AnimalNeo4jNode;
import com.wang.service.AnimalNeo4jNodeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.Optional;

@SpringBootTest
public class Neo4jTest {

    @Resource
    private AnimalNeo4jNodeRepository animalNeo4jNodeRepository;
    @Resource
    private AnimalRelationshipNeo4jNodeRepository animalRelationshipNeo4jNodeRepository;

    @Test
    public void testCreateNode() {
        AnimalNeo4jNode animalNeo4jNode = new AnimalNeo4jNode();
        animalNeo4jNode.setName("cat");
        animalNeo4jNode.setColor("black");
        animalNeo4jNodeRepository.save(animalNeo4jNode);
        System.out.println("node---->" + animalNeo4jNode);
    }

    @Test
    public void testGetNode() {
        Optional<AnimalNeo4jNode> cat = animalNeo4jNodeRepository.findById(3L);
        System.out.println(cat.get());
    }

    @Test
    public void deleteNode() {
        Optional<AnimalNeo4jNode> cat = animalNeo4jNodeRepository.findById(2L);
        if (cat.isPresent()) {
            animalNeo4jNodeRepository.delete(cat.get());
        }
    }

    @Test
    public void testCreateNodeAndRelationship() {
        AnimalNeo4jNode cat = new AnimalNeo4jNode();
        cat.setName("cat");
        cat.setColor("black");
        animalNeo4jNodeRepository.save(cat);

        AnimalNeo4jNode fish = new AnimalNeo4jNode();
        fish.setName("fish");
        fish.setColor("black");
        animalNeo4jNodeRepository.save(fish);

        AnimalRelationshipNeo4jNode catFishRelationship = new AnimalRelationshipNeo4jNode();
        catFishRelationship.setParent(cat);
        catFishRelationship.setChild(fish);
        catFishRelationship.setType("eat");
        animalRelationshipNeo4jNodeRepository.save(catFishRelationship);
        System.out.println("node relationship---->" + catFishRelationship);
    }
}
