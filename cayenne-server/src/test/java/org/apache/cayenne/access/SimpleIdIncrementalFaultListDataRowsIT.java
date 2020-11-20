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

import org.apache.cayenne.DataRow;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.query.SelectQuery;
import org.apache.cayenne.query.SortOrder;
import org.apache.cayenne.test.jdbc.DBHelper;
import org.apache.cayenne.test.jdbc.TableHelper;
import org.apache.cayenne.testdo.testmap.Artist;
import org.apache.cayenne.unit.di.server.CayenneProjects;
import org.apache.cayenne.unit.di.server.ServerCase;
import org.apache.cayenne.unit.di.server.UseServerRuntime;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests IncrementalFaultList behavior when fetching data rows.
 */
@UseServerRuntime(CayenneProjects.TESTMAP_PROJECT)
public class SimpleIdIncrementalFaultListDataRowsIT extends ServerCase {

    @Inject
    private DataContext context;

    @Inject
    private DataContext context1;

    @Inject
    private DBHelper dbHelper;

    private TableHelper tArtist;
    private SimpleIdIncrementalFaultList<?> list;

    @Before
    public void setUp() throws Exception {
        tArtist = new TableHelper(dbHelper, "ARTIST");
        tArtist.setColumns("ARTIST_ID", "ARTIST_NAME");
        createArtistsDataSet();

        SelectQuery q = SelectQuery.dataRowQuery(Artist.class);
        q.setPageSize(6);
        q.addOrdering("db:ARTIST_ID", SortOrder.ASCENDING);

        list = new SimpleIdIncrementalFaultList<>(context, q, 10000);
    }

    protected void createArtistsDataSet() throws Exception {
        tArtist.insert(33001, "artist1");
        tArtist.insert(33002, "artist2");
        tArtist.insert(33003, "artist3");
        tArtist.insert(33004, "artist4");
        tArtist.insert(33005, "artist5");
        tArtist.insert(33006, "artist6");
        tArtist.insert(33007, "artist7");
        tArtist.insert(33008, "artist8");
        tArtist.insert(33009, "artist9");
        tArtist.insert(33010, "artist10");
        tArtist.insert(33011, "artist11");
        tArtist.insert(33012, "artist12");
        tArtist.insert(33013, "artist13");
        tArtist.insert(33014, "artist14");
        tArtist.insert(33015, "artist15");
        tArtist.insert(33016, "artist16");
        tArtist.insert(33017, "artist17");
        tArtist.insert(33018, "artist18");
        tArtist.insert(33019, "artist19");
        tArtist.insert(33020, "artist20");
        tArtist.insert(33021, "artist21");
        tArtist.insert(33022, "artist22");
        tArtist.insert(33023, "artist23");
        tArtist.insert(33024, "artist24");
        tArtist.insert(33025, "artist25");
    }

    @Test
    public void testGet1() throws Exception {
        assertEquals(1, list.idWidth);
        assertTrue(list.elements.get(0) instanceof Long);
        assertTrue(list.elements.get(19) instanceof Long);

        Object a = list.get(19);

        assertNotNull(a);
        assertTrue(a instanceof DataRow);
        assertEquals(3, ((DataRow) a).size());
        assertEquals("artist20", ((DataRow) a).get("ARTIST_NAME"));
    }

    @Test
    public void testIndexOf1() throws Exception {

        Expression qual = ExpressionFactory.matchExp("artistName", "artist20");
        SelectQuery<DataRow> query = SelectQuery.dataRowQuery(Artist.class, qual);
        List<?> artists = context1.performQuery(query);

        assertEquals(1, artists.size());

        DataRow row = (DataRow) artists.get(0);
        assertEquals(19, list.indexOf(row));

        DataRow clone = new DataRow(row);
        assertEquals(19, list.indexOf(clone));

        row.remove("ARTIST_ID");
        assertEquals(-1, list.indexOf(row));
    }

    @Test
    public void testIndexOf2() throws Exception {

        // resolve first page
        list.get(0);

        Expression qual = ExpressionFactory.matchExp("artistName", "artist2");
        SelectQuery<DataRow> query = SelectQuery.dataRowQuery(Artist.class, qual);
        List<?> artists = context1.performQuery(query);

        assertEquals(1, artists.size());

        DataRow row = (DataRow) artists.get(0);
        assertEquals(1, list.indexOf(row));

        row.remove("ARTIST_NAME");
        assertEquals(-1, list.indexOf(row));
    }

    @Test
    public void testLastIndexOf1() throws Exception {

        // resolve first page
        list.get(0);

        Expression qual = ExpressionFactory.matchExp("artistName", "artist3");
        SelectQuery<DataRow> query = SelectQuery.dataRowQuery(Artist.class, qual);
        List<?> artists = context1.performQuery(query);

        assertEquals(1, artists.size());

        DataRow row = (DataRow) artists.get(0);
        assertEquals(2, list.lastIndexOf(row));

        row.remove("ARTIST_NAME");
        assertEquals(-1, list.lastIndexOf(row));
    }

    @Test
    public void testLastIndexOf2() throws Exception {

        Expression qual = ExpressionFactory.matchExp("artistName", "artist20");
        SelectQuery<DataRow> query = SelectQuery.dataRowQuery(Artist.class, qual);
        List<?> artists = context1.performQuery(query);

        assertEquals(1, artists.size());

        DataRow row = (DataRow) artists.get(0);
        assertEquals(19, list.lastIndexOf(row));

        row.remove("ARTIST_ID");
        assertEquals(-1, list.lastIndexOf(row));
    }

    @Test
    public void testIterator() throws Exception {
        assertEquals(1, list.idWidth);

        Iterator<?> it = list.iterator();
        int counter = 0;
        while (it.hasNext()) {
            Object obj = it.next();
            assertNotNull(obj);
            assertTrue(
                    "Unexpected object class: " + obj.getClass().getName(),
                    obj instanceof DataRow);
            assertEquals(3, ((DataRow) obj).size());

            // iterator must be resolved page by page
            int expectedResolved = list.pageIndex(counter)
                    * list.getPageSize()
                    + list.getPageSize();
            if (expectedResolved > list.size()) {
                expectedResolved = list.size();
            }

            assertEquals(list.size() - expectedResolved, list.getUnfetchedObjects());

            if (list.getUnfetchedObjects() >= list.getPageSize()) {
                assertTrue(list.elements.get(list.size() - 1) instanceof Long);
            }

            counter++;
        }
    }
}
