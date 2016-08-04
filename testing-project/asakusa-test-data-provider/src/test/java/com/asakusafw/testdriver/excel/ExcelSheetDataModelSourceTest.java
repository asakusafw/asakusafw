/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
package com.asakusafw.testdriver.excel;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Calendar;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.Rule;
import org.junit.Test;

import com.asakusafw.testdriver.core.DataModelDefinition;
import com.asakusafw.testdriver.core.DataModelReflection;
import com.asakusafw.testdriver.model.SimpleDataModelDefinition;

/**
 * Test for {@link ExcelSheetDataModelSource}.
 */
public class ExcelSheetDataModelSourceTest {

    /**
     * temporary resource manager.
     */
    @Rule
    public final TemporaryResource resources = new TemporaryResource();

    static final DataModelDefinition<Simple> SIMPLE = new SimpleDataModelDefinition<>(Simple.class);

    /**
     * simple.
     * @throws Exception if occur
     */
    @Test
    public void simple() throws Exception {
        ExcelSheetDataModelSource source = open("simple.xls");
        Simple simple = next(source);
        assertThat(simple.number, is(100));
        assertThat(simple.text, is("Hello, world!"));
        end(source);
    }

    /**
     * using xlsx.
     * @throws Exception if occur
     */
    @Test
    public void xssf() throws Exception {
        ExcelSheetDataModelSource source = open("simple.xlsx");
        Simple simple = next(source);
        assertThat(simple.number, is(100));
        assertThat(simple.text, is("Hello, world!"));
        end(source);
    }

    /**
     * multiple rows.
     * @throws Exception if occur
     */
    @Test
    public void multiple() throws Exception {
        ExcelSheetDataModelSource source = open("multiple.xls");
        Simple r1 = next(source);
        assertThat(r1.number, is(10));
        assertThat(r1.text, is("aaa"));

        Simple r2 = next(source);
        assertThat(r2.number, is(20));
        assertThat(r2.text, is("bbb"));

        Simple r3 = next(source);
        assertThat(r3.number, is(30));
        assertThat(r3.text, is("ccc"));

        end(source);
    }

    /**
     * contains blank cells.
     * @throws Exception if occur
     */
    @Test
    public void blank_cell() throws Exception {
        ExcelSheetDataModelSource source = open("blank_cell.xls");
        Simple r1 = next(source);
        assertThat(r1.number, is(10));
        assertThat(r1.text, is(nullValue()));

        Simple r2 = next(source);
        assertThat(r2.number, is(nullValue()));
        assertThat(r2.text, is("a"));

        end(source);
    }

    /**
     * stringified by '.
     * @throws Exception if occur
     */
    @Test
    public void stringify() throws Exception {
        ExcelSheetDataModelSource source = open("stringify.xls");
        Simple r1 = next(source);
        assertThat(r1.number, is(100));
        assertThat(r1.text, is("100"));

        Simple r2 = next(source);
        assertThat(r2.number, is(200));
        assertThat(r2.text, is("200"));

        end(source);
    }

    /**
     * empty string.
     * @throws Exception if occur
     */
    @Test
    public void empty_string() throws Exception {
        ExcelSheetDataModelSource source = open("empty_string.xls");
        Simple r1 = next(source);
        assertThat(r1.text, is(""));

        end(source);
    }

    /**
     * boolean values.
     * @throws Exception if occur
     */
    @Test
    public void boolean_values() throws Exception {
        ExcelSheetDataModelSource source = open("boolean.xls");

        Simple r1 = next(source);
        assertThat(r1.booleanValue, is(true));

        Simple r2 = next(source);
        assertThat(r2.booleanValue, is(false));

        Simple r3 = next(source);
        assertThat(r3.booleanValue, is(true));

        Simple r4 = next(source);
        assertThat(r4.booleanValue, is(false));

        Simple r5 = next(source);
        assertThat(r5.booleanValue, is(true));

        end(source);
    }

