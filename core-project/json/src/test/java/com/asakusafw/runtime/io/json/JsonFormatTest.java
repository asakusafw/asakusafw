/**
 * Copyright 2011-2018 Asakusa Framework Team.
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
package com.asakusafw.runtime.io.json;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.junit.Test;

import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.runtime.io.json.JsonFormat.InputOption;
import com.asakusafw.runtime.io.json.JsonFormat.OutputOption;

/**
 * Test for {@link JsonFormat}.
 */
public class JsonFormatTest {

    private static final String PROPERTY_NAME = "p";

    private static final Set<InputOption> IOPTS = JsonFormat.DEFAULT_INPUT_OPTIONS;

    private static final Set<OutputOption> OOPTS = JsonFormat.DEFAULT_OUTPUT_OPTIONS;

    ErrorAction onMalformedInput = ErrorAction.ERROR;

    ErrorAction onMissingInput = ErrorAction.ERROR;

    Charset inputCharset = StandardCharsets.UTF_8;

    /**
     * read - simple case.
     * @throws Exception if failed
     */
    @Test
    public void read() throws Exception {
        JsonFormat<Data> format = reader((r, d) -> d.object = r.readString())
                .build();
        try (ModelInput<Data> in = format.open("<testing>", input("{'p': 'Hello, world!'}"), IOPTS)) {
            assertThat(read(in), contains("Hello, world!"));
        }
    }

    /**
     * read empty file.
     * @throws Exception if failed
     */
    @Test
    public void read_empty_file() throws Exception {
        JsonFormat<Data> format = reader((r, d) -> d.object = r.readString())
                .build();
        try (ModelInput<Data> in = format.open("<testing>", input("// comment only"), IOPTS)) {
            assertThat(read(in), empty());
        }
    }

    /**
     * read - simple case.
     * @throws Exception if failed
     */
    @Test
    public void read_multiple_objects() throws Exception {
        JsonFormat<Data> format = reader((r, d) -> d.object = r.readString())
                .build();
        try (ModelInput<Data> in = format.open("<testing>", input(
                "{'p': 'A'}",
                "{'p': 'B'}",
                "{'p': 'C'}"), IOPTS)) {
            assertThat(read(in), contains("A", "B", "C"));
        }
    }

    /**
     * read malformed file.
     * @throws Exception if failed
     */
    @Test(expected = JsonFormatException.class)
    public void read_malformed_file() throws Exception {
        JsonFormat<Data> format = reader((r, d) -> d.object = r.readString())
                .build();
        try (ModelInput<Data> in = format.open("<testing>", input("\"string value\""), IOPTS)) {
            read(in);
        }
    }

    /**
     * read malformed file.
     * @throws Exception if failed
     */
    @Test(expected = JsonFormatException.class)
    public void read_malformed_object() throws Exception {
        JsonFormat<Data> format = reader((r, d) -> d.object = r.readString())
                .build();
        try (ModelInput<Data> in = format.open("<testing>", input("{1}"), IOPTS)) {
            read(in);
        }
    }

    /**
     * read malformed file.
     * @throws Exception if failed
     */
    @Test(expected = JsonFormatException.class)
    public void read_unexpected_eof() throws Exception {
        JsonFormat<Data> format = reader((r, d) -> d.object = r.readString())
                .build();
        try (ModelInput<Data> in = format.open("<testing>", input("{"), IOPTS)) {
            read(in);
        }
    }

    /**
     * write - simple case.
     * @throws Exception if failed
     */
    @Test
    public void write() throws Exception {
        JsonFormat<Data> format = builder((r, d) -> d.object = r.readString(), (w, d) -> w.writeString(d.stringValue))
                .build();
        byte[] written = write(format, (data, v) -> data.stringValue = v, "Hello, world!");
        assertThat(read(format, written), contains("Hello, world!"));
    }

    /**
     * write - multiple objects.
     * @throws Exception if failed
     */
    @Test
    public void write_multiple_objects() throws Exception {
        JsonFormat<Data> format = builder((r, d) -> d.object = r.readString(), (w, d) -> w.writeString(d.stringValue))
                .build();
        byte[] written = write(format, (data, v) -> data.stringValue = v, "A", "B", "C");
        assertThat(read(format, written), contains("A", "B", "C"));
    }

