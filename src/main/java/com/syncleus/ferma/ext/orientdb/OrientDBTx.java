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

import org.apache.tinkerpop.gremlin.orientdb.OrientGraph;
import org.apache.tinkerpop.gremlin.orientdb.OrientTransaction;

import com.orientechnologies.orient.core.exception.OConcurrentModificationException;
import com.syncleus.ferma.tx.AbstractTx;
import com.syncleus.ferma.tx.FramedTxGraph;
import com.syncleus.ferma.tx.Tx;

public class OrientDBTx extends AbstractTx<FramedTxGraph> {

	private boolean isNested = false;

	public OrientDBTx(OrientTransaction transaction, DelegatingFramedOrientGraph parentGraph) {
		super(transaction, parentGraph);
	}

	/**
	 * Create new transaction using the given transaction as a base. The created transaction will be regarded as nested transaction.
	 * 
	 * @param tx
	 */
	public OrientDBTx(Tx tx) {
		super(tx.getDelegate(), tx.getGraph());
		this.isNested = true;
	}

	@Override
	public void close() {
		try {
			if (!isNested) {
				Tx.setActive(null);
			}
			if (isSuccess()) {
				commit();
			} else {
				rollback();
			}
			if (!isNested) {
				getDelegate().close();
			}
			OrientGraph graph = ((OrientGraph) getGraph().getBaseGraph());
			graph.commit();
			if (!isNested) {
				graph.close();
			}
		} catch (OConcurrentModificationException e) {
			throw e;
		}
	}

	public boolean isNested() {
		return isNested;
	}
}
