package com.syncleus.ferma.ext.orientdb;

import org.apache.tinkerpop.gremlin.orientdb.OrientTransaction;

import com.syncleus.ferma.tx.AbstractTx;

public class DelegatingFramedOrientTransaction extends AbstractTx<DelegatingFramedOrientGraph> {

	public DelegatingFramedOrientTransaction(OrientTransaction tx, DelegatingFramedOrientGraph delegatingFramedOrientGraph) {
		super(tx, delegatingFramedOrientGraph);
	}

}
