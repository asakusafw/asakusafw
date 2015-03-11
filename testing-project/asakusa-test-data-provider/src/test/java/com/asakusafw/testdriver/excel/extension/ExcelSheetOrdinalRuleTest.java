/**
 * Copyright 2011-2015 Asakusa Framework Team.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.asakusafw.testdriver.excel.extension;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.Calendar;

import org.junit.Test;

import com.asakusafw.testdriver.core.PropertyName;
import com.asakusafw.testdriver.core.PropertyType;
import com.asakusafw.testdriver.core.TestContext;
import com.asakusafw.testdriver.core.VerifyContext;
import com.asakusafw.testdriver.excel.ExcelRuleExtractor.FormatException;
import com.asakusafw.testdriver.rule.ValuePredicate;

/**
 * Test for {@link ExcelSheetOrdinalRule}.
 */
public class ExcelSheetOrdinalRuleTest {

    private static final Calendar NOW = Calendar.getInstance();

    /**
     * simple case for integral value.
     * @throws Exception if failed
     */
    @Test
    public void test_int() throws Exception {
        assertOk(PropertyType.INT, 5, "<", 9);
        assertNg(PropertyType.INT, 5, "<", 5);
        assertNg(PropertyType.INT, 5, "<", 1);

        assertNg(PropertyType.INT, 5, ">", 9);
        assertNg(PropertyType.INT, 5, ">", 5);
        assertOk(PropertyType.INT, 5, ">", 1);

        assertOk(PropertyType.INT, 5, "<=", 9);
        assertOk(PropertyType.INT, 5, "<=", 5);
        assertNg(PropertyType.INT, 5, "<=", 1);

        assertNg(PropertyType.INT, 5, ">=", 9);
        assertOk(PropertyType.INT, 5, ">=", 5);
        assertOk(PropertyType.INT, 5, ">=", 1);
    }

    /**
     * simple case for float value.
     * @throws Exception if failed
     */
    @Test
    public void test_float() throws Exception {
        assertOk(PropertyType.FLOAT, 0.5, "<", 0.9);
        assertNg(PropertyType.FLOAT, 0.5, "<", 0.5);
        assertNg(PropertyType.FLOAT, 0.5, "<", 0.1);

        assertNg(PropertyType.FLOAT, 0.5, ">", 0.9);
        assertNg(PropertyType.FLOAT, 0.5, ">", 0.5);
        assertOk(PropertyType.FLOAT, 0.5, ">", 0.1);

        assertOk(PropertyType.FLOAT, 0.5, "<=", 0.9);
        assertOk(PropertyType.FLOAT, 0.5, "<=", 0.5);
        assertNg(PropertyType.FLOAT, 0.5, "<=", 0.1);

        assertNg(PropertyType.FLOAT, 0.5, ">=", 0.9);
        assertOk(PropertyType.FLOAT, 0.5, ">=", 0.5);
        assertOk(PropertyType.FLOAT, 0.5, ">=", 0.1);
    }

    /**
     * simple case for decimal value.
     * @throws Exception if failed
     */
    @Test
    public void test_decimal() throws Exception {
        assertOk(PropertyType.DECIMAL, dec("5"), "<", dec("9"));
        assertNg(PropertyType.DECIMAL, dec("5"), "<", dec("5"));
        assertNg(PropertyType.DECIMAL, dec("5"), "<", dec("1"));

        assertNg(PropertyType.DECIMAL, dec("5"), ">", dec("9"));
        assertNg(PropertyType.DECIMAL, dec("5"), ">", dec("5"));
        assertOk(PropertyType.DECIMAL, dec("5"), ">", dec("1"));

        assertOk(PropertyType.DECIMAL, dec("5"), "<=", dec("9"));
        assertOk(PropertyType.DECIMAL, dec("5"), "<=", dec("5"));
        assertNg(PropertyType.DECIMAL, dec("5"), "<=", dec("1"));

        assertNg(PropertyType.DECIMAL, dec("5"), ">=", dec("9"));
        assertOk(PropertyType.DECIMAL, dec("5"), ">=", dec("5"));
        assertOk(PropertyType.DECIMAL, dec("5"), ">=", dec("1"));
    }

