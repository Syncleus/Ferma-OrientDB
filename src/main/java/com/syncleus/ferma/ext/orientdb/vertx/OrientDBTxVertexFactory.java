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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.orientechnologies.orient.core.exception.OConcurrentModificationException;
import com.orientechnologies.orient.core.exception.OSchemaException;
import com.syncleus.ferma.tx.Tx;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class OrientDBTxVertexFactory extends com.syncleus.ferma.ext.orientdb.OrientDBTxFactory implements TxVertxFactory {

	private static final Logger log = LoggerFactory.getLogger(OrientDBTxVertexFactory.class);

	protected Vertx vertx;

	private int maxRetry = 25;

	public OrientDBTxVertexFactory(OrientGraphFactory factory, Vertx vertx, String... basePaths) {
		super(factory, basePaths);
		this.vertx = vertx;
	}

	@Override
	public <T> void asyncTx(AsyncTxHandler<Future<T>> txHandler, Handler<AsyncResult<T>> resultHandler) {
		vertx.executeBlocking(bh -> {
			tx(txHandler, rh -> {
				if (rh.succeeded()) {
					bh.complete(rh.result());
				} else {
					bh.fail(rh.cause());
				}
			});
		}, false, resultHandler);
		return;
	}

	@Override
	public <T> Future<T> tx(AsyncTxHandler<Future<T>> txHandler) {
		Future<T> future = Future.future();
		try (Tx tx = tx()) {
			txHandler.handle(future);
		} catch (Exception e) {
			log.error("Error while handling no-transaction.", e);
			return Future.failedFuture(e);
		}
		return future;
	}

	@Override
	public <T> void tx(AsyncTxHandler<Future<T>> txHandler, Handler<AsyncResult<T>> resultHandler) {
		/**
		 * OrientDB uses the MVCC pattern which requires a retry of the code that manipulates the graph in cases where for example an
		 * {@link OConcurrentModificationException} is thrown.
		 */
		Future<T> currentTransactionCompleted = null;
		for (int retry = 0; retry < maxRetry; retry++) {
			currentTransactionCompleted = Future.future();
			try (Tx tx = tx()) {
				// TODO FIXME get rid of the countdown latch
				CountDownLatch latch = new CountDownLatch(1);
				currentTransactionCompleted.setHandler(rh -> {
					if (rh.succeeded()) {
						tx.success();
					} else {
						tx.failure();
					}
					latch.countDown();
				});
				txHandler.handle(currentTransactionCompleted);
				latch.await(30, TimeUnit.SECONDS);
				break;
			} catch (OSchemaException e) {
				log.error("OrientDB schema exception detected.");
				// TODO maybe we should invoke a metadata getschema reload?
				// factory.getTx().getRawGraph().getMetadata().getSchema().reload();
				// Database.getThreadLocalGraph().getMetadata().getSchema().reload();
			} catch (OConcurrentModificationException e) {
				if (log.isTraceEnabled()) {
					log.trace("Error while handling transaction. Retrying " + retry, e);
				}
			} catch (Exception e) {
				log.error("Error handling transaction", e);
				resultHandler.handle(Future.failedFuture(e));
				return;
			}
			if (log.isDebugEnabled()) {
				log.debug("Retrying .. {" + retry + "}");
			}
		}
		if (currentTransactionCompleted != null && currentTransactionCompleted.isComplete()) {
			resultHandler.handle(currentTransactionCompleted);
			return;
		}
		resultHandler.handle(Future.failedFuture("retry limit for tx exceeded"));
		return;

	}
}
