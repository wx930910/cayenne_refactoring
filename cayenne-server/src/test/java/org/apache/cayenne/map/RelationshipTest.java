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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Test;
import org.mockito.Mockito;

public class RelationshipTest {

	@Test
	public void testName() throws Exception {
		Relationship rel = Mockito.mock(Relationship.class,
				Mockito.withSettings().defaultAnswer(Mockito.CALLS_REAL_METHODS).useConstructor());
		Mockito.when(rel.getTargetEntity()).thenReturn(null);
		Mockito.doNothing().when(rel).encodeAsXML(Mockito.any(), Mockito.any());
		Mockito.when(rel.getReverseRelationship()).thenReturn(null);
		Mockito.when(rel.isMandatory()).thenReturn(false);
		// Relationship rel = new MockRelationship();

		String tstName = "tst_name";
		rel.setName(tstName);
		assertEquals(tstName, rel.getName());
	}

	@Test
	public void testSourceEntity() {
		Relationship rel = Mockito.mock(Relationship.class,
				Mockito.withSettings().defaultAnswer(Mockito.CALLS_REAL_METHODS).useConstructor());
		Mockito.when(rel.getTargetEntity()).thenReturn(null);
		Mockito.doNothing().when(rel).encodeAsXML(Mockito.any(), Mockito.any());
		Mockito.when(rel.getReverseRelationship()).thenReturn(null);
		Mockito.when(rel.isMandatory()).thenReturn(false);
		// Relationship rel = new MockRelationship();
		Entity tstEntity = new MockEntity();
		rel.setSourceEntity(tstEntity);
		assertSame(tstEntity, rel.getSourceEntity());
	}

	@Test
	public void testTargetEntity() {
		Relationship rel = Mockito.mock(Relationship.class,
				Mockito.withSettings().defaultAnswer(Mockito.CALLS_REAL_METHODS).useConstructor());
		Mockito.when(rel.getTargetEntity()).thenReturn(null);
		Mockito.doNothing().when(rel).encodeAsXML(Mockito.any(), Mockito.any());
		Mockito.when(rel.getReverseRelationship()).thenReturn(null);
		Mockito.when(rel.isMandatory()).thenReturn(false);
		// Relationship rel = new MockRelationship();
		Entity tstEntity = new MockEntity();
		tstEntity.setName("abc");
		rel.setTargetEntityName(tstEntity);
		assertSame("abc", rel.getTargetEntityName());
	}

	@Test
	public void testTargetEntityName() {
		Relationship rel = Mockito.mock(Relationship.class,
				Mockito.withSettings().defaultAnswer(Mockito.CALLS_REAL_METHODS).useConstructor());
		Mockito.when(rel.getTargetEntity()).thenReturn(null);
		Mockito.doNothing().when(rel).encodeAsXML(Mockito.any(), Mockito.any());
		Mockito.when(rel.getReverseRelationship()).thenReturn(null);
		Mockito.when(rel.isMandatory()).thenReturn(false);
		// Relationship rel = new MockRelationship();
		rel.setTargetEntityName("abc");
		assertSame("abc", rel.getTargetEntityName());
	}
}
