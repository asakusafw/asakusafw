/**
 * Copyright 2011-2017 Asakusa Framework Team.
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
package com.asakusafw.runtime.io.csv;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import com.asakusafw.runtime.io.csv.CsvFormatException.Reason;
import com.asakusafw.runtime.value.BooleanOption;
import com.asakusafw.runtime.value.ByteOption;
import com.asakusafw.runtime.value.Date;
import com.asakusafw.runtime.value.DateOption;
import com.asakusafw.runtime.value.DateTime;
import com.asakusafw.runtime.value.DateTimeOption;
import com.asakusafw.runtime.value.DecimalOption;
import com.asakusafw.runtime.value.DoubleOption;
import com.asakusafw.runtime.value.FloatOption;
import com.asakusafw.runtime.value.IntOption;
import com.asakusafw.runtime.value.LongOption;
import com.asakusafw.runtime.value.ShortOption;
import com.asakusafw.runtime.value.StringOption;

/**
 * Test for {@link CsvParser}.
 */
public class CsvParserTest {

    /**
     * Tracks test names.
     */
    @Rule
    public final TestName testName = new TestName();

    private List<String> headers = CsvConfiguration.DEFAULT_HEADER_CELLS;

    private String trueFormat = CsvConfiguration.DEFAULT_TRUE_FORMAT;

    private String falseFormat = CsvConfiguration.DEFAULT_FALSE_FORMAT;

    private String dateFormat = CsvConfiguration.DEFAULT_DATE_FORMAT;

    private String dateTimeFormat = CsvConfiguration.DEFAULT_DATE_TIME_FORMAT;

    private CsvParser create(String content) {
        CsvConfiguration conf = createConfiguration();
        return new CsvParser(
                new ByteArrayInputStream(content.getBytes(conf.getCharset())),
                testName.getMethodName(),
                conf);
    }

    private CsvConfiguration createConfiguration() {
        CsvConfiguration conf = new CsvConfiguration(
                CsvConfiguration.DEFAULT_CHARSET,
                headers,
                trueFormat,
                falseFormat,
                dateFormat,
                dateTimeFormat);
        return conf;
    }

    /**
     * test for booleans.
     * @throws Exception if failed
     */
    @Test
    public void boolean_values() throws Exception {
        trueFormat = "true";
        falseFormat = "false";

        CsvParser parser = create("true,false,");
        BooleanOption option = new BooleanOption();

        assertThat(parser.next(), is(true));

        parser.fill(option);
        assertThat(option.get(), is(true));

        parser.fill(option);
        assertThat(option.get(), is(false));

        parser.fill(option);
        assertThat(option.isNull(), is(true));

        parser.endRecord();
        assertThat(parser.next(), is(false));
    }

    /**
     * test for bytes.
     * @throws Exception if failed
     */
    @Test
    public void byte_values() throws Exception {
        CsvParser parser = create("0,1,50,-1,-50,127,-128, 0,0 ,");
        ByteOption option = new ByteOption();

        assertThat(parser.next(), is(true));

        parser.fill(option);
        assertThat(option.get(), is((byte) 0));

        parser.fill(option);
        assertThat(option.get(), is((byte) 1));

        parser.fill(option);
        assertThat(option.get(), is((byte) 50));

        parser.fill(option);
        assertThat(option.get(), is((byte) -1));

        parser.fill(option);
        assertThat(option.get(), is((byte) -50));

        parser.fill(option);
        assertThat(option.get(), is((byte) 127));

        parser.fill(option);
        assertThat(option.get(), is((byte) -128));

        parser.fill(option);
        assertThat(option.get(), is((byte) 0));

        parser.fill(option);
        assertThat(option.get(), is((byte) 0));

        parser.fill(option);
        assertThat(option.isNull(), is(true));

        parser.endRecord();
        assertThat(parser.next(), is(false));
    }

    /**
     * test for invalid bytes.
     * @throws Exception if failed
     */
    @Test
    public void invalid_byte() throws Exception {
        CsvParser parser = create(String.valueOf(Byte.MAX_VALUE + 1));
        assertThat(parser.next(), is(true));
        try {
            parser.fill(new ByteOption());
            fail();
        } catch (CsvFormatException e) {
            assertThat(e.getStatus().getReason(), is(Reason.INVALID_CELL_FORMAT));
        }
    }

    /**
     * test for short values.
     * @throws Exception if failed
     */
    @Test
    public void short_values() throws Exception {
        CsvParser parser = create("0,1,50,-1,-50,"
                + Short.MAX_VALUE + ","
                + Short.MIN_VALUE + ", 0,0 ,");
        ShortOption option = new ShortOption();

        assertThat(parser.next(), is(true));

        parser.fill(option);
        assertThat(option.get(), is((short) 0));

        parser.fill(option);
        assertThat(option.get(), is((short) 1));

        parser.fill(option);
        assertThat(option.get(), is((short) 50));

        parser.fill(option);
        assertThat(option.get(), is((short) -1));

        parser.fill(option);
        assertThat(option.get(), is((short) -50));

        parser.fill(option);
        assertThat(option.get(), is(Short.MAX_VALUE));

        parser.fill(option);
        assertThat(option.get(), is(Short.MIN_VALUE));

        parser.fill(option);
        assertThat(option.get(), is((short) 0));

        parser.fill(option);
        assertThat(option.get(), is((short) 0));

        parser.fill(option);
        assertThat(option.isNull(), is(true));

        parser.endRecord();
        assertThat(parser.next(), is(false));
    }

