/**
 * Copyright 2014 Asakusa Framework Team.
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.junit.Test;

import com.asakusafw.testdriver.core.PropertyName;
import com.asakusafw.testdriver.core.PropertyType;
import com.asakusafw.testdriver.core.TestContext;
import com.asakusafw.testdriver.core.VerifyContext;
import com.asakusafw.testdriver.excel.ExcelRuleExtractor.FormatException;
import com.asakusafw.testdriver.rule.ValuePredicate;

/**
 * Test for {@link ExcelSheetApproximateRule}.
 */
public class ExcelSheetApproximateRuleTest {

    /**
     * simple case for integral value.
     * @throws Exception if failed
     */
    @Test
    public void simple_int() throws Exception {
        ValuePredicate<Object> pred = parse(PropertyType.INT, "~10");
        assertNg(pred, 50, 30);
        assertNg(pred, 50, 39);
        assertOk(pred, 50, 40);
        assertOk(pred, 50, 45);
        assertOk(pred, 50, 50);
        assertOk(pred, 50, 55);
        assertOk(pred, 50, 60);
        assertNg(pred, 50, 61);
        assertNg(pred, 50, 70);
    }

    /**
     * simple case for integral value.
     * @throws Exception if failed
     */
    @Test
    public void simple_int_plus() throws Exception {
        ValuePredicate<Object> pred = parse(PropertyType.INT, "~+10");
        assertNg(pred, 50, 45);
        assertNg(pred, 50, 49);
        assertOk(pred, 50, 50);
        assertOk(pred, 50, 55);
        assertOk(pred, 50, 60);
        assertNg(pred, 50, 61);
        assertNg(pred, 50, 70);
    }

    /**
     * simple case for integral value.
     * @throws Exception if failed
     */
    @Test
    public void simple_int_minus() throws Exception {
        ValuePredicate<Object> pred = parse(PropertyType.INT, "~-10");
        assertNg(pred, 50, 30);
        assertNg(pred, 50, 39);
        assertOk(pred, 50, 40);
        assertOk(pred, 50, 45);
        assertOk(pred, 50, 50);
        assertNg(pred, 50, 51);
        assertNg(pred, 50, 55);
    }

    /**
     * invalid case for integral value.
     * @throws Exception if failed
     */
    @Test(expected = FormatException.class)
    public void simple_int_invalid() throws Exception {
        parse(PropertyType.INT, "~INVALID");
    }

    /**
     * simple case for float value.
     * @throws Exception if failed
     */
    @Test
    public void simple_float() throws Exception {
        ValuePredicate<Object> pred = parse(PropertyType.FLOAT, "~2.5");
        assertNg(pred, 15.0, 10.0);
        assertNg(pred, 15.0, 12.4);
        assertOk(pred, 15.0, 12.6);
        assertOk(pred, 15.0, 14.0);
        assertOk(pred, 15.0, 15.0);
        assertOk(pred, 15.0, 16.0);
        assertOk(pred, 15.0, 17.4);
        assertNg(pred, 15.0, 17.6);
        assertNg(pred, 15.0, 20.0);
    }

    /**
     * simple case for float value.
     * @throws Exception if failed
     */
    @Test
    public void simple_float_plus() throws Exception {
        ValuePredicate<Object> pred = parse(PropertyType.FLOAT, "~+2.5");
        assertNg(pred, 15.0, 13.0);
        assertNg(pred, 15.0, 14.9);
        assertOk(pred, 15.0, 15.0);
        assertOk(pred, 15.0, 16.0);
        assertOk(pred, 15.0, 17.4);
        assertNg(pred, 15.0, 17.6);
        assertNg(pred, 15.0, 20.0);
    }

    /**
     * simple case for float value.
     * @throws Exception if failed
     */
    @Test
    public void simple_float_minus() throws Exception {
        ValuePredicate<Object> pred = parse(PropertyType.FLOAT, "~-2.5");
        assertNg(pred, 15.0, 10.0);
        assertNg(pred, 15.0, 12.4);
        assertOk(pred, 15.0, 12.6);
        assertOk(pred, 15.0, 14.0);
        assertOk(pred, 15.0, 15.0);
        assertNg(pred, 15.0, 15.1);
        assertNg(pred, 15.0, 17.0);
    }

    /**
     * invalid case for float value.
     * @throws Exception if failed
     */
    @Test(expected = FormatException.class)
    public void simple_float_invalid() throws Exception {
        parse(PropertyType.FLOAT, "~INVALID");
    }


    /**
     * simple case for decimal value.
     * @throws Exception if failed
     */
    @Test
    public void simple_decimal() throws Exception {
        ValuePredicate<Object> pred = parse(PropertyType.DECIMAL, "~2.5");
        assertNg(pred, dec("15.0"), dec("10.0"));
        assertNg(pred, dec("15.0"), dec("12.4"));
        assertOk(pred, dec("15.0"), dec("12.5"));
        assertOk(pred, dec("15.0"), dec("14.0"));
        assertOk(pred, dec("15.0"), dec("15.0"));
        assertOk(pred, dec("15.0"), dec("16.0"));
        assertOk(pred, dec("15.0"), dec("17.5"));
        assertNg(pred, dec("15.0"), dec("17.6"));
        assertNg(pred, dec("15.0"), dec("20.0"));
    }

