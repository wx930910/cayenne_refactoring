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

package org.apache.cayenne.dbsync.merge.token.model;

import org.apache.cayenne.dbsync.merge.context.MergerContext;
import org.apache.cayenne.dbsync.merge.factory.MergerTokenFactory;
import org.apache.cayenne.dbsync.merge.token.MergerToken;
import org.apache.cayenne.map.DataMap;
import org.apache.cayenne.map.DbEntity;
import org.apache.cayenne.map.ObjEntity;

/**
 * A {@link MergerToken} to remove a {@link DbEntity} from a {@link DataMap}. Any
 * {@link ObjEntity} mapped to the {@link DbEntity} will also be removed.
 * 
 */
public class DropTableToModel extends AbstractToModelToken.Entity {

    public DropTableToModel(DbEntity entity) {
        super("Drop Table", 35, entity);
    }

    @Override
    public MergerToken createReverse(MergerTokenFactory factory) {
        return factory.createCreateTableToDb(getEntity());
    }

    @Override
    public void execute(MergerContext mergerContext) {
        for (ObjEntity objEntity : getEntity().mappedObjEntities()) {
            objEntity.getDataMap().removeObjEntity(objEntity.getName(), true);
            mergerContext.getDelegate().objEntityRemoved(objEntity);
        }
        getEntity().getDataMap().removeDbEntity(getEntity().getName(), true);
        mergerContext.getDelegate().dbEntityRemoved(getEntity());
    }
}
