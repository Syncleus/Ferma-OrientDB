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

import org.apache.tinkerpop.gremlin.orientdb.OrientGraph;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.util.wrapped.WrappedEdge;

import com.syncleus.ferma.AbstractEdgeFrame;
import com.syncleus.ferma.FramedGraph;
import com.syncleus.ferma.WrappedFramedGraph;
import com.syncleus.ferma.annotations.GraphElement;
import com.syncleus.ferma.tx.Tx;
import com.syncleus.ferma.typeresolvers.PolymorphicTypeResolver;

@GraphElement
public class AbstractInterceptingEdgeFrame extends AbstractEdgeFrame {

	private Object id;
	public ThreadLocal<Edge> threadLocalEdge = ThreadLocal.withInitial(() -> {
		OrientGraph baseGraph = ((WrappedFramedGraph<OrientGraph>) getGraph()).getBaseGraph();
		return baseGraph.edges(id).next();
	});

	@Override
	protected void init(FramedGraph graph, Element element) {
		super.init(graph, element);
		this.id = element.id();
	}

	public String getFermaType() {
		return getProperty(PolymorphicTypeResolver.TYPE_RESOLUTION_KEY);
	}

	public String getUuid() {
		return getProperty("uuid");
	}

	public void setUuid(String uuid) {
		setProperty("uuid", uuid);
	}

	@Override
	public FramedGraph getGraph() {
		return Tx.getActive().getGraph();
	}

	@Override
	public Edge getElement() {
		Edge edge = threadLocalEdge.get();
		// Unwrap wrapped edge
		if (edge instanceof WrappedEdge) {
			edge = ((WrappedEdge<Edge>) edge).getBaseEdge();
		}
		return edge;
	}

}
