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

package org.apache.cayenne.map;

import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.query.Query;
import org.apache.cayenne.query.QueryMetadata;
import org.apache.cayenne.query.SelectQuery;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 */
public class SelectQueryDescriptorTest {

    @Test
    public void testGetQueryType() throws Exception {
        SelectQueryDescriptor builder = QueryDescriptor.selectQueryDescriptor();
        builder.setRoot("FakeRoot");
        assertTrue(builder.buildQuery() instanceof SelectQuery);
    }

    @Test
    public void testGetQueryRoot() throws Exception {
        DataMap map = new DataMap();
        ObjEntity entity = new ObjEntity("A");
        map.addObjEntity(entity);

        SelectQueryDescriptor builder = QueryDescriptor.selectQueryDescriptor();
        builder.setRoot(entity);

        assertTrue(builder.buildQuery() instanceof SelectQuery);
        assertSame(entity, ((SelectQuery) builder.buildQuery()).getRoot());
    }

    @Test
    public void testGetQueryQualifier() throws Exception {
        SelectQueryDescriptor builder = QueryDescriptor.selectQueryDescriptor();
        builder.setRoot("FakeRoot");
        builder.setQualifier(ExpressionFactory.exp("abc = 5"));

        SelectQuery query = (SelectQuery) builder.buildQuery();

        assertEquals(ExpressionFactory.exp("abc = 5"), query.getQualifier());
    }

    @Test
    public void testGetQueryProperties() throws Exception {
        SelectQueryDescriptor builder = QueryDescriptor.selectQueryDescriptor();
        builder.setRoot("FakeRoot");
        builder.setProperty(QueryMetadata.FETCH_LIMIT_PROPERTY, "5");
        builder.setProperty(QueryMetadata.STATEMENT_FETCH_SIZE_PROPERTY, "6");

        Query query = builder.buildQuery();
        assertTrue(query instanceof SelectQuery);
        assertEquals(5, ((SelectQuery) query).getFetchLimit());
        
        assertEquals(6, ((SelectQuery) query).getStatementFetchSize());

        // TODO: test other properties...
    }
}
