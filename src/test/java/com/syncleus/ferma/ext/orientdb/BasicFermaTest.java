package com.syncleus.ferma.ext.orientdb;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.apache.tinkerpop.gremlin.orientdb.OrientGraphFactory;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;

import com.syncleus.ferma.ext.orientdb.impl.OrientTransactionFactoryImpl;
import com.syncleus.ferma.ext.orientdb.model.Person;
import com.syncleus.ferma.tx.Tx;

public class BasicFermaTest {

	@Test
	public void testFerma3() {
		try (OrientGraphFactory graphFactory = new OrientGraphFactory("memory:tinkerpop" + System.currentTimeMillis())) {
			OrientTransactionFactory factory = new OrientTransactionFactoryImpl(graphFactory, "com.syncleus.ferma.ext.orientdb.model");
			Person p;
			Object id;
			try (Tx tx = factory.tx()) {
				p = tx.getGraph().addFramedVertex(Person.class);
				p.setName("Joe");

				Person p2 = tx.getGraph().addFramedVertex(Person.class);
				p.addFriend(p2);
				Person person = tx.getGraph().traverse((g) -> g.V()).nextExplicit(Person.class);
				assertNotNull(person.getId());
				assertNotNull(person.getName());
				assertNotNull(p.getId());
				tx.commit();
				id = p.getId();
				assertTrue(p.getFriends().iterator().hasNext());
				tx.success();
			}

			assertNull("No transaction should be active", Tx.getActive());
			try (Tx tx = factory.tx()) {
				Vertex fromId = tx.getGraph().getBaseGraph().vertices(id).next();
				assertNotNull(fromId);
				assertTrue(p.getFriends().iterator().hasNext());
			}
		}
	}
}