    /**
     * null value.
     * @throws Exception if failed
     */
    @Test
    public void v_null() throws Exception {
        JsonFormat<Data> format = builder((r, d) -> assertThat(r.isNull(), is(true)), (w, d) -> w.writeNull())
                .build();
        byte[] written = write(format, (data, v) -> data.object = v, (String) null);
        assertThat(read(format, written), contains(nullValue()));
    }

    /**
     * string value.
     * @throws Exception if failed
     */
    @Test
    public void v_string() throws Exception {
        JsonFormat<Data> format = builder((r, d) -> d.object = r.readString(), (w, d) -> w.writeString(d.stringValue))
                .build();
        byte[] written = write(format, (data, v) -> data.stringValue = v, "string");
        assertThat(read(format, written), contains("string"));
    }

    /**
     * string value.
     * @throws Exception if failed
     */
    @Test
    public void v_string_stringbuilder() throws Exception {
        JsonFormat<Data> format = builder(
                (r, d) -> {
                    StringBuilder buf = new StringBuilder();
                    r.readString(buf);
                    d.object = buf.toString();
                },
                (w, d) -> w.writeString(new StringBuilder(d.stringValue)))
                .build();
        byte[] written = write(format, (data, v) -> data.stringValue = v, "buffer");
        assertThat(read(format, written), contains("buffer"));
    }

    /**
     * string value.
     * @throws Exception if failed
     */
    @Test
    public void v_string_charbuffer() throws Exception {
        JsonFormat<Data> format = builder(
                (r, d) -> d.object = r.readString(),
                (w, d) -> w.writeString(CharBuffer.wrap(d.stringValue)))
                .build();
        byte[] written = write(format, (data, v) -> data.stringValue = v, "buffer");
        assertThat(read(format, written), contains("buffer"));
    }

    /**
     * string value.
     * @throws Exception if failed
     */
    @Test
    public void v_string_charbuffer_array() throws Exception {
        JsonFormat<Data> format = builder(
                (r, d) -> d.object = r.readString(),
                (w, d) -> w.writeString(CharBuffer.wrap(d.stringValue.toCharArray())))
                .build();
        byte[] written = write(format, (data, v) -> data.stringValue = v, "buffer");
        assertThat(read(format, written), contains("buffer"));
    }

    /**
     * string value.
     * @throws Exception if failed
     */
    @Test
    public void v_string_stringbuffer() throws Exception {
        JsonFormat<Data> format = builder(
                (r, d) -> d.object = r.readString(),
                (w, d) -> w.writeString(new StringBuffer(d.stringValue)))
                .build();
        byte[] written = write(format, (data, v) -> data.stringValue = v, "buffer");
        assertThat(read(format, written), contains("buffer"));
    }

    /**
     * string value.
     * @throws Exception if failed
     */
    @Test
    public void v_string_int() throws Exception {
        JsonFormat<Data> format = builder((r, d) -> d.object = r.readString(), (w, d) -> w.writeInt(d.intValue))
                .build();
        byte[] written = write(format, (data, v) -> data.intValue = v, 100);
        assertThat(read(format, written), contains("100"));
    }

    /**
     * string value (null).
     * @throws Exception if failed
     */
    @Test(expected = JsonFormatException.class)
    public void v_string_null() throws Exception {
        JsonFormat<Data> format = reader((r, d) -> d.object = r.readString())
                .build();
        try (ModelInput<Data> in = format.open("<testing>", input("{'p': null}"), IOPTS)) {
            read(in);
        }
    }

    /**
     * int value.
     * @throws Exception if failed
     */
    @Test
    public void v_int() throws Exception {
        JsonFormat<Data> format = builder((r, d) -> d.object = r.readInt(), (w, d) -> w.writeInt(d.intValue))
                .build();
        byte[] written = write(format, (data, v) -> data.intValue = v, 100);
        assertThat(read(format, written), contains(100));
    }

    /**
     * int value.
     * @throws Exception if failed
     */
    @Test
    public void v_int_string() throws Exception {
        JsonFormat<Data> format = builder((r, d) -> d.object = r.readInt(), (w, d) -> w.writeString(d.stringValue))
                .build();
        byte[] written = write(format, (data, v) -> data.stringValue = v, "100");
        assertThat(read(format, written), contains(100));
    }

    /**
     * int value (malformed).
     * @throws Exception if failed
     */
    @Test(expected = JsonFormatException.class)
    public void v_int_malformed() throws Exception {
        JsonFormat<Data> format = reader((r, d) -> d.object = r.readInt())
                .build();
        try (ModelInput<Data> in = format.open("<testing>", input("{'p': '?'}"), IOPTS)) {
            read(in);
        }
    }

