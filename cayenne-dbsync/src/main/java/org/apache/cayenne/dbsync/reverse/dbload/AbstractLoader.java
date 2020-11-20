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

package org.apache.cayenne.dbsync.reverse.dbload;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Objects;

import org.apache.cayenne.dba.DbAdapter;

public abstract class AbstractLoader {

    static final String WILDCARD = "%";

    protected DbAdapter adapter;
    protected DbLoaderConfiguration config;
    protected DbLoaderDelegate delegate;

    AbstractLoader(DbAdapter adapter, DbLoaderConfiguration config, DbLoaderDelegate delegate) {
        this.adapter = adapter;
        this.config = Objects.requireNonNull(config);
        this.delegate = Objects.requireNonNull(delegate);
    }

    public abstract void load(DatabaseMetaData metaData, DbLoadDataStore map) throws SQLException;
}
