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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.util.Collections;
import java.util.Date;

import org.apache.cayenne.Cayenne;
import org.apache.cayenne.DataObject;
import org.apache.cayenne.DataRow;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.ObjectId;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.testdo.testmap.Artist;
import org.apache.cayenne.testdo.testmap.Gallery;
import org.apache.cayenne.testdo.testmap.Painting;
import org.apache.cayenne.unit.di.server.CayenneProjects;
import org.apache.cayenne.unit.di.server.ServerCase;
import org.apache.cayenne.unit.di.server.UseServerRuntime;
import org.junit.Test;
import org.mockito.Mockito;

@UseServerRuntime(CayenneProjects.TESTMAP_PROJECT)
public class ObjectStoreIT extends ServerCase {

	@Inject
	private DataContext context;

	private DataObject mockDataObj() {
		ObjectId[] objectId = new ObjectId[1];
		int[] persistenceState = new int[1];
		DataContext context = null;
		ObjectContext[] objectContext = new ObjectContext[1];
		DataObject res = Mockito.mock(DataObject.class);
		Mockito.when(res.getObjectId()).thenAnswer(invo -> {
			return objectId[0];
		});
		Mockito.doAnswer(invo -> {
			objectId[0] = invo.getArgument(0);
			return null;
		}).when(res).setObjectId(Mockito.any(ObjectId.class));
		Mockito.when(res.getPersistenceState()).thenReturn(persistenceState[0]);
		Mockito.doAnswer(invo -> {
			persistenceState[0] = invo.getArgument(0);
			return null;
		}).when(res).setPersistenceState(Mockito.anyInt());
		Mockito.when(res.getObjectContext()).thenReturn(objectContext[0]);
		Mockito.doAnswer(invo -> {
			objectContext[0] = invo.getArgument(0);
			return null;
		}).when(res).setObjectContext(Mockito.any());
		return res;
	}

	@Test
	public void testRegisteredObjectsCount() throws Exception {

		assertEquals(0, context.getObjectStore().registeredObjectsCount());

		DataObject o1 = mockDataObj();
		o1.setObjectId(new ObjectId("T", "key1", "v1"));
		context.getObjectStore().registerNode(o1.getObjectId(), o1);
		assertEquals(1, context.getObjectStore().registeredObjectsCount());

		// test object with same id
		DataObject o2 = mockDataObj();
		o2.setObjectId(new ObjectId("T", "key1", "v1"));
		context.getObjectStore().registerNode(o2.getObjectId(), o2);
		assertEquals(1, context.getObjectStore().registeredObjectsCount());

		// test new object
		DataObject o3 = mockDataObj();
		o3.setObjectId(new ObjectId("T", "key3", "v3"));
		context.getObjectStore().registerNode(o3.getObjectId(), o3);
		assertEquals(2, context.getObjectStore().registeredObjectsCount());
	}

	@Test
	public void testObjectsUnregistered() throws Exception {

		DataRow row = new DataRow(10);
		row.put("ARTIST_ID", new Integer(1));
		row.put("ARTIST_NAME", "ArtistXYZ");
		row.put("DATE_OF_BIRTH", new Date());
		DataObject object = context.objectFromDataRow(Artist.class, row);
		ObjectId oid = object.getObjectId();

		// insert object into the ObjectStore
		context.getObjectStore().registerNode(oid, object);
		assertSame(object, context.getObjectStore().getNode(oid));
		assertNotNull(context.getObjectStore().getCachedSnapshot(oid));

		context.getObjectStore().objectsUnregistered(Collections.singletonList(object));

		assertEquals(oid, object.getObjectId());
		assertNull(context.getObjectStore().getNode(oid));

		// in the future this may not be the case
		assertNull(context.getObjectStore().getCachedSnapshot(oid));
	}

	@Test
	public void testUnregisterThenRegister() throws Exception {

		// Create a gallery.
		Gallery g = context.newObject(Gallery.class);
		g.setGalleryName("Test Gallery");

		// Create an artist in the same context.
		Artist a = context.newObject(Artist.class);
		a.setArtistName("Test Artist");

		// Create a painting in the same context.
		Painting p = context.newObject(Painting.class);
		p.setPaintingTitle("Test Painting");

		// Set the painting's gallery.
		p.setToGallery(g);
		assertEquals(g, p.getToGallery());

		// Unregister the painting from the context.
		context.unregisterObjects(Collections.singletonList(p));

		// Make sure that even though the painting has been removed from the
		// context's
		// object graph that the reference to the gallery is the same.
		assertEquals(g, p.getToGallery());

		// Now, set the relationship between "p" & "a." Since "p" is not
		// registered with a
		// context, but "a" is, "p" should be auto-registered with the context
		// of "a."
		p.setToArtist(a);

		// Now commit the gallery, artist, & painting.
		context.commitChanges();

		// Check one last time that the painting's gallery is set to what we
		// expect.
		assertEquals(g, p.getToGallery());

		// Now, retrieve the same painting from the DB. Note that the gallery
		// relationship
		// is null even though according to our painting, that should not be the
		// case; a
		// NULL
		// value has been recorded to the DB for the painting's gallery_id
		// field.
		//
		// The full object graph is not being re-registered during
		// auto-registration
		// with the context.
		Painting newP = (Painting) Cayenne.objectForPK(context, p.getObjectId());
		assertNotNull(newP.getToGallery());
	}
}