    /**
     * int value (overflow).
     * @throws Exception if failed
     */
    @Test(expected = JsonFormatException.class)
    public void v_int_overflow() throws Exception {
        JsonFormat<Data> format = reader((r, d) -> d.object = r.readInt())
                .build();
        try (ModelInput<Data> in = format.open("<testing>",
                inputf("{'p': %d}", (long) Integer.MAX_VALUE + 1), IOPTS)) {
            read(in);
        }
    }

    /**
     * long value.
     * @throws Exception if failed
     */
    @Test
    public void v_long() throws Exception {
        JsonFormat<Data> format = builder((r, d) -> d.object = r.readLong(), (w, d) -> w.writeLong(d.longValue))
                .build();
        byte[] written = write(format, (data, v) -> data.longValue = v, 100L);
        assertThat(read(format, written), contains(100L));
    }

    /**
     * long value.
     * @throws Exception if failed
     */
    @Test
    public void v_long_string() throws Exception {
        JsonFormat<Data> format = builder((r, d) -> d.object = r.readLong(), (w, d) -> w.writeString(d.stringValue))
                .build();
        byte[] written = write(format, (data, v) -> data.stringValue = v, "100");
        assertThat(read(format, written), contains(100L));
    }

    /**
     * long value (malformed).
     * @throws Exception if failed
     */
    @Test(expected = JsonFormatException.class)
    public void v_long_malformed() throws Exception {
        JsonFormat<Data> format = reader((r, d) -> d.object = r.readLong())
                .build();
        try (ModelInput<Data> in = format.open("<testing>", input("{'p': '?'}"), IOPTS)) {
            read(in);
        }
    }

