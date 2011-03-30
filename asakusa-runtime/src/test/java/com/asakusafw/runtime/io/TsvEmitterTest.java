/**
 * Copyright 2011 Asakusa Framework Team.
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
package com.asakusafw.runtime.io;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;


import org.junit.Before;
import org.junit.Test;

import com.asakusafw.runtime.io.RecordParser;
import com.asakusafw.runtime.io.TsvEmitter;
import com.asakusafw.runtime.io.TsvParser;
import com.asakusafw.runtime.value.BooleanOption;
import com.asakusafw.runtime.value.ByteOption;
import com.asakusafw.runtime.value.Date;
import com.asakusafw.runtime.value.DateOption;
import com.asakusafw.runtime.value.DateTime;
import com.asakusafw.runtime.value.DateTimeOption;
import com.asakusafw.runtime.value.DateUtil;
import com.asakusafw.runtime.value.DecimalOption;
import com.asakusafw.runtime.value.IntOption;
import com.asakusafw.runtime.value.LongOption;
import com.asakusafw.runtime.value.ShortOption;
import com.asakusafw.runtime.value.StringOption;

/**
 * Test for {@link TsvEmitter}.
 */
@SuppressWarnings("deprecation")
public class TsvEmitterTest {

    private static final String LONG_STRING = "あいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよわをんーがぎぐげごだぢづあいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよわをんーがぎぐげごだぢづあいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよわをんーがぎぐげごだぢづあいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよわをんーがぎぐげごだぢづあいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよわをんーがぎぐげごだぢづあいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよわをんーがぎぐげごだぢづあいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよわをんーがぎぐげごだぢづあいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよわをんーがぎぐげごだぢづあいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよわをんーがぎぐげごだぢづあいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよわをんーがぎぐげごだぢづあいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよわをんーがぎぐげごだぢづあいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよわをんーがぎぐげごだぢづあいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよわをんーがぎぐげごだぢづあいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよわをんーがぎぐげごだぢづあいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよわをんーがぎぐげごだぢづあいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよわをんーがぎぐげごだぢづあいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよわをんーがぎぐげごだぢづあいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよわをんーがぎぐげごだぢづあいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよわをんーがぎぐげごだぢづあいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよわをんーがぎぐげごだぢづあいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよわをんーがぎぐげごだぢづあいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよわをんーがぎぐげごだぢづあいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよわをんーがぎぐげごだぢづあいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよわをんーがぎぐげごだぢづあいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよわをんーがぎぐげごだぢづあいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよわをんーがぎぐげごだぢづあいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよわをんーがぎぐげごだぢづあいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよわをんーがぎぐげごだぢづあいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよわをんーがぎぐげごだぢづあいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよわをんーがぎぐげごだぢづあいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよわをんーがぎぐげごだぢづあいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよわをんーがぎぐげごだぢづあいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよわをんーがぎぐげごだぢづあいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよわをんーがぎぐげごだぢづあいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよわをんーがぎぐげごだぢづあいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよわをんーがぎぐげごだぢづあいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよわをんーがぎぐげごだぢづあいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよわをんーがぎぐげごだぢづあいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよわをんーがぎぐげごだぢづあいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよわをんーがぎぐげごだぢづあいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよわをんーがぎぐげごだぢづあいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよわをんーがぎぐげごだぢづあいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよわをんーがぎぐげごだぢづあいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよわをんーがぎぐげごだぢづあいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよわをんーがぎぐげごだぢづあいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよわをんーがぎぐげごだぢづあいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよわをんーがぎぐげごだぢづあいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよわをんーがぎぐげごだぢづあいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよわをんーがぎぐげごだぢづあいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよわをんーがぎぐげごだぢづあいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよわをんーがぎぐげごだぢづあいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよわをんーがぎぐげごだぢづあいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよわをんーがぎぐげごだぢづあいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよわをんーがぎぐげごだぢづあいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよわをんーがぎぐげごだぢづあいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよわをんーがぎぐげごだぢづあいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよわをんーがぎぐげごだぢづあいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよわをんーがぎぐげごだぢづあいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよわをんーがぎぐげごだぢづあいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよわをんーがぎぐげごだぢづあいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよわをんーがぎぐげごだぢづあいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよわをんーがぎぐげごだぢづあいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよわをんーがぎぐげごだぢづあいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよわをんーがぎぐげごだぢづあいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよわをんーがぎぐげごだぢづあいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよわをんーがぎぐげごだぢづあいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよわをんーがぎぐげごだぢづあいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよわをんーがぎぐげごだぢづあいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよわをんーがぎぐげごだぢづあいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよわをんーがぎぐげごだぢづあいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよわをんーがぎぐげごだぢづあいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよわをんーがぎぐげごだぢづあいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよわをんーがぎぐげごだぢづあいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよわをんーがぎぐげごだぢづあいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよわをんーがぎぐげごだぢづあいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよわをんーがぎぐげごだぢづあいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよわをんーがぎぐげごだぢづあいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよわをんーがぎぐげごだぢづあいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよわをんーがぎぐげごだぢづあいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよわをんーがぎぐげごだぢづ";

