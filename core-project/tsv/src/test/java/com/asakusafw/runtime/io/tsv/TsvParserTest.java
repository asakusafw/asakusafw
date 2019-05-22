/**
 * Copyright 2011-2019 Asakusa Framework Team.
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
package com.asakusafw.runtime.io.tsv;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;

import org.junit.After;
import org.junit.Test;

import com.asakusafw.runtime.io.tsv.TsvParser;
import com.asakusafw.runtime.value.BooleanOption;
import com.asakusafw.runtime.value.ByteOption;
import com.asakusafw.runtime.value.Date;
import com.asakusafw.runtime.value.DateOption;
import com.asakusafw.runtime.value.DateTime;
import com.asakusafw.runtime.value.DateTimeOption;
import com.asakusafw.runtime.value.DateUtil;
import com.asakusafw.runtime.value.DecimalOption;
import com.asakusafw.runtime.value.DoubleOption;
import com.asakusafw.runtime.value.FloatOption;
import com.asakusafw.runtime.value.IntOption;
import com.asakusafw.runtime.value.LongOption;
import com.asakusafw.runtime.value.ShortOption;
import com.asakusafw.runtime.value.StringOption;

/**
 * Test for {@link TsvParser}.
 */
public class TsvParserTest {

    private TsvParser parser;

    private void create(String fileName) throws IOException {
        InputStream in = TsvParserTest.class.getResourceAsStream(fileName);
        assertThat(fileName, in, is(not(nullValue())));
        parser = new TsvParser(new InputStreamReader(in, "UTF-8"));
    }

    /**
     * Cleans up the test.
     * @throws Exception if some errors were occurred
     */
    @After
    public void tearDown() throws Exception {
        if (parser != null) {
            parser.close();
        }
    }

    /**
     * test for parsing {@code boolean} value.
     * @throws Exception if failed
     */
    @Test
    public void fillBoolean() throws Exception {
        BooleanOption value = new BooleanOption();
        create("boolean");

        assertThat(parser.next(), is(true));
        parser.fill(value);
        assertThat(value.get(), is(true));
        parser.fill(value);
        assertThat(value.get(), is(false));
        parser.endRecord();

        assertThat(parser.next(), is(true));
        parser.fill(value);
        assertThat(value.get(), is(false));
        parser.fill(value);
        assertThat(value.get(), is(true));
        parser.endRecord();

        assertThat(parser.next(), is(true));
        parser.fill(value);
        assertThat(value.isNull(), is(true));
        parser.fill(value);
        assertThat(value.isNull(), is(false));
        parser.endRecord();
    }

    /**
     * test for parsing {@code byte} value.
     * @throws Exception if failed
     */
    @Test
    public void fillByte() throws Exception {
        ByteOption value = new ByteOption();
        create("byte");

        assertThat(parser.next(), is(true));
        parser.fill(value);
        assertThat(value.get(), is((byte) 0));
        parser.fill(value);
        assertThat(value.get(), is((byte) 10));
        parser.fill(value);
        assertThat(value.get(), is((byte) -10));
        parser.endRecord();

        assertThat(parser.next(), is(true));
        parser.fill(value);
        assertThat(value.isNull(), is(true));
        parser.fill(value);
        assertThat(value.get(), is(Byte.MAX_VALUE));
        parser.fill(value);
        assertThat(value.get(), is(Byte.MIN_VALUE));
        parser.endRecord();

        assertThat(parser.next(), is(false));
    }

    /**
     * test for parsing {@code short} value.
     * @throws Exception if failed
     */
    @Test
    public void fillShort() throws Exception {
        ShortOption value = new ShortOption();
        create("short");

        assertThat(parser.next(), is(true));
        parser.fill(value);
        assertThat(value.get(), is((short) 0));
        parser.fill(value);
        assertThat(value.get(), is((short) 10));
        parser.fill(value);
        assertThat(value.get(), is((short) -10));
        parser.endRecord();

        assertThat(parser.next(), is(true));
        parser.fill(value);
        assertThat(value.isNull(), is(true));
        parser.fill(value);
        assertThat(value.get(), is(Short.MAX_VALUE));
        parser.fill(value);
        assertThat(value.get(), is(Short.MIN_VALUE));
        parser.endRecord();

        assertThat(parser.next(), is(false));
    }