    /**
     * test for invalid short values.
     * @throws Exception if failed
     */
    @Test
    public void invalid_short() throws Exception {
        CsvParser parser = create(String.valueOf(Short.MAX_VALUE + 1));
        assertThat(parser.next(), is(true));
        try {
            parser.fill(new ShortOption());
            fail();
        } catch (CsvFormatException e) {
            assertThat(e.getStatus().getReason(), is(Reason.INVALID_CELL_FORMAT));
        }
    }

    /**
     * test for int values.
     * @throws Exception if failed
     */
    @Test
    public void int_values() throws Exception {
        CsvParser parser = create("0,1,50,-1,-50,"
                + Integer.MAX_VALUE + ","
                + Integer.MIN_VALUE + ", 0,0 ,");
        IntOption option = new IntOption();

        assertThat(parser.next(), is(true));

        parser.fill(option);
        assertThat(option.get(), is(0));

        parser.fill(option);
        assertThat(option.get(), is(1));

        parser.fill(option);
        assertThat(option.get(), is(50));

        parser.fill(option);
        assertThat(option.get(), is(-1));

        parser.fill(option);
        assertThat(option.get(), is(-50));

        parser.fill(option);
        assertThat(option.get(), is(Integer.MAX_VALUE));

        parser.fill(option);
        assertThat(option.get(), is(Integer.MIN_VALUE));

        parser.fill(option);
        assertThat(option.get(), is(0));

        parser.fill(option);
        assertThat(option.get(), is(0));

        parser.fill(option);
        assertThat(option.isNull(), is(true));

        parser.endRecord();
        assertThat(parser.next(), is(false));
    }

    /**
     * test for invalid int values.
     * @throws Exception if failed
     */
    @Test
    public void invalid_int() throws Exception {
        CsvParser parser = create(String.valueOf((long) Integer.MAX_VALUE + 1));
        assertThat(parser.next(), is(true));
        try {
            parser.fill(new IntOption());
            fail();
        } catch (CsvFormatException e) {
            assertThat(e.getStatus().getReason(), is(Reason.INVALID_CELL_FORMAT));
        }
    }

    /**
     * test for long values.
     * @throws Exception if failed
     */
    @Test
    public void long_values() throws Exception {
        CsvParser parser = create("0,1,50,-1,-50,"
                + Long.MAX_VALUE + ","
                + Long.MIN_VALUE + ", 0,0 ,");
        LongOption option = new LongOption();

        assertThat(parser.next(), is(true));

        parser.fill(option);
        assertThat(option.get(), is((long) 0));

        parser.fill(option);
        assertThat(option.get(), is((long) 1));

        parser.fill(option);
        assertThat(option.get(), is((long) 50));

        parser.fill(option);
        assertThat(option.get(), is((long) -1));

        parser.fill(option);
        assertThat(option.get(), is((long) -50));

        parser.fill(option);
        assertThat(option.get(), is(Long.MAX_VALUE));

        parser.fill(option);
        assertThat(option.get(), is(Long.MIN_VALUE));

        parser.fill(option);
        assertThat(option.get(), is((long) 0));

        parser.fill(option);
        assertThat(option.get(), is((long) 0));

        parser.fill(option);
        assertThat(option.isNull(), is(true));

        parser.endRecord();
        assertThat(parser.next(), is(false));
    }

    /**
     * test for invalid long values.
     * @throws Exception if failed
     */
    @Test
    public void invalid_long() throws Exception {
        CsvParser parser = create(String.valueOf(Long.MAX_VALUE + "0"));
        assertThat(parser.next(), is(true));
        try {
            parser.fill(new LongOption());
            fail();
        } catch (CsvFormatException e) {
            assertThat(e.getStatus().getReason(), is(Reason.INVALID_CELL_FORMAT));
        }
    }

    /**
     * test for float values.
     * @throws Exception if failed
     */
    @Test
    public void float_values() throws Exception {
        CsvParser parser = create(
                "0,1,50,-1,-50,"
                + "0.5,-0.5,100.,-100., 0,0 ,");
        FloatOption option = new FloatOption();

        assertThat(parser.next(), is(true));

        parser.fill(option);
        assertThat(option.get(), is((float) 0));

        parser.fill(option);
        assertThat(option.get(), is((float) 1));

        parser.fill(option);
        assertThat(option.get(), is((float) 50));

        parser.fill(option);
        assertThat(option.get(), is((float) -1));

        parser.fill(option);
        assertThat(option.get(), is((float) -50));

        parser.fill(option);
        assertThat(option.get(), is((float) 0.5));

        parser.fill(option);
        assertThat(option.get(), is((float) -0.5));

        parser.fill(option);
        assertThat(option.get(), is((float) 100.));

        parser.fill(option);
        assertThat(option.get(), is((float) -100.));

        parser.fill(option);
        assertThat(option.get(), is((float) 0));

        parser.fill(option);
        assertThat(option.get(), is((float) 0));

        parser.fill(option);
        assertThat(option.isNull(), is(true));

        parser.endRecord();
        assertThat(parser.next(), is(false));
    }

    /**
     * test for invalid float values.
     * @throws Exception if failed
     */
    @Test
    public void invalid_float() throws Exception {
        CsvParser parser = create(String.valueOf("?"));
        assertThat(parser.next(), is(true));
        try {
            parser.fill(new FloatOption());
            fail();
        } catch (CsvFormatException e) {
            assertThat(e.getStatus().getReason(), is(Reason.INVALID_CELL_FORMAT));
        }
    }

    /**
     * test for double values.
     * @throws Exception if failed
     */
    @Test
    public void double_values() throws Exception {
        CsvParser parser = create(
                "0,1,50,-1,-50,"
                + "0.5,-0.5,100.,-100., 0,0 ,");
        DoubleOption option = new DoubleOption();

        assertThat(parser.next(), is(true));

        parser.fill(option);
        assertThat(option.get(), is((double) 0));

        parser.fill(option);
        assertThat(option.get(), is((double) 1));

        parser.fill(option);
        assertThat(option.get(), is((double) 50));

        parser.fill(option);
        assertThat(option.get(), is((double) -1));

        parser.fill(option);
        assertThat(option.get(), is((double) -50));

        parser.fill(option);
        assertThat(option.get(), is(0.5));

        parser.fill(option);
        assertThat(option.get(), is(-0.5));

        parser.fill(option);
        assertThat(option.get(), is(100.));

        parser.fill(option);
        assertThat(option.get(), is(-100.));

        parser.fill(option);
        assertThat(option.get(), is((double) 0));

        parser.fill(option);
        assertThat(option.get(), is((double) 0));

        parser.fill(option);
        assertThat(option.isNull(), is(true));

        parser.endRecord();
        assertThat(parser.next(), is(false));
    }

    /**
     * test for invalid double values.
     * @throws Exception if failed
     */
    @Test
    public void invalid_double() throws Exception {
        CsvParser parser = create(String.valueOf("?"));
        assertThat(parser.next(), is(true));
        try {
            parser.fill(new DoubleOption());
            fail();
        } catch (CsvFormatException e) {
            assertThat(e.getStatus().getReason(), is(Reason.INVALID_CELL_FORMAT));
        }
    }

    /**
     * test for decimal values.
     * @throws Exception if failed
     */
    @Test
    public void decimal_values() throws Exception {
        CsvParser parser = create(
                "0,1,50,-1,-50,"
                + "0.5,-0.5,3.1415,-3.1415, 0,0 , ,");
        DecimalOption option = new DecimalOption();

        assertThat(parser.next(), is(true));

        parser.fill(option);
        assertThat(option.get(), is(decimal("0")));

        parser.fill(option);
        assertThat(option.get(), is(decimal("1")));

        parser.fill(option);
        assertThat(option.get(), is(decimal("50")));

        parser.fill(option);
        assertThat(option.get(), is(decimal("-1")));

        parser.fill(option);
        assertThat(option.get(), is(decimal("-50")));

        parser.fill(option);
        assertThat(option.get(), is(decimal("0.5")));

        parser.fill(option);
        assertThat(option.get(), is(decimal("-0.5")));

        parser.fill(option);
        assertThat(option.get(), is(decimal("3.1415")));

        parser.fill(option);
        assertThat(option.get(), is(decimal("-3.1415")));

        parser.fill(option);
        assertThat(option.get(), is(decimal("0")));

        parser.fill(option);
        assertThat(option.get(), is(decimal("0")));

        parser.fill(option);
        assertThat(option.isNull(), is(true));

        parser.fill(option);
        assertThat(option.isNull(), is(true));

        parser.endRecord();
        assertThat(parser.next(), is(false));
    }

    private BigDecimal decimal(String string) {
        return new BigDecimal(string);
    }

    /**
     * test for invalid decimal values.
     * @throws Exception if failed
     */
    @Test
    public void invalid_decimal() throws Exception {
        CsvParser parser = create(String.valueOf("?"));
        assertThat(parser.next(), is(true));
        try {
            parser.fill(new DecimalOption());
            fail();
        } catch (CsvFormatException e) {
            assertThat(e.getStatus().getReason(), is(Reason.INVALID_CELL_FORMAT));
        }
    }

    /**
     * test for string values.
     * @throws Exception if failed
     */
    @Test
    public void string_values() throws Exception {
        CsvParser parser = create(
                "Hello,"
                + "\u3042\u3044\u3046\u3048\u304a,"
                + "\",\"\"\r\n\", ,");
        StringOption option = new StringOption();

        assertThat(parser.next(), is(true));

        parser.fill(option);
        assertThat(option.getAsString(), is("Hello"));

        parser.fill(option);
        assertThat(option.getAsString(), is("\u3042\u3044\u3046\u3048\u304a"));

        parser.fill(option);
        assertThat(option.getAsString(), is(",\"\r\n"));

        parser.fill(option);
        assertThat(option.getAsString(), is(" "));

        parser.fill(option);
        assertThat(option.isNull(), is(true));

        parser.endRecord();
        assertThat(parser.next(), is(false));
    }

    /**
     * test for date values.
     * @throws Exception if failed
     */
    @Test
    public void date_values() throws Exception {
        dateFormat = "yyyy/MM/dd";
        CsvParser parser = create(
                "2011/03/31,"
                + "1971/4/1,"
                + " 1971/4/1 ,");
        DateOption option = new DateOption();

        assertThat(parser.next(), is(true));

        parser.fill(option);
        assertThat(option.get(), is(new Date(2011, 3, 31)));

        parser.fill(option);
        assertThat(option.get(), is(new Date(1971, 4, 1)));

        parser.fill(option);
        assertThat(option.get(), is(new Date(1971, 4, 1)));

        parser.fill(option);
        assertThat(option.isNull(), is(true));

        parser.endRecord();
        assertThat(parser.next(), is(false));
    }

    /**
     * test for date values.
     * @throws Exception if failed
     */
    @Test
    public void date_values_direct() throws Exception {
        dateFormat = "yyyyMMdd";
        CsvParser parser = create(
                "20110331,"
                + "19710401,");
        DateOption option = new DateOption();

        assertThat(parser.next(), is(true));

        parser.fill(option);
        assertThat(option.get(), is(new Date(2011, 3, 31)));

        parser.fill(option);
        assertThat(option.get(), is(new Date(1971, 4, 1)));

        parser.fill(option);
        assertThat(option.isNull(), is(true));

        parser.endRecord();
        assertThat(parser.next(), is(false));
    }

    /**
     * test for invalid date values.
     * @throws Exception if failed
     */
    @Test
    public void invalid_date() throws Exception {
        CsvParser parser = create(String.valueOf("?"));
        assertThat(parser.next(), is(true));
        try {
            parser.fill(new DateOption());
            fail();
        } catch (CsvFormatException e) {
            assertThat(e.getStatus().getReason(), is(Reason.INVALID_CELL_FORMAT));
        }
    }

    /**
     * test for date-time values.
     * @throws Exception if failed
     */
    @Test
    public void datetime_values() throws Exception {
        dateTimeFormat = "yyyy/MM/dd HH:mm:ss";
        CsvParser parser = create(
                "2011/03/31 23:59:59,"
                + "1971/4/1 1:2:3,"
                + " 1971/4/1 1:2:3 ,");
        DateTimeOption option = new DateTimeOption();

        assertThat(parser.next(), is(true));

        parser.fill(option);
        assertThat(option.get(), is(new DateTime(2011, 3, 31, 23, 59, 59)));

        parser.fill(option);
        assertThat(option.get(), is(new DateTime(1971, 4, 1, 1, 2, 3)));

        parser.fill(option);
        assertThat(option.get(), is(new DateTime(1971, 4, 1, 1, 2, 3)));

        parser.fill(option);
        assertThat(option.isNull(), is(true));

        parser.endRecord();
        assertThat(parser.next(), is(false));
    }

    /**
     * test for date-time values.
     * @throws Exception if failed
     */
    @Test
    public void datetime_values_direct() throws Exception {
        dateTimeFormat = "yyyyMMddHHmmss";
        CsvParser parser = create(
                "20110331235959,"
                + "19710401010203,"
                + " 19710401010203 ,");
        DateTimeOption option = new DateTimeOption();

        assertThat(parser.next(), is(true));

        parser.fill(option);
        assertThat(option.get(), is(new DateTime(2011, 3, 31, 23, 59, 59)));

        parser.fill(option);
        assertThat(option.get(), is(new DateTime(1971, 4, 1, 1, 2, 3)));

        parser.fill(option);
        assertThat(option.get(), is(new DateTime(1971, 4, 1, 1, 2, 3)));

        parser.fill(option);
        assertThat(option.isNull(), is(true));

        parser.endRecord();
        assertThat(parser.next(), is(false));
    }

    /**
     * test for invalid datetime values.
     * @throws Exception if failed
     */
    @Test
    public void invalid_datetime() throws Exception {
        CsvParser parser = create(String.valueOf("?"));
        assertThat(parser.next(), is(true));
        try {
            parser.fill(new DateTimeOption());
            fail();
        } catch (CsvFormatException e) {
            assertThat(e.getStatus().getReason(), is(Reason.INVALID_CELL_FORMAT));
        }
    }

    /**
     * has header.
     * @throws Exception if failed
     */
    @Test
    public void with_header() throws Exception {
        headers = Arrays.asList("key", "value");

        CsvParser parser = create(
                "key,value\r\n" +
                "hello,world\r\n");
        StringOption key = new StringOption();
        StringOption value = new StringOption();

        assertThat(parser.next(), is(true));
        assertThat(parser.getCurrentLineNumber(), is(2L));
        assertThat(parser.getCurrentRecordNumber(), is(1L));
        parser.fill(key);
        parser.fill(value);
        parser.endRecord();
        assertThat(parser.next(), is(false));

        assertThat(key.getAsString(), is("hello"));
        assertThat(value.getAsString(), is("world"));
    }

    /**
     * has header.
     * @throws Exception if failed
     */
    @Test
    public void not_header() throws Exception {
        headers = Arrays.asList("key", "data");

        CsvParser parser = create(
                "key,value\r\n" +
                "hello,world\r\n");
        StringOption key = new StringOption();
        StringOption value = new StringOption();

        assertThat(parser.next(), is(true));
        assertThat(parser.getCurrentLineNumber(), is(1L));
        assertThat(parser.getCurrentRecordNumber(), is(1L));
        parser.fill(key);
        parser.fill(value);
        parser.endRecord();
        assertThat(key.getAsString(), is("key"));
        assertThat(value.getAsString(), is("value"));

        assertThat(parser.next(), is(true));
        assertThat(parser.getCurrentLineNumber(), is(2L));
        assertThat(parser.getCurrentRecordNumber(), is(2L));
        parser.fill(key);
        parser.fill(value);
        parser.endRecord();
        assertThat(key.getAsString(), is("hello"));
        assertThat(value.getAsString(), is("world"));

        assertThat(parser.next(), is(false));
    }

    /**
     * header is declared but file is empty.
     * @throws Exception if failed
     */
    @Test
    public void empty_with_header() throws Exception {
        headers = Arrays.asList("key", "value");
        CsvParser parser = create("");
        assertThat(parser.next(), is(false));
    }

    /**
     * only header.
     * @throws Exception if failed
     */
    @Test
    public void only_header() throws Exception {
        headers = Arrays.asList("key", "value");
        CsvParser parser = create("key,value\r\n");
        assertThat(parser.next(), is(false));
    }

    /**
     * check LINE_HEAD states.
     * @throws Exception if failed
     */
    @Test
    public void state_line_head() throws Exception {
        CsvParser parser = create(
                "\"\"\n" +
                ",\n" +
                "\r\n" +
                "\n" +
                "x\n" +
                "");
        assertThat(parser.next(), is(true));
        assertThat(parser.getCurrentLineNumber(), is(1L));
        assertThat(parser.getCurrentRecordNumber(), is(1L));
        assertFill(parser, null);
        parser.endRecord();

        assertThat(parser.next(), is(true));
        assertThat(parser.getCurrentLineNumber(), is(2L));
        assertThat(parser.getCurrentRecordNumber(), is(2L));
        assertFill(parser, null);
        assertFill(parser, null);
        parser.endRecord();

        assertThat(parser.next(), is(true));
        assertThat(parser.getCurrentLineNumber(), is(3L));
        assertThat(parser.getCurrentRecordNumber(), is(3L));
        assertFill(parser, null);
        parser.endRecord();

        assertThat(parser.next(), is(true));
        assertThat(parser.getCurrentLineNumber(), is(4L));
        assertThat(parser.getCurrentRecordNumber(), is(4L));
        assertFill(parser, null);
        parser.endRecord();

        assertThat(parser.next(), is(true));
        assertThat(parser.getCurrentLineNumber(), is(5L));
        assertThat(parser.getCurrentRecordNumber(), is(5L));
        assertFill(parser, "x");
        parser.endRecord();

        assertThat(parser.next(), is(false));
    }

    /**
     * check CELL_HEAD states.
     * @throws Exception if failed
     */
    @Test
    public void state_cell_head() throws Exception {
        CsvParser parser = create(
                ",\"\"\n" +
                ",,\n" +
                ",\r\n" +
                ",\n" +
                ",x\n" +
                "," +
                "");
        assertThat(parser.next(), is(true));
        assertThat(parser.getCurrentLineNumber(), is(1L));
        assertThat(parser.getCurrentRecordNumber(), is(1L));
        assertFill(parser, null);
        assertFill(parser, null);
        parser.endRecord();

        assertThat(parser.next(), is(true));
        assertThat(parser.getCurrentLineNumber(), is(2L));
        assertThat(parser.getCurrentRecordNumber(), is(2L));
        assertFill(parser, null);
        assertFill(parser, null);
        assertFill(parser, null);
        parser.endRecord();

        assertThat(parser.next(), is(true));
        assertThat(parser.getCurrentLineNumber(), is(3L));
        assertThat(parser.getCurrentRecordNumber(), is(3L));
        assertFill(parser, null);
        assertFill(parser, null);
        parser.endRecord();

        assertThat(parser.next(), is(true));
        assertThat(parser.getCurrentLineNumber(), is(4L));
        assertThat(parser.getCurrentRecordNumber(), is(4L));
        assertFill(parser, null);
        assertFill(parser, null);
        parser.endRecord();

        assertThat(parser.next(), is(true));
        assertThat(parser.getCurrentLineNumber(), is(5L));
        assertThat(parser.getCurrentRecordNumber(), is(5L));
        assertFill(parser, null);
        assertFill(parser, "x");
        parser.endRecord();

        assertThat(parser.next(), is(true));
        assertThat(parser.getCurrentLineNumber(), is(6L));
        assertThat(parser.getCurrentRecordNumber(), is(6L));
        assertFill(parser, null);
        assertFill(parser, null);
        parser.endRecord();

        assertThat(parser.next(), is(false));
    }

