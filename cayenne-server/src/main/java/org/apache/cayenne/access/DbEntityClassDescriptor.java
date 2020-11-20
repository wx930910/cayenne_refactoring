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

import org.apache.cayenne.CayenneRuntimeException;
import org.apache.cayenne.map.DbAttribute;
import org.apache.cayenne.map.DbEntity;
import org.apache.cayenne.map.DbRelationship;
import org.apache.cayenne.map.ObjAttribute;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.map.ObjRelationship;
import org.apache.cayenne.reflect.ClassDescriptor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A descriptor of a primary or secondary DbEntity for a given persistent class during
 * commit.
 * 
 * @since 3.0
 */
class DbEntityClassDescriptor {

    private ClassDescriptor classDescriptor;
    private List<DbRelationship> pathFromMaster;
    private DbEntity dbEntity;

    DbEntityClassDescriptor(ClassDescriptor classDescriptor) {
        this.classDescriptor = classDescriptor;
        this.dbEntity = classDescriptor.getEntity().getDbEntity();
    }

    DbEntityClassDescriptor(ClassDescriptor classDescriptor, ObjAttribute masterAttribute) {
        this.classDescriptor = classDescriptor;

        Iterator<?> it = masterAttribute.getDbPathIterator();

        if (masterAttribute.isFlattened()) {

            while (it.hasNext()) {
                Object object = it.next();
                if (object instanceof DbRelationship) {

                    if (pathFromMaster == null) {
                        pathFromMaster = new ArrayList<>(1);
                    }

                    pathFromMaster.add((DbRelationship) object);
                }
                else if (object instanceof DbAttribute) {
                    this.dbEntity = ((DbAttribute) object).getEntity();
                }
            }
        }

        if (dbEntity == null) {
            dbEntity = classDescriptor.getEntity().getDbEntity();
        }
    }

    DbEntityClassDescriptor(ClassDescriptor classDescriptor, ObjRelationship masterRelationship) {
        if(masterRelationship.getDbRelationships().size() > 2) {
            throw new CayenneRuntimeException("Only two step flattened relationships are supported, " + masterRelationship.getDbRelationshipPath());
        }
        this.classDescriptor = classDescriptor;
        DbRelationship pathRelationship = masterRelationship.getDbRelationships().get(0);
        pathFromMaster = new ArrayList<>(1);
        pathFromMaster.add(pathRelationship);
        dbEntity = pathRelationship.getTargetEntity();
    }

    boolean isMaster() {
        return pathFromMaster == null;
    }

    ClassDescriptor getClassDescriptor() {
        return classDescriptor;
    }

    List<DbRelationship> getPathFromMaster() {
        return pathFromMaster;
    }

    DbEntity getDbEntity() {
        return dbEntity;
    }

    ObjEntity getEntity() {
        return classDescriptor.getEntity();
    }
}
