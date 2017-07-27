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

import static com.syncleus.ferma.ext.orientdb.util.TestUtils.failingLatch;
import static com.syncleus.ferma.ext.orientdb.util.TestUtils.run;
import static com.syncleus.ferma.ext.orientdb.util.TestUtils.sleep;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.syncleus.ferma.ext.orientdb.model.Person;
import com.syncleus.ferma.tx.Tx;

import io.vertx.core.AsyncResult;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class TxTest extends AbstractOrientDBVertexTest {

	private static final Logger log = LoggerFactory.getLogger(TxTest.class);

	@Test
	public void testAsyncTestErrorHandling() throws Exception {
		CompletableFuture<AsyncResult<Object>> fut = new CompletableFuture<>();
		graph.asyncTx(tx -> {
			tx.fail("blub");
		}, rh -> {
			fut.complete(rh);
		});

		AsyncResult<Object> result = fut.get(5, TimeUnit.SECONDS);
		assertTrue(result.failed());
		assertNotNull(result.cause());
		assertEquals("blub", result.cause().getMessage());
	}

	@Test
	public void testAsyncTestSuccessHandling() throws Exception {
		CompletableFuture<AsyncResult<Object>> fut = new CompletableFuture<>();
		graph.asyncTx(tx -> {
			tx.complete("test");
		}, rh -> {
			fut.complete(rh);
		});
		AsyncResult<Object> result = fut.get(5, TimeUnit.SECONDS);
		assertTrue(result.succeeded());
		assertNull(result.cause());
		assertEquals("test", result.result());
	}

	@Test
	public void testConcurrentUpdate() throws Exception {
		final int nThreads = 10;
		final int nRuns = 200;

		try (Tx tx2 = graph.tx()) {
			Person person = tx2.getGraph().addFramedVertex(Person.class);
			for (int r = 1; r <= nRuns; r++) {
				final int currentRun = r;
				CountDownLatch latch = new CountDownLatch(nThreads);

				// Start two threads with a retry tx
				for (int i = 0; i < nThreads; i++) {
					final int threadNo = i;
					if (log.isTraceEnabled()) {
						log.trace("Thread [" + threadNo + "] Starting");
					}
					graph.asyncTx(tx -> {
						manipulatePerson(Tx.getActive().getGraph(), person);
						tx.complete(person);
					}, rh -> {
						if (rh.failed()) {
							rh.cause().printStackTrace();
						}
						assertEquals(Person.class, rh.result().getClass());
						latch.countDown();
					});
				}

				log.debug("Waiting on lock");
				failingLatch(latch);

				try (Tx tx = graph.tx()) {
					int expect = nThreads * r;
					assertEquals("Expected {" + expect + "} tags since this is the " + r + "th run.", expect, person.getFriends().size());
				}
			}
		}
	}

	// @Test
	// public void testTransaction() throws InterruptedException {
	// AtomicInteger i = new AtomicInteger(0);
	//
	// UserRoot root;
	// try (Tx tx = graph.tx()) {
	// root = person
	// }
	// int e = i.incrementAndGet();
	// try (Tx tx = graph.tx()) {
	// assertNotNull(root.create("testuser" + e, group(), user()));
	// assertNotNull(boot.userRoot().findByUsername("testuser" + e));
	// tx.success();
	// }
	// try (Tx tx = graph.tx()) {
	// assertNotNull(boot.userRoot().findByUsername("testuser" + e));
	// }
	// int u = i.incrementAndGet();
	// Runnable task = () -> {
	// try (Tx tx = graph.tx()) {
	// assertNotNull(root.create("testuser" + u, group(), user()));
	// assertNotNull(boot.userRoot().findByUsername("testuser" + u));
	// tx.failure();
	// }
	// assertNull(boot.userRoot().findByUsername("testuser" + u));
	//
	// };
	// Thread t = new Thread(task);
	// t.start();
	// t.join();
	// try (Tx tx = graph.tx()) {
	// assertNull(boot.userRoot().findByUsername("testuser" + u));
	// System.out.println("RUN: " + i.get());
	// }
	//
	// }

	// @Test
	// public void testMultiThreadedModifications() throws InterruptedException {
	// User user = user();
	//
	// Runnable task2 = () -> {
	// try (Tx tx = graph.tx()) {
	// user.setUsername("test2");
	// assertNotNull(boot.userRoot().findByUsername("test2"));
	// tx.success();
	// }
	// assertNotNull(boot.userRoot().findByUsername("test2"));
	//
	// Runnable task = () -> {
	// try (Tx tx = db.tx()) {
	// user.setUsername("test3");
	// assertNotNull(boot.userRoot().findByUsername("test3"));
	// tx.failure();
	// }
	// assertNotNull(boot.userRoot().findByUsername("test2"));
	// assertNull(boot.userRoot().findByUsername("test3"));
	//
	// };
	// Thread t = new Thread(task);
	// t.start();
	// try {
	// t.join();
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// };
	// Thread t2 = new Thread(task2);
	// t2.start();
	// t2.join();
	// try (Tx tx = graph.tx()) {
	// assertNull(boot.userRoot().findByUsername("test3"));
	// assertNotNull(boot.userRoot().findByUsername("test2"));
	// }
	//
	// }
	//
	@Test
	public void testAsyncTxFailed() throws Throwable {
		CompletableFuture<AsyncResult<Object>> cf = new CompletableFuture<>();
		graph.asyncTx(tx -> {
			tx.failed();
		}, rh -> {
			cf.complete(rh);
		});
		assertFalse(cf.get().succeeded());
	}

	@Test(expected = RuntimeException.class)
	public void testAsyncTxWithError() throws Throwable {
		CompletableFuture<Throwable> cf = new CompletableFuture<>();
		graph.asyncTx(tx -> {
			throw new RuntimeException("error");
		}, rh -> {
			cf.complete(rh.cause());
		});
		assertEquals("error", cf.get().getMessage());
		throw cf.get();
	}

	@Test
	public void testAsyncTxNestedAsync() throws InterruptedException, ExecutionException {
		CompletableFuture<AsyncResult<Object>> cf = new CompletableFuture<>();
		graph.asyncTx(tx -> {
			run(() -> {
				sleep(1000);
				tx.complete("OK");
			});
		}, rh -> {
			System.out.println("Completed async tx");
			cf.complete(rh);
		});
		assertTrue(cf.get().succeeded());
		assertEquals("OK", cf.get().result());
	}

	@Test
	public void testAsyncTxSuccess() throws Throwable {
		CompletableFuture<AsyncResult<Object>> cf = new CompletableFuture<>();
		graph.asyncTx(tx -> {
			tx.complete("OK");
		}, rh -> {
			cf.complete(rh);
		});
		assertEquals("OK", cf.get().result());
	}

}
