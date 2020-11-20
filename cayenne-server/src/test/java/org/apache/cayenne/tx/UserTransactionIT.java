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

package org.apache.cayenne.tx;

import static org.junit.Assert.assertEquals;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.log.JdbcEventLogger;
import org.apache.cayenne.testdo.testmap.Artist;
import org.apache.cayenne.unit.di.server.CayenneProjects;
import org.apache.cayenne.unit.di.server.ServerCase;
import org.apache.cayenne.unit.di.server.UseServerRuntime;
import org.junit.Test;
import org.mockito.Mockito;

@UseServerRuntime(CayenneProjects.TESTMAP_PROJECT)
public class UserTransactionIT extends ServerCase {

	@Inject
	private ObjectContext context;

	@Inject
	private JdbcEventLogger logger;

	@Test
	public void testCommit() throws Exception {

		Artist a = context.newObject(Artist.class);
		a.setArtistName("AAA");

		Transaction t = mockTransaction(new CayenneTransaction(logger));
		BaseTransaction.bindThreadTransaction(t);

		try {
			context.commitChanges();
		} finally {
			t.rollback();
			BaseTransaction.bindThreadTransaction(null);
		}

		// assertEquals(0, t.commitCount);
		Mockito.verify(t, Mockito.never()).commit();
		assertEquals(1, t.getConnections().size());
	}

	private Transaction mockTransaction(Transaction delegate) {
		Transaction res = Mockito.mock(Transaction.class);
		Mockito.doAnswer(invo -> {
			delegate.begin();
			return null;
		}).when(res).begin();
		Mockito.doAnswer(invo -> {
			delegate.commit();
			return null;
		}).when(res).commit();
		Mockito.doAnswer(invo -> {
			delegate.rollback();
			return null;
		}).when(res).rollback();
		Mockito.doAnswer(invo -> {
			delegate.setRollbackOnly();
			return null;
		}).when(res).setRollbackOnly();
		Mockito.when(res.isRollbackOnly()).thenAnswer(invo -> {
			return delegate.isRollbackOnly();
		});
		try {
			Mockito.when(res.getOrCreateConnection(Mockito.anyString(), Mockito.any())).thenAnswer(invo -> {
				String connectionName = invo.getArgument(0);
				DataSource dataSource = invo.getArgument(1);
				return delegate.getOrCreateConnection(connectionName, dataSource);
			});
			Mockito.when(res.getConnections()).thenAnswer(invo -> {
				return delegate.getConnections();
			});
			Mockito.doAnswer(invo -> {
				TransactionListener listener = invo.getArgument(0);
				delegate.addListener(listener);
				return null;
			}).when(res).addListener(Mockito.any());
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return res;
	}

	class TxWrapper implements Transaction {

		int commitCount;
		private Transaction delegate;

		TxWrapper(Transaction delegate) {
			this.delegate = delegate;
		}

		public void begin() {
			delegate.begin();
		}

		public void commit() {
			commitCount++;
			delegate.commit();
		}

		public void rollback() {
			delegate.rollback();
		}

		public void setRollbackOnly() {
			delegate.setRollbackOnly();
		}

		public boolean isRollbackOnly() {
			return delegate.isRollbackOnly();
		}

		@Override
		public Connection getOrCreateConnection(String connectionName, DataSource dataSource) throws SQLException {
			return delegate.getOrCreateConnection(connectionName, dataSource);
		}

		@Override
		public Map<String, Connection> getConnections() {
			return delegate.getConnections();
		}

		@Override
		public void addListener(TransactionListener listener) {
			delegate.addListener(listener);
		}

		@Override
		public boolean isExternal() {
			return false;
		}
	}

}
