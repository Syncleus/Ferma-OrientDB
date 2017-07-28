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

import com.syncleus.ferma.ext.orientdb.OrientDBFactory;
import com.syncleus.ferma.ext.orientdb.OrientDBTypeResolver;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;

public class OrientDBTxFactoryImpl implements OrientDBFactory {

	protected OrientGraphFactory factory;

	private OrientDBTypeResolver typeResolver;

	public OrientDBTxFactoryImpl(OrientGraphFactory factory, String... basePaths) {
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

	@Override
	public int getMaxRetry() {
		return 10;
	}

}