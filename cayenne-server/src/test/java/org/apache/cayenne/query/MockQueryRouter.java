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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.cayenne.access.OperationObserver;
import org.apache.cayenne.access.QueryEngine;
import org.apache.cayenne.map.DataMap;
import org.apache.cayenne.map.EntityResolver;
import org.apache.cayenne.tx.BaseTransaction;
import org.mockito.Mockito;

public class MockQueryRouter implements QueryRouter {

	protected List queries = new ArrayList();

	public void reset() {
		this.queries = new ArrayList();
	}

	public List getQueries() {
		return Collections.unmodifiableList(queries);
	}

	public int getQueryCount() {
		return queries.size();
	}

	public void route(QueryEngine engine, Query query, Query substitutedQuery) {
		queries.add(query);
	}

	public QueryEngine engineForDataMap(DataMap map) {
		Map results = new HashMap();
		EntityResolver entityResolver = null;
		int[] runCount = { 0 };
		QueryEngine res = Mockito.mock(QueryEngine.class);
		Mockito.doAnswer(invo -> {
			Collection queries = invo.getArgument(0);
			OperationObserver resultConsumer = invo.getArgument(1);
			BaseTransaction transaction = invo.getArgument(2);
			runCount[0]++;

			// stick preset results to the consumer
			Iterator it = queries.iterator();
			while (it.hasNext()) {
				Query query = (Query) it.next();
				resultConsumer.nextRows(query, (List) results.get(query));
			}
			return null;
		}).when(res).performQueries(Mockito.anyCollection(), Mockito.any());
		Mockito.when(res.getEntityResolver()).thenAnswer(invo -> {
			return entityResolver;
		});
		return res;
	}

	@Override
	public QueryEngine engineForName(String name) {
		Map results = new HashMap();
		EntityResolver entityResolver = null;
		int[] runCount = { 0 };
		QueryEngine res = Mockito.mock(QueryEngine.class);
		Mockito.doAnswer(invo -> {
			Collection queries = invo.getArgument(0);
			OperationObserver resultConsumer = invo.getArgument(1);
			BaseTransaction transaction = invo.getArgument(2);
			runCount[0]++;

			// stick preset results to the consumer
			Iterator it = queries.iterator();
			while (it.hasNext()) {
				Query query = (Query) it.next();
				resultConsumer.nextRows(query, (List) results.get(query));
			}
			return null;
		}).when(res).performQueries(Mockito.anyCollection(), Mockito.any());
		Mockito.when(res.getEntityResolver()).thenAnswer(invo -> {
			return entityResolver;
		});
		return res;
		// return new MockQueryEngine();
	}
}