    /**
     * int value (overflow).
     * @throws Exception if failed
     */
    @Test(expected = JsonFormatException.class)
    public void v_long_overflow() throws Exception {
        JsonFormat<Data> format = reader((r, d) -> d.object = r.readLong())
                .build();
        try (ModelInput<Data> in = format.open("<testing>", inputf("{'p': %d}",
                BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.valueOf(1))),
                IOPTS)) {
            read(in);
        }
    }

    /**
     * float value.
     * @throws Exception if failed
     */
    @Test
    public void v_float() throws Exception {
        JsonFormat<Data> format = builder((r, d) -> d.object = r.readFloat(), (w, d) -> w.writeFloat(d.floatValue))
                .build();
        byte[] written = write(format, (data, v) -> data.floatValue = v, 1.25f);
        assertThat(read(format, written), contains(1.25f));
    }

    /**
     * float value.
     * @throws Exception if failed
     */
    @Test
    public void v_float_string() throws Exception {
        JsonFormat<Data> format = builder((r, d) -> d.object = r.readFloat(), (w, d) -> w.writeString(d.stringValue))
                .build();
        byte[] written = write(format, (data, v) -> data.stringValue = v, "1.25");
        assertThat(read(format, written), contains(1.25f));
    }

    /**
     * float value.
     * @throws Exception if failed
     */
    @Test
    public void v_float_pinf() throws Exception {
        JsonFormat<Data> format = reader((r, d) -> d.object = r.readFloat())
                .build();
        try (ModelInput<Data> in = format.open("<testing>", input("{'p': '+Infinity'}"), IOPTS)) {
            Float v = (Float) read(in).get(0);
            assertThat(v.isInfinite(), is(true));
            assertThat(v, is(greaterThan(0f)));
        }
    }

    /**
     * float value.
     * @throws Exception if failed
     */
    @Test
    public void v_float_ninf() throws Exception {
        JsonFormat<Data> format = reader((r, d) -> d.object = r.readFloat())
                .build();
        try (ModelInput<Data> in = format.open("<testing>", input("{'p': '-Infinity'}"), IOPTS)) {
            Float v = (Float) read(in).get(0);
            assertThat(v.isInfinite(), is(true));
            assertThat(v, is(lessThan(0f)));
        }
    }

    /**
     * float value.
     * @throws Exception if failed
     */
    @Test
    public void v_float_nan() throws Exception {
        JsonFormat<Data> format = reader((r, d) -> d.object = r.readFloat())
                .build();
        try (ModelInput<Data> in = format.open("<testing>", input("{'p': 'NaN'}"), IOPTS)) {
            Float v = (Float) read(in).get(0);
            assertThat(v.isNaN(), is(true));
        }
    }

    /**
     * float value (malformed).
     * @throws Exception if failed
     */
    @Test(expected = JsonFormatException.class)
    public void v_float_malformed() throws Exception {
        JsonFormat<Data> format = reader((r, d) -> d.object = r.readFloat())
                .build();
        try (ModelInput<Data> in = format.open("<testing>", input("{'p': '?'}"), IOPTS)) {
            read(in);
        }
    }

    /**
     * double value.
     * @throws Exception if failed
     */
    @Test
    public void v_double() throws Exception {
        JsonFormat<Data> format = builder((r, d) -> d.object = r.readDouble(), (w, d) -> w.writeDouble(d.doubleValue))
                .build();
        byte[] written = write(format, (data, v) -> data.doubleValue = v, 1.25d);
        assertThat(read(format, written), contains(1.25d));
    }

    /**
     * double value.
     * @throws Exception if failed
     */
    @Test
    public void v_double_int() throws Exception {
        JsonFormat<Data> format = builder((r, d) -> d.object = r.readDouble(), (w, d) -> w.writeInt(d.intValue))
                .build();
        byte[] written = write(format, (data, v) -> data.intValue = v, 100);
        assertThat(read(format, written), contains(100d));
    }

    /**
     * double value.
     * @throws Exception if failed
     */
    @Test
    public void v_double_string() throws Exception {
        JsonFormat<Data> format = builder((r, d) -> d.object = r.readDouble(), (w, d) -> w.writeString(d.stringValue))
                .build();
        byte[] written = write(format, (data, v) -> data.stringValue = v, "1.25");
        assertThat(read(format, written), contains(1.25d));
    }

    /**
     * double value.
     * @throws Exception if failed
     */
    @Test
    public void v_double_pinf() throws Exception {
        JsonFormat<Data> format = reader((r, d) -> d.object = r.readDouble())
                .build();
        try (ModelInput<Data> in = format.open("<testing>", input("{'p': '+Infinity'}"), IOPTS)) {
            Double v = (Double) read(in).get(0);
            assertThat(v.isInfinite(), is(true));
            assertThat(v, is(greaterThan(0d)));
        }
    }

    /**
     * double value.
     * @throws Exception if failed
     */
    @Test
    public void v_double_ninf() throws Exception {
        JsonFormat<Data> format = reader((r, d) -> d.object = r.readDouble())
                .build();
        try (ModelInput<Data> in = format.open("<testing>", input("{'p': '-Infinity'}"), IOPTS)) {
            Double v = (Double) read(in).get(0);
            assertThat(v.isInfinite(), is(true));
            assertThat(v, is(lessThan(0d)));
        }
    }

    /**
     * double value.
     * @throws Exception if failed
     */
    @Test
    public void v_double_nan() throws Exception {
        JsonFormat<Data> format = reader((r, d) -> d.object = r.readDouble())
                .build();
        try (ModelInput<Data> in = format.open("<testing>", input("{'p': 'NaN'}"), IOPTS)) {
            Double v = (Double) read(in).get(0);
            assertThat(v.isNaN(), is(true));
        }
    }

    /**
     * double value (malformed).
     * @throws Exception if failed
     */
    @Test(expected = JsonFormatException.class)
    public void v_double_malformed() throws Exception {
        JsonFormat<Data> format = reader((r, d) -> d.object = r.readDouble())
                .build();
        try (ModelInput<Data> in = format.open("<testing>", input("{'p': '?'}"), IOPTS)) {
            read(in);
        }
    }

    /**
     * decimal value.
     * @throws Exception if failed
     */
    @Test
    public void v_decimal() throws Exception {
        JsonFormat<Data> format = builder((r, d) -> d.object = r.readDecimal(), (w, d) -> w.writeDecimal(d.decimalValue))
                .build();
        byte[] written = write(format, (data, v) -> data.decimalValue = v, new BigDecimal("3.14"));
        assertThat(read(format, written), contains(new BigDecimal("3.14")));
    }

    /**
     * decimal value.
     * @throws Exception if failed
     */
    @Test
    public void v_decimal_string() throws Exception {
        JsonFormat<Data> format = builder((r, d) -> d.object = r.readDecimal(), (w, d) -> w.writeString(d.stringValue))
                .build();
        byte[] written = write(format, (data, v) -> data.stringValue = v, "3.14");
        assertThat(read(format, written), contains(new BigDecimal("3.14")));
    }

    /**
     * decimal value (malformed).
     * @throws Exception if failed
     */
    @Test(expected = JsonFormatException.class)
    public void v_decimal_malformed() throws Exception {
        JsonFormat<Data> format = reader((r, d) -> d.object = r.readDecimal())
                .build();
        try (ModelInput<Data> in = format.open("<testing>", input("{'p': '?'}"), IOPTS)) {
            read(in);
        }
    }

    /**
     * boolean value.
     * @throws Exception if failed
     */
    @Test
    public void v_boolean() throws Exception {
        JsonFormat<Data> format = builder((r, d) -> d.object = r.readBoolean(), (w, d) -> w.writeBoolean(d.booleanValue))
                .build();
        byte[] written = write(format, (data, v) -> data.booleanValue = v, true);
        assertThat(read(format, written), contains(true));
    }

    /**
     * boolean value.
     * @throws Exception if failed
     */
    @Test
    public void v_boolean_string() throws Exception {
        JsonFormat<Data> format = builder((r, d) -> d.object = r.readBoolean(), (w, d) -> w.writeString(d.stringValue))
                .build();
        byte[] written = write(format, (data, v) -> data.stringValue = v, "true");
        assertThat(read(format, written), contains(true));
    }

    /**
     * boolean value.
     * @throws Exception if failed
     */
    @Test
    public void v_boolean_false() throws Exception {
        JsonFormat<Data> format = builder((r, d) -> d.object = r.readBoolean(), (w, d) -> w.writeString(d.stringValue))
                .build();
        byte[] written = write(format, (data, v) -> data.stringValue = v, "false");
        assertThat(read(format, written), contains(false));
    }

    /**
     * boolean value (malformed).
     * @throws Exception if failed
     */
    @Test(expected = JsonFormatException.class)
    public void v_boolean_malformed() throws Exception {
        JsonFormat<Data> format = reader((r, d) -> d.object = r.readBoolean())
                .build();
        try (ModelInput<Data> in = format.open("<testing>", input("{'p': '?'}"), IOPTS)) {
            read(in);
        }
    }

    /**
     * missing property - error.
     * @throws Exception if failed
     */
    @Test(expected = JsonFormatException.class)
    public void missing_property_error() throws Exception {
        JsonFormat<Data> format = reader((r, d) -> { throw new IllegalStateException(); })
                .build();
        try (ModelInput<Data> in = format.open("<testing>", input("{}"), IOPTS)) {
            read(in);
        }
    }

    /**
     * missing property - ignore.
     * @throws Exception if failed
     */
    @Test
    public void missing_property_ignore() throws Exception {
        onMissingInput = ErrorAction.IGNORE;
        JsonFormat<Data> format = reader((r, d) -> { throw new IllegalStateException(); })
                .build();
        try (ModelInput<Data> in = format.open("<testing>", input("{}"), IOPTS)) {
            assertThat(read(in, it -> it.absent), contains(true));
        }
    }

    /**
     * missing property - ignore.
     * @throws Exception if failed
     */
    @Test
    public void missing_property_report() throws Exception {
        onMissingInput = ErrorAction.REPORT;
        JsonFormat<Data> format = reader((r, d) -> { throw new IllegalStateException(); })
                .build();
        try (ModelInput<Data> in = format.open("<testing>", input("{}"), IOPTS)) {
            assertThat(read(in, it -> it.absent), contains(true));
        }
    }

    /**
     * unknown property - error.
     * @throws Exception if failed
     */
    @Test(expected = JsonFormatException.class)
    public void unknown_property_error() throws Exception {
        JsonFormat<Data> format = reader((r, d) -> d.object = r.readString())
                .build();
        try (ModelInput<Data> in = format.open("<testing>", input("{'p': 'known', 'unknown': null}"), IOPTS)) {
            read(in);
        }
    }

    /**
     * unknown property - ignore.
     * @throws Exception if failed
     */
    @Test
    public void unknown_property_ignore() throws Exception {
        JsonFormat<Data> format = reader((r, d) -> d.object = r.readString())
                .withOnUnknownInput(ErrorAction.IGNORE)
                .build();
        try (ModelInput<Data> in = format.open("<testing>", input("{'p': 'known', 'unknown': null}"), IOPTS)) {
            assertThat(read(in), contains("known"));
        }
    }

    /**
     * unknown property - report.
     * @throws Exception if failed
     */
    @Test
    public void unknown_property_report() throws Exception {
        JsonFormat<Data> format = reader((r, d) -> d.object = r.readString())
                .withOnUnknownInput(ErrorAction.REPORT)
                .build();
        try (ModelInput<Data> in = format.open("<testing>", input("{'p': 'known', 'unknown': null}"), IOPTS)) {
            assertThat(read(in), contains("known"));
        }
    }

    /**
     * unknown property - exclude.
     * @throws Exception if failed
     */
    @Test
    public void unknown_property_exclude() throws Exception {
        JsonFormat<Data> format = reader((r, d) -> d.object = r.readString())
                .withExclude(Pattern.compile("un\\w+"))
                .build();
        try (ModelInput<Data> in = format.open("<testing>", input("{'p': 'known', 'unknown': null}"), IOPTS)) {
            assertThat(read(in), contains("known"));
        }
    }

    /**
     * invalid value.
     * @throws Exception if failed
     */
    @Test(expected = JsonFormatException.class)
    public void v_invalid_error() throws Exception {
        JsonFormat<Data> format = reader((r, d) -> { throw new IllegalArgumentException(); })
                .build();
        try (ModelInput<Data> in = format.open("<testing>", input("{'p': 1}"), IOPTS)) {
            read(in);
        }
    }

    /**
     * invalid value.
     * @throws Exception if failed
     */
    @Test
    public void v_invalid_skip() throws Exception {
        onMalformedInput = ErrorAction.REPORT;
        JsonFormat<Data> format = reader((r, d) -> { throw new IllegalArgumentException(); })
                .build();
        try (ModelInput<Data> in = format.open("<testing>", input("{'p': 1}"), IOPTS)) {
            read(in);
        }
    }

    /**
     * object value.
     * @throws Exception if failed
     */
    @Test(expected = JsonFormatException.class)
    public void v_object_error() throws Exception {
        JsonFormat<Data> format = reader((r, d) -> d.object = r.readString())
                .build();
        try (ModelInput<Data> in = format.open("<testing>", input("{'p': {'nested': 1}}"), IOPTS)) {
            read(in);
        }
    }

    /**
     * object value.
     * @throws Exception if failed
     */
    @Test
    public void v_object_skip() throws Exception {
        onMalformedInput = ErrorAction.REPORT;
        JsonFormat<Data> format = reader((r, d) -> d.object = r.readString())
                .build();
        try (ModelInput<Data> in = format.open("<testing>", input("{'p': {'nested': 1}}"), IOPTS)) {
            assertThat(read(in), contains(nullValue()));
        }
    }

    /**
     * array value.
     * @throws Exception if failed
     */
    @Test(expected = JsonFormatException.class)
    public void v_array_error() throws Exception {
        JsonFormat<Data> format = reader((r, d) -> d.object = r.readString())
                .build();
        try (ModelInput<Data> in = format.open("<testing>", input("{'p': [1, 2, 3]}"), IOPTS)) {
            read(in);
        }
    }

    /**
     * array value.
     * @throws Exception if failed
     */
    @Test
    public void v_array_skip() throws Exception {
        onMalformedInput = ErrorAction.REPORT;
        JsonFormat<Data> format = reader((r, d) -> d.object = r.readString())
                .build();
        try (ModelInput<Data> in = format.open("<testing>", input("{'p': [1, 2, 3]}"), IOPTS)) {
            assertThat(read(in), contains(nullValue()));
        }
    }

    /**
     * read index tracking.
     * @throws Exception if failed
     */
    @Test
    public void read_tracking() throws Exception {
        JsonFormat<Data> format = reader((r, d) -> d.object = r.readString())
                .build();
        try (JsonInput<Data> in = format.open("<testing>", input(
                "{'p': 'A'}",
                "",
                "{'p': 'B'}",
                "",
                "",
                "{'p': 'C'}"), IOPTS)) {
            Data model = new Data();
            assertThat(in.readTo(model), is(true));
            assertThat(in.getRecordIndex(), is(0L));
            assertThat(in.getLineNumber(), is(0L));

            assertThat(in.readTo(model), is(true));
            assertThat(in.getRecordIndex(), is(1L));
            assertThat(in.getLineNumber(), is(2L));

            assertThat(in.readTo(model), is(true));
            assertThat(in.getRecordIndex(), is(2L));
            assertThat(in.getLineNumber(), is(5L));

            assertThat(in.readTo(model), is(false));
        }
    }

    /**
     * read index tracking.
     * @throws Exception if failed
     */
    @Test
    public void read_tracking_disabled() throws Exception {
        JsonFormat<Data> format = reader((r, d) -> d.object = r.readString())
                .build();
        try (JsonInput<Data> in = format.open("<testing>", input(
                "{'p': 'A'}",
                "",
                "{'p': 'B'}",
                "",
                "",
                "{'p': 'C'}"), Collections.emptySet())) {
            Data model = new Data();
            assertThat(in.readTo(model), is(true));
            assertThat(in.getRecordIndex(), is(-1L));
            assertThat(in.getLineNumber(), is(-1L));

            assertThat(in.readTo(model), is(true));
            assertThat(in.getRecordIndex(), is(-1L));
            assertThat(in.getLineNumber(), is(-1L));

            assertThat(in.readTo(model), is(true));
            assertThat(in.getRecordIndex(), is(-1L));
            assertThat(in.getLineNumber(), is(-1L));

            assertThat(in.readTo(model), is(false));
        }
    }

    /**
     * line separator.
     * @throws Exception if failed
     */
    @Test
    public void write_line_separator_unix() throws Exception {
        JsonFormat<Data> format = builder((r, d) -> d.object = r.readString(), (w, d) -> w.writeString(d.stringValue))
                .withLineSeparator(LineSeparator.UNIX)
                .build();
        byte[] written = write(format, (data, v) -> data.stringValue = v, "A", "B", "C");
        String separators = new String(written, StandardCharsets.UTF_8).replaceAll("\\{.*?\\}", ";");
        assertThat(separators, is(";\n;\n;"));
    }

    /**
     * line separator.
     * @throws Exception if failed
     */
    @Test
    public void write_line_separator_windows() throws Exception {
        JsonFormat<Data> format = builder((r, d) -> d.object = r.readString(), (w, d) -> w.writeString(d.stringValue))
                .withLineSeparator(LineSeparator.WINDOWS)
                .build();
        byte[] written = write(format, (data, v) -> data.stringValue = v, "A", "B", "C");
        String separators = new String(written, StandardCharsets.UTF_8).replaceAll("\\{.*?\\}", ";");
        assertThat(separators, is(";\r\n;\r\n;"));
    }

    /**
     * configure charset.
     * @throws Exception if failed
     */
    @Test
    public void charset() throws Exception {
        JsonFormat<Data> format = builder((r, d) -> d.object = r.readString(), (w, d) -> w.writeString(d.stringValue))
                .withCharset(StandardCharsets.UTF_16)
                .build();
        byte[] written = write(format, (data, v) -> data.stringValue = v, "Hello, world!");
        try (ModelInput<Data> in = format.open("<testing>", new ByteArrayInputStream(written), IOPTS)) {
            assertThat(read(in), contains("Hello, world!"));
        }

        String string = new String(written, StandardCharsets.UTF_16);
        assertThat(string, containsString("Hello, world!"));
    }

    /**
     * w/ escape no ascii characters.
     * @throws Exception if failed
     */
    @Test
    public void escape_no_ascii() throws Exception {
        JsonFormat<Data> format = builder((r, d) -> d.object = r.readString(), (w, d) -> w.writeString(d.stringValue))
                .withEscapeNoAsciiCharacter(true)
                .build();
        byte[] written = write(format, (data, v) -> data.stringValue = v, "A=\u3042");
        String lines = new String(written, StandardCharsets.UTF_8);
        assertThat(lines, containsString("A=\\u3042"));
    }

    /**
     * w/o escape no ascii characters.
     * @throws Exception if failed
     */
    @Test
    public void escape_no_ascii_disabled() throws Exception {
        JsonFormat<Data> format = builder((r, d) -> d.object = r.readString(), (w, d) -> w.writeString(d.stringValue))
                .withEscapeNoAsciiCharacter(false)
                .build();
        byte[] written = write(format, (data, v) -> data.stringValue = v, "A=\u3042");
        String lines = new String(written, StandardCharsets.UTF_8);
        assertThat(lines, containsString("A=\u3042"));
    }

    /**
     * use plain decimals.
     * @throws Exception if failed
     */
    @Test
    public void plain_decimal() throws Exception {
        JsonFormat<Data> format = builder((r, d) -> d.object = r.readString(), (w, d) -> w.writeDecimal(d.decimalValue))
                .withUsePlainDecimal(true)
                .build();
        byte[] written = write(format, (data, v) -> data.decimalValue = v,
                new BigDecimal(BigInteger.valueOf(1), -3)); // 1*10^3
        String lines = new String(written, StandardCharsets.UTF_8);
        assertThat(lines, containsString("1000"));
    }

    /**
     * use plain decimals.
     * @throws Exception if failed
     */
    @Test
    public void plain_decimal_disabled() throws Exception {
        JsonFormat<Data> format = builder((r, d) -> d.object = r.readString(), (w, d) -> w.writeDecimal(d.decimalValue))
                .withUsePlainDecimal(false)
                .build();
        byte[] written = write(format, (data, v) -> data.decimalValue = v,
                new BigDecimal(BigInteger.valueOf(1), -3)); // 1*10^3
        String lines = new String(written, StandardCharsets.UTF_8);
        assertThat(lines, containsString("1E+3"));
    }

    /**
     * write w/o redundant while spaces between objects.
     * @throws Exception if failed
     */
    @Test
    public void write_no_object_separator() throws Exception {
        JsonFormat<Data> format = builder((r, d) -> d.object = r.readString(), (w, d) -> w.writeString(d.stringValue))
                .withLineSeparator(LineSeparator.UNIX)
                .build();
        byte[] written = write(format, (data, v) -> data.stringValue = v, "A", "B", "C");
        assertThat(read(format, written), contains("A", "B", "C"));
        List<String> lines = Arrays.stream(new String(written, StandardCharsets.UTF_8).split(LineSeparator.UNIX.getSequence()))
                .collect(Collectors.toList());
        assertThat(lines, everyItem(startsWith("{")));
        assertThat(lines, everyItem(endsWith("}")));
    }

    private InputStream inputf(String format, Object... args) throws IOException {
        return input(String.format(format, args));
    }

    private InputStream input(String... lines) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        for (String line : lines) {
            buffer.write(line.getBytes(inputCharset));
            buffer.write('\n');
        }
        return new ByteArrayInputStream(buffer.toByteArray());
    }

    private static List<Object> read(JsonFormat<Data> format, byte[] bytes) throws IOException {
        return read(format, bytes, d -> d.object);
    }

    private static <T> List<T> read(JsonFormat<Data> format, byte[] bytes, Function<Data, T> extractor) throws IOException {
        try (ModelInput<Data> in = format.open("<testing>", new ByteArrayInputStream(bytes), IOPTS)) {
            return read(in, extractor);
        }
    }

    private static List<Object> read(ModelInput<Data> inputs) throws IOException {
        return read(inputs, d -> d.object);
    }

    private static <T> List<T> read(ModelInput<Data> inputs, Function<Data, T> extractor) throws IOException {
        List<T> results = new ArrayList<>();
        Data data = new Data();
        while (inputs.readTo(data)) {
            results.add(extractor.apply(data));
        }
        return results;
    }

    @SafeVarargs
    private static <T> byte[] write(JsonFormat<Data> format, BiConsumer<Data, T> action, T... values) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (ModelOutput<Data> out = format.open("<testing>", buffer, OOPTS)) {
            Data data = new Data();
            for (T value : values) {
                action.accept(data, value);
                out.write(data);
            }
        }
        return buffer.toByteArray();
    }

    private JsonFormat.Builder<Data> reader(IoAction<ValueReader, Data> action) {
        return builder(action, (writer, data) -> {
            throw new AssertionError();
        });
    }

    private JsonFormat.Builder<Data> builder(IoAction<ValueReader, Data> read, IoAction<ValueWriter, Data> write) {
        PropertyAdapter<Data> adapter = new PropertyAdapter<Data>() {
            @Override
            public void absent(Data property) {
                property.absent = true;
            }
            @Override
            public void read(ValueReader reader, Data property) throws IOException {
                read.perform(reader, property);
                property.absent = false;
            }
            @Override
            public void write(Data property, ValueWriter writer) throws IOException {
                write.perform(writer, property);
            }
        };
        return JsonFormat.builder(Data.class).withProperty(
                it -> it,
                PropertyDefinition.builder(PROPERTY_NAME, () -> adapter)
                        .withOnMalformedInput(onMalformedInput)
                        .withOnMissingInput(onMissingInput)
                        .build());
    }

    @SuppressWarnings("javadoc")
    public static class Data {
        Object object;
        String stringValue;
        int intValue;
        long longValue;
        float floatValue;
        double doubleValue;
        BigDecimal decimalValue;
        boolean booleanValue;
        boolean absent;
    }

    private interface IoAction<V, T> {
        void perform(V value, T property) throws IOException;
    }
}
