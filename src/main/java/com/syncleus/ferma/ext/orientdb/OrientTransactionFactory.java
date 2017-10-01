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

import org.apache.tinkerpop.gremlin.orientdb.OrientGraphFactory;

import com.orientechnologies.orient.core.exception.OConcurrentModificationException;
import com.orientechnologies.orient.core.exception.OSchemaException;
import com.orientechnologies.orient.core.storage.ORecordDuplicatedException;
import com.syncleus.ferma.framefactories.FrameFactory;
import com.syncleus.ferma.tx.Tx;
import com.syncleus.ferma.tx.TxAction;
import com.syncleus.ferma.tx.TxFactory;

public interface OrientTransactionFactory extends TxFactory {

	/**
	 * Return the configured orientdb graph factory from which transaction are created.
	 * 
	 * @return
	 */
	OrientGraphFactory getFactory();

	/**
	 * Return the configured type resolver.
	 * 
	 * @return
	 */
	OrientDBTypeResolver getTypeResolver();

	/**
	 * Return the amount of maximum retries for a txAction
	 * 
	 * @return
	 */
	int getMaxRetry();

	@Override
	default <T> T tx(TxAction<T> txAction) {
		/**
		 * OrientDB uses the MVCC pattern which requires a retry of the code that manipulates the graph in cases where for example an
		 * {@link OConcurrentModificationException} is thrown.
		 */
		T handlerResult = null;
		boolean handlerFinished = false;
		for (int retry = 0; retry < getMaxRetry(); retry++) {

			try (Tx tx = tx()) {
				handlerResult = txAction.handle(tx);
				handlerFinished = true;
				tx.success();
			} catch (OConcurrentModificationException e) {
				try {
					// Delay the retry by 50ms to give the other transaction a chance to finish
					Thread.sleep(50 + (retry * 5));
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				// Reset previous result
				handlerFinished = false;
				handlerResult = null;
			}
			if (handlerFinished) {
				return handlerResult;
			}
		}
		throw new RuntimeException("Retry limit {" + getMaxRetry() + "} for trx exceeded");
	}

	/**
	 * Return the configured frame factory.
	 * 
	 * @return
	 */
	FrameFactory getFrameFactory();

	/**
	 * Set a custom frame factory instead of the default.
	 * 
	 * @param frameFactory
	 */
	void setFrameFactory(FrameFactory frameFactory);

	/**
	 * Setup all element classes in orientdb for the found graph elements.
	 */
	void setupElementClasses();

	/**
	 * Add a new vertex class.
	 * 
	 * @param typeName
	 * @param superTypeName
	 */
	void addVertexClass(String typeName, String superTypeName);

	/**
	 * Add a new edge class.
	 * 
	 * @param label
	 */
	void addEdgeClass(String label);

}
