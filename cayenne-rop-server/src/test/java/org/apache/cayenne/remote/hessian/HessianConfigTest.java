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

package org.apache.cayenne.remote.hessian;

import com.caucho.hessian.io.AbstractSerializerFactory;
import com.caucho.hessian.io.SerializerFactory;
import org.apache.cayenne.map.EntityResolver;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class HessianConfigTest {

    @Test
    public void testLoadFactoryNoExtensions() {
        SerializerFactory factory = HessianConfig.createFactory(null, null);
        assertNotNull(factory);
    }

    @Test
    public void testLoadFactoryNoInjection() throws Exception {
        AbstractSerializerFactory factory = HessianConfig.loadFactory(
                MockAbstractSerializerFactory.class.getName(),
                null);

        assertTrue(factory instanceof MockAbstractSerializerFactory);
        assertNull(((MockAbstractSerializerFactory) factory).getEntityResolver());
    }

    @Test
    public void testLoadFactoryInjection() throws Exception {
        EntityResolver resolver = new EntityResolver();
        AbstractSerializerFactory factory = HessianConfig.loadFactory(
                MockAbstractSerializerFactory.class.getName(),
                resolver);

        assertTrue(factory instanceof MockAbstractSerializerFactory);
        assertSame(resolver, ((MockAbstractSerializerFactory) factory)
                .getEntityResolver());
    }
}