    /**
     * test for parsing {@code int} value.
     * @throws Exception if failed
     */
    @Test
    public void fillInt() throws Exception {
        IntOption value = new IntOption();
        create("int");

        assertThat(parser.next(), is(true));
        parser.fill(value);
        assertThat(value.get(), is(0));
        parser.fill(value);
        assertThat(value.get(), is(10));
        parser.fill(value);
        assertThat(value.get(), is(-10));
        parser.endRecord();

        assertThat(parser.next(), is(true));
        parser.fill(value);
        assertThat(value.isNull(), is(true));
        parser.fill(value);
        assertThat(value.get(), is(Integer.MAX_VALUE));
        parser.fill(value);
        assertThat(value.get(), is(Integer.MIN_VALUE));
        parser.endRecord();

        assertThat(parser.next(), is(false));
    }

    /**
     * test for parsing {@code long} value.
     * @throws Exception if failed
     */
    @Test
    public void fillLong() throws Exception {
        LongOption value = new LongOption();
        create("long");

        assertThat(parser.next(), is(true));
        parser.fill(value);
        assertThat(value.get(), is(0L));
        parser.fill(value);
        assertThat(value.get(), is(10L));
        parser.fill(value);
        assertThat(value.get(), is(-10L));
        parser.endRecord();

        assertThat(parser.next(), is(true));
        parser.fill(value);
        assertThat(value.isNull(), is(true));
        parser.fill(value);
        assertThat(value.get(), is(Long.MAX_VALUE));
        parser.fill(value);
        assertThat(value.get(), is(Long.MIN_VALUE));
        parser.endRecord();

        assertThat(parser.next(), is(false));
    }

    /**
     * test for parsing {@code float} value.
     * @throws Exception if failed
     */
    @Test
    public void fillFloat() throws Exception {
        FloatOption value = new FloatOption();
        create("float");

        assertThat(parser.next(), is(true));
        parser.fill(value);
        assertThat(value.get(), is(+0.f));
        parser.fill(value);
        assertThat(value.get(), is(-0.f));
        parser.fill(value);
        assertThat(value.get(), is(+10.5f));
        parser.fill(value);
        assertThat(value.get(), is(-10.5f));
        parser.endRecord();

        assertThat(parser.next(), is(true));
        parser.fill(value);
        assertThat(value.isNull(), is(true));
        parser.fill(value);
        assertThat(value.get(), is(Float.POSITIVE_INFINITY));
        parser.fill(value);
        assertThat(value.get(), is(Float.NEGATIVE_INFINITY));
        parser.fill(value);
        assertThat(value.get(), is(Float.NaN));
        parser.endRecord();

        assertThat(parser.next(), is(false));
    }

    /**
     * test for parsing {@code float} value.
     * @throws Exception if failed
     */
    @Test
    public void fillDouble() throws Exception {
        DoubleOption value = new DoubleOption();
        create("float");

        assertThat(parser.next(), is(true));
        parser.fill(value);
        assertThat(value.get(), is(+0.d));
        parser.fill(value);
        assertThat(value.get(), is(-0.d));
        parser.fill(value);
        assertThat(value.get(), is(+10.5d));
        parser.fill(value);
        assertThat(value.get(), is(-10.5d));
        parser.endRecord();

        assertThat(parser.next(), is(true));
        parser.fill(value);
        assertThat(value.isNull(), is(true));
        parser.fill(value);
        assertThat(value.get(), is(Double.POSITIVE_INFINITY));
        parser.fill(value);
        assertThat(value.get(), is(Double.NEGATIVE_INFINITY));
        parser.fill(value);
        assertThat(value.get(), is(Double.NaN));
        parser.endRecord();

        assertThat(parser.next(), is(false));
    }

    /**
     * test for parsing {@code decimal} value.
     * @throws Exception if failed
     */
    @Test
    public void fillDecimal() throws Exception {
        DecimalOption value = new DecimalOption();
        create("decimal");

        assertThat(parser.next(), is(true));
        parser.fill(value);
        assertThat(value.get(), is(decimal("0")));
        parser.fill(value);
        assertThat(value.get(), is(decimal("10")));
        parser.fill(value);
        assertThat(value.get(), is(decimal("-10")));
        parser.endRecord();

        assertThat(parser.next(), is(true));
        parser.fill(value);
        assertThat(value.isNull(), is(true));
        parser.fill(value);
        assertThat(value.get(), is(decimal("0.9999999999999999999999999999999999999999999999999")));
        parser.fill(value);
        assertThat(value.get(), is(decimal("9223372036854775809")));
        parser.endRecord();

        assertThat(parser.next(), is(false));
    }

