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

package org.apache.cayenne.modeler.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.apache.cayenne.map.event.MapEvent;
import org.junit.Test;
import org.mockito.Mockito;

/**
 */
public class ModelerEventTest {

	@Test
	public void testConstructor1() throws Exception {

		Object src = new Object();
		MapEvent event = Mockito.mock(MapEvent.class,
				Mockito.withSettings().defaultAnswer(Mockito.CALLS_REAL_METHODS).useConstructor(src));
		Mockito.when(event.getNewName()).thenReturn("");
		assertSame(src, event.getSource());
	}

	@Test
	public void testId() throws Exception {
		MapEvent event = Mockito.mock(MapEvent.class,
				Mockito.withSettings().defaultAnswer(Mockito.CALLS_REAL_METHODS).useConstructor(new Object()));
		Mockito.when(event.getNewName()).thenReturn("");
		assertEquals(MapEvent.CHANGE, event.getId());

		event.setId(MapEvent.ADD);
		assertEquals(MapEvent.ADD, event.getId());
	}

}
