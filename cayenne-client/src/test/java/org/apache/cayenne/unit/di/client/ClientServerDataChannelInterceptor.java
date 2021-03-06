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
package org.apache.cayenne.unit.di.client;

import org.apache.cayenne.configuration.rop.client.ClientRuntime;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.di.Provider;
import org.apache.cayenne.graph.GraphChangeHandler;
import org.apache.cayenne.remote.service.LocalConnection;
import org.apache.cayenne.unit.di.DataChannelInterceptor;
import org.apache.cayenne.unit.di.UnitTestClosure;
import org.mockito.Mockito;

public class ClientServerDataChannelInterceptor implements DataChannelInterceptor {

	@Inject
	protected Provider<ClientRuntime> clientRuntimeProvider;

	private ClientServerDataChannelDecorator getChannelDecorator() {

		LocalConnection connection = (LocalConnection) clientRuntimeProvider.get().getConnection();

		return (ClientServerDataChannelDecorator) connection.getChannel();
	}

	public void runWithQueriesBlocked(UnitTestClosure closure) {
		ClientServerDataChannelDecorator channel = getChannelDecorator();

		channel.setBlockingMessages(true);
		try {
			closure.execute();
		} finally {
			channel.setBlockingMessages(false);
		}
	}

	public int runWithQueryCounter(UnitTestClosure closure) {
		throw new UnsupportedOperationException("TODO... unused for now");
	}

	public GraphChangeHandler runWithSyncStatsCollection(UnitTestClosure closure) {
		ClientServerDataChannelDecorator channel = getChannelDecorator();

		GraphChangeHandler stats = Mockito.mock(GraphChangeHandler.class);

		channel.setSyncStatsCounter(stats);
		try {
			closure.execute();
		} finally {
			channel.setSyncStatsCounter(null);
		}

		return stats;
	}

}
