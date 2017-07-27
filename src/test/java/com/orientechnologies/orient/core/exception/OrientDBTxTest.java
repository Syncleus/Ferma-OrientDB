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
package com.orientechnologies.orient.core.exception;

import static com.syncleus.ferma.ext.orientdb.util.TestUtils.run;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import com.syncleus.ferma.ext.orientdb.model.Person;
import com.syncleus.ferma.ext.orientdb.vertx.AbstractOrientDBVertexTest;
import com.syncleus.ferma.tx.Tx;

import io.vertx.core.AsyncResult;

public class OrientDBTxTest extends AbstractOrientDBVertexTest {

	private Person p;

	@Test
	public void testAsyncTxRetryHandling() throws Exception {
		CompletableFuture<AsyncResult<Object>> fut = new CompletableFuture<>();
		AtomicInteger e = new AtomicInteger(0);
		graph.asyncTx(tx -> {
			e.incrementAndGet();
			if (e.get() == 1) {
				String msg = "Cannot UPDATE the record #13:8 because the version is not the latest. Probably you are updating an old record or it has been modified by another user (db=v7 your=v6)";
				// "test #9:1 blub adsd"
				throw new OConcurrentModificationException(msg);
			} else {
				tx.complete("OK");
			}
		} , rh -> {
			fut.complete(rh);
		});
		AsyncResult<Object> result = fut.get(5, TimeUnit.SECONDS);
		assertEquals(2, e.get());
		assertEquals("OK", result.result());
		assertTrue(result.succeeded());
		assertNull(result.cause());
	}

	@Test
	public void testAsyncTxRetryHandling2() throws Exception {
		// Test creation of user in current thread
		int nFriendsBefore;
		try (Tx tx = graph.tx()) {
			p = addPersonWithFriends(tx.getGraph(), "Person2");
			manipulatePerson(tx.getGraph(), p);
			tx.success();
			nFriendsBefore = p.getFriends().size();
		}

		CyclicBarrier b = new CyclicBarrier(3);
		AtomicInteger i = new AtomicInteger(0);

		run(() -> {
			graph.tx(tx -> {
				i.incrementAndGet();

				System.out.println("Tx1");
				addFriend(Tx.getActive().getGraph(), p);
				tx.complete();
				if (i.get() <= 2) {
					b.await();
				}
			} , rh -> {
				System.out.println("Completed");
			});
		});

		run(() -> {
			graph.tx(tx -> {
				i.incrementAndGet();

				System.out.println("Tx2");
				addFriend(Tx.getActive().getGraph(), p);
				tx.complete();
				if (i.get() <= 2) {
					b.await();
				}
			} , rh -> {
				System.out.println("Completed");
			});
		});

		b.await();
		Thread.sleep(1000);
		System.out.println("Asserting");
		try (Tx tx = graph.tx()) {
			p = tx.getGraph().getFramedVertexExplicit(Person.class, p.getId());
			int nFriendsAfter = p.getFriends().size();
			assertEquals(nFriendsBefore + 2, nFriendsAfter);
		}

	}

	@Test
	public void testTxConflictHandling() throws InterruptedException, BrokenBarrierException, TimeoutException {
		// Test creation of user in current thread
		int nFriendsBefore;
		try (Tx tx = graph.tx()) {
			p = addPersonWithFriends(tx.getGraph(), "Person2");
			manipulatePerson(tx.getGraph(), p);
			tx.success();
			nFriendsBefore = p.getFriends().size();
		}

		CyclicBarrier b = new CyclicBarrier(3);

		addFriendToPerson(p, b);
		addFriendToPerson(p, b);

		b.await();
		Thread.sleep(1000);
		try (Tx tx = graph.tx()) {
			p = tx.getGraph().getFramedVertexExplicit(Person.class, p.getId());
			int nFriendsAfter = p.getFriends().size();
			assertEquals(nFriendsBefore + 2, nFriendsAfter);
		}

	}

	private void addFriendToPerson(Person p, CyclicBarrier b) {
		run(() -> {
			for (int retry = 0; retry < 10; retry++) {
				System.out.println("Try: " + retry);
				boolean doRetry = false;
				// try {
				try (Tx tx = graph.tx()) {
					addFriend(tx.getGraph(), p);
					tx.success();
					if (retry == 0) {
						try {
							b.await();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					break;
				} catch (OConcurrentModificationException e) {
					// throw e;
					// break;
				}
				// } catch (OConcurrentModificationException e) {
				// System.out.println("Error " + OConcurrentModificationException.class.getName());
				// doRetry = true;
				// }
				// if (!doRetry) {
				// break;
				// }
				System.out.println("Retry");
			}
		});
	}
}
