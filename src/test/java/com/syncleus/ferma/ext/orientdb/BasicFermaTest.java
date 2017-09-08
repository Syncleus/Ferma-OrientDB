/**
 * Copyright 2004 - 2017 Syncleus, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
			OrientTransactionFactory factory = new OrientTransactionFactoryImpl(graphFactory, false, "com.syncleus.ferma.ext.orientdb.model");
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