    /**
     * simple case for date value.
     * @throws Exception if failed
     */
    @Test
    public void test_date() throws Exception {
        assertOk(PropertyType.DATE, date(5), "<", date(9));
        assertNg(PropertyType.DATE, date(5), "<", date(5));
        assertNg(PropertyType.DATE, date(5), "<", date(1));

        assertNg(PropertyType.DATE, date(5), ">", date(9));
        assertNg(PropertyType.DATE, date(5), ">", date(5));
        assertOk(PropertyType.DATE, date(5), ">", date(1));

        assertOk(PropertyType.DATE, date(5), "<=", date(9));
        assertOk(PropertyType.DATE, date(5), "<=", date(5));
        assertNg(PropertyType.DATE, date(5), "<=", date(1));

        assertNg(PropertyType.DATE, date(5), ">=", date(9));
        assertOk(PropertyType.DATE, date(5), ">=", date(5));
        assertOk(PropertyType.DATE, date(5), ">=", date(1));
    }

    /**
     * simple case for date-time value.
     * @throws Exception if failed
     */
    @Test
    public void test_datetime() throws Exception {
        assertOk(PropertyType.DATETIME, datetime(5), "<", datetime(9));
        assertNg(PropertyType.DATETIME, datetime(5), "<", datetime(5));
        assertNg(PropertyType.DATETIME, datetime(5), "<", datetime(1));

        assertNg(PropertyType.DATETIME, datetime(5), ">", datetime(9));
        assertNg(PropertyType.DATETIME, datetime(5), ">", datetime(5));
        assertOk(PropertyType.DATETIME, datetime(5), ">", datetime(1));

        assertOk(PropertyType.DATETIME, datetime(5), "<=", datetime(9));
        assertOk(PropertyType.DATETIME, datetime(5), "<=", datetime(5));
        assertNg(PropertyType.DATETIME, datetime(5), "<=", datetime(1));

        assertNg(PropertyType.DATETIME, datetime(5), ">=", datetime(9));
        assertOk(PropertyType.DATETIME, datetime(5), ">=", datetime(5));
        assertOk(PropertyType.DATETIME, datetime(5), ">=", datetime(1));
    }

    /**
     * unsupported expression.
     * @throws Exception if failed
     */
    @Test
    public void unsupported() throws Exception {
        ValuePredicate<?> pred = parse(PropertyType.INT, "?");
        assertThat(pred, is(nullValue()));
    }

    /**
     * inconsistent type.
     * @throws Exception if failed
     */
    @Test(expected = FormatException.class)
    public void inconsistent_type() throws Exception {
        parse(PropertyType.BOOLEAN, "<");
    }

    private static void assertOk(
            PropertyType type, Object actual, String expr, Object expected) throws FormatException {
        ValuePredicate<Object> pred = parse(type, expr);
        assertThat(pred.describeExpected(expected, actual), pred.accepts(expected, actual), is(true));
    }

    private static void assertNg(
            PropertyType type, Object actual, String expr, Object expected) throws FormatException {
        ValuePredicate<Object> pred = parse(type, expr);
        assertThat(pred.describeExpected(expected, actual), pred.accepts(expected, actual), is(false));
    }

    private Number dec(String string) {
        return new BigDecimal(string);
    }

    private Calendar date(int offset) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.YEAR, NOW.get(Calendar.YEAR));
        calendar.set(Calendar.MONTH, NOW.get(Calendar.MONTH));
        calendar.set(Calendar.DATE, NOW.get(Calendar.DATE));
        calendar.add(Calendar.DATE, offset);
        return calendar;
    }

    private Calendar datetime(int offset) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.YEAR, NOW.get(Calendar.YEAR));
        calendar.set(Calendar.MONTH, NOW.get(Calendar.MONTH));
        calendar.set(Calendar.DATE, NOW.get(Calendar.DATE));
        calendar.set(Calendar.HOUR_OF_DAY, NOW.get(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE, NOW.get(Calendar.MINUTE));
        calendar.set(Calendar.SECOND, NOW.get(Calendar.SECOND));
        calendar.add(Calendar.SECOND, offset);
        return calendar;
    }

    @SuppressWarnings("unchecked")
    private static ValuePredicate<Object> parse(PropertyType type, String expr) throws FormatException {
        VerifyContext context = new VerifyContext(new TestContext.Empty());
        ExcelSheetOrdinalRule rule = new ExcelSheetOrdinalRule();
        return (ValuePredicate<Object>) rule.resolve(context, PropertyName.newInstance("testing"), type, expr);
    }
}
