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

package org.apache.cayenne;

import org.apache.cayenne.util.Util;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class ObjectIdTest {

    @Test
    public void testConstructor() {
        ObjectId temp1 = new ObjectId("e");
        assertEquals("e", temp1.getEntityName());
        assertTrue(temp1.isTemporary());
        assertNotNull(temp1.getKey());

        byte[] key = new byte[] { 1, 2, 3 };
        ObjectId temp2 = new ObjectId("e1", key);
        assertEquals("e1", temp2.getEntityName());
        assertTrue(temp2.isTemporary());
        assertSame(key, temp2.getKey());
    }

    @Test
    public void testSerializabilityTemp() throws Exception {
        ObjectId temp1 = new ObjectId("e");
        ObjectId temp2 = Util.cloneViaSerialization(temp1);

        assertTrue(temp1.isTemporary());
        assertNotSame(temp1, temp2);
        assertEquals(temp1, temp2);
    }

    @Test
    public void testSerializabilityPerm() throws Exception {
        ObjectId perm1 = new ObjectId("e", "a", "b");

        // make sure hashcode is resolved
        int h = perm1.hashCode();
        assertEquals(h, perm1.hashCode);
        assertTrue(perm1.hashCode != 0);

        ObjectId perm2 = Util.cloneViaSerialization(perm1);

        // make sure hashCode is reset to 0
        assertTrue(perm2.hashCode == 0);

        assertFalse(perm2.isTemporary());
        assertNotSame(perm1, perm2);
        assertEquals(perm1, perm2);
    }

    @Test
    public void testEquals0() {
        ObjectId oid1 = new ObjectId("TE");
        assertEquals(oid1, oid1);
        assertEquals(oid1.hashCode(), oid1.hashCode());
    }

    @Test
    public void testEquals1() {
        ObjectId oid1 = new ObjectId("T", "a", "b");
        ObjectId oid2 = new ObjectId("T", "a", "b");
        assertEquals(oid1, oid2);
        assertEquals(oid1.hashCode(), oid2.hashCode());
    }

    @Test
    public void testEquals2() {
        Map<String, Object> hm = new HashMap<>();
        ObjectId oid1 = new ObjectId("T", hm);
        ObjectId oid2 = new ObjectId("T", hm);
        assertEquals(oid1, oid2);
        assertEquals(oid1.hashCode(), oid2.hashCode());
    }

    @Test
    public void testEquals3() {
        String pknm = "xyzabc";

        Map<String, Object> hm1 = new HashMap<>();
        hm1.put(pknm, "123");

        Map<String, Object> hm2 = new HashMap<>();
        hm2.put(pknm, "123");

        ObjectId oid1 = new ObjectId("T", hm1);
        ObjectId oid2 = new ObjectId("T", hm2);
        assertEquals(oid1, oid2);
        assertEquals(oid1.hashCode(), oid2.hashCode());
    }

    /**
     * This is a test case reproducing conditions for the bug "8458963".
     */
    @Test
    public void testEquals5() {

        Map<String, Object> hm1 = new HashMap<>();
        hm1.put("key1", 1);
        hm1.put("key2", 11);

        Map<String, Object> hm2 = new HashMap<>();
        hm2.put("key1", 11);
        hm2.put("key2", 1);

        ObjectId ref = new ObjectId("T", hm1);
        ObjectId oid = new ObjectId("T", hm2);
        assertFalse(ref.equals(oid));
    }

    /**
     * Multiple key objectId
     */
    @Test
    public void testEquals6() {

        Map<String, Object> hm1 = new HashMap<>();
        hm1.put("key1", 1);
        hm1.put("key2", 2);

        Map<String, Object> hm2 = new HashMap<>();
        hm2.put("key1", 1);
        hm2.put("key2", 2);

        ObjectId ref = new ObjectId("T", hm1);
        ObjectId oid = new ObjectId("T", hm2);
        assertTrue(ref.equals(oid));
        assertEquals(ref.hashCode(), oid.hashCode());
    }

    /**
     * Checks that hashCode works even if keys are inserted in the map in a
     * different order...
     */
    @Test
    public void testEquals7() {

        // create maps with guaranteed iteration order

        @SuppressWarnings("unchecked")
        Map<String, Object> hm1 = new LinkedHashMap<>();
        hm1.put("KEY1", 1);
        hm1.put("KEY2", 2);

        @SuppressWarnings("unchecked")
        Map<String, Object> hm2 = new LinkedHashMap();
        // put same keys but in different order
        hm2.put("KEY2", 2);
        hm2.put("KEY1", 1);

        ObjectId ref = new ObjectId("T", hm1);
        ObjectId oid = new ObjectId("T", hm2);
        assertTrue(ref.equals(oid));
        assertEquals(ref.hashCode(), oid.hashCode());
    }

    @Test
    public void testEqualsBinaryKey() {

        Map<String, Object> hm1 = new HashMap<>();
        hm1.put("key1", new byte[] { 3, 4, 10, -1 });

        Map<String, Object> hm2 = new HashMap<>();
        hm2.put("key1", new byte[] { 3, 4, 10, -1 });

        ObjectId ref = new ObjectId("T", hm1);
        ObjectId oid = new ObjectId("T", hm2);
        assertEquals(ref.hashCode(), oid.hashCode());
        assertTrue(ref.equals(oid));
    }

    @Test
    public void testEqualsNull() {
        ObjectId o = new ObjectId("T", "ARTIST_ID", new Integer(42));
        assertFalse(o.equals(null));
    }

    @Test
    public void testIdAsMapKey() {
        Map<ObjectId, Object> map = new HashMap<>();
        Object o1 = new Object();

        String pknm = "xyzabc";

        Map<String, Object> hm1 = new HashMap<>();
        hm1.put(pknm, "123");

        Map<String, Object> hm2 = new HashMap<>();
        hm2.put(pknm, "123");

        ObjectId oid1 = new ObjectId("T", hm1);
        ObjectId oid2 = new ObjectId("T", hm2);

        map.put(oid1, o1);
        assertSame(o1, map.get(oid2));
    }

    @Test
    public void testNotEqual1() {

        ObjectId oid1 = new ObjectId("T1");
        ObjectId oid2 = new ObjectId("T2");
        assertFalse(oid1.equals(oid2));
    }

    @Test
    public void testNotEqual2() {

        Map<String, Object> hm1 = new HashMap<>();
        hm1.put("pk1", "123");

        Map<String, Object> hm2 = new HashMap<>();
        hm2.put("pk2", "123");

        ObjectId oid1 = new ObjectId("T", hm1);
        ObjectId oid2 = new ObjectId("T", hm2);
        assertFalse(oid1.equals(oid2));
    }

    /**
     * Test different numeric types.
     */
    @Test
    public void testEquals8() {

        // create maps with guaranteed iteration order

        @SuppressWarnings("unchecked")
        Map<String, Object> hm1 = new LinkedHashMap();
        hm1.put("KEY1", 1);
        hm1.put("KEY2", 2);

        @SuppressWarnings("unchecked")
        Map<String, Object> hm2 = new LinkedHashMap();
        // put same keys but in different order
        hm2.put("KEY2", new BigDecimal(2.00));
        hm2.put("KEY1", 1L);

        ObjectId ref = new ObjectId("T", hm1);
        ObjectId oid = new ObjectId("T", hm2);
        assertTrue(ref.equals(oid));
        assertEquals(ref.hashCode(), oid.hashCode());
    }

    @Test
    public void testToString() {
        Map<String, Object> m1 = new HashMap<>();
        m1.put("a", "1");
        m1.put("b", "2");
        ObjectId i1 = new ObjectId("e1", m1);

        Map<String, Object> m2 = new HashMap<>();
        m2.put("b", "2");
        m2.put("a", "1");

        ObjectId i2 = new ObjectId("e1", m2);

        assertEquals(i1, i2);
        assertEquals(i1.toString(), i2.toString());
    }
}