    /**
     * byte values.
     * @throws Exception if occur
     */
    @Test
    public void byte_values() throws Exception {
        ExcelSheetDataModelSource source = open("byte.xls");

        Simple r1 = next(source);
        assertThat(r1.byteValue, is(Byte.MIN_VALUE));

        Simple r2 = next(source);
        assertThat(r2.byteValue, is(Byte.MAX_VALUE));

        Simple r3 = next(source);
        assertThat(r3.byteValue, is((byte) -50));

        Simple r4 = next(source);
        assertThat(r4.byteValue, is((byte) +2));

        Simple r5 = next(source);
        assertThat(r5.byteValue, is((byte) 1));

        end(source);
    }

    /**
     * short values.
     * @throws Exception if occur
     */
    @Test
    public void short_values() throws Exception {
        ExcelSheetDataModelSource source = open("short.xls");

        Simple r1 = next(source);
        assertThat(r1.shortValue, is(Short.MIN_VALUE));

        Simple r2 = next(source);
        assertThat(r2.shortValue, is(Short.MAX_VALUE));

        Simple r3 = next(source);
        assertThat(r3.shortValue, is((short) -999));

        Simple r4 = next(source);
        assertThat(r4.shortValue, is((short) +10000));

        Simple r5 = next(source);
        assertThat(r5.shortValue, is((short) 1));

        end(source);
    }

    /**
     * int values.
     * @throws Exception if occur
     */
    @Test
    public void int_values() throws Exception {
        ExcelSheetDataModelSource source = open("int.xls");

        Simple r1 = next(source);
        assertThat(r1.number, is(Integer.MIN_VALUE));

        Simple r2 = next(source);
        assertThat(r2.number, is(Integer.MAX_VALUE));

        Simple r3 = next(source);
        assertThat(r3.number, is(-100000));

        Simple r4 = next(source);
        assertThat(r4.number, is(+200000));

        Simple r5 = next(source);
        assertThat(r5.number, is(1));

        end(source);
    }

    /**
     * long values.
     * @throws Exception if occur
     */
    @Test
    public void long_values() throws Exception {
        ExcelSheetDataModelSource source = open("long.xls");

        Simple r1 = next(source);
        assertThat(r1.longValue, is(-5000L));

        Simple r2 = next(source);
        assertThat(r2.longValue, is(+6000L));

        Simple r3 = next(source);
        assertThat(r3.longValue, is(Long.MIN_VALUE));

        Simple r4 = next(source);
        assertThat(r4.longValue, is(Long.MAX_VALUE));

        Simple r5 = next(source);
        assertThat(r5.longValue, is(1L));

        end(source);
    }

    /**
     * float values.
     * @throws Exception if occur
     */
    @Test
    public void float_values() throws Exception {
        ExcelSheetDataModelSource source = open("float.xls");

        Simple r1 = next(source);
        assertThat(r1.floatValue, is(-1.0f));

        Simple r2 = next(source);
        assertThat(r2.floatValue, is(+2.5f));

        Simple r3 = next(source);
        assertThat(r3.floatValue, is(-1.25f));

        Simple r4 = next(source);
        assertThat(r4.floatValue, is(+1000.0f));

        Simple r5 = next(source);
        assertThat(r5.floatValue, is(1f));

        end(source);
    }

    /**
     * double values.
     * @throws Exception if occur
     */
    @Test
    public void double_values() throws Exception {
        ExcelSheetDataModelSource source = open("double.xls");

        Simple r1 = next(source);
        assertThat(r1.doubleValue, is(-1.0d));

        Simple r2 = next(source);
        assertThat(r2.doubleValue, is(+2.5d));

        Simple r3 = next(source);
        assertThat(r3.doubleValue, is(-1.25d));

        Simple r4 = next(source);
        assertThat(r4.doubleValue, is(+1000.0d));

        Simple r5 = next(source);
        assertThat(r5.doubleValue, is(1d));

        end(source);
    }

    /**
     * big integer values.
     * @throws Exception if occur
     */
    @Test
    public void integer_values() throws Exception {
        ExcelSheetDataModelSource source = open("integer.xls");

        Simple r1 = next(source);
        assertThat(r1.bigIntegerValue, is(dec(-5000L).toBigInteger()));

        Simple r2 = next(source);
        assertThat(r2.bigIntegerValue, is(dec(+6000L).toBigInteger()));

        Simple r3 = next(source);
        assertThat(r3.bigIntegerValue, is(dec(Long.MIN_VALUE).subtract(BigDecimal.ONE).toBigInteger()));

        Simple r4 = next(source);
        assertThat(r4.bigIntegerValue, is(dec(Long.MAX_VALUE).add(BigDecimal.ONE).toBigInteger()));

        Simple r5 = next(source);
        assertThat(r5.bigIntegerValue, is(BigDecimal.ONE.toBigInteger()));

        end(source);
    }

    /**
     * big decimal values.
     * @throws Exception if occur
     */
    @Test
    public void decimal_values() throws Exception {
        ExcelSheetDataModelSource source = open("decimal.xls");

        Simple r1 = next(source);
        assertThat(r1.bigDecimalValue, is(dec(-5000L)));

        Simple r2 = next(source);
        assertThat(r2.bigDecimalValue, is(dec(+6000.5)));

        Simple r3 = next(source);
        assertThat(r3.bigDecimalValue, is(dec(+6000.5)));

        Simple r4 = next(source);
        assertThat(r4.bigDecimalValue, is(new BigDecimal("1.4142135623730950488016887242097")));

        Simple r5 = next(source);
        assertThat(r5.bigDecimalValue, is(new BigDecimal("1.1")));

        end(source);
    }

    /**
     * date values.
     * @throws Exception if occur
     */
    @Test
    public void date_values() throws Exception {
        ExcelSheetDataModelSource source = open("date.xls");

        Simple r1 = next(source);
        assertThat(r1.dateValue, is(date(2011, 6, 13)));

        Simple r2 = next(source);
        assertThat(r2.dateValue, is(date(2040, 1, 1)));

        Simple r3 = next(source);
        assertThat(r3.dateValue, is(date(2014, 6, 6)));

        end(source);
    }