    private StringWriter buffer = new StringWriter();

    private TsvEmitter emitter;

    /**
     * Initializes the test.
     * @throws Exception if some errors were occurred
     */
    @Before
    public void setUp() throws Exception {
        emitter = new TsvEmitter(buffer);
    }

    private RecordParser parser() throws IOException {
        emitter.close();
        return new TsvParser(new StringReader(buffer.toString()));
    }

    /**
     * booleanの値を出力するテスト。
     * @throws Exception 例外が発生した場合
     */
    @Test
    public void emitBoolean() throws Exception {
        BooleanOption value = new BooleanOption();

        value.modify(true);
        emitter.emit(value);
        value.modify(false);
        emitter.emit(value);
        emitter.endRecord();

        value.modify(false);
        emitter.emit(value);
        value.modify(true);
        emitter.emit(value);
        emitter.endRecord();

        value.setNull();
        emitter.emit(value);
        value.modify(true);
        emitter.emit(value);
        emitter.endRecord();
        emitter.close();

        RecordParser parser = parser();
        assertThat(parser.next(), is(true));
        parser.fill(value);
        assertThat(value.get(), is(true));
        parser.fill(value);
        assertThat(value.get(), is(false));

        assertThat(parser.next(), is(true));
        parser.fill(value);
        assertThat(value.get(), is(false));
        parser.fill(value);
        assertThat(value.get(), is(true));

        assertThat(parser.next(), is(true));
        parser.fill(value);
        assertThat(value.isNull(), is(true));
        parser.fill(value);
        assertThat(value.isNull(), is(false));
    }

    /**
     * byteの値を出力するテスト。
     * @throws Exception 例外が発生した場合
     */
    @Test
    public void emitByte() throws Exception {
        ByteOption value = new ByteOption();

        value.modify((byte) 0);
        emitter.emit(value);
        value.modify((byte) 10);
        emitter.emit(value);
        value.modify((byte) -10);
        emitter.emit(value);
        emitter.endRecord();

        value.setNull();
        emitter.emit(value);
        value.modify(Byte.MAX_VALUE);
        emitter.emit(value);
        value.modify(Byte.MIN_VALUE);
        emitter.emit(value);
        emitter.endRecord();

        emitter.close();

        RecordParser parser = parser();
        assertThat(parser.next(), is(true));
        parser.fill(value);
        assertThat(value.get(), is((byte) 0));
        parser.fill(value);
        assertThat(value.get(), is((byte) 10));
        parser.fill(value);
        assertThat(value.get(), is((byte) -10));

        assertThat(parser.next(), is(true));
        parser.fill(value);
        assertThat(value.isNull(), is(true));
        parser.fill(value);
        assertThat(value.get(), is(Byte.MAX_VALUE));
        parser.fill(value);
        assertThat(value.get(), is(Byte.MIN_VALUE));

        assertThat(parser.next(), is(false));
    }

    /**
     * shortの値を出力するテスト。
     * @throws Exception 例外が発生した場合
     */
    @Test
    public void emitShort() throws Exception {
        ShortOption value = new ShortOption();

        value.modify((short) 0);
        emitter.emit(value);
        value.modify((short) 10);
        emitter.emit(value);
        value.modify((short) -10);
        emitter.emit(value);
        emitter.endRecord();

        value.setNull();
        emitter.emit(value);
        value.modify(Short.MAX_VALUE);
        emitter.emit(value);
        value.modify(Short.MIN_VALUE);
        emitter.emit(value);
        emitter.endRecord();

        emitter.close();

        RecordParser parser = parser();
        assertThat(parser.next(), is(true));
        parser.fill(value);
        assertThat(value.get(), is((short) 0));
        parser.fill(value);
        assertThat(value.get(), is((short) 10));
        parser.fill(value);
        assertThat(value.get(), is((short) -10));

        assertThat(parser.next(), is(true));
        parser.fill(value);
        assertThat(value.isNull(), is(true));
        parser.fill(value);
        assertThat(value.get(), is(Short.MAX_VALUE));
        parser.fill(value);
        assertThat(value.get(), is(Short.MIN_VALUE));

        assertThat(parser.next(), is(false));
    }