    /**
     * test for parsing {@code string} value.
     * @throws Exception if failed
     */
    @Test
    public void fillString() throws Exception {
        StringOption value = new StringOption();
        create("string");

        assertThat(parser.next(), is(true));
        parser.fill(value);
        assertThat(value.getAsString(), is(""));
        parser.fill(value);
        assertThat(value.getAsString(), is("Hello, world!"));
        parser.fill(value);
        assertThat(value.getAsString(), is(TsvEmitterTest.HELLO_JP));
        parser.endRecord();

        assertThat(parser.next(), is(true));
        parser.fill(value);
        assertThat(value.isNull(), is(true));
        parser.fill(value);
        assertThat(value.getAsString(), is("\n\t\\"));
        parser.fill(value);
        assertThat(value.getAsString(), is(TsvEmitterTest.LONG_STRING));
        parser.endRecord();

        assertThat(parser.next(), is(false));
    }

    /**
     * test for parsing {@code date} value.
     * @throws Exception if failed
     */
    @Test
    public void fillDate() throws Exception {
        DateOption value = new DateOption();
        create("date");

        assertThat(parser.next(), is(true));
        parser.fill(value);
        assertThat(value.get(), is(date(2000, 2, 9)));
        parser.fill(value);
        assertThat(value.get(), is(date(2000, 3, 1)));
        parser.fill(value);
        assertThat(value.get(), is(date(100, 3, 30)));
        parser.endRecord();

        assertThat(parser.next(), is(true));
        parser.fill(value);
        assertThat(value.isNull(), is(true));
        parser.fill(value);
        assertThat(value.get(), is(date(1, 1, 1)));
        parser.fill(value);
        assertThat(value.get(), is(date(9999, 12, 31)));
        parser.endRecord();

        assertThat(parser.next(), is(true));
        parser.fill(value);
        assertThat(value.isNull(), is(true));
        parser.fill(value);
        assertThat(value.isNull(), is(true));
        parser.fill(value);
        assertThat(value.isNull(), is(true));

        assertThat(parser.next(), is(false));
    }

    /**
     * test for parsing {@code date-time} value.
     * @throws Exception if failed
     */
    @Test
    public void fillDateTime() throws Exception {
        DateTimeOption value = new DateTimeOption();
        create("datetime");

        assertThat(parser.next(), is(true));
        parser.fill(value);
        assertThat(value.get(), is(time(2000, 2, 9, 1, 2, 3)));
        parser.fill(value);
        assertThat(value.get(), is(time(2000, 3, 1, 8, 9, 10)));
        parser.fill(value);
        assertThat(value.get(), is(time(100, 3, 30, 11, 12, 0)));
        parser.endRecord();

        assertThat(parser.next(), is(true));
        parser.fill(value);
        assertThat(value.isNull(), is(true));
        parser.fill(value);
        assertThat(value.get(), is(time(1, 1, 1, 0, 0, 0)));
        parser.fill(value);
        assertThat(value.get(), is(time(9999, 12, 31, 23, 59, 59)));
        parser.endRecord();

        assertThat(parser.next(), is(true));
        parser.fill(value);
        assertThat(value.isNull(), is(true));
        parser.fill(value);
        assertThat(value.isNull(), is(true));
        parser.fill(value);
        assertThat(value.isNull(), is(true));
        parser.endRecord();

        assertThat(parser.next(), is(false));
    }

    private Date date(int y, int m, int d) {
        int elapsed = DateUtil.getDayFromDate(y, m, d);
        Date date = new Date();
        date.setElapsedDays(elapsed);
        return date;
    }

    private DateTime time(int y, int m, int d, int h, int min, int s) {
        int days = DateUtil.getDayFromDate(y, m, d);
        int secs = DateUtil.getSecondFromTime(h, min, s);
        DateTime date = new DateTime();
        date.setElapsedSeconds((long) days * 86400 + secs);
        return date;
    }

    private BigDecimal decimal(String representation) {
        return new BigDecimal(representation);
    }
}
