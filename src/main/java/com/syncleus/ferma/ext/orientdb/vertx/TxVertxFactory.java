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
package com.syncleus.ferma.ext.orientdb.vertx;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

public interface TxVertxFactory {

	/**
	 * Execute the txHandler within the scope of the no transaction and call the result handler once the transaction handler code has finished.
	 * 
	 * @param txHandler
	 *            Handler that will be executed within the scope of the transaction.
	 * @param resultHandler
	 *            Handler that is being invoked when the transaction has been committed
	 * @param <T>
	 *            Input type of the handler
	 */
	<T> void tx(AsyncTxHandler<Future<T>> txHandler, Handler<AsyncResult<T>> resultHandler);

	/**
	 * Asynchronously execute the txHandler within the scope of a transaction and invoke the result handler after the transaction code handler finishes or
	 * fails.
	 * 
	 * @param txHandler
	 *            Handler that will be executed within the scope of the transaction.
	 * @param resultHandler
	 *            Handler that is being invoked when the transaction has been committed
	 * @param <T>
	 *            Input type of the handler
	 */
	<T> void asyncTx(AsyncTxHandler<Future<T>> txHandler, Handler<AsyncResult<T>> resultHandler);

	/**
	 * Execute the given handler within the scope of a transaction.
	 * 
	 * @param txHandler
	 *            handler that is invoked within the scope of the no-transaction.
	 * @return Result of the transaction
	 * @param <T>
	 *            Input type of the handler
	 */
	<T> Future<T> tx(AsyncTxHandler<Future<T>> txHandler);

}
