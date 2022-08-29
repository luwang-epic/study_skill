package com.wang.janusgraph;


import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphFactory;
import org.janusgraph.core.JanusGraphTransaction;

public class JanusGraphSample {

    public static void main( String[] args ) {
        JanusGraph graph = JanusGraphFactory.build()
                .set("storage.backend", "berkeleyje")
                .set("storage.directory", "D:\\java\\local\\janusgraph-0.5.2\\file_database\\program")
                .open();

        // add a vertex by graph transaction
        JanusGraphTransaction tx = graph.newTransaction();
        tx.addVertex("user").property("name", "Jesson");
        tx.commit();

        GraphTraversalSource g = graph.traversal();
        // add a vertex by traversal
        g.addV("user").property("name", "Dennis").next();
        g.tx().commit();


        // query by traversal
        System.out.println("Vertex count = " + g.V().count().next());

        g.tx().close();
        graph.close();
    }

}
