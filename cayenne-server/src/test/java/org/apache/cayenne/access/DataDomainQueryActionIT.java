/*****************************************************************
 *   Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 ****************************************************************/
package org.apache.cayenne.access;

import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.List;

import org.apache.cayenne.cache.QueryCache;
import org.apache.cayenne.cache.QueryCacheEntryFactory;
import org.apache.cayenne.configuration.server.ServerRuntime;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.query.QueryCacheStrategy;
import org.apache.cayenne.query.SelectQuery;
import org.apache.cayenne.testdo.testmap.Painting;
import org.apache.cayenne.unit.di.server.CayenneProjects;
import org.apache.cayenne.unit.di.server.ServerCase;
import org.apache.cayenne.unit.di.server.UseServerRuntime;
import org.junit.After;
import org.junit.Test;
import org.mockito.Mockito;

@UseServerRuntime(CayenneProjects.TESTMAP_PROJECT)
public class DataDomainQueryActionIT extends ServerCase {

	@Inject
	private DataContext context;

	@Inject
	private ServerRuntime runtime;

	@After
	public void tearDown() {
		runtime.getDataDomain().resetProperties();
	}

	@Test
	public void testCachedQuery() {

		DataDomain domain = runtime.getDataDomain();

		Painting p = context.newObject(Painting.class);
		p.setPaintingTitle("sample");

		SelectQuery query = new SelectQuery(Painting.class);

		query.addPrefetch(Painting.TO_GALLERY.disjoint());
		query.addPrefetch(Painting.TO_ARTIST.disjoint());
		query.addOrdering(Painting.PAINTING_TITLE.asc());
		query.setCacheStrategy(QueryCacheStrategy.SHARED_CACHE);
		query.setPageSize(5);

		QueryCache cache = domain.queryCache;
		QueryCache c = Mockito.mock(QueryCache.class);
		Mockito.when(c.get(Mockito.any(), Mockito.any())).thenAnswer(invo -> {
			QueryCacheEntryFactory factory = invo.getArgument(1);
			Object results = factory.createObject();
			assertTrue("Query cache is not serializable.", results instanceof Serializable);
			return null;
		});
		Mockito.doAnswer(invo -> {
			List results = invo.getArgument(1);
			assertTrue("Query cache is not serializable.", results instanceof Serializable);
			return null;
		}).when(c).put(Mockito.any(), Mockito.anyList());

		domain.queryCache = c;

		DataDomainQueryAction action = new DataDomainQueryAction(context, domain, query);
		action.execute();

		domain.queryCache = cache;
	}

}