    /**
     * intの値を出力するテスト。
     * @throws Exception 例外が発生した場合
     */
    @Test
    public void emitInt() throws Exception {
        IntOption value = new IntOption();

        value.modify(0);
        emitter.emit(value);
        value.modify(10);
        emitter.emit(value);
        value.modify(-10);
        emitter.emit(value);
        emitter.endRecord();

        value.setNull();
        emitter.emit(value);
        value.modify(Integer.MAX_VALUE);
        emitter.emit(value);
        value.modify(Integer.MIN_VALUE);
        emitter.emit(value);
        emitter.endRecord();

        emitter.close();

        RecordParser parser = parser();
        assertThat(parser.next(), is(true));
        parser.fill(value);
        assertThat(value.get(), is(0));
        parser.fill(value);
        assertThat(value.get(), is(10));
        parser.fill(value);
        assertThat(value.get(), is(-10));

        assertThat(parser.next(), is(true));
        parser.fill(value);
        assertThat(value.isNull(), is(true));
        parser.fill(value);
        assertThat(value.get(), is(Integer.MAX_VALUE));
        parser.fill(value);
        assertThat(value.get(), is(Integer.MIN_VALUE));

        assertThat(parser.next(), is(false));
    }

    /**
     * longの値を出力するテスト。
     * @throws Exception 例外が発生した場合
     */
    @Test
    public void emitLong() throws Exception {
        LongOption value = new LongOption();

        value.modify(0L);
        emitter.emit(value);
        value.modify(10L);
        emitter.emit(value);
        value.modify(-10L);
        emitter.emit(value);
        emitter.endRecord();

        value.setNull();
        emitter.emit(value);
        value.modify(Long.MAX_VALUE);
        emitter.emit(value);
        value.modify(Long.MIN_VALUE);
        emitter.emit(value);
        emitter.endRecord();

        emitter.close();
        emitter.close();

        RecordParser parser = parser();
        assertThat(parser.next(), is(true));
        parser.fill(value);
        assertThat(value.get(), is(0L));
        parser.fill(value);
        assertThat(value.get(), is(10L));
        parser.fill(value);
        assertThat(value.get(), is(-10L));

        assertThat(parser.next(), is(true));
        parser.fill(value);
        assertThat(value.isNull(), is(true));
        parser.fill(value);
        assertThat(value.get(), is(Long.MAX_VALUE));
        parser.fill(value);
        assertThat(value.get(), is(Long.MIN_VALUE));

        assertThat(parser.next(), is(false));
    }

    /**
     * decimalの値を出力するテスト。
     * @throws Exception 例外が発生した場合
     */
    @Test
    public void emitDecimal() throws Exception {
        DecimalOption value = new DecimalOption();

        value.modify(decimal("0"));
        emitter.emit(value);
        value.modify(decimal("10"));
        emitter.emit(value);
        value.modify(decimal("-10"));
        emitter.emit(value);
        emitter.endRecord();

        value.setNull();
        emitter.emit(value);
        value.modify(decimal("0.9999999999999999999999999999999999999999999999999"));
        emitter.emit(value);
        value.modify(decimal("9223372036854775809"));
        emitter.emit(value);
        emitter.endRecord();

        emitter.close();

        RecordParser parser = parser();
        assertThat(parser.next(), is(true));
        parser.fill(value);
        assertThat(value.get(), is(decimal("0")));
        parser.fill(value);
        assertThat(value.get(), is(decimal("10")));
        parser.fill(value);
        assertThat(value.get(), is(decimal("-10")));

        assertThat(parser.next(), is(true));
        parser.fill(value);
        assertThat(value.isNull(), is(true));
        parser.fill(value);
        assertThat(value.get(), is(decimal("0.9999999999999999999999999999999999999999999999999")));
        parser.fill(value);
        assertThat(value.get(), is(decimal("9223372036854775809")));

        assertThat(parser.next(), is(false));
    }

