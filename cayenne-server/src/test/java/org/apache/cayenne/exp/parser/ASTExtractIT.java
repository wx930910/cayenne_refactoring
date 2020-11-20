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

package org.apache.cayenne.exp.parser;

import java.util.Calendar;
import java.util.List;

import org.apache.cayenne.CayenneRuntimeException;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.exp.FunctionExpressionFactory;
import org.apache.cayenne.exp.Property;
import org.apache.cayenne.query.ObjectSelect;
import org.apache.cayenne.test.jdbc.DBHelper;
import org.apache.cayenne.testdo.date_time.DateTestEntity;
import org.apache.cayenne.unit.UnitDbAdapter;
import org.apache.cayenne.unit.di.server.CayenneProjects;
import org.apache.cayenne.unit.di.server.ServerCase;
import org.apache.cayenne.unit.di.server.UseServerRuntime;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @since 4.0
 */
@UseServerRuntime(CayenneProjects.DATE_TIME_PROJECT)
public class ASTExtractIT extends ServerCase {

    @Inject
    private ObjectContext context;

    @Inject
    private DBHelper dbHelper;

    @Inject
    private UnitDbAdapter unitDbAdapter;

    @Before
    public void createDataSet() throws Exception {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MILLISECOND, 0);

        DateTestEntity o1 = context.newObject(DateTestEntity.class);
        cal.set(2015, Calendar.FEBRUARY, 28,
                0, 0, 0);
        o1.setDateColumn(cal.getTime());

        cal.set(2017, Calendar.MARCH, 30,
                0, 0, 0);
        o1.setTimeColumn(cal.getTime());

        cal.set(Calendar.DAY_OF_MONTH, 29);
        o1.setTimestampColumn(cal.getTime());

        DateTestEntity o2 = context.newObject(DateTestEntity.class);
        cal.set(2016, Calendar.MARCH, 29,
                0, 0, 0);
        o2.setDateColumn(cal.getTime());

        cal.set(2017, Calendar.APRIL, 1,
                23, 59, 39);
        o2.setTimeColumn(cal.getTime());

        cal.set(Calendar.DAY_OF_MONTH, 2);
        o2.setTimestampColumn(cal.getTime());

        context.commitChanges();
    }


    @Test
    public void testYear() {
        Expression exp = ExpressionFactory.exp("year(dateColumn) = 2015");

        try {
            long res = ObjectSelect.query(DateTestEntity.class, exp).selectCount(context);
            assertEquals(1, res);
        } catch (CayenneRuntimeException e) {
            if(unitDbAdapter.supportsExtractPart(ASTExtract.DateTimePart.YEAR)) {
                throw e;
            } // else ok
        }
    }

    @Test
    public void testMonth() {
        Expression exp = ExpressionFactory.exp("month(dateColumn) = 3");

        try {
            long res = ObjectSelect.query(DateTestEntity.class, exp).selectCount(context);
            assertEquals(1, res);
        } catch (CayenneRuntimeException e) {
            if(unitDbAdapter.supportsExtractPart(ASTExtract.DateTimePart.MONTH)) {
                throw e;
            } // else ok
        }
    }

    @Test
    public void testWeek() {
        // 13 or 14 depends of first day in week in current db
        Expression exp = ExpressionFactory.exp("week(dateColumn) in (13, 14)");
        try {
            long res = ObjectSelect.query(DateTestEntity.class, exp).selectCount(context);
            assertEquals(1, res);
        } catch (CayenneRuntimeException e) {
            if(unitDbAdapter.supportsExtractPart(ASTExtract.DateTimePart.WEEK)) {
                throw e;
            } // else ok
        }
    }

    @Test
    public void testDayOfYear() {
        // day can start from 0
        Expression exp = ExpressionFactory.exp("dayOfYear(dateColumn) in (59, 58)");
        try {
            long res = ObjectSelect.query(DateTestEntity.class, exp).selectCount(context);
            assertEquals(1, res);
        } catch (CayenneRuntimeException e) {
            if(unitDbAdapter.supportsExtractPart(ASTExtract.DateTimePart.DAY_OF_YEAR)) {
                throw e;
            } // else ok
        }
    }

    @Test
    public void testDayOfYearSelect() {
        Property<Integer> dayOfYear = Property.create(
                FunctionExpressionFactory.yearExp(DateTestEntity.DATE_COLUMN.path()), Integer.class);

        try {
            List<Integer> res = ObjectSelect.query(DateTestEntity.class).column(dayOfYear).select(context);
            assertEquals(2, res.size());
        } catch (CayenneRuntimeException e) {
            if(unitDbAdapter.supportsExtractPart(ASTExtract.DateTimePart.DAY_OF_YEAR)) {
                throw e;
            } // else ok
        }
    }

    @Test
    public void testDay() {
        Expression exp = ExpressionFactory.exp("day(dateColumn) = 28");
        try {
            long res = ObjectSelect.query(DateTestEntity.class, exp).selectCount(context);
            assertEquals(1, res);
        } catch (CayenneRuntimeException e) {
            if(unitDbAdapter.supportsExtractPart(ASTExtract.DateTimePart.DAY)) {
                throw e;
            } // else ok
        }
    }

    @Test
    public void testDayOfMonth() {
        Expression exp = ExpressionFactory.exp("dayOfMonth(dateColumn) = 28");
        try {
            long res = ObjectSelect.query(DateTestEntity.class, exp).selectCount(context);
            assertEquals(1, res);
        } catch (CayenneRuntimeException e) {
            if(unitDbAdapter.supportsExtractPart(ASTExtract.DateTimePart.DAY_OF_MONTH)) {
                throw e;
            } // else ok
        }
    }

    @Test
    public void testDayOfWeek() {
        Expression exp = ExpressionFactory.exp("dayOfWeek(dateColumn) in (2, 3)");
        try {
            long res = ObjectSelect.query(DateTestEntity.class, exp).selectCount(context);
            assertEquals(1, res);
        } catch (CayenneRuntimeException e) {
            if(unitDbAdapter.supportsExtractPart(ASTExtract.DateTimePart.DAY_OF_WEEK)) {
                throw e;
            }
        }
    }

    @Test
    public void testHour() {
        Expression exp = ExpressionFactory.exp("hour(timestampColumn) = 23");
        try {
            long res = ObjectSelect.query(DateTestEntity.class, exp).selectCount(context);
            assertEquals(1, res);
        } catch (CayenneRuntimeException e) {
            if(unitDbAdapter.supportsExtractPart(ASTExtract.DateTimePart.HOUR)) {
                throw e;
            } // else ok
        }
    }

    @Test
    public void testMinute() {
        Expression exp = ExpressionFactory.exp("minute(timestampColumn) = 59");
        try {
            long res = ObjectSelect.query(DateTestEntity.class, exp).selectCount(context);
            assertEquals(1, res);
        } catch (CayenneRuntimeException e) {
            if(unitDbAdapter.supportsExtractPart(ASTExtract.DateTimePart.MINUTE)) {
                throw e;
            }
        }
    }

    @Test
    public void testSecond() {
        Expression exp = ExpressionFactory.exp("second(timestampColumn) = 39");
        try {
            long res = ObjectSelect.query(DateTestEntity.class, exp).selectCount(context);
            assertEquals(1, res);
        } catch (CayenneRuntimeException e) {
            if(unitDbAdapter.supportsExtractPart(ASTExtract.DateTimePart.SECOND)) {
                throw e;
            }
        }
    }
}