    /**
     * simple case for decimal value.
     * @throws Exception if failed
     */
    @Test
    public void simple_decimal_plus() throws Exception {
        ValuePredicate<Object> pred = parse(PropertyType.DECIMAL, "~+2.5");
        assertNg(pred, dec("15.0"), dec("13.0"));
        assertNg(pred, dec("15.0"), dec("14.9"));
        assertOk(pred, dec("15.0"), dec("15.0"));
        assertOk(pred, dec("15.0"), dec("16.0"));
        assertOk(pred, dec("15.0"), dec("17.5"));
        assertNg(pred, dec("15.0"), dec("17.6"));
        assertNg(pred, dec("15.0"), dec("20.0"));
    }

    /**
     * simple case for decimal value.
     * @throws Exception if failed
     */
    @Test
    public void simple_decimal_minus() throws Exception {
        ValuePredicate<Object> pred = parse(PropertyType.DECIMAL, "~-2.5");
        assertNg(pred, dec("15.0"), dec("10.0"));
        assertNg(pred, dec("15.0"), dec("12.4"));
        assertOk(pred, dec("15.0"), dec("12.5"));
        assertOk(pred, dec("15.0"), dec("14.0"));
        assertOk(pred, dec("15.0"), dec("15.0"));
        assertNg(pred, dec("15.0"), dec("15.1"));
        assertNg(pred, dec("15.0"), dec("17.0"));
    }

    /**
     * invalid case for decimal value.
     * @throws Exception if failed
     */
    @Test(expected = FormatException.class)
    public void simple_decimal_invalid() throws Exception {
        parse(PropertyType.DECIMAL, "~INVALID");
    }

    /**
     * simple case for date value.
     * @throws Exception if failed
     */
    @Test
    public void simple_date() throws Exception {
        ValuePredicate<Object> pred = parse(PropertyType.DATE, "~5");
        assertNg(pred, date("1970/01/15"), date("1970/01/05"));
        assertNg(pred, date("1970/01/15"), date("1970/01/09"));
        assertOk(pred, date("1970/01/15"), date("1970/01/10"));
        assertOk(pred, date("1970/01/15"), date("1970/01/12"));
        assertOk(pred, date("1970/01/15"), date("1970/01/15"));
        assertOk(pred, date("1970/01/15"), date("1970/01/18"));
        assertOk(pred, date("1970/01/15"), date("1970/01/20"));
        assertNg(pred, date("1970/01/15"), date("1970/01/21"));
        assertNg(pred, date("1970/01/15"), date("1970/01/25"));
    }

    /**
     * simple case for date value.
     * @throws Exception if failed
     */
    @Test
    public void simple_date_plus() throws Exception {
        ValuePredicate<Object> pred = parse(PropertyType.DATE, "~+5");
        assertNg(pred, date("1970/01/15"), date("1970/01/05"));
        assertNg(pred, date("1970/01/15"), date("1970/01/14"));
        assertOk(pred, date("1970/01/15"), date("1970/01/15"));
        assertOk(pred, date("1970/01/15"), date("1970/01/18"));
        assertOk(pred, date("1970/01/15"), date("1970/01/20"));
        assertNg(pred, date("1970/01/15"), date("1970/01/21"));
        assertNg(pred, date("1970/01/15"), date("1970/01/25"));
    }

    /**
     * simple case for date value.
     * @throws Exception if failed
     */
    @Test
    public void simple_date_minus() throws Exception {
        ValuePredicate<Object> pred = parse(PropertyType.DATE, "~-5");
        assertNg(pred, date("1970/01/15"), date("1970/01/05"));
        assertNg(pred, date("1970/01/15"), date("1970/01/09"));
        assertOk(pred, date("1970/01/15"), date("1970/01/10"));
        assertOk(pred, date("1970/01/15"), date("1970/01/12"));
        assertOk(pred, date("1970/01/15"), date("1970/01/15"));
        assertNg(pred, date("1970/01/15"), date("1970/01/16"));
        assertNg(pred, date("1970/01/15"), date("1970/01/25"));
    }

    /**
     * invalid case for date value.
     * @throws Exception if failed
     */
    @Test(expected = FormatException.class)
    public void simple_date_invalid() throws Exception {
        parse(PropertyType.DATE, "~INVALID");
    }

    /**
     * simple case for date-time value.
     * @throws Exception if failed
     */
    @Test
    public void simple_datetime() throws Exception {
        ValuePredicate<Object> pred = parse(PropertyType.DATETIME, "~15");
        assertNg(pred, datetime("1970/01/01 00:00:30"), datetime("1970/01/01 00:00:00"));
        assertNg(pred, datetime("1970/01/01 00:00:30"), datetime("1970/01/01 00:00:14"));
        assertOk(pred, datetime("1970/01/01 00:00:30"), datetime("1970/01/01 00:00:15"));
        assertOk(pred, datetime("1970/01/01 00:00:30"), datetime("1970/01/01 00:00:20"));
        assertOk(pred, datetime("1970/01/01 00:00:30"), datetime("1970/01/01 00:00:30"));
        assertOk(pred, datetime("1970/01/01 00:00:30"), datetime("1970/01/01 00:00:40"));
        assertOk(pred, datetime("1970/01/01 00:00:30"), datetime("1970/01/01 00:00:45"));
        assertNg(pred, datetime("1970/01/01 00:00:30"), datetime("1970/01/01 00:00:46"));
        assertNg(pred, datetime("1970/01/01 00:00:30"), datetime("1970/01/01 00:01:00"));
    }

