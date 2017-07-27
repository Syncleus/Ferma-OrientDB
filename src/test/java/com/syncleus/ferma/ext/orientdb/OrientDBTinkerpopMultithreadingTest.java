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
/**
 * This product currently only contains code developed by authors
 * of specific components, as identified by the source code files.
 *
 * Since product implements StAX API, it has dependencies to StAX API
 * classes.
 *
 * For additional credits (generally to people who reported problems)
 * see CREDITS file.
 */
package com.syncleus.ferma.ext.orientdb;

import static com.syncleus.ferma.ext.orientdb.util.TestUtils.runAndWait;

import org.junit.Test;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientElement;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;

public class OrientDBTinkerpopMultithreadingTest extends AbstractOrientDBTest {

	private OrientGraphFactory factory = new OrientGraphFactory("memory:tinkerpop").setupPool(4, 10);

	@Test
	public void testMultithreading() {
		OrientGraph graph = factory.getTx();
		Vertex v2 = graph.addVertex(null);
		Vertex v = graph.addVertex(null);
		graph.commit();

		// Object id = v.getId();
		runAndWait(() -> {
			graph.getRawGraph().activateOnCurrentThread();
			graph.attach((OrientElement) v);
			v.setProperty("sfaf", "dxgvasdg");
			v.addEdge("adadsg", v2);
			// Vertex e = memoryGraph.getVertex(id);
			// assertNotNull(e);
			// assertEquals("marko", e.getProperty("name"));
			// e.setProperty("name", "joe");
			// memoryGraph.rollback();
		});
		// assertEquals("marko", v.getProperty("name"));

	}

}