    /**
     * check CELL_BODY states.
     * @throws Exception if failed
     */
    @Test
    public void state_cell_body() throws Exception {
        CsvParser parser = create(
                "x\"\n" +
                "x,\n" +
                "x\r\n" +
                "x\n" +
                "xy\n" +
                "x" +
                "");
        assertThat(parser.next(), is(true));
        assertThat(parser.getCurrentLineNumber(), is(1L));
        assertThat(parser.getCurrentRecordNumber(), is(1L));
        assertFill(parser, "x\"");
        parser.endRecord();

        assertThat(parser.next(), is(true));
        assertThat(parser.getCurrentLineNumber(), is(2L));
        assertThat(parser.getCurrentRecordNumber(), is(2L));
        assertFill(parser, "x");
        assertFill(parser, null);
        parser.endRecord();

        assertThat(parser.next(), is(true));
        assertThat(parser.getCurrentLineNumber(), is(3L));
        assertThat(parser.getCurrentRecordNumber(), is(3L));
        assertFill(parser, "x");
        parser.endRecord();

        assertThat(parser.next(), is(true));
        assertThat(parser.getCurrentLineNumber(), is(4L));
        assertThat(parser.getCurrentRecordNumber(), is(4L));
        assertFill(parser, "x");
        parser.endRecord();

        assertThat(parser.next(), is(true));
        assertThat(parser.getCurrentLineNumber(), is(5L));
        assertThat(parser.getCurrentRecordNumber(), is(5L));
        assertFill(parser, "xy");
        parser.endRecord();

        assertThat(parser.next(), is(true));
        assertThat(parser.getCurrentLineNumber(), is(6L));
        assertThat(parser.getCurrentRecordNumber(), is(6L));
        assertFill(parser, "x");
        parser.endRecord();

        assertThat(parser.next(), is(false));
    }

    /**
     * check QUOTED states.
     * @throws Exception if failed
     */
    @Test
    public void state_quoted() throws Exception {
        CsvParser parser = create(
                "\"\"\n" +
                "\",\"\n" +
                "\"\r\"\n" +
                "\"\n\"\n" +
                "\"x\"\n" +
                "\"");
        assertThat(parser.next(), is(true));
        assertThat(parser.getCurrentLineNumber(), is(1L));
        assertThat(parser.getCurrentRecordNumber(), is(1L));
        assertFill(parser, null);
        parser.endRecord();

        assertThat(parser.next(), is(true));
        assertThat(parser.getCurrentLineNumber(), is(2L));
        assertThat(parser.getCurrentRecordNumber(), is(2L));
        assertFill(parser, ",");
        parser.endRecord();

        assertThat(parser.next(), is(true));
        assertThat(parser.getCurrentLineNumber(), is(3L));
        assertThat(parser.getCurrentRecordNumber(), is(3L));
        assertFill(parser, "\r");
        parser.endRecord();

        assertThat(parser.next(), is(true));
        assertThat(parser.getCurrentLineNumber(), is(5L));
        assertThat(parser.getCurrentRecordNumber(), is(4L));
        assertFill(parser, "\n");
        parser.endRecord();

        assertThat(parser.next(), is(true));
        assertThat(parser.getCurrentLineNumber(), is(7L));
        assertThat(parser.getCurrentRecordNumber(), is(5L));
        assertFill(parser, "x");
        parser.endRecord();

        try {
            assertThat(parser.next(), is(true));
            parser.fill(new StringOption());
            parser.endRecord();
            fail();
        } catch (CsvFormatException e) {
            assertThat(e.getStatus().getReason(), is(Reason.UNEXPECTED_EOF));
        }

        assertThat(parser.next(), is(false));
    }

    /**
     * check NEST_QUOTE states.
     * @throws Exception if failed
     */
    @Test
    public void state_nest_quote() throws Exception {
        CsvParser parser = create(
                "\"a\"\"\"\n" +
                "\"a\",\n" +
                "\"a\"\r\n" +
                "\"a\"\n" +
                "\"a\"x\n" +
                "\"a\"" +
                "");
        assertThat(parser.next(), is(true));
        assertThat(parser.getCurrentLineNumber(), is(1L));
        assertThat(parser.getCurrentRecordNumber(), is(1L));
        assertFill(parser, "a\"");
        parser.endRecord();

        assertThat(parser.next(), is(true));
        assertThat(parser.getCurrentLineNumber(), is(2L));
        assertThat(parser.getCurrentRecordNumber(), is(2L));
        assertFill(parser, "a");
        assertFill(parser, null);
        parser.endRecord();

        assertThat(parser.next(), is(true));
        assertThat(parser.getCurrentLineNumber(), is(3L));
        assertThat(parser.getCurrentRecordNumber(), is(3L));
        assertFill(parser, "a");
        parser.endRecord();

        assertThat(parser.next(), is(true));
        assertThat(parser.getCurrentLineNumber(), is(4L));
        assertThat(parser.getCurrentRecordNumber(), is(4L));
        assertFill(parser, "a");
        parser.endRecord();

        assertThat(parser.next(), is(true));
        assertThat(parser.getCurrentLineNumber(), is(5L));
        assertThat(parser.getCurrentRecordNumber(), is(5L));
        assertFill(parser, "ax");
        parser.endRecord();

        assertThat(parser.next(), is(true));
        assertThat(parser.getCurrentLineNumber(), is(6L));
        assertThat(parser.getCurrentRecordNumber(), is(6L));
        assertFill(parser, "a");
        parser.endRecord();

        assertThat(parser.next(), is(false));
    }

