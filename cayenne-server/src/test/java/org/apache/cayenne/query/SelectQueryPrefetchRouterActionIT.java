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

package org.apache.cayenne.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.cayenne.access.OperationObserver;
import org.apache.cayenne.access.QueryEngine;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.map.DataMap;
import org.apache.cayenne.map.EntityResolver;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.testdo.testmap.Artist;
import org.apache.cayenne.testdo.testmap.Gallery;
import org.apache.cayenne.testdo.testmap.Painting;
import org.apache.cayenne.tx.BaseTransaction;
import org.apache.cayenne.unit.di.server.CayenneProjects;
import org.apache.cayenne.unit.di.server.ServerCase;
import org.apache.cayenne.unit.di.server.UseServerRuntime;
import org.junit.Test;
import org.mockito.Mockito;

@UseServerRuntime(CayenneProjects.TESTMAP_PROJECT)
public class SelectQueryPrefetchRouterActionIT extends ServerCase {

	@Inject
	private EntityResolver resolver;

	@Test
	public void testPaintings1() {
		ObjEntity paintingEntity = resolver.getObjEntity(Painting.class);
		SelectQuery q = new SelectQuery(Artist.class, ExpressionFactory.matchExp("artistName", "abc"));
		q.addPrefetch(Artist.PAINTING_ARRAY.disjoint());

		SelectQueryPrefetchRouterAction action = new SelectQueryPrefetchRouterAction();

		MockQueryRouter router = new MockQueryRouter();
		action.route(q, router, resolver);
		assertEquals(1, router.getQueryCount());

		PrefetchSelectQuery prefetch = (PrefetchSelectQuery) router.getQueries().get(0);

		assertSame(paintingEntity, prefetch.getRoot());
		assertEquals(ExpressionFactory.exp("db:toArtist.ARTIST_NAME = 'abc'"), prefetch.getQualifier());
	}

	@Test
	public void testPrefetchPaintings2() {
		ObjEntity paintingEntity = resolver.getObjEntity(Painting.class);

		SelectQuery<Artist> q = new SelectQuery<>(Artist.class,
				ExpressionFactory.exp("artistName = 'abc' or artistName = 'xyz'"));
		q.addPrefetch(Artist.PAINTING_ARRAY.disjoint());

		SelectQueryPrefetchRouterAction action = new SelectQueryPrefetchRouterAction();

		MockQueryRouter router = new MockQueryRouter();
		action.route(q, router, resolver);
		assertEquals(1, router.getQueryCount());

		PrefetchSelectQuery prefetch = (PrefetchSelectQuery) router.getQueries().get(0);
		assertSame(paintingEntity, prefetch.getRoot());
		assertEquals(ExpressionFactory.exp("db:toArtist.ARTIST_NAME = 'abc' or db:toArtist.ARTIST_NAME = 'xyz'"),
				prefetch.getQualifier());
	}

	@Test
	public void testGalleries() {
		ObjEntity galleryEntity = resolver.getObjEntity(Gallery.class);
		SelectQuery q = new SelectQuery(Artist.class, ExpressionFactory.matchExp("artistName", "abc"));
		q.addPrefetch("paintingArray.toGallery");

		SelectQueryPrefetchRouterAction action = new SelectQueryPrefetchRouterAction();

		// MockQueryRouter router = new MockQueryRouter();
		QueryRouter router = Mockito.mock(QueryRouter.class);
		List queries = new ArrayList();
		Mockito.doAnswer(invo -> {
			queries.add(invo.getArgument(1));
			return null;
		}).when(router).route(Mockito.any(), Mockito.any(), Mockito.any());
		Mockito.when(router.engineForDataMap(Mockito.any())).thenAnswer(invo -> {
			DataMap map = invo.getArgument(0);
			Map results = new HashMap();
			EntityResolver entityResolver = null;
			int[] runCount = { 0 };
			QueryEngine res = Mockito.mock(QueryEngine.class);
			Mockito.doAnswer(invo1 -> {
				Collection que = invo.getArgument(0);
				OperationObserver resultConsumer = invo.getArgument(1);
				BaseTransaction transaction = invo.getArgument(2);
				runCount[0]++;

				// stick preset results to the consumer
				Iterator it = que.iterator();
				while (it.hasNext()) {
					Query query = (Query) it.next();
					resultConsumer.nextRows(query, (List) results.get(query));
				}
				return null;
			}).when(res).performQueries(Mockito.anyCollection(), Mockito.any());
			Mockito.when(res.getEntityResolver()).thenAnswer(invo1 -> {
				return entityResolver;
			});
			return res;
		});
		Mockito.when(router.engineForName(Mockito.anyString())).thenAnswer(invo -> {
			String name = invo.getArgument(0);
			Map results = new HashMap();
			EntityResolver entityResolver = null;
			int[] runCount = { 0 };
			QueryEngine res = Mockito.mock(QueryEngine.class);
			Mockito.doAnswer(invo1 -> {
				Collection qur = invo.getArgument(0);
				OperationObserver resultConsumer = invo.getArgument(1);
				BaseTransaction transaction = invo.getArgument(2);
				runCount[0]++;

				// stick preset results to the consumer
				Iterator it = qur.iterator();
				while (it.hasNext()) {
					Query query = (Query) it.next();
					resultConsumer.nextRows(query, (List) results.get(query));
				}
				return null;
			}).when(res).performQueries(Mockito.anyCollection(), Mockito.any());
			Mockito.when(res.getEntityResolver()).thenAnswer(invo1 -> {
				return entityResolver;
			});
			return res;
		});
		action.route(q, router, resolver);
		assertEquals(1, queries.size());

		PrefetchSelectQuery prefetch = (PrefetchSelectQuery) Collections.unmodifiableList(queries).get(0);

		assertSame(galleryEntity, prefetch.getRoot());
		assertEquals(ExpressionFactory.exp("db:paintingArray.toArtist.ARTIST_NAME = 'abc'"), prefetch.getQualifier());
	}
}