    /**
     * simple case for date-time value.
     * @throws Exception if failed
     */
    @Test
    public void simple_datetime_plus() throws Exception {
        ValuePredicate<Object> pred = parse(PropertyType.DATETIME, "~+15");
        assertNg(pred, datetime("1970/01/01 00:00:30"), datetime("1970/01/01 00:00:00"));
        assertNg(pred, datetime("1970/01/01 00:00:30"), datetime("1970/01/01 00:00:29"));
        assertOk(pred, datetime("1970/01/01 00:00:30"), datetime("1970/01/01 00:00:30"));
        assertOk(pred, datetime("1970/01/01 00:00:30"), datetime("1970/01/01 00:00:40"));
        assertOk(pred, datetime("1970/01/01 00:00:30"), datetime("1970/01/01 00:00:45"));
        assertNg(pred, datetime("1970/01/01 00:00:30"), datetime("1970/01/01 00:00:46"));
        assertNg(pred, datetime("1970/01/01 00:00:30"), datetime("1970/01/01 00:01:00"));
    }

    /**
     * simple case for date-time value.
     * @throws Exception if failed
     */
    @Test
    public void simple_datetime_minus() throws Exception {
        ValuePredicate<Object> pred = parse(PropertyType.DATETIME, "~-15");
        assertNg(pred, datetime("1970/01/01 00:00:30"), datetime("1970/01/01 00:00:00"));
        assertNg(pred, datetime("1970/01/01 00:00:30"), datetime("1970/01/01 00:00:14"));
        assertOk(pred, datetime("1970/01/01 00:00:30"), datetime("1970/01/01 00:00:15"));
        assertOk(pred, datetime("1970/01/01 00:00:30"), datetime("1970/01/01 00:00:20"));
        assertOk(pred, datetime("1970/01/01 00:00:30"), datetime("1970/01/01 00:00:30"));
        assertNg(pred, datetime("1970/01/01 00:00:30"), datetime("1970/01/01 00:00:31"));
        assertNg(pred, datetime("1970/01/01 00:00:30"), datetime("1970/01/01 00:01:00"));
    }

    /**
     * invalid case for date-time value.
     * @throws Exception if failed
     */
    @Test(expected = FormatException.class)
    public void simple_datetime_invalid() throws Exception {
        parse(PropertyType.DATE, "~INVALID");
    }

    /**
     * unsupported expression.
     * @throws Exception if failed
     */
    @Test
    public void unsupported() throws Exception {
        ValuePredicate<Object> pred = parse(PropertyType.INT, "UNSUPPORTED");
        assertThat(pred, is(nullValue()));
    }

    /**
     * invalid expression.
     * @throws Exception if failed
     */
    @Test(expected = FormatException.class)
    public void invalid() throws Exception {
        parse(PropertyType.INT, "~");
    }

    /**
     * inconsistent type.
     * @throws Exception if failed
     */
    @Test(expected = FormatException.class)
    public void inconsistent_type() throws Exception {
        parse(PropertyType.BOOLEAN, "~1");
    }

    @SuppressWarnings("unchecked")
    private static ValuePredicate<Object> parse(PropertyType type, String expr) throws FormatException {
        VerifyContext context = new VerifyContext(new TestContext.Empty());
        ExcelSheetApproximateRule rule = new ExcelSheetApproximateRule();
        return (ValuePredicate<Object>) rule.resolve(context, PropertyName.newInstance("testing"), type, expr);
    }

    private static void assertOk(ValuePredicate<Object> pred, Object expected, Object actual) {
        assertThat(pred.describeExpected(expected, actual), pred.accepts(expected, actual), is(true));
    }

    private static void assertNg(ValuePredicate<Object> pred, Object expected, Object actual) {
        assertThat(pred.describeExpected(expected, actual), pred.accepts(expected, actual), is(false));
    }

    private BigDecimal dec(String string) {
        return new BigDecimal(string);
    }

    private Calendar date(String string) {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new SimpleDateFormat("yyyy/MM/dd").parse(string));
            Calendar result = Calendar.getInstance();
            result.clear();
            result.set(
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DATE));
            return result;
        } catch (ParseException e) {
            throw new AssertionError(e);
        }
    }

    private Calendar datetime(String string) {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").parse(string));
            Calendar result = Calendar.getInstance();
            result.clear();
            result.set(
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DATE),
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    calendar.get(Calendar.SECOND));
            return result;
        } catch (ParseException e) {
            throw new AssertionError(e);
        }
    }
}