    private Calendar date(int year, int month, int date) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(year, month - 1, date);
        return calendar;
    }

    /**
     * date values.
     * @throws Exception if occur
     */
    @Test
    public void datetime_values() throws Exception {
        ExcelSheetDataModelSource source = open("datetime.xls");

        Simple r1 = next(source);
        assertThat(r1.datetimeValue, is(datetime(2011, 6, 13, 23, 59, 59)));

        Simple r2 = next(source);
        assertThat(r2.datetimeValue, is(datetime(2040, 1, 1, 2, 3, 4)));

        Simple r3 = next(source);
        assertThat(r3.datetimeValue, is(datetime(2014, 6, 6, 7, 8, 9)));

        end(source);
    }

    private Calendar datetime(
            int year, int month, int date,
            int hour, int minute, int second) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(year, month - 1, date, hour, minute, second);
        return calendar;
    }

    private BigDecimal dec(long value) {
        return new BigDecimal(value);
    }

    private BigDecimal dec(double value) {
        return new BigDecimal(value);
    }

    /**
     * invalid boolean values.
     * @throws Exception if occur
     */
    @Test(expected = IOException.class)
    public void boolean_error() throws Exception {
        ExcelSheetDataModelSource source = open("boolean_error.xls");
        next(source);
    }

    /**
     * integer overflow.
     * @throws Exception if occur
     */
    @Test(expected = IOException.class)
    public void number_outofrange() throws Exception {
        ExcelSheetDataModelSource source = open("number_outofrange.xls");
        next(source);
    }

    /**
     * long integer overflow.
     * @throws Exception if occur
     */
    @Test(expected = IOException.class)
    public void long_outofrange() throws Exception {
        ExcelSheetDataModelSource source = open("long_outofrange.xls");
        next(source);
    }

    /**
     * invalid integer.
     * @throws Exception if occur
     */
    @Test(expected = IOException.class)
    public void number_error() throws Exception {
        ExcelSheetDataModelSource source = open("number_error.xls");
        next(source);
    }

    /**
     * invalid double.
     * @throws Exception if occur
     */
    @Test(expected = IOException.class)
    public void double_error() throws Exception {
        ExcelSheetDataModelSource source = open("double_error.xls");
        next(source);
    }

    /**
     * invalid big decimal.
     * @throws Exception if occur
     */
    @Test(expected = IOException.class)
    public void decimal_error() throws Exception {
        ExcelSheetDataModelSource source = open("decimal_error.xls");
        next(source);
    }

    /**
     * invalid string.
     * @throws Exception if occur
     */
    @Test(expected = IOException.class)
    public void string_error() throws Exception {
        ExcelSheetDataModelSource source = open("string_error.xls");
        next(source);
    }

    /**
     * invalid date.
     * @throws Exception if occur
     */
    @Test(expected = IOException.class)
    public void date_error() throws Exception {
        ExcelSheetDataModelSource source = open("date_error.xls");
        next(source);
    }

    /**
     * contains blank row.
     * @throws Exception if occur
     */
    @Test
    public void blank_row() throws Exception {
        ExcelSheetDataModelSource source = open("blank_row.xls");
        Simple r1 = next(source);
        assertThat(r1.number, is(100));
        assertThat(r1.text, is("vvv"));

        Simple r2 = next(source);
        assertThat(r2.number, is(200));
        assertThat(r2.text, is("^^^"));

        end(source);
    }

    /**
     * contains blank row but is decorated.
     * @throws Exception if occur
     */
    @Test
    public void decorated_blank_row() throws Exception {
        ExcelSheetDataModelSource source = open("decorated_blank_row.xls");
        Simple r1 = next(source);
        assertThat(r1.number, is(100));
        assertThat(r1.text, is("vvv"));

        Simple r2 = next(source);
        assertThat(r2.number, is(200));
        assertThat(r2.text, is("^^^"));

        end(source);
    }

    /**
     * blank sheet.
     * @throws Exception if occur
     */
    @Test(expected = IOException.class)
    public void blank_sheet() throws Exception {
        open("blank_sheet.xls");
    }

    /**
     * blank sheet but is decorated.
     * @throws Exception if occur
     */
    @Test(expected = IOException.class)
    public void decorated_blank_sheet() throws Exception {
        open("decorated_blank_sheet.xls");
    }

    /**
     * invalid sheet header.
     * @throws Exception if occur
     */
    @Test(expected = IOException.class)
    public void invalid_header() throws Exception {
        open("invalid_header.xls");
    }

    /**
     * blank sheet but is decorated.
     * @throws Exception if occur
     */
    @Test(expected = IOException.class)
    public void unknown_property() throws Exception {
        open("unknown_property.xls");
    }

    /**
     * formula.
     * @throws Exception if occur
     */
    @Test(expected = IOException.class)
    public void formula() throws Exception {
        ExcelSheetDataModelSource source = open("formula_error.xls");
        source.next();
    }

    /**
     * error cell.
     * @throws Exception if occur
     */
    @Test(expected = IOException.class)
    public void cell_error() throws Exception {
        ExcelSheetDataModelSource source = open("cell_error.xls");
        source.next();
    }

    private ExcelSheetDataModelSource open(String file) throws IOException {
        URL resource = getClass().getResource("data/" + file);
        assertThat(file, resource, not(nullValue()));
        URI uri;
        try {
            uri = resource.toURI();
        } catch (URISyntaxException e) {
            throw new AssertionError(e);
        }
        try (InputStream in = resource.openStream()) {
            Workbook book = resources.bless(Util.openWorkbookFor(file, in));
            Sheet sheet = book.getSheetAt(0);
            return new ExcelSheetDataModelSource(SIMPLE, uri, sheet);
        }
    }

    private Simple next(ExcelSheetDataModelSource source) throws IOException {
        DataModelReflection next = source.next();
        assertThat(next, is(not(nullValue())));
        return SIMPLE.toObject(next);
    }

    private void end(ExcelSheetDataModelSource source) throws IOException {
        DataModelReflection next = source.next();
        assertThat(String.valueOf(next), next, nullValue());
        source.close();
    }
}
