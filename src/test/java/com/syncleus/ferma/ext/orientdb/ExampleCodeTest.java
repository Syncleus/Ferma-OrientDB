package com.syncleus.ferma.ext.orientdb;

import org.junit.Test;

import com.syncleus.ferma.ext.orientdb.impl.OrientTransactionFactoryImpl;
import com.syncleus.ferma.ext.orientdb.model.Person;
import com.syncleus.ferma.tx.Tx;
import com.syncleus.ferma.tx.TxFactory;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;

/**
 * Test the example code of the readme to ensure that it is working
 */
public class ExampleCodeTest {

    @Test
    public void testExample() {
        // Setup the orientdb graph factory from which the transaction factory will create transactions
        OrientGraphFactory graphFactory = new OrientGraphFactory("memory:tinkerpop").setupPool(4, 10);
        TxFactory graph = new OrientTransactionFactoryImpl(graphFactory, "com.syncleus.ferma.ext.orientdb.model");

        try (Tx tx = graph.tx()) {
            Person joe = tx.getGraph().addFramedVertex(Person.class);
            joe.setName("Joe");
            Person hugo = tx.getGraph().addFramedVertex(Person.class);
            hugo.setName("Hugo");

            // Both are mutal friends
            joe.addFriend(hugo);
            hugo.addFriend(joe);
            tx.success();
        }

        try (Tx tx = graph.tx()) {
            for (Person p : tx.getGraph().getFramedVerticesExplicit(Person.class)) {
                System.out.println("Found person with name: " + p.getName());
            }
        }

        String result = graph.tx((tx) -> {
            Person hugo = tx.getGraph().getFramedVertices("name", "Hugo", Person.class).iterator().next();
            StringBuffer sb = new StringBuffer();
            sb.append("Hugo's friends:");

            for (Person p : hugo.getFriends()) {
                sb.append(" " + p.getName());
            }
            return sb.toString();
        });
        System.out.println(result);
    }

}
