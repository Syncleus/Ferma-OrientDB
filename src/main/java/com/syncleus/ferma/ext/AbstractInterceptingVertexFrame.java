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
package com.syncleus.ferma.ext;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.tinkerpop.gremlin.orientdb.OrientGraph;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.util.wrapped.WrappedVertex;

import com.syncleus.ferma.AbstractVertexFrame;
import com.syncleus.ferma.FramedGraph;
import com.syncleus.ferma.VertexFrame;
import com.syncleus.ferma.WrappedFramedGraph;
import com.syncleus.ferma.tx.Tx;
import com.syncleus.ferma.typeresolvers.PolymorphicTypeResolver;

/**
 * Abstract implementation of a orientdb specific ferma vertex frame. The internal orientdb vertex id is stored in order to reload the vertex if the vertex
 * object was passed from one thread/transaction to another.
 */
public class AbstractInterceptingVertexFrame extends AbstractVertexFrame {

	/**
	 * Reference to the orientdb vertex id.
	 */
	private Object id;

	/**
	 * Thread specific reference to the underlying orientdb graph element.
	 */
	public ThreadLocal<Vertex> threadLocalVertex = ThreadLocal.withInitial(() -> {
		OrientGraph baseGraph = ((WrappedFramedGraph<OrientGraph>) getGraph()).getBaseGraph();
		Iterator<Vertex> it = baseGraph.vertices(id);
		if (it.hasNext()) {
			return it.next();
		}
		return null;
	});

	@Override
	protected void init() {
		super.init();
	}

	@Override
	protected void init(FramedGraph graph, Element element) {
		super.init(graph, element);
		this.id = element.id();
	}

	/**
	 * Return the properties which are prefixed using the given key.
	 * 
	 * @param prefix
	 *            Prefix of the properties
	 * @return Map which contains all found properties
	 */
	public Map<String, String> getProperties(String prefix) {
		Map<String, String> properties = new HashMap<>();

		for (String key : getPropertyKeys()) {
			if (key.startsWith(prefix)) {
				properties.put(key, getProperty(key));
			}
		}
		return properties;
	}

	@SuppressWarnings("unchecked")
	public Object getId() {
		return id;
	}

	/**
	 * Set unique in-bound edges from the given vertex to the current vertex for all listed edge labels.
	 * 
	 * @param vertex
	 *            Vertex to link from
	 * @param labels
	 *            Labels to be used to create the edges
	 */
	public void setLinkInTo(VertexFrame vertex, String... labels) {
		// Unlink all edges between both objects with the given label
		unlinkIn(vertex, labels);
		// Create a new edge with the given label
		linkIn(vertex, labels);
	}

	/**
	 * Set unique out-bound edges from the given vertex to the current vertex for all listed edge labels.
	 * 
	 * @param vertex
	 *            Vertex to link to
	 * @param labels
	 *            Labels to be used to create the edges
	 */
	public void setLinkOutTo(VertexFrame vertex, String... labels) {
		// Unlink all edges between both objects with the given label
		unlinkOut(vertex, labels);
		// Create a new edge with the given label
		linkOut(vertex, labels);
	}

	/**
	 * Return the uuid property value.
	 * 
	 * @return UUID value
	 */
	public String getUuid() {
		return getProperty("uuid");
	}

	/**
	 * Set the uuid property value.
	 * 
	 * @param uuid
	 *            UUID value
	 */
	public void setUuid(String uuid) {
		setProperty("uuid", uuid);
	}

	/**
	 * Return the unwrapped vertex object
	 * 
	 * @return Underlying vertex
	 */
	public Vertex getVertex() {
		return getElement();
	}

	/**
	 * Return the ferma type of the vertex
	 * 
	 * @return Ferma type
	 */
	public String getFermaType() {
		return getProperty(PolymorphicTypeResolver.TYPE_RESOLUTION_KEY);
	}

	@Override
	public FramedGraph getGraph() {
		// Get the graph not by the element but instead by the currently active transaction.
		return Tx.getActive().getGraph();
	}

	@Override
	public Vertex getElement() {
		threadLocalVertex.remove();
		Vertex vertex = threadLocalVertex.get();
		// Unwrap wrapped vertex
		if (vertex instanceof WrappedVertex) {
			vertex = (Vertex) ((WrappedVertex<Vertex>) vertex).getBaseVertex();
		}
		return vertex;
	}

	public <P extends Element, T extends Element> GraphTraversal<P, T> hasType(GraphTraversal<P, T> traverser, Class<?> type) {
		return getGraph().getTypeResolver().hasType(traverser, type);
	}

}
