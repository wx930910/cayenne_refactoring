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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.cayenne.dba.DbAdapter;
import org.apache.cayenne.dbsync.reverse.filters.CatalogFilter;
import org.apache.cayenne.dbsync.reverse.filters.SchemaFilter;
import org.apache.cayenne.map.DbEntity;
import org.apache.cayenne.map.DetectedDbEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class EntityLoader extends PerCatalogAndSchemaLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(DbLoader.class);

    private final String[] types;

    EntityLoader(DbAdapter adapter, DbLoaderConfiguration config, DbLoaderDelegate delegate) {
        super(adapter, config, delegate);
        types = getTableTypes();
    }

    @Override
    protected ResultSet getResultSet(String catalogName, String schemaName, DatabaseMetaData metaData) throws SQLException {
        return metaData.getTables(catalogName, schemaName, WILDCARD, types);
    }

    @Override
    protected void processResultSetRow(CatalogFilter catalog, SchemaFilter schema, DbLoadDataStore map, ResultSet rs) throws SQLException {
        String name = rs.getString("TABLE_NAME");
        String catalogName = rs.getString("TABLE_CAT");
        String schemaName = rs.getString("TABLE_SCHEM");
        String type = rs.getString("TABLE_TYPE");

        // Oracle 9i and newer has a nifty recycle bin feature...
        // but we don't want dropped tables to be included here;
        // in fact they may even result in errors on reverse engineering
        // as their names have special chars like "/", etc.
        // So skip them all together (it's about "name == null" check)
        if (name == null || !schema.tables.isIncludeTable(name)) {
            return;
        }

        // check catalogName for null was added because postgres
        // for some reasons returns null for table's catalog
        if (!(catalog.name == null || catalogName == null || catalog.name.equals(catalogName))
                || !(schema.name == null || schema.name.equals(schemaName))) {
            LOGGER.error(catalogName + "." + schema + "." + schemaName + " wrongly loaded for catalog/schema : "
                    + catalog.name + "." + schema.name);
            return;
        }

        DetectedDbEntity table = new DetectedDbEntity(name);
        table.setCatalog(catalogName);
        table.setSchema(schemaName);
        table.setType(type);
        addDbEntityToMap(table, map);
    }

    private void addDbEntityToMap(DetectedDbEntity table, DbLoadDataStore map) {
        DbEntity oldEnt = map.addDbEntitySafe(table);
        if (oldEnt != null) {
            LOGGER.warn("Overwrite DbEntity: " + oldEnt.getName());
            delegate.dbEntityRemoved(oldEnt);
        }
        delegate.dbEntityAdded(table);
    }

    private String[] getTableTypes() {
        String[] configTypes = config.getTableTypes();
        String viewType = adapter.tableTypeForView();
        String tableType = adapter.tableTypeForTable();

        List<String> resultTableTypes = new ArrayList<>();
        if(configTypes == null || configTypes.length == 0) {
            addTypeToList(viewType, resultTableTypes);
            addTypeToList(tableType, resultTableTypes);
        } else {
            for(String type : configTypes) {
                if(type.equalsIgnoreCase("TABLE")) {
                    addTypeToList(tableType, resultTableTypes);
                } else if(type.equalsIgnoreCase("VIEW")) {
                    addTypeToList(viewType, resultTableTypes);
                } else {
                    addTypeToList(type, resultTableTypes);
                }
            }
        }

        return resultTableTypes.toArray(new String[0]);
    }

    private void addTypeToList(String type, List<String> tableTypes) {
        if(type != null) {
            tableTypes.add(type);
        }
    }
}
