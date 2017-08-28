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
package com.syncleus.ferma.ext.orientdb.impl;

import org.apache.tinkerpop.gremlin.orientdb.OrientGraphFactory;

import com.syncleus.ferma.ext.orientdb.DelegatingFramedOrientGraph;
import com.syncleus.ferma.ext.orientdb.OrientDBTx;
import com.syncleus.ferma.ext.orientdb.OrientDBTypeResolver;
import com.syncleus.ferma.ext.orientdb.OrientTransactionFactory;
import com.syncleus.ferma.tx.Tx;

public class OrientTransactionFactoryImpl implements OrientTransactionFactory {

	protected OrientGraphFactory factory;

	private OrientDBTypeResolver typeResolver;

	public OrientTransactionFactoryImpl(OrientGraphFactory factory, String... basePaths) {
		this.factory = factory;
		this.typeResolver = new OrientDBTypeResolver(basePaths);
	}

	@Override
	public OrientGraphFactory getFactory() {
		return factory;
	}

	@Override
	public OrientDBTypeResolver getTypeResolver() {
		return typeResolver;
	}

	/**
	 * Return the maxium count a transaction should be repeated if a retry is needed.
	 * 
	 * @return
	 */
	public int getMaxRetry() {
		return 20;
	}

	@Override
	public Tx createTx() {
		DelegatingFramedOrientGraph framedGraph = new DelegatingFramedOrientGraph(getFactory().getTx(), getTypeResolver());
		OrientDBTx tx = new OrientDBTx(getFactory().getTx().tx(), framedGraph);
		return tx;
	}

}