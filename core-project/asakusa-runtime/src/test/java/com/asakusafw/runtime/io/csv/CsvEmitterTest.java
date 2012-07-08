/**
 * Copyright 2011-2012 Asakusa Framework Team.
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
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

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
import com.asakusafw.runtime.value.ValueOption;

/**
 * Test for {@link CsvEmitter}.
 */
public class CsvEmitterTest {

    /**
     * Tracks test names.
     */
    @Rule
    public final TestName testName = new TestName();

    private final ByteArrayOutputStream output = new ByteArrayOutputStream();

    private List<String> headers = CsvConfiguration.DEFAULT_HEADER_CELLS;

    private String trueFormat = CsvConfiguration.DEFAULT_TRUE_FORMAT;

    private String falseFormat = CsvConfiguration.DEFAULT_FALSE_FORMAT;

    private String dateFormat = CsvConfiguration.DEFAULT_DATE_FORMAT;

    private String dateTimeFormat = CsvConfiguration.DEFAULT_DATE_TIME_FORMAT;

    private CsvEmitter createEmitter() {
        CsvConfiguration conf = new CsvConfiguration(
                CsvConfiguration.DEFAULT_CHARSET,
                headers,
                trueFormat,
                falseFormat,
                dateFormat,
                dateTimeFormat);
        return new CsvEmitter(output, testName.getMethodName(), conf);
    }

    private CsvParser createParser() {
        CsvConfiguration conf = new CsvConfiguration(
                CsvConfiguration.DEFAULT_CHARSET,
                headers,
                trueFormat,
                falseFormat,
                dateFormat,
                dateTimeFormat);
        return new CsvParser(new ByteArrayInputStream(output.toByteArray()), testName.getMethodName(), conf);
    }

    private void assertRestorable(ValueOption<?> option) {
        CsvConfiguration conf = new CsvConfiguration(
                CsvConfiguration.DEFAULT_CHARSET,
                headers,
                trueFormat,
                falseFormat,
                dateFormat,
                dateTimeFormat);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        CsvEmitter emitter = new CsvEmitter(buffer, testName.getMethodName(), conf);
        try {
            emit(emitter, option);
            emitter.endRecord();
            emitter.close();

            CsvParser parser = new CsvParser(
                    new ByteArrayInputStream(buffer.toByteArray()), testName.getMethodName(), conf);
            assertThat(parser.next(), is(true));
            ValueOption<?> copy = option.getClass().newInstance();
            fill(parser, copy);
            parser.endRecord();
            assertThat(parser.next(), is(false));

            assertThat(copy, is((Object) option));
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    private <T extends ValueOption<?>> T fill(CsvParser parser, T option) {
        try {
            CsvParser.class.getMethod("fill", option.getClass()).invoke(parser, option);
            return option;
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    private <T extends ValueOption<?>> T emit(CsvEmitter emitter, T option) {
        try {
            CsvEmitter.class.getMethod("emit", option.getClass()).invoke(emitter, option);
            return option;
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    /**
     * test for boolean values.
     * @throws Exception if failed
     */
    @Test
    public void boolean_values() throws Exception {
        assertRestorable(new BooleanOption(true));
        assertRestorable(new BooleanOption(false));
        assertRestorable(new BooleanOption());

        trueFormat = "false";
        falseFormat = "true";
        assertRestorable(new BooleanOption(true));
        assertRestorable(new BooleanOption(false));
    }

    /**
     * test for byte values.
     * @throws Exception if failed
     */
    @Test
    public void byte_values() throws Exception {
        assertRestorable(new ByteOption((byte) 0));
        assertRestorable(new ByteOption((byte) 1));
        assertRestorable(new ByteOption((byte) -1));
        assertRestorable(new ByteOption((byte) 50));
        assertRestorable(new ByteOption((byte) -50));
        assertRestorable(new ByteOption(Byte.MAX_VALUE));
        assertRestorable(new ByteOption(Byte.MIN_VALUE));
        assertRestorable(new ByteOption());
    }

    /**
     * test for short values.
     * @throws Exception if failed
     */
    @Test
    public void short_values() throws Exception {
        assertRestorable(new ShortOption((short) 0));
        assertRestorable(new ShortOption((short) 1));
        assertRestorable(new ShortOption((short) -1));
        assertRestorable(new ShortOption((short) 50));
        assertRestorable(new ShortOption((short) -50));
        assertRestorable(new ShortOption(Short.MAX_VALUE));
        assertRestorable(new ShortOption(Short.MIN_VALUE));
        assertRestorable(new ShortOption());
    }

    /**
     * test for int values.
     * @throws Exception if failed
     */
    @Test
    public void int_values() throws Exception {
        assertRestorable(new IntOption(0));
        assertRestorable(new IntOption(1));
        assertRestorable(new IntOption(-1));
        assertRestorable(new IntOption(50));
        assertRestorable(new IntOption(-50));
        assertRestorable(new IntOption(Integer.MAX_VALUE));
        assertRestorable(new IntOption(Integer.MIN_VALUE));
        assertRestorable(new IntOption());
    }

    /**
     * test for long values.
     * @throws Exception if failed
     */
    @Test
    public void long_vaules() throws Exception {
        assertRestorable(new LongOption(0));
        assertRestorable(new LongOption(1));
        assertRestorable(new LongOption(-1));
        assertRestorable(new LongOption(50));
        assertRestorable(new LongOption(-50));
        assertRestorable(new LongOption(Long.MAX_VALUE));
        assertRestorable(new LongOption(Long.MIN_VALUE));
        assertRestorable(new LongOption());
    }

    /**
     * test for float values.
     * @throws Exception if failed
     */
    @Test
    public void float_values() throws Exception {
        assertRestorable(new FloatOption(0));
        assertRestorable(new FloatOption(1));
        assertRestorable(new FloatOption(-1));
        assertRestorable(new FloatOption(50));
        assertRestorable(new FloatOption(-50));
        assertRestorable(new FloatOption(Float.MAX_VALUE));
        assertRestorable(new FloatOption(Float.MIN_VALUE));
        assertRestorable(new FloatOption());
    }

    /**
     * test for double values.
     * @throws Exception if failed
     */
    @Test
    public void double_values() throws Exception {
        assertRestorable(new DoubleOption(0));
        assertRestorable(new DoubleOption(1));
        assertRestorable(new DoubleOption(-1));
        assertRestorable(new DoubleOption(50));
        assertRestorable(new DoubleOption(-50));
        assertRestorable(new DoubleOption(Double.MAX_VALUE));
        assertRestorable(new DoubleOption(Double.MIN_VALUE));
        assertRestorable(new DoubleOption());
    }

    /**
     * test for decimal values.
     * @throws Exception if failed
     */
    @Test
    public void decimal_values() throws Exception {
        assertRestorable(new DecimalOption(decimal("0")));
        assertRestorable(new DecimalOption(decimal("-100")));
        assertRestorable(new DecimalOption(decimal("1")));
        assertRestorable(new DecimalOption(decimal("-1")));
        assertRestorable(new DecimalOption(decimal("100")));
        assertRestorable(new DecimalOption(decimal("-100")));
        assertRestorable(new DecimalOption(decimal("3.1415")));
        assertRestorable(new DecimalOption(decimal("-3.1415")));
        assertRestorable(new DecimalOption());
    }

    private BigDecimal decimal(String string) {
        return new BigDecimal(string);
    }

    /**
     * test for text values.
     * @throws Exception if failed
     */
    @Test
    public void text_values() throws Exception {
        assertRestorable(new StringOption("Hello"));
        assertRestorable(new StringOption("\u3042\u3044\u3046\u3048\u304a"));
        assertRestorable(new StringOption("\",\r\n\t "));
        assertRestorable(new StringOption());
    }

    /**
     * test for date values.
     * @throws Exception if failed
     */
    @Test
    public void date_values() throws Exception {
        assertRestorable(new DateOption(new Date(2011, 3, 31)));
        assertRestorable(new DateOption(new Date(1971, 4, 1)));
        assertRestorable(new DateOption());

        dateFormat = "yyyy-MM\"dd";
        assertRestorable(new DateOption(new Date(2011, 3, 31)));
        assertRestorable(new DateOption(new Date(1971, 4, 1)));

        dateFormat = "yyyy-MM,dd";
        assertRestorable(new DateOption(new Date(2011, 3, 31)));
        assertRestorable(new DateOption(new Date(1971, 4, 1)));

        dateFormat = "yyyy-MM\ndd";
        assertRestorable(new DateOption(new Date(2011, 3, 31)));
        assertRestorable(new DateOption(new Date(1971, 4, 1)));

        dateFormat = "yyyy-MM\rdd";
        assertRestorable(new DateOption(new Date(2011, 3, 31)));
        assertRestorable(new DateOption(new Date(1971, 4, 1)));
    }

    /**
     * test for date values.
     * @throws Exception if failed
     */
    @Test
    public void date_values_direct() throws Exception {
        dateFormat = "yyyyMMdd";
        assertRestorable(new DateOption(new Date(2011, 3, 31)));
        assertRestorable(new DateOption(new Date(1971, 4, 1)));
        assertRestorable(new DateOption());
    }

    /**
     * test for date-time values.
     * @throws Exception if failed
     */
    @Test
    public void datetime_values() throws Exception {
        assertRestorable(new DateTimeOption(new DateTime(2011, 3, 31, 23, 59, 59)));
        assertRestorable(new DateTimeOption(new DateTime(1971, 4, 1, 2 ,3, 4)));
        assertRestorable(new DateTimeOption());

        dateTimeFormat = "yyyy-MM\"dd HH:mm:ss";
        assertRestorable(new DateTimeOption(new DateTime(2011, 3, 31, 23, 59, 59)));
        assertRestorable(new DateTimeOption(new DateTime(1971, 4, 1, 2 ,3, 4)));

        dateTimeFormat = "yyyy-MM,dd HH:mm:ss";
        assertRestorable(new DateTimeOption(new DateTime(2011, 3, 31, 23, 59, 59)));
        assertRestorable(new DateTimeOption(new DateTime(1971, 4, 1, 2 ,3, 4)));

        dateTimeFormat = "yyyy-MM\ndd HH:mm:ss";
        assertRestorable(new DateTimeOption(new DateTime(2011, 3, 31, 23, 59, 59)));
        assertRestorable(new DateTimeOption(new DateTime(1971, 4, 1, 2 ,3, 4)));

        dateTimeFormat = "yyyy-MM\rdd HH:mm:ss";
        assertRestorable(new DateTimeOption(new DateTime(2011, 3, 31, 23, 59, 59)));
        assertRestorable(new DateTimeOption(new DateTime(1971, 4, 1, 2 ,3, 4)));
    }

    /**
     * test for date-time values.
     * @throws Exception if failed
     */
    @Test
    public void datetime_values_direct() throws Exception {
        dateTimeFormat = "yyyyMMddHHmmss";
        assertRestorable(new DateTimeOption(new DateTime(2011, 3, 31, 23, 59, 59)));
        assertRestorable(new DateTimeOption(new DateTime(1971, 4, 1, 2 ,3, 4)));
        assertRestorable(new DateTimeOption());
    }

    /**
     * test for multiple cells in record.
     * @throws Exception if failed
     */
    @Test
    public void multi_cells() throws Exception {
        CsvEmitter emitter = createEmitter();
        emitter.emit(new StringOption("a"));
        emitter.emit(new StringOption("b"));
        emitter.emit(new StringOption("c"));
        emitter.endRecord();
        emitter.close();

        CsvParser parser = createParser();
        assertThat(parser.next(), is(true));
        assertThat(fill(parser, new StringOption()), is(new StringOption("a")));
        assertThat(fill(parser, new StringOption()), is(new StringOption("b")));
        assertThat(fill(parser, new StringOption()), is(new StringOption("c")));
        parser.endRecord();

        assertThat(parser.next(), is(false));
        parser.close();
    }

    /**
     * test for multiple records in file.
     * @throws Exception if failed
     */
    @Test
    public void multi_records() throws Exception {
        CsvEmitter emitter = createEmitter();
        emitter.emit(new StringOption("a"));
        emitter.endRecord();
        emitter.emit(new StringOption("b"));
        emitter.endRecord();
        emitter.emit(new StringOption("c"));
        emitter.endRecord();
        emitter.close();

        CsvParser parser = createParser();
        assertThat(parser.next(), is(true));
        assertThat(fill(parser, new StringOption()), is(new StringOption("a")));
        parser.endRecord();
        assertThat(parser.next(), is(true));
        assertThat(fill(parser, new StringOption()), is(new StringOption("b")));
        parser.endRecord();
        assertThat(parser.next(), is(true));
        assertThat(fill(parser, new StringOption()), is(new StringOption("c")));
        parser.endRecord();

        assertThat(parser.next(), is(false));
        parser.close();
    }

    /**
     * test for multiple records and cells in file.
     * @throws Exception if failed
     */
    @Test
    public void matrix() throws Exception {
        CsvEmitter emitter = createEmitter();
        emitter.emit(new StringOption("a-1"));
        emitter.emit(new StringOption("a-2"));
        emitter.emit(new StringOption("a-3"));
        emitter.endRecord();
        emitter.emit(new StringOption("b-1"));
        emitter.emit(new StringOption("b-2"));
        emitter.emit(new StringOption("b-3"));
        emitter.endRecord();
        emitter.emit(new StringOption("c-1"));
        emitter.emit(new StringOption("c-2"));
        emitter.emit(new StringOption("c-3"));
        emitter.endRecord();
        emitter.close();

        CsvParser parser = createParser();
        assertThat(parser.next(), is(true));
        assertThat(fill(parser, new StringOption()), is(new StringOption("a-1")));
        assertThat(fill(parser, new StringOption()), is(new StringOption("a-2")));
        assertThat(fill(parser, new StringOption()), is(new StringOption("a-3")));
        parser.endRecord();
        assertThat(parser.next(), is(true));
        assertThat(fill(parser, new StringOption()), is(new StringOption("b-1")));
        assertThat(fill(parser, new StringOption()), is(new StringOption("b-2")));
        assertThat(fill(parser, new StringOption()), is(new StringOption("b-3")));
        parser.endRecord();
        assertThat(parser.next(), is(true));
        assertThat(fill(parser, new StringOption()), is(new StringOption("c-1")));
        assertThat(fill(parser, new StringOption()), is(new StringOption("c-2")));
        assertThat(fill(parser, new StringOption()), is(new StringOption("c-3")));
        parser.endRecord();

        assertThat(parser.next(), is(false));
        parser.close();
    }

    /**
     * has header.
     * @throws Exception if failed
     */
    @Test
    public void with_header() throws Exception {
        headers = Arrays.asList("key", "value");
        CsvEmitter emitter = createEmitter();
        emitter.emit(new StringOption("a"));
        emitter.emit(new StringOption("b"));
        emitter.endRecord();
        emitter.close();

        // directly read a header
        headers = Arrays.asList();
        CsvParser parser = createParser();

        assertThat(parser.next(), is(true));
        assertThat(fill(parser, new StringOption()), is(new StringOption("key")));
        assertThat(fill(parser, new StringOption()), is(new StringOption("value")));
        parser.endRecord();

        assertThat(parser.next(), is(true));
        assertThat(fill(parser, new StringOption()), is(new StringOption("a")));
        assertThat(fill(parser, new StringOption()), is(new StringOption("b")));
        parser.endRecord();

        assertThat(parser.next(), is(false));
        parser.close();
    }

    /**
     * has malformed header.
     * @throws Exception if failed
     */
    @Test
    public void with_malformed_header() throws Exception {
        headers = Arrays.asList("\"malformed,\r\nheader\"", "");
        CsvEmitter emitter = createEmitter();
        emitter.emit(new StringOption("a"));
        emitter.emit(new StringOption("b"));
        emitter.endRecord();
        emitter.close();

        // directly read a header
        headers = Arrays.asList();
        CsvParser parser = createParser();

        assertThat(parser.next(), is(true));
        assertThat(fill(parser, new StringOption()), is(new StringOption("\"malformed,\r\nheader\"")));
        assertThat(fill(parser, new StringOption()), is(new StringOption()));
        parser.endRecord();

        assertThat(parser.next(), is(true));
        assertThat(fill(parser, new StringOption()), is(new StringOption("a")));
        assertThat(fill(parser, new StringOption()), is(new StringOption("b")));
        parser.endRecord();

        assertThat(parser.next(), is(false));
        parser.close();
    }

    /**
     * has header but content is empty.
     * @throws Exception if failed
     */
    @Test
    public void empty_with_header() throws Exception {
        headers = Arrays.asList("key", "value");
        CsvEmitter emitter = createEmitter();
        emitter.close();

        // directly read a header
        headers = Arrays.asList();
        CsvParser parser = createParser();

        assertThat(parser.next(), is(true));
        assertThat(fill(parser, new StringOption()), is(new StringOption("key")));
        assertThat(fill(parser, new StringOption()), is(new StringOption("value")));
        parser.endRecord();

        assertThat(parser.next(), is(false));
        parser.close();
    }
}