    /**
     * check SAW_CR states.
     * @throws Exception if failed
     */
    @Test
    public void state_saw_cr() throws Exception {
        CsvParser parser = create(
                "\r\"x\"\n" +
                "\r,x\n" +
                "\r\rx\n" +
                "\r\n" +
                "\rx\n" +
                "\r" +
                "");

        assertThat(parser.next(), is(true));
        assertThat(parser.getCurrentLineNumber(), is(1L));
        assertThat(parser.getCurrentRecordNumber(), is(1L));
        assertFill(parser, null);
        parser.endRecord();
        assertThat(parser.next(), is(true));
        assertThat(parser.getCurrentLineNumber(), is(2L));
        assertThat(parser.getCurrentRecordNumber(), is(2L));
        assertFill(parser, "x");
        parser.endRecord();

        assertThat(parser.next(), is(true));
        assertThat(parser.getCurrentLineNumber(), is(3L));
        assertThat(parser.getCurrentRecordNumber(), is(3L));
        assertFill(parser, null);
        parser.endRecord();
        assertThat(parser.next(), is(true));
        assertThat(parser.getCurrentLineNumber(), is(4L));
        assertThat(parser.getCurrentRecordNumber(), is(4L));
        assertFill(parser, null);
        assertFill(parser, "x");
        parser.endRecord();

        assertThat(parser.next(), is(true));
        assertThat(parser.getCurrentLineNumber(), is(5L));
        assertThat(parser.getCurrentRecordNumber(), is(5L));
        assertFill(parser, null);
        parser.endRecord();
        assertThat(parser.next(), is(true));
        assertThat(parser.getCurrentLineNumber(), is(6L));
        assertThat(parser.getCurrentRecordNumber(), is(6L));
        assertFill(parser, null);
        parser.endRecord();
        assertThat(parser.next(), is(true));
        assertThat(parser.getCurrentLineNumber(), is(7L));
        assertThat(parser.getCurrentRecordNumber(), is(7L));
        assertFill(parser, "x");
        parser.endRecord();

        assertThat(parser.next(), is(true));
        assertThat(parser.getCurrentLineNumber(), is(8L));
        assertThat(parser.getCurrentRecordNumber(), is(8L));
        assertFill(parser, null);
        parser.endRecord();

        assertThat(parser.next(), is(true));
        assertThat(parser.getCurrentLineNumber(), is(9L));
        assertThat(parser.getCurrentRecordNumber(), is(9L));
        assertFill(parser, null);
        parser.endRecord();
        assertThat(parser.next(), is(true));
        assertThat(parser.getCurrentLineNumber(), is(10L));
        assertThat(parser.getCurrentRecordNumber(), is(10L));
        assertFill(parser, "x");
        parser.endRecord();

        assertThat(parser.next(), is(true));
        assertThat(parser.getCurrentLineNumber(), is(11L));
        assertThat(parser.getCurrentRecordNumber(), is(11L));
        assertFill(parser, null);
        parser.endRecord();

        assertThat(parser.next(), is(false));
    }

    /**
     * check QUOTED_SAW_CR states.
     * @throws Exception if failed
     */
    @Test
    public void state_quoted_saw_cr() throws Exception {
        CsvParser parser = create(
                "\"\r\"\n" +
                "\"\r,\"\n" +
                "\"\r\r\"\n" +
                "\"\r\n\"\n" +
                "\"\rx\"\n" +
                "\"\r" +
                "");
        assertThat(parser.next(), is(true));
        assertThat(parser.getCurrentLineNumber(), is(1L));
        assertThat(parser.getCurrentRecordNumber(), is(1L));
        assertFill(parser, "\r");
        parser.endRecord();

        assertThat(parser.next(), is(true));
        assertThat(parser.getCurrentLineNumber(), is(3L));
        assertThat(parser.getCurrentRecordNumber(), is(2L));
        assertFill(parser, "\r,");
        parser.endRecord();

        assertThat(parser.next(), is(true));
        assertThat(parser.getCurrentLineNumber(), is(5L));
        assertThat(parser.getCurrentRecordNumber(), is(3L));
        assertFill(parser, "\r\r");
        parser.endRecord();

        assertThat(parser.next(), is(true));
        assertThat(parser.getCurrentLineNumber(), is(8L));
        assertThat(parser.getCurrentRecordNumber(), is(4L));
        assertFill(parser, "\r\n");
        parser.endRecord();

        assertThat(parser.next(), is(true));
        assertThat(parser.getCurrentLineNumber(), is(10L));
        assertThat(parser.getCurrentRecordNumber(), is(5L));
        assertFill(parser, "\rx");
        parser.endRecord();

        try {
            assertThat(parser.next(), is(true));
            parser.fill(new StringOption("\r"));
            parser.endRecord();
            fail();
        } catch (CsvFormatException e) {
            assertThat(e.getStatus().getReason(), is(Reason.UNEXPECTED_EOF));
        }

        assertThat(parser.next(), is(false));
    }

    /**
     * many separators in a cell.
     * @throws Exception if failed
     */
    @Test
    public void many_separators() throws Exception {
        StringBuilder buf = new StringBuilder();
        final int separators = 10000;
        for (int i = 0; i < separators; i++) {
            buf.append('a');
            buf.append(',');
        }

        CsvParser parser = create(buf.toString());

        assertThat(parser.next(), is(true));
        for (int i = 0; i < separators; i++) {
            assertFill(parser, "a");
        }
        assertFill(parser, null);
        parser.endRecord();

        assertThat(parser.next(), is(false));
    }