    /**
     * 文字列を出力するテスト。
     * @throws Exception 例外が発生した場合
     */
    @Test
    public void emitString() throws Exception {
        StringOption value = new StringOption();

        value.modify("");
        emitter.emit(value);
        value.modify("Hello, world!");
        emitter.emit(value);
        value.modify("こんにちは、世界！");
        emitter.emit(value);
        emitter.endRecord();

        value.setNull();
        emitter.emit(value);
        value.modify("\n\t\\");
        emitter.emit(value);
        value.modify(LONG_STRING);
        emitter.emit(value);
        emitter.endRecord();

        emitter.close();

        RecordParser parser = parser();
        assertThat(parser.next(), is(true));
        parser.fill(value);
        assertThat(value.getAsString(), is(""));
        parser.fill(value);
        assertThat(value.getAsString(), is("Hello, world!"));
        parser.fill(value);
        assertThat(value.getAsString(), is("こんにちは、世界！"));

        assertThat(parser.next(), is(true));
        parser.fill(value);
        assertThat(value.isNull(), is(true));
        parser.fill(value);
        assertThat(value.getAsString(), is("\n\t\\"));
        parser.fill(value);
        assertThat(value.getAsString(), is(LONG_STRING));

        assertThat(parser.next(), is(false));
    }

    /**
     * 日付の値を出力するテスト。
     * @throws Exception 例外が発生した場合
     */
    @Test
    public void emitDate() throws Exception {
        DateOption value = new DateOption();

        value.modify(date(2000, 2, 9));
        emitter.emit(value);
        value.modify(date(2000, 3, 1));
        emitter.emit(value);
        value.modify(date(100, 3, 30));
        emitter.emit(value);
        emitter.endRecord();

        value.setNull();
        emitter.emit(value);
        value.modify(date(1, 1, 1));
        emitter.emit(value);
        value.modify(date(9999, 12, 31));
        emitter.emit(value);
        emitter.endRecord();

        emitter.close();

        RecordParser parser = parser();
        assertThat(parser.next(), is(true));
        parser.fill(value);
        assertThat(value.get(), is(date(2000, 2, 9)));
        parser.fill(value);
        assertThat(value.get(), is(date(2000, 3, 1)));
        parser.fill(value);
        assertThat(value.get(), is(date(100, 3, 30)));

        assertThat(parser.next(), is(true));
        parser.fill(value);
        assertThat(value.isNull(), is(true));
        parser.fill(value);
        assertThat(value.get(), is(date(1, 1, 1)));
        parser.fill(value);
        assertThat(value.get(), is(date(9999, 12, 31)));

        assertThat(parser.next(), is(false));
    }


    /**
     * 時刻の値を出力するテスト。
     * @throws Exception 例外が発生した場合
     */
    @Test
    public void emitDateTime() throws Exception {
        DateTimeOption value = new DateTimeOption();

        value.modify(time(2000, 2, 9, 1, 2, 3));
        emitter.emit(value);
        value.modify(time(2000, 3, 1, 8, 9, 10));
        emitter.emit(value);
        value.modify(time(100, 3, 30, 11, 12, 0));
        emitter.emit(value);
        emitter.endRecord();

        value.setNull();
        emitter.emit(value);
        value.modify(time(1, 1, 1, 0, 0, 0));
        emitter.emit(value);
        value.modify(time(9999, 12, 31, 23, 59, 59));
        emitter.emit(value);
        emitter.endRecord();

        emitter.close();

        RecordParser parser = parser();
        assertThat(parser.next(), is(true));
        parser.fill(value);
        assertThat(value.get(), is(time(2000, 2, 9, 1, 2, 3)));
        parser.fill(value);
        assertThat(value.get(), is(time(2000, 3, 1, 8, 9, 10)));
        parser.fill(value);
        assertThat(value.get(), is(time(100, 3, 30, 11, 12, 0)));

        assertThat(parser.next(), is(true));
        parser.fill(value);
        assertThat(value.isNull(), is(true));
        parser.fill(value);
        assertThat(value.get(), is(time(1, 1, 1, 0, 0, 0)));
        parser.fill(value);
        assertThat(value.get(), is(time(9999, 12, 31, 23, 59, 59)));

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

    // TODO BigDecimal
    private BigDecimal decimal(String representation) {
        return new BigDecimal(representation);
    }
}
