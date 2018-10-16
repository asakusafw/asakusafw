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
package com.asakusafw.dmdl.directio.json.driver;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExternalResource;

import com.asakusafw.dmdl.directio.common.driver.GeneratorTesterRoot;
import com.asakusafw.dmdl.java.emitter.driver.ObjectDriver;
import com.asakusafw.runtime.directio.BinaryStreamFormat;
import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.runtime.io.json.JsonFormatException;
import com.asakusafw.runtime.io.json.PropertyAdapter;
import com.asakusafw.runtime.io.json.ValueReader;
import com.asakusafw.runtime.io.json.ValueWriter;
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
 * Test for {@link JsonFormatEmitter}.
 */
public class JsonFormatEmitterTest extends GeneratorTesterRoot {

    private static final String SEGMENT = "json";

    /**
     * Escapes time zone.
     */
    @Rule
    public final ExternalResource escapeTimeZone = new ExternalResource() {
        TimeZone escape;
        @Override
        protected void before() {
            escape = TimeZone.getDefault();
            TimeZone.setDefault(TimeZone.getTimeZone("PST"));
        }
        @Override
        protected void after() {
            if (escape != null) {
                TimeZone.setDefault(escape);
            }
        }
    };

    /**
     * Initializes the test.
     * @throws Exception if some errors were occurred
     */
    @Before
    public void setUp() throws Exception {
        emitDrivers.add(new JsonFormatEmitter());
        emitDrivers.add(new ObjectDriver());
    }