    /**
     * many characters in a cell.
     * @throws Exception if failed
     */
    @Test
    public void many_characters() throws Exception {
        StringBuilder buf = new StringBuilder();
        final int characters = 100000;
        for (int i = 0; i < characters; i++) {
            buf.append('a');
        }

        CsvParser parser = create(buf.toString());

        assertThat(parser.next(), is(true));
        StringOption option = new StringOption();
        parser.fill(option);
        assertThat(option.getAsString().length(), is(characters));
        parser.endRecord();

        assertThat(parser.next(), is(false));
    }

    /**
     * too many characters in record.
     * @throws Exception if failed
     */
    @Test
    public void too_many_characters() throws Exception {
        InputStream infinite = new InputStream() {
            @Override
            public int read() throws IOException {
                return 'a';
            }
        };
        CsvConfiguration conf = createConfiguration();
        try (CsvParser parser = new CsvParser(infinite, "testing", conf)) {
            assertThat(parser.next(), is(true));
            fail();
        } catch (IOException e) {
            // ok.
        }
    }

    /**
     * record is too short.
     * @throws Exception if failed
     */
    @Test
    public void too_short_record() throws Exception {
        CsvParser parser = create("a,b,c");

        assertThat(parser.next(), is(true));
        try {
            assertFill(parser, "a");
            assertFill(parser, "b");
            assertFill(parser, "c");
            parser.fill(new StringOption());
            parser.endRecord();
            fail();
        } catch (CsvFormatException e) {
            assertThat(e.getStatus().getReason(), is(Reason.TOO_SHORT_RECORD));
        }
        assertThat(parser.next(), is(false));
    }

    /**
     * record is too long.
     * @throws Exception if failed
     */
    @Test
    public void too_long_record() throws Exception {
        CsvParser parser = create("a,b,c");

        assertThat(parser.next(), is(true));
        try {
            assertFill(parser, "a");
            assertFill(parser, "b");
            parser.endRecord();
            fail();
        } catch (CsvFormatException e) {
            assertThat(e.getStatus().getReason(), is(Reason.TOO_LONG_RECORD));
        }
        assertThat(parser.next(), is(false));
    }

    /**
     * Simple stress test for {@link DecimalOption} type.
     * @throws Exception if failed
     */
    @Test
    public void stress_decimal() throws Exception {
        int count = 5000000;
        CsvConfiguration conf = createConfiguration();
        try (RCReader reader = new RCReader("3.141592\r\n".getBytes(conf.getCharset()), count);
                CsvParser parser = new CsvParser(reader, "testing", conf)) {
            int rows = 0;
            DecimalOption date = new DecimalOption();
            while (parser.next()) {
                parser.fill(date);
                parser.endRecord();
                if (rows == 0) {
                    assertThat(date, is(new DecimalOption(new BigDecimal("3.141592"))));
                }
                rows++;
            }
            assertThat(rows, is(count));
        }
    }

    /**
     * Simple stress test for {@link Date} type.
     * @throws Exception if failed
     */
    @Test
    public void stress_date() throws Exception {
        int count = 5000000;
        CsvConfiguration conf = createConfiguration();

        try (RCReader reader = new RCReader("1999-12-31\r\n".getBytes(conf.getCharset()), count);
                CsvParser parser = new CsvParser(reader, "testing", conf)) {
            int rows = 0;
            DateOption date = new DateOption();
            while (parser.next()) {
                parser.fill(date);
                parser.endRecord();
                if (rows == 0) {
                    assertThat(date, is(new DateOption(new Date(1999, 12, 31))));
                }
                rows++;
            }
            parser.close();
            assertThat(rows, is(count));
        }
    }

    /**
     * Simple stress test for {@link DateTime} type.
     * @throws Exception if failed
     */
    @Test
    public void stress_datetime() throws Exception {
        int count = 5000000;
        CsvConfiguration conf = createConfiguration();
        try (RCReader reader = new RCReader("1999-12-31 01:23:45\r\n".getBytes(conf.getCharset()), count);
                CsvParser parser = new CsvParser(reader, "testing", conf)) {
            int rows = 0;
            DateTimeOption date = new DateTimeOption();
            while (parser.next()) {
                parser.fill(date);
                parser.endRecord();
                if (rows == 0) {
                    assertThat(date, is(new DateTimeOption(new DateTime(1999, 12, 31, 1, 23, 45))));
                }
                rows++;
            }
            parser.close();
            assertThat(rows, is(count));
        }
    }

    private static class RCReader extends InputStream {

        private final byte[] element;

        private final int limit;

        private int offset;

        public RCReader(byte[] element, int count) {
            this.element = element;
            this.limit = element.length * count;
        }

        @Override
        public int read() throws IOException {
            if (offset >= limit) {
                return -1;
            }
            return element[offset++ % element.length];
        }

        @Override
        public void close() throws IOException {
            return;
        }
    }

    private void assertFill(CsvParser parser, String expect) throws CsvFormatException, IOException {
        StringOption buffer = new StringOption();
        parser.fill(buffer);
        assertThat(buffer.toString(), buffer.has(expect), is(true));
    }
}
