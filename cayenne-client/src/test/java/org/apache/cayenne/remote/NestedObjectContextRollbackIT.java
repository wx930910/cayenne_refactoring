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
package org.apache.cayenne.remote;

import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.configuration.rop.client.ClientRuntime;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.remote.service.LocalConnection;
import org.apache.cayenne.testdo.mt.ClientMtTable1;
import org.apache.cayenne.unit.di.server.CayenneProjects;
import org.apache.cayenne.unit.di.server.UseServerRuntime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@UseServerRuntime(CayenneProjects.MULTI_TIER_PROJECT)
@RunWith(value=Parameterized.class)
public class NestedObjectContextRollbackIT extends RemoteCayenneCase {
    
    @Inject
    private ClientRuntime runtime;

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {LocalConnection.HESSIAN_SERIALIZATION},
                {LocalConnection.JAVA_SERIALIZATION},
                {LocalConnection.NO_SERIALIZATION},
        });
    }

    public NestedObjectContextRollbackIT(int serializationPolicy) {
        super.serializationPolicy = serializationPolicy;
    }

    @Test
    public void testRollbackChanges() {
        ObjectContext child1 = runtime.newContext(clientContext);
        
        assertFalse(clientContext.hasChanges());
        assertFalse(child1.hasChanges());
        
        clientContext.newObject(ClientMtTable1.class);
        child1.newObject(ClientMtTable1.class);
        
        assertTrue(clientContext.hasChanges());
        assertTrue(child1.hasChanges());
        
        child1.rollbackChanges();
        assertFalse(clientContext.hasChanges());
        assertFalse(child1.hasChanges());
        
        clientContext.rollbackChanges();
    }

    @Test
    public void testRollbackChangesLocally() {
        ObjectContext child1 = runtime.newContext(clientContext);
        
        assertFalse(clientContext.hasChanges());
        assertFalse(child1.hasChanges());
        
        clientContext.newObject(ClientMtTable1.class);
        child1.newObject(ClientMtTable1.class);
        
        assertTrue(clientContext.hasChanges());
        assertTrue(child1.hasChanges());
        
        child1.rollbackChangesLocally();
        assertTrue(clientContext.hasChanges());
        assertFalse(child1.hasChanges());
        
        clientContext.rollbackChanges();
    }
}
