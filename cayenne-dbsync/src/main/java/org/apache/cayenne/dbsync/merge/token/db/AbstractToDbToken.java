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

package org.apache.cayenne.dbsync.merge.token.db;

import org.apache.cayenne.dba.DbAdapter;
import org.apache.cayenne.dbsync.merge.context.MergeDirection;
import org.apache.cayenne.dbsync.merge.context.MergerContext;
import org.apache.cayenne.dbsync.merge.token.AbstractMergerToken;
import org.apache.cayenne.dbsync.merge.token.MergerToken;
import org.apache.cayenne.log.JdbcEventLogger;
import org.apache.cayenne.map.DbAttribute;
import org.apache.cayenne.map.DbEntity;
import org.apache.cayenne.validation.SimpleValidationFailure;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * Common abstract superclass for all {@link MergerToken}s going from the model
 * to the database.
 */
public abstract class AbstractToDbToken extends AbstractMergerToken {

	protected AbstractToDbToken(String tokenName, int sortingWeight) {
		super(tokenName, sortingWeight);
	}

	@Override
	public void execute(MergerContext mergerContext) {
		for (String sql : createSql(mergerContext.getDataNode().getAdapter())) {
			executeSql(mergerContext, sql);
		}
	}

	void executeSql(MergerContext mergerContext, String sql) {
		JdbcEventLogger logger = mergerContext.getDataNode().getJdbcEventLogger();
		logger.log(sql);

		try (Connection conn = mergerContext.getDataNode().getDataSource().getConnection()) {
			try (Statement st = conn.createStatement()) {
				st.execute(sql);
			}
		} catch (SQLException e) {
			mergerContext.getValidationResult().addFailure(new SimpleValidationFailure(sql, e.getMessage()));
			logger.logQueryError(e);
		}
	}

	public abstract List<String> createSql(DbAdapter adapter);

	@Override
	public final MergeDirection getDirection() {
		return MergeDirection.TO_DB;
	}

	abstract static class Entity extends AbstractToDbToken {

		private final DbEntity entity;

		protected Entity(String tokenName, int sortingWeight, DbEntity entity) {
			super(tokenName, sortingWeight);
			this.entity = entity;
		}

		public DbEntity getEntity() {
			return entity;
		}

		public String getTokenValue() {
			return getEntity().getName();
		}
	}

	abstract static class EntityAndColumn extends Entity {

		private final DbAttribute column;

		protected EntityAndColumn(String tokenName, int sortingWeight, DbEntity entity, DbAttribute column) {
			super(tokenName, sortingWeight, entity);
			this.column = column;
		}

		public DbAttribute getColumn() {
			return column;
		}

		@Override
		public String getTokenValue() {
			return getEntity().getName() + "." + getColumn().getName();
		}
	}
}