    /**
     * simple case.
     * @throws Exception if failed
     */
    @Test
    public void simple() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.json",
                "simple = { value : TEXT; };",
        });
        ModelWrapper model = loaded.newModel("Simple")
                .setOption("value", new StringOption("Hello, world!"));

        BinaryStreamFormat<?> support = (BinaryStreamFormat<?>) loaded.newObject(SEGMENT, "SimpleJsonFormat");
        assertThat(support.getSupportedType(), is((Object) model.unwrap().getClass()));
        assertThat(support.getMinimumFragmentSize(), is(lessThan(0L)));

        BinaryStreamFormat<Object> unsafe = unsafe(support);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (ModelOutput<Object> writer = unsafe.createOutput(unsafe.getSupportedType(), "hello", output)) {
            writer.write(model.unwrap());
        }

        assertThat(parse(output.toByteArray()), contains(map("value", "'Hello, world!'")));

        Object buffer = loaded.newModel("Simple").unwrap();
        try (ModelInput<Object> reader = unsafe.createInput(unsafe.getSupportedType(), "hello", in(output))) {
            assertThat(reader.readTo(buffer), is(true));
            assertThat(buffer, is(model.unwrap()));

            assertThat(reader.readTo(buffer), is(false));
        }
    }

    /**
     * w/ multiple rows/cols.
     * @throws Exception if failed
     */
    @Test
    public void multiple() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.json",
                "simple = {",
                "  a : TEXT;",
                "  b : TEXT;",
                "  c : TEXT;",
                "};",
        });
        byte[] contents = restore(loaded,
                loaded.newModel("Simple")
                        .setOption("a", new StringOption("0a"))
                        .setOption("b", new StringOption("0b"))
                        .setOption("c", new StringOption("0c")),
                loaded.newModel("Simple")
                        .setOption("a", new StringOption("1a"))
                        .setOption("b", new StringOption("1b"))
                        .setOption("c", new StringOption("1c")),
                loaded.newModel("Simple")
                        .setOption("a", new StringOption("2a"))
                        .setOption("b", new StringOption("2b"))
                        .setOption("c", new StringOption("2c")));
        assertThat(parse(contents), contains(
                map("a", "'0a'", "b", "'0b'", "c", "'0c'"),
                map("a", "'1a'", "b", "'1b'", "c", "'1c'"),
                map("a", "'2a'", "b", "'2b'", "c", "'2c'")));
    }

    /**
     * w/ {@code BOOLEAN} field.
     * @throws Exception if failed
     */
    @Test
    public void type_boolean() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.json",
                "simple = {",
                "  value : BOOLEAN;",
                "};",
        });
        byte[] contents = restore(loaded, loaded.newModel("Simple")
                .setOption("value", new BooleanOption(true)));
        assertThat(parse(contents), contains(map("value", "true")));
    }

    /**
     * w/ {@code BYTE} field.
     * @throws Exception if failed
     */
    @Test
    public void type_byte() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.json",
                "simple = {",
                "  value : BYTE;",
                "};",
        });
        byte[] contents = restore(loaded, loaded.newModel("Simple")
                .setOption("value", new ByteOption((byte) 1)));
        assertThat(parse(contents), contains(map("value", "1")));
    }

    /**
     * w/ {@code SHORT} field.
     * @throws Exception if failed
     */
    @Test
    public void type_short() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.json",
                "simple = {",
                "  value : SHORT;",
                "};",
        });
        byte[] contents = restore(loaded, loaded.newModel("Simple")
                .setOption("value", new ShortOption((short) 1)));
        assertThat(parse(contents), contains(map("value", "1")));
    }

    /**
     * w/ {@code INT} field.
     * @throws Exception if failed
     */
    @Test
    public void type_int() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.json",
                "simple = {",
                "  value : INT;",
                "};",
        });
        byte[] contents = restore(loaded, loaded.newModel("Simple")
                .setOption("value", new IntOption(1)));
        assertThat(parse(contents), contains(map("value", "1")));
    }

    /**
     * w/ {@code LONG} field.
     * @throws Exception if failed
     */
    @Test
    public void type_long() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.json",
                "simple = {",
                "  value : LONG;",
                "};",
        });
        byte[] contents = restore(loaded, loaded.newModel("Simple")
                .setOption("value", new LongOption(1)));
        assertThat(parse(contents), contains(map("value", "1")));
    }

    /**
     * w/ {@code FLOAT} field.
     * @throws Exception if failed
     */
    @Test
    public void type_float() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.json",
                "simple = {",
                "  value : FLOAT;",
                "};",
        });
        byte[] contents = restore(loaded, loaded.newModel("Simple")
                .setOption("value", new FloatOption(1)));
        assertThat(parse(contents), contains(map("value", "1.0")));
    }

    /**
     * w/ {@code DOUBLE} field.
     * @throws Exception if failed
     */
    @Test
    public void type_double() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.json",
                "simple = {",
                "  value : DOUBLE;",
                "};",
        });
        byte[] contents = restore(loaded, loaded.newModel("Simple")
                .setOption("value", new DoubleOption(1)));
        assertThat(parse(contents), contains(map("value", "1.0")));
    }

    /**
     * w/ format.
     * @throws Exception if failed
     */
    @Test
    public void format() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.json(",
                "  format = jsonl,",
                ")",
                "simple = {",
                "  value : TEXT;",
                "};",
        });
        BinaryStreamFormat<?> support = (BinaryStreamFormat<?>) loaded.newObject(SEGMENT, "SimpleJsonFormat");
        assertThat(support.getMinimumFragmentSize(), is(greaterThan(0L)));

        byte[] contents = restore(loaded,
                loaded.newModel("Simple").setOption("value", new StringOption("Hello, world!")));
        assertThat(parse(contents), contains(map("value", "'Hello, world!'")));
    }

    /**
     * w/ charset.
     * @throws Exception if failed
     */
    @Test
    public void charset() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.json(",
                "  charset = 'UTF-16LE',",
                ")",
                "simple = {",
                "  value : TEXT;",
                "};",
        });

        byte[] contents = restore(loaded,
                loaded.newModel("Simple").setOption("value", new StringOption("Hello, world!")));
        assertThat(parse(contents, StandardCharsets.UTF_16LE), contains(map("value", "'Hello, world!'")));
    }

    /**
     * charset may suppress splitting input.
     * @throws Exception if failed
     */
    @Test
    public void charset_suppress_splitting() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.json(",
                "  format = jsonl,",
                "  charset = 'UTF-16LE',",
                ")",
                "simple = {",
                "  value : TEXT;",
                "};",
        });
        BinaryStreamFormat<?> support = (BinaryStreamFormat<?>) loaded.newObject(SEGMENT, "SimpleJsonFormat");
        assertThat(support.getMinimumFragmentSize(), is(lessThan(0L)));

        byte[] contents = restore(loaded,
                loaded.newModel("Simple").setOption("value", new StringOption("Hello, world!")));
        assertThat(parse(contents, StandardCharsets.UTF_16LE), contains(map("value", "'Hello, world!'")));
    }

    /**
     * w/ compression.
     * @throws Exception if failed
     */
    @Test
    public void compression() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.json(",
                "  format = jsonl,",
                "  compression = gzip,",
                ")",
                "simple = {",
                "  value : TEXT;",
                "};",
        });
        BinaryStreamFormat<?> support = (BinaryStreamFormat<?>) loaded.newObject(SEGMENT, "SimpleJsonFormat");
        assertThat(support.getMinimumFragmentSize(), is(lessThan(0L)));

        byte[] contents = restore(loaded,
                loaded.newModel("Simple").setOption("value", new StringOption("Hello, world!")));

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (InputStream in = new GZIPInputStream(new ByteArrayInputStream(contents))) {
            byte[] buf = new byte[256];
            while (true) {
                int read = in.read(buf);
                if (read < 0) {
                    break;
                }
                buffer.write(buf, 0, read);
            }
        }
        assertThat(parse(buffer.toByteArray()), contains(map("value", "'Hello, world!'")));
    }

    /**
     * w/ line_separator.
     * @throws Exception if failed
     */
    @Test
    public void line_separator() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.json(",
                "  line_separator = windows,",
                ")",
                "simple = {",
                "  value : TEXT;",
                "};",
        });

        byte[] contents = restore(loaded,
                loaded.newModel("Simple").setOption("value", new StringOption("A")),
                loaded.newModel("Simple").setOption("value", new StringOption("B")));
        String lines = new String(contents, StandardCharsets.UTF_8).replaceAll("\\{.*?\\}", "{}");
        assertThat(lines, is("{}\r\n{}"));
    }

    /**
     * w/ {@code date_format}.
     * @throws Exception if failed
     */
    @Test
    public void date_format() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.json(",
                "  date_format = 'yyyy_MM_dd',",
                ")",
                "simple = {",
                "  a : DATE;",
                "};",
        });
        byte[] contents = restore(loaded, loaded.newModel("Simple")
                .setOption("a", new DateOption(new Date(2019, 1, 2))));
        assertThat(parse(contents), contains(map("a", "'2019_01_02'")));
    }

    /**
     * w/ {@code date_format}.
     * @throws Exception if failed
     */
    @Test
    public void date_format_field() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.json",
                "simple = {",
                "  @directio.json.field(date_format = 'yyyy_MM_dd')",
                "  a : DATE;",
                "  b : DATE;",
                "};",
        });
        byte[] contents = restore(loaded, loaded.newModel("Simple")
                .setOption("a", new DateOption(new Date(2019, 1, 2)))
                .setOption("b", new DateOption(new Date(2019, 1, 2))));
        assertThat(parse(contents), contains(map("a", "'2019_01_02'", "b", "'2019-01-02'")));
    }

    /**
     * w/ {@code datetime_format}.
     * @throws Exception if failed
     */
    @Test
    public void datetime_format() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.json(",
                "  datetime_format = 'yyyy_MM_dd HH-mm-ss',",
                ")",
                "simple = {",
                "  a : DATETIME;",
                "};",
        });
        byte[] contents = restore(loaded, loaded.newModel("Simple")
                .setOption("a", new DateTimeOption(new DateTime(2019, 1, 2, 22, 10, 0))));
        assertThat(parse(contents), contains(map("a", "'2019_01_02 22-10-00'")));
    }

    /**
     * w/ {@code datetime_format}.
     * @throws Exception if failed
     */
    @Test
    public void datetime_format_field() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.json",
                "simple = {",
                "  @directio.json.field(datetime_format = 'yyyy_MM_dd HH-mm-ss')",
                "  a : DATETIME;",
                "  b : DATETIME;",
                "};",
        });
        byte[] contents = restore(loaded, loaded.newModel("Simple")
                .setOption("a", new DateTimeOption(new DateTime(2019, 1, 2, 22, 10, 0)))
                .setOption("b", new DateTimeOption(new DateTime(2019, 1, 2, 22, 10, 0))));
        assertThat(parse(contents), contains(map(
                "a", "'2019_01_02 22-10-00'",
                "b", "'2019-01-02 22:10:00'")));
    }

    /**
     * w/ {@code datetime_format} (raise warning).
     * @throws Exception if failed
     */
    @Test
    public void datetime_format_inconsistent_type() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.json",
                "simple = {",
                "  @directio.json.field(datetime_format = '_')",
                "  a : TEXT;",
                "};",
        });
        byte[] contents = restore(loaded, loaded.newModel("Simple")
                .setOption("a", new StringOption("Hello, world!")));
        assertThat(parse(contents), contains(map("a", "'Hello, world!'")));
    }

    /**
     * w/ {@code timezone}.
     * @throws Exception if failed
     */
    @Test
    public void timezone() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.json(",
                "  timezone = 'UTC',",
                ")",
                "simple = {",
                "  a : DATETIME;",
                "};",
        });
        byte[] contents = restore(loaded, loaded.newModel("Simple")
                .setOption("a", new DateTimeOption(new DateTime(2019, 1, 2, 12, 34, 56))));
        assertThat(parse(contents), contains(map("a", "'2019-01-02 20:34:56'")));
    }

    /**
     * w/ {@code timezone}.
     * @throws Exception if failed
     */
    @Test
    public void timezone_field() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.json",
                "simple = {",
                "  a : DATETIME;",
                "  @directio.json.field(timezone = 'UTC')",
                "  b : DATETIME;",
                "};",
        });
        byte[] contents = restore(loaded, loaded.newModel("Simple")
                .setOption("a", new DateTimeOption(new DateTime(2019, 1, 2, 12, 34, 56)))
                .setOption("b", new DateTimeOption(new DateTime(2019, 1, 2, 12, 34, 56))));
        assertThat(parse(contents), contains(map(
                "a", "'2019-01-02 12:34:56'",
                "b", "'2019-01-02 20:34:56'")));
    }

    /**
     * w/ {@code timezone}.
     * @throws Exception if failed
     */
    @Test
    public void timezone_null() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.json(",
                "  timezone = 'UTC',",
                ")",
                "simple = {",
                "  @directio.json.field(timezone = null)",
                "  a : DATETIME;",
                "  b : DATETIME;",
                "};",
        });
        byte[] contents = restore(loaded, loaded.newModel("Simple")
                .setOption("a", new DateTimeOption(new DateTime(2019, 1, 2, 12, 34, 56)))
                .setOption("b", new DateTimeOption(new DateTime(2019, 1, 2, 12, 34, 56))));
        assertThat(parse(contents), contains(map(
                "a", "'2019-01-02 12:34:56'",
                "b", "'2019-01-02 20:34:56'")));
    }

    /**
     * w/ {@code timezone} for non datetime field.
     * @throws Exception if failed
     */
    @Test
    public void timezone_inconsistent_type() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.json",
                "simple = {",
                "  @directio.json.field(timezone = 'UTC')",
                "  a : TEXT;",
                "};",
        });
        byte[] contents = restore(loaded, loaded.newModel("Simple")
                .setOption("a", new StringOption("Hello, world!")));
        assertThat(parse(contents), contains(map("a", "'Hello, world!'")));
    }

    /**
     * w/ {@code decimal_output_style}.
     * @throws Exception if failed
     */
    @Test
    public void decimal_output_style() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.json(",
                "  decimal_output_style = plain,",
                ")",
                "simple = {",
                "  a : DECIMAL;",
                "};",
        });
        ModelWrapper model = loaded.newModel("Simple")
                .setOption("a", new DecimalOption(new BigDecimal(BigInteger.valueOf(12), -10)));
        byte[] contents = write(loaded, model);
        assertThat(parse(contents), contains(map("a", "120000000000")));
    }

    /**
     * w/ {@code null_style}.
     * @throws Exception if failed
     */
    @Test
    public void null_style() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.json(",
                "  null_style = absent,",
                ")",
                "simple = {",
                "  a : TEXT;",
                "};",
        });
        ModelWrapper model = loaded.newModel("Simple")
                .setOption("a", new StringOption());
        byte[] contents = write(loaded, model);
        assertThat(parse(contents), contains(map()));
    }

    /**
     * w/ {@code null_style}.
     * @throws Exception if failed
     */
    @Test
    public void null_style_field() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.json(",
                ")",
                "simple = {",
                "  @directio.json.field(null_style = absent)",
                "  a : TEXT;",
                "  b : TEXT;",
                "};",
        });
        ModelWrapper model = loaded.newModel("Simple")
                .setOption("a", new StringOption())
                .setOption("b", new StringOption());
        byte[] contents = write(loaded, model);
        assertThat(parse(contents), contains(map("b", "null")));
    }

    /**
     * w/ {@code null_style}.
     * @throws Exception if failed
     */
    @Test
    public void null_style_override() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.json(",
                "  null_style = absent,",
                ")",
                "simple = {",
                "  a : TEXT;",
                "  @directio.json.field(null_style = value)",
                "  b : TEXT;",
                "};",
        });
        ModelWrapper model = loaded.newModel("Simple")
                .setOption("a", new StringOption())
                .setOption("b", new StringOption());
        byte[] contents = write(loaded, model);
        assertThat(parse(contents), contains(map("b", "null")));
    }

    /**
     * w/ {@code escape_non_ascii}.
     * @throws Exception if failed
     */
    @Test
    public void escape_non_ascii() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.json(",
                "  escape_non_ascii = true,",
                ")",
                "simple = {",
                "  a : TEXT;",
                "};",
        });
        ModelWrapper model = loaded.newModel("Simple")
                .setOption("a", new StringOption("A=\u3042"));
        byte[] contents = write(loaded, model);
        assertThat(parse(contents), contains(map("a", "'A=\\u3042'")));
    }

    /**
     * w/ {@code on_malformed_input}.
     * @throws Exception if failed
     */
    @Test
    public void on_malformed_input_error() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.json(",
                "  on_malformed_input = error,",
                ")",
                "simple = {",
                "  a : INT;",
                "};",
        });
        read(loaded, "{'a':'100'}", wrapper -> {
            assertThat(wrapper.getOption("a"), is(new IntOption(100)));
        });
        readError(loaded, "{'a':'ABC'}");
    }

    /**
     * w/ {@code on_malformed_input}.
     * @throws Exception if failed
     */
    @Test
    public void on_malformed_input_report() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.json(",
                "  on_malformed_input = report,",
                ")",
                "simple = {",
                "  a : INT;",
                "};",
        });
        read(loaded, "{'a':'100'}", wrapper -> {
            assertThat(wrapper.getOption("a"), is(new IntOption(100)));
        });
        read(loaded, "{'a':'ABC'}", wrapper -> {
            assertThat(wrapper.getOption("a"), is(new IntOption()));
        });
    }

    /**
     * w/ {@code on_malformed_input}.
     * @throws Exception if failed
     */
    @Test
    public void on_malformed_input_field() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.json(",
                ")",
                "simple = {",
                "  @directio.json.field(on_malformed_input = report)",
                "  a : INT;",
                "  b : INT;",
                "};",
        });
        read(loaded, "{'a':'ABC', 'b': '100'}", wrapper -> {
            assertThat(wrapper.getOption("a"), is(new IntOption()));
            assertThat(wrapper.getOption("b"), is(new IntOption(100)));
        });
        readError(loaded, "{'a':'100', 'b': 'ABC'}");
    }

    /**
     * w/ {@code on_malformed_input}.
     * @throws Exception if failed
     */
    @Test
    public void on_malformed_input_override() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.json(",
                "  on_malformed_input = ignore,",
                ")",
                "simple = {",
                "  a : INT;",
                "  @directio.json.field(on_malformed_input = error)",
                "  b : INT;",
                "};",
        });
        read(loaded, "{'a':'ABC', 'b': '100'}", wrapper -> {
            assertThat(wrapper.getOption("a"), is(new IntOption()));
            assertThat(wrapper.getOption("b"), is(new IntOption(100)));
        });
        readError(loaded, "{'a':'100', 'b': 'ABC'}");
    }

    /**
     * w/ {@code on_missing_input}.
     * @throws Exception if failed
     */
    @Test
    public void on_missing_input_error() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.json(",
                "  on_missing_input = error,",
                ")",
                "simple = {",
                "  a : INT;",
                "};",
        });
        read(loaded, "{'a':null}", wrapper -> {
            assertThat(wrapper.getOption("a"), is(new IntOption()));
        });
        readError(loaded, "{}");
    }

    /**
     * w/ {@code on_missing_input}.
     * @throws Exception if failed
     */
    @Test
    public void on_missing_input_report() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.json(",
                "  on_missing_input = report,",
                ")",
                "simple = {",
                "  a : INT;",
                "};",
        });
        read(loaded, "{'a':null}", wrapper -> {
            assertThat(wrapper.getOption("a"), is(new IntOption()));
        });
        read(loaded, "{}", wrapper -> {
            assertThat(wrapper.getOption("a"), is(new IntOption()));
        });
    }

    /**
     * w/ {@code on_missing_input}.
     * @throws Exception if failed
     */
    @Test
    public void on_missing_input_field() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.json(",
                ")",
                "simple = {",
                "  a : INT;",
                "  @directio.json.field(on_missing_input = error)",
                "  b : INT;",
                "};",
        });
        read(loaded, "{'b': '100'}", wrapper -> {
            assertThat(wrapper.getOption("a"), is(new IntOption()));
            assertThat(wrapper.getOption("b"), is(new IntOption(100)));
        });
        readError(loaded, "{'a':'100'}");
    }

    /**
     * w/ {@code on_missing_input}.
     * @throws Exception if failed
     */
    @Test
    public void on_missing_input_override() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.json(",
                "  on_missing_input = error,",
                ")",
                "simple = {",
                "  @directio.json.field(on_missing_input = ignore)",
                "  a : INT;",
                "  b : INT;",
                "};",
        });
        read(loaded, "{'b': '100'}", wrapper -> {
            assertThat(wrapper.getOption("a"), is(new IntOption()));
            assertThat(wrapper.getOption("b"), is(new IntOption(100)));
        });
        readError(loaded, "{'a':'100'}");
    }

    /**
     * w/ {@code on_unknown_input}.
     * @throws Exception if failed
     */
    @Test
    public void on_unknown_input_error() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.json(",
                "  on_unknown_input = error,",
                ")",
                "simple = {",
                "  a : INT;",
                "};",
        });
        read(loaded, "{'a': 100}", wrapper -> {
            assertThat(wrapper.getOption("a"), is(new IntOption(100)));
        });
        readError(loaded, "{'a': 100, 'unknown': null}");
    }

    /**
     * w/ {@code on_unknown_input}.
     * @throws Exception if failed
     */
    @Test
    public void on_unknown_input_report() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.json(",
                "  on_unknown_input = report,",
                ")",
                "simple = {",
                "  a : INT;",
                "};",
        });
        read(loaded, "{'a': 100}", wrapper -> {
            assertThat(wrapper.getOption("a"), is(new IntOption(100)));
        });
        read(loaded, "{'a': 100, 'unknown': null}", wrapper -> {
            assertThat(wrapper.getOption("a"), is(new IntOption(100)));
        });
    }

    /**
     * w/ {@code adapter}.
     * @throws Exception if failed
     */
    @Test
    public void adapter() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.json(",
                "  adapter = '" + getMockAdapter() + "'",
                ")",
                "simple = {",
                "  a : TEXT;",
                "};",
        });
        ModelWrapper model = loaded.newModel("Simple")
                .setOption("a", new StringOption("hello"));
        byte[] contents = restore(loaded, model);
        assertThat(parse(contents), contains(map("a", "'HELLO'")));
    }

    /**
     * w/ {@code adapter}.
     * @throws Exception if failed
     */
    @Test
    public void adapter_field() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.json",
                "simple = {",
                "  @directio.json.field(adapter = '" + getMockAdapter() + "')",
                "  a : TEXT;",
                "  b : TEXT;",
                "};",
        });
        ModelWrapper model = loaded.newModel("Simple")
                .setOption("a", new StringOption("hello"))
                .setOption("b", new StringOption("hello"));
        byte[] contents = restore(loaded, model);
        assertThat(parse(contents), contains(map("a", "'HELLO'", "b", "'hello'")));
    }

    /**
     * w/ {@code adapter}.
     * @throws Exception if failed
     */
    @Test
    public void adapter_override() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.json(",
                "  adapter = '" + getMockAdapter() + "'",
                ")",
                "simple = {",
                "  a : TEXT;",
                "  @directio.json.field(adapter = null)",
                "  b : TEXT;",
                "};",
        });
        ModelWrapper model = loaded.newModel("Simple")
                .setOption("a", new StringOption("hello"))
                .setOption("b", new StringOption("hello"));
        byte[] contents = restore(loaded, model);
        assertThat(parse(contents), contains(map("a", "'HELLO'", "b", "'hello'")));
    }

    private static String getMockAdapter() {
        return MockPropertyAdapter.class.getName().replace('$', '.');
    }

    /**
     * w/ {@code directio.json.field}.
     * @throws Exception if failed
     */
    @Test
    public void field_field() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.json(",
                "  format = jsonl,",
                ")",
                "simple = {",
                "  @directio.json.field",
                "  a : TEXT;",
                "};",
        });
        BinaryStreamFormat<?> support = (BinaryStreamFormat<?>) loaded.newObject(SEGMENT, "SimpleJsonFormat");
        assertThat(support.getMinimumFragmentSize(), is(not(-1L)));

        byte[] contents = write(loaded, loaded.newModel("Simple")
                .setOption("a", new StringOption("A")));
        read(contents, loaded, loaded.newModel("Simple")
                .setOption("a", new StringOption("A")));
        assertThat(parse(contents), contains(map("a", "'A'")));
    }

    /**
     * w/ {@code directio.json.ignore}.
     * @throws Exception if failed
     */
    @Test
    public void field_ignore() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.json(",
                "  format = jsonl,",
                ")",
                "simple = {",
                "  a : TEXT;",
                "  @directio.json.ignore",
                "  b : TEXT;",
                "};",
        });
        BinaryStreamFormat<?> support = (BinaryStreamFormat<?>) loaded.newObject(SEGMENT, "SimpleJsonFormat");
        assertThat(support.getMinimumFragmentSize(), is(not(-1L)));

        byte[] contents = write(loaded, loaded.newModel("Simple")
                .setOption("a", new StringOption("A"))
                .setOption("b", new StringOption("B")));
        read(contents, loaded, loaded.newModel("Simple")
                .setOption("a", new StringOption("A"))
                .setOption("b", new StringOption()));
        assertThat(parse(contents), contains(map("a", "'A'")));
    }

    /**
     * w/ {@code directio.json.file_name}.
     * @throws Exception if failed
     */
    @Test
    public void field_file_name() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.json(",
                "  format = jsonl,",
                ")",
                "simple = {",
                "  a : TEXT;",
                "  @directio.json.file_name",
                "  b : TEXT;",
                "};",
        });
        BinaryStreamFormat<?> support = (BinaryStreamFormat<?>) loaded.newObject(SEGMENT, "SimpleJsonFormat");
        assertThat(support.getMinimumFragmentSize(), is(not(-1L)));

        byte[] contents = write(loaded, loaded.newModel("Simple")
                .setOption("a", new StringOption("A"))
                .setOption("b", new StringOption("B")));
        read(contents, loaded, loaded.newModel("Simple")
                .setOption("a", new StringOption("A"))
                .setOption("b", new StringOption("testing")));
        assertThat(parse(contents), contains(map("a", "'A'")));
    }

    /**
     * w/ {@code directio.json.line_number}.
     * @throws Exception if failed
     */
    @Test
    public void field_line_number() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.json(",
                "  format = jsonl,",
                ")",
                "simple = {",
                "  a : TEXT;",
                "  @directio.json.line_number",
                "  b : LONG;",
                "};",
        });
        BinaryStreamFormat<?> support = (BinaryStreamFormat<?>) loaded.newObject(SEGMENT, "SimpleJsonFormat");
        assertThat(support.getMinimumFragmentSize(), is(-1L));

        read(loaded, "\n\n{'a':'Hello, world!'}\n\n", wrapper -> {
            assertThat(wrapper.getOption("a"), is(new StringOption("Hello, world!")));
            assertThat(wrapper.getOption("b"), is(new LongOption(3)));
        });
    }

    /**
     * w/ {@code directio.json.line_number}.
     * @throws Exception if failed
     */
    @Test
    public void field_line_number_int() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.json(",
                "  format = jsonl,",
                ")",
                "simple = {",
                "  a : TEXT;",
                "  @directio.json.line_number",
                "  b : INT;",
                "};",
        });
        BinaryStreamFormat<?> support = (BinaryStreamFormat<?>) loaded.newObject(SEGMENT, "SimpleJsonFormat");
        assertThat(support.getMinimumFragmentSize(), is(-1L));

        read(loaded, "\n\n{'a':'Hello, world!'}\n\n", wrapper -> {
            assertThat(wrapper.getOption("a"), is(new StringOption("Hello, world!")));
            assertThat(wrapper.getOption("b"), is(new IntOption(3)));
        });
    }

    /**
     * w/ {@code directio.json.record_number}.
     * @throws Exception if failed
     */
    @Test
    public void field_record_number() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.json(",
                "  format = jsonl,",
                ")",
                "simple = {",
                "  a : TEXT;",
                "  @directio.json.record_number",
                "  b : LONG;",
                "};",
        });
        BinaryStreamFormat<?> support = (BinaryStreamFormat<?>) loaded.newObject(SEGMENT, "SimpleJsonFormat");
        assertThat(support.getMinimumFragmentSize(), is(-1L));

        read(loaded, "\n\n{'a':'Hello, world!'}\n\n", wrapper -> {
            assertThat(wrapper.getOption("a"), is(new StringOption("Hello, world!")));
            assertThat(wrapper.getOption("b"), is(new LongOption(1)));
        });
    }

    /**
     * w/ {@code directio.json.record_number}.
     * @throws Exception if failed
     */
    @Test
    public void field_record_number_int() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.json(",
                "  format = jsonl,",
                ")",
                "simple = {",
                "  a : TEXT;",
                "  @directio.json.record_number",
                "  b : INT;",
                "};",
        });
        BinaryStreamFormat<?> support = (BinaryStreamFormat<?>) loaded.newObject(SEGMENT, "SimpleJsonFormat");
        assertThat(support.getMinimumFragmentSize(), is(-1L));

        read(loaded, "\n\n{'a':'Hello, world!'}\n\n", wrapper -> {
            assertThat(wrapper.getOption("a"), is(new StringOption("Hello, world!")));
            assertThat(wrapper.getOption("b"), is(new IntOption(1)));
        });
    }

    /**
     * w/ unknown property.
     * @throws Exception if failed
     */
    @Test
    public void invalid_unknown_property() throws Exception {
        shouldSemanticErrorFromLines(new String[] {
                "@directio.json(",
                "  undef = true,",
                ")",
                "simple = {",
                "  a : TEXT;",
                "};",
        });
    }

    /**
     * w/ unknown charset.
     * @throws Exception if failed
     */
    @Test
    public void invalid_charset_unknown() throws Exception {
        shouldSemanticErrorFromLines(new String[] {
                "@directio.json(",
                "  charset = '?',",
                ")",
                "simple = {",
                "  a : TEXT;",
                "};",
        });
    }

    /**
     * w/ unknown compression.
     * @throws Exception if failed
     */
    @Test
    public void invalid_compression_unknown() throws Exception {
        shouldSemanticErrorFromLines(new String[] {
                "@directio.json(",
                "  compression = '?',",
                ")",
                "simple = {",
                "  a : TEXT;",
                "};",
        });
    }

    /**
     * w/ unknown line_separator.
     * @throws Exception if failed
     */
    @Test
    public void invalid_line_separator_unknown() throws Exception {
        shouldSemanticErrorFromLines(new String[] {
                "@directio.json(",
                "  line_separator = '\\n',",
                ")",
                "simple = {",
                "  a : TEXT;",
                "};",
        });
    }

    /**
     * malformed date_format.
     * @throws Exception if failed
     */
    @Test
    public void invalid_date_format_malformed() throws Exception {
        shouldSemanticErrorFromLines(new String[] {
                "@directio.json(",
                "  date_format = 'Hello, world!',",
                ")",
                "simple = {",
                "  a : TEXT;",
                "};",
        });
    }

    /**
     * malformed datetime_format.
     * @throws Exception if failed
     */
    @Test
    public void invalid_datetime_format_malformed() throws Exception {
        shouldSemanticErrorFromLines(new String[] {
                "@directio.json(",
                "  datetime_format = 'Hello, world!',",
                ")",
                "simple = {",
                "  a : TEXT;",
                "};",
        });
    }

    /**
     * malformed timezone.
     * @throws Exception if failed
     */
    @Test
    public void invalid_timezone_malformed() throws Exception {
        shouldSemanticErrorFromLines(new String[] {
                "@directio.json(",
                "  timezone = 'Hello, world!',",
                ")",
                "simple = {",
                "  a : TEXT;",
                "};",
        });
    }

    /**
     * w/o valid fields.
     * @throws Exception if failed
     */
    @Test
    public void invalid_field_empty() throws Exception {
        shouldSemanticErrorFromLines(new String[] {
                "@directio.json",
                "simple = {",
                "  @directio.json.ignore",
                "  a : TEXT;",
                "};",
        });
    }

    /**
     * conflict field attributes.
     * @throws Exception if failed
     */
    @Test
    public void invalid_field_conflict() throws Exception {
        shouldSemanticErrorFromLines(new String[] {
                "@directio.json",
                "simple = {",
                "  @directio.json.field",
                "  @directio.json.ignore",
                "  a : TEXT;",
                "};",
        });
    }

    /**
     * field w/ unknown property.
     * @throws Exception if failed
     */
    @Test
    public void invalid_field_unknown() throws Exception {
        shouldSemanticErrorFromLines(new String[] {
                "@directio.json",
                "simple = {",
                "  @directio.json.field(undef = '?')",
                "  a : TEXT;",
                "};",
        });
    }

    /**
     * file_name field w/ inconsistent type.
     * @throws Exception if failed
     */
    @Test
    public void invalid_field_file_name_inconsistent_type() throws Exception {
        shouldSemanticErrorFromLines(new String[] {
                "@directio.json",
                "simple = {",
                "  @directio.json.file_name",
                "  a : INT;",
                "  b : TEXT;",
                "};",
        });
    }

    /**
     * line_number field w/ inconsistent type.
     * @throws Exception if failed
     */
    @Test
    public void invalid_field_line_number_inconsistent_type() throws Exception {
        shouldSemanticErrorFromLines(new String[] {
                "@directio.json",
                "simple = {",
                "  @directio.json.line_number",
                "  a : TEXT;",
                "  b : TEXT;",
                "};",
        });
    }

    /**
     * record_number field w/ inconsistent type.
     * @throws Exception if failed
     */
    @Test
    public void invalid_field_record_number_inconsistent_type() throws Exception {
        shouldSemanticErrorFromLines(new String[] {
                "@directio.json",
                "simple = {",
                "  @directio.json.record_number",
                "  a : TEXT;",
                "  b : TEXT;",
                "};",
        });
    }

    /**
     * w/ {@code directio.json.field}.
     * @throws Exception if failed
     */
    @Test
    public void field_conflict_name() throws Exception {
        shouldSemanticErrorFromLines(new String[] {
                "@directio.json",
                "simple = {",
                "  @directio.json.field(name = 'conflict')",
                "  a : TEXT;",
                "  @directio.json.field(name = 'conflict')",
                "  b : TEXT;",
                "};",
        });
    }

    private static void read(
            byte[] contents, ModelLoader loaded, ModelWrapper... objects) throws IOException, InterruptedException {
        String name = objects[0].getModelClass().getSimpleName();
        BinaryStreamFormat<Object> unsafe = unsafe(loaded.newObject(SEGMENT, name + "JsonFormat"));
        Object buffer = loaded.newModel(name).unwrap();
        try (ModelInput<Object> reader = unsafe.createInput(unsafe.getSupportedType(), "testing",
                new ByteArrayInputStream(contents))) {
            for (ModelWrapper object : objects) {
                assertThat(reader.readTo(buffer), is(true));
                assertThat(buffer, is(object.unwrap()));
            }
            assertThat(reader.readTo(buffer), is(false));
        }
    }

    private static byte[] write(ModelLoader loaded, ModelWrapper... objects) {
        String name = objects[0].getModelClass().getSimpleName();
        BinaryStreamFormat<Object> unsafe = unsafe(loaded.newObject(SEGMENT, name + "JsonFormat"));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (ModelOutput<Object> writer = unsafe.createOutput(unsafe.getSupportedType(), "testing", output)) {
            for (ModelWrapper object : objects) {
                writer.write(object.unwrap());
            }
        } catch (IOException | InterruptedException e) {
            throw new AssertionError(e);
        }
        return output.toByteArray();
    }

    private byte[] restore(ModelLoader loaded, ModelWrapper... objects) throws IOException, InterruptedException {
        byte[] contents = write(loaded, objects);
        read(contents, loaded, objects);
        return contents;
    }

    private void read(ModelLoader loaded, String contents, Consumer<ModelWrapper> tester) {
        BinaryStreamFormat<Object> unsafe = unsafe(loaded.newObject(SEGMENT, "SimpleJsonFormat"));
        ModelWrapper wrapper = loaded.newModel("Simple");
        Object buffer = wrapper.unwrap();
        try (ModelInput<Object> reader =  unsafe.createInput(
                unsafe.getSupportedType(),
                "testing",
                new ByteArrayInputStream(contents.getBytes(StandardCharsets.UTF_8)))) {
            assertThat(reader.readTo(buffer), is(true));
            tester.accept(wrapper);

            assertThat(reader.readTo(buffer), is(false));
        } catch (IOException | InterruptedException e) {
            throw new AssertionError(e);
        }
    }

    private void readError(ModelLoader loaded, String contents) {
        BinaryStreamFormat<Object> unsafe = unsafe(loaded.newObject(SEGMENT, "SimpleJsonFormat"));
        ModelWrapper wrapper = loaded.newModel("Simple");
        Object buffer = wrapper.unwrap();
        try (ModelInput<Object> reader =  unsafe.createInput(
                unsafe.getSupportedType(),
                "testing",
                new ByteArrayInputStream(contents.getBytes(StandardCharsets.UTF_8)))) {
            reader.readTo(buffer);
            fail();
        } catch (JsonFormatException e) {
            // ok.
        } catch (IOException | InterruptedException e) {
            throw new AssertionError(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static BinaryStreamFormat<Object> unsafe(Object support) {
        return (BinaryStreamFormat<Object>) support;
    }

    private static ByteArrayInputStream in(ByteArrayOutputStream output) {
        return new ByteArrayInputStream(output.toByteArray());
    }

    private static Map<String, String> map(String... kvs) {
        assertThat(kvs.length % 2, is(0));
        Map<String, String> results = new HashMap<>();
        for (int i = 0; i < kvs.length; i += 2) {
            results.put(kvs[i], normalizeValue(kvs[i + 1]));
        }
        return results;
    }

    private static List<Map<String, String>> parse(byte[] contents) {
        return parse(contents, StandardCharsets.UTF_8);
    }

    private static List<Map<String, String>> parse(byte[] contents, Charset charset) {
        String file = new String(contents, charset);
        Pattern object = Pattern.compile("\\{(.*?)\\}");
        Pattern field = Pattern.compile("\"(.+?)\"\\s*:\\s*(\".*?\"|[\\w\\.\\+\\-]+),?");

        List<Map<String, String>> results = new ArrayList<>();
        Matcher omatch = object.matcher(file);
        int ostart = 0;
        while (omatch.find(ostart)) {
            Map<String, String> entry = new HashMap<>();
            Matcher fmatch = field.matcher(omatch.group(1));
            int fstart = 0;
            while (fmatch.find(fstart)) {
                String key = fmatch.group(1);
                String value = fmatch.group(2);
                entry.put(key, normalizeValue(value));
                fstart = fmatch.end();
            }
            results.add(entry);
            ostart = omatch.end();
        }
        return results;
    }

    private static String normalizeValue(String value) {
        return value.replace('"', '\'');
    }

    @SafeVarargs
    private static <E> org.hamcrest.Matcher<Iterable<? extends E>> contains(E... items) {
        return Matchers.contains(items);
    }

    @SuppressWarnings({ "deprecation", "javadoc" })
    public static class MockPropertyAdapter implements PropertyAdapter<StringOption> {

        @Override
        public void absent(StringOption property) {
            property.setNull();
        }

        @Override
        public void read(ValueReader reader, StringOption property) throws IOException {
            property.modify(reader.readString().toLowerCase(Locale.ENGLISH));
        }

        @Override
        public void write(StringOption property, ValueWriter writer) throws IOException {
            writer.writeString(property.getAsString().toUpperCase(Locale.ENGLISH));
        }
    }
}
