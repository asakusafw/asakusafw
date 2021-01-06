/**
 * Copyright 2011-2021 Asakusa Framework Team.
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
package com.asakusafw.dmdl.directio.text.tabular;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.TimeZone;
import java.util.function.Consumer;
import java.util.zip.GZIPInputStream;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExternalResource;

import com.asakusafw.dmdl.directio.common.driver.GeneratorTesterRoot;
import com.asakusafw.dmdl.directio.text.mock.EmptyLineFilter;
import com.asakusafw.dmdl.directio.text.mock.MockFieldAdapter;
import com.asakusafw.dmdl.directio.text.mock.UpperCaseTransformer;
import com.asakusafw.dmdl.java.emitter.driver.ObjectDriver;
import com.asakusafw.runtime.directio.BinaryStreamFormat;
import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.ModelOutput;
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
 * Test for {@link TabularTextEmitter}.
 */
public class TabularTextEmitterTest extends GeneratorTesterRoot {

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
        emitDrivers.add(new TabularTextEmitter());
        emitDrivers.add(new ObjectDriver());
    }

    /**
     * simple case.
     * @throws Exception if failed
     */
    @Test
    public void simple() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.text.tabular",
                "simple = { value : TEXT; };",
        });
        ModelWrapper model = loaded.newModel("Simple")
                .setOption("value", new StringOption("Hello, world!"));

        BinaryStreamFormat<?> support = (BinaryStreamFormat<?>) loaded.newObject("text", "SimpleTabularTextFormat");
        assertThat(support.getSupportedType(), is((Object) model.unwrap().getClass()));
        assertThat(support.getMinimumFragmentSize(), is(greaterThan(0L)));

        BinaryStreamFormat<Object> unsafe = unsafe(support);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (ModelOutput<Object> writer = unsafe.createOutput(unsafe.getSupportedType(), "hello", output)) {
            writer.write(model.unwrap());
        }

        assertThat(text(output), is("Hello, world!\n"));

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
                "@directio.text.tabular",
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
        assertThat(text(contents), is("0a\t0b\t0c\n1a\t1b\t1c\n2a\t2b\t2c\n"));
    }

    /**
     * w/ {@code BOOLEAN} field.
     * @throws Exception if failed
     */
    @Test
    public void type_boolean() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.text.tabular",
                "simple = {",
                "  a : BOOLEAN;",
                "};",
        });
        byte[] contents = restore(loaded, loaded.newModel("Simple")
                .setOption("a", new BooleanOption(true)));
        assertThat(text(contents), is("true\n"));
    }

    /**
     * w/ {@code BYTE} field.
     * @throws Exception if failed
     */
    @Test
    public void type_byte() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.text.tabular",
                "simple = {",
                "  a : BYTE;",
                "};",
        });
        byte[] contents = restore(loaded, loaded.newModel("Simple")
                .setOption("a", new ByteOption((byte) 1)));
        assertThat(text(contents), is("1\n"));
    }

    /**
     * w/ {@code SHORT} field.
     * @throws Exception if failed
     */
    @Test
    public void type_short() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.text.tabular",
                "simple = {",
                "  a : SHORT;",
                "};",
        });
        byte[] contents = restore(loaded, loaded.newModel("Simple")
                .setOption("a", new ShortOption((short) 1)));
        assertThat(text(contents), is("1\n"));
    }

    /**
     * w/ {@code INT} field.
     * @throws Exception if failed
     */
    @Test
    public void type_int() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.text.tabular",
                "simple = {",
                "  a : INT;",
                "};",
        });
        byte[] contents = restore(loaded, loaded.newModel("Simple")
                .setOption("a", new IntOption(1)));
        assertThat(text(contents), is("1\n"));
    }

    /**
     * w/ {@code LONG} field.
     * @throws Exception if failed
     */
    @Test
    public void type_long() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.text.tabular",
                "simple = {",
                "  a : LONG;",
                "};",
        });
        byte[] contents = restore(loaded, loaded.newModel("Simple")
                .setOption("a", new LongOption(1)));
        assertThat(text(contents), is("1\n"));
    }

    /**
     * w/ {@code FLOAT} field.
     * @throws Exception if failed
     */
    @Test
    public void type_float() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.text.tabular",
                "simple = {",
                "  a : FLOAT;",
                "};",
        });
        byte[] contents = restore(loaded, loaded.newModel("Simple")
                .setOption("a", new FloatOption(1)));
        assertThat(text(contents), is("1.0\n"));
    }

    /**
     * w/ {@code DOUBLE} field.
     * @throws Exception if failed
     */
    @Test
    public void type_double() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.text.tabular",
                "simple = {",
                "  a : DOUBLE;",
                "};",
        });
        byte[] contents = restore(loaded, loaded.newModel("Simple")
                .setOption("a", new DoubleOption(1)));
        assertThat(text(contents), is("1.0\n"));
    }

    /**
     * w/ {@code header}.
     * @throws Exception if failed
     */
    @Test
    public void header() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.text.tabular(",
                "  header = force,",
                ")",
                "simple = {",
                "  a : TEXT;",
                "  b : TEXT;",
                "};",
        });
        byte[] contents = restore(loaded, loaded.newModel("Simple")
                .setOption("a", new StringOption("A"))
                .setOption("b", new StringOption("B")));
        assertThat(text(contents), is("a\tb\nA\tB\n"));
    }

    /**
     * w/ {@code header}.
     * @throws Exception if failed
     */
    @Test
    public void header_name() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.text.tabular(",
                "  header = force,",
                ")",
                "simple = {",
                "  @directio.text.field(name = 'H')",
                "  a : TEXT;",
                "  b : TEXT;",
                "};",
        });
        byte[] contents = restore(loaded, loaded.newModel("Simple")
                .setOption("a", new StringOption("A"))
                .setOption("b", new StringOption("B")));
        assertThat(text(contents), is("H\tb\nA\tB\n"));
    }

    /**
     * w/ {@code charset}.
     * @throws Exception if failed
     */
    @Test
    public void charset() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.text.tabular(",
                "  charset = 'UTF-16LE',",
                ")",
                "simple = {",
                "  a : TEXT;",
                "};",
        });
        BinaryStreamFormat<?> support = (BinaryStreamFormat<?>) loaded.newObject("text", "SimpleTabularTextFormat");
        assertThat(support.getMinimumFragmentSize(), is(-1L));

        ModelWrapper model = loaded.newModel("Simple")
                .setOption("a", new StringOption("Hello, world!"));
        byte[] contents = write(loaded, model);
        assertThat(new String(contents, StandardCharsets.UTF_16LE), is("Hello, world!\n"));
    }

    /**
     * w/ {@code compression}.
     * @throws Exception if failed
     */
    @Test
    public void compression() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.text.tabular(",
                "  compression = gzip,",
                ")",
                "simple = {",
                "  a : TEXT;",
                "};",
        });
        ModelWrapper model = loaded.newModel("Simple")
                .setOption("a", new StringOption("Hello, world!"));
        byte[] contents = write(loaded, model);

        ByteArrayInputStream input = new ByteArrayInputStream(contents);
        try (Scanner s = new Scanner(new GZIPInputStream(input), StandardCharsets.UTF_8.name())) {
            assertThat(s.hasNextLine(), is(true));
            assertThat(s.nextLine(), is("Hello, world!"));

            assertThat(s.hasNextLine(), is(false));
        }
    }

    /**
     * w/ {@code line_separator}.
     * @throws Exception if failed
     */
    @Test
    public void line_separator() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.text.tabular(",
                "  line_separator = windows,",
                ")",
                "simple = {",
                "  a : TEXT;",
                "};",
        });
        ModelWrapper model = loaded.newModel("Simple")
                .setOption("a", new StringOption("Hello, world!"));
        byte[] contents = restore(loaded, model);
        assertThat(text(contents), is("Hello, world!\r\n"));
    }

    /**
     * w/ {@code field_separator}.
     * @throws Exception if failed
     */
    @Test
    public void field_separator() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.text.tabular(",
                "  field_separator = ':',",
                ")",
                "simple = {",
                "  a : TEXT;",
                "  b : TEXT;",
                "  c : TEXT;",
                "};",
        });
        ModelWrapper model = loaded.newModel("Simple")
                .setOption("a", new StringOption("A"))
                .setOption("b", new StringOption("B"))
                .setOption("c", new StringOption("C"));
        byte[] contents = restore(loaded, model);
        assertThat(text(contents), is("A:B:C\n"));
    }

    /**
     * w/ {@code escape_sequence}.
     * @throws Exception if failed
     */
    @Test
    public void escape_sequence() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.text.tabular(",
                "  escape_character = '^',",
                "  escape_sequence = {",
                "    't' : '\\t'",
                "  },",
                ")",
                "simple = {",
                "  a : TEXT;",
                "  b : TEXT;",
                "};",
        });
        ModelWrapper model = loaded.newModel("Simple")
                .setOption("a", new StringOption("A"))
                .setOption("b", new StringOption("B\tC"));
        byte[] contents = restore(loaded, model);
        assertThat(text(contents), is("A\tB^tC\n"));
    }

    /**
     * w/ empty {@code escape_sequence}.
     * @throws Exception if failed
     */
    @Test
    public void escape_sequence_empty() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.text.tabular(",
                "  escape_character = '^',",
                "  escape_sequence = {},",
                ")",
                "simple = {",
                "  a : TEXT;",
                "  b : TEXT;",
                "};",
        });
        ModelWrapper model = loaded.newModel("Simple")
                .setOption("a", new StringOption("A"))
                .setOption("b", new StringOption("B"));
        byte[] contents = restore(loaded, model);
        assertThat(text(contents), is("A\tB\n"));
    }

    /**
     * w/ {@code escape_sequence} includes {@code null}.
     * @throws Exception if failed
     */
    @Test
    public void escape_sequence_null() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.text.tabular(",
                "  escape_character = '^',",
                "  escape_sequence = {",
                "    'N' : null",
                "  },",
                ")",
                "simple = {",
                "  a : TEXT;",
                "};",
        });
        ModelWrapper model = loaded.newModel("Simple")
                .setOption("a", new StringOption());
        byte[] contents = restore(loaded, model);
        assertThat(text(contents), is("^N\n"));
    }

    /**
     * w/ {@code escape_line_separator}.
     * @throws Exception if failed
     */
    @Test
    public void escape_line_separator() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.text.tabular(",
                "  escape_character = '^',",
                "  escape_line_separator = true,",
                ")",
                "simple = {",
                "  a : TEXT;",
                "  b : TEXT;",
                "};",
        });
        ModelWrapper model = loaded.newModel("Simple")
                .setOption("a", new StringOption("A"))
                .setOption("b", new StringOption("B\nC"));
        byte[] contents = restore(loaded, model);
        assertThat(text(contents), is("A\tB^\nC\n"));
    }

    /**
     * w/ {@code escape_line_separator}.
     * @throws Exception if failed
     */
    @Test
    public void escape_line_separator_false() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.text.tabular(",
                "  escape_character = '^',",
                "  escape_line_separator = false,",
                ")",
                "simple = {",
                "  a : TEXT;",
                "};",
        });
        read("a^\nb\n".getBytes(StandardCharsets.UTF_8), loaded,
                loaded.newModel("Simple").setOption("a", new StringOption("a^")),
                loaded.newModel("Simple").setOption("a", new StringOption("b")));
    }

    /**
     * w/ {@code input_transformer}.
     * @throws Exception if failed
     */
    @Test
    public void input_transformer() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.text.tabular(",
                "  input_transformer = '" + EmptyLineFilter.class.getName() + "'",
                ")",
                "simple = {",
                "  a : TEXT;",
                "};",
        });
        read(loaded, "\n\nHello, world!\n\n", wrapper -> {
            assertThat(wrapper.getOption("a"), is(new StringOption("Hello, world!")));
        });
    }

    /**
     * w/ {@code output_transformer}.
     * @throws Exception if failed
     */
    @Test
    public void output_transformer() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.text.tabular(",
                "  output_transformer = '" + UpperCaseTransformer.class.getName() + "'",
                ")",
                "simple = {",
                "  a : TEXT;",
                "};",
        });
        ModelWrapper model = loaded.newModel("Simple")
                .setOption("a", new StringOption("hello"));
        byte[] contents = write(loaded, model);
        assertThat(text(contents), is("HELLO\n"));
    }

    /**
     * w/ {@code on_less_input}.
     * @throws Exception if failed
     */
    @Test
    public void on_less_input() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.text.tabular(",
                "  on_less_input = report,",
                ")",
                "simple = {",
                "  a : TEXT;",
                "  b : TEXT;",
                "};",
        });
        read(loaded, "Hello, world!\n", wrapper -> {
            assertThat(wrapper.getOption("a"), is(new StringOption("Hello, world!")));
            assertThat(wrapper.getOption("b"), is(new StringOption()));
        });
    }

    /**
     * w/ {@code on_more_input}.
     * @throws Exception if failed
     */
    @Test
    public void on_more_input() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.text.tabular(",
                "  on_more_input = report,",
                ")",
                "simple = {",
                "  a : TEXT;",
                "  b : TEXT;",
                "};",
        });
        read(loaded, "A\tB\tEX\n", wrapper -> {
            assertThat(wrapper.getOption("a"), is(new StringOption("A")));
            assertThat(wrapper.getOption("b"), is(new StringOption("B")));
        });
    }

    /**
     * w/ {@code trim_input}.
     * @throws Exception if failed
     */
    @Test
    public void trim_input() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.text.tabular(",
                "  trim_input = true,",
                ")",
                "simple = {",
                "  a : TEXT;",
                "  b : TEXT;",
                "  c : TEXT;",
                "};",
        });
        read(loaded, " A\tB \t  C  \n", wrapper -> {
            assertThat(wrapper.getOption("a"), is(new StringOption("A")));
            assertThat(wrapper.getOption("b"), is(new StringOption("B")));
            assertThat(wrapper.getOption("c"), is(new StringOption("C")));
        });
    }

    /**
     * w/ {@code trim_input}.
     * @throws Exception if failed
     */
    @Test
    public void trim_input_field() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.text.tabular",
                "simple = {",
                "  a : TEXT;",
                "  @directio.text.field(trim_input = true)",
                "  b : TEXT;",
                "  c : TEXT;",
                "};",
        });
        read(loaded, " A \t B \t C \n", wrapper -> {
            assertThat(wrapper.getOption("a"), is(new StringOption(" A ")));
            assertThat(wrapper.getOption("b"), is(new StringOption("B")));
            assertThat(wrapper.getOption("c"), is(new StringOption(" C ")));
        });
    }

    /**
     * w/ {@code skip_empty_input}.
     * @throws Exception if failed
     */
    @Test
    public void skip_empty_input() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.text.tabular(",
                "  skip_empty_input = true,",
                ")",
                "simple = {",
                "  a : TEXT;",
                "  b : TEXT;",
                "};",
        });
        read(loaded, "\tA\t\tB\t\n", w -> {
            assertThat(w.getOption("a"), is(new StringOption("A")));
            assertThat(w.getOption("b"), is(new StringOption("B")));
        });
    }

    /**
     * w/ {@code skip_empty_input}.
     * @throws Exception if failed
     */
    @Test
    public void skip_empty_input_field() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.text.tabular",
                "simple = {",
                "  @directio.text.field(skip_empty_input = true)",
                "  a : TEXT;",
                "  b : TEXT;",
                "  c : TEXT;",
                "};",
        });
        read(loaded, "\tA\t\tB\n", wrapper -> {
            assertThat(wrapper.getOption("a"), is(new StringOption("A")));
            assertThat(wrapper.getOption("b"), is(new StringOption("")));
            assertThat(wrapper.getOption("c"), is(new StringOption("B")));
        });
    }

    /**
     * w/ {@code on_malformed_input}.
     * @throws Exception if failed
     */
    @Test
    public void on_malformed_input() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.text.tabular(",
                "  on_malformed_input = report,",
                ")",
                "simple = {",
                "  a : TEXT;",
                "  b : INT;",
                "  c : TEXT;",
                "};",
        });
        read(loaded, "A\tB\tC\n", wrapper -> {
            assertThat(wrapper.getOption("a"), is(new StringOption("A")));
            assertThat(wrapper.getOption("b"), is(new IntOption()));
            assertThat(wrapper.getOption("c"), is(new StringOption("C")));
        });
    }

    /**
     * w/ {@code on_malformed_input}.
     * @throws Exception if failed
     */
    @Test
    public void on_malformed_input_field() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.text.tabular",
                "simple = {",
                "  @directio.text.field(on_malformed_input = report)",
                "  a : INT;",
                "  b : INT;",
                "};",
        });
        read(loaded, "INVALID\t1\n", wrapper -> {
            assertThat(wrapper.getOption("a"), is(new IntOption()));
            assertThat(wrapper.getOption("b"), is(new IntOption(1)));
        });
        readError(loaded, "0\tINVALID\n");
    }

    /**
     * w/ {@code on_unmappable_output}.
     * @throws Exception if failed
     */
    @Test
    public void on_unmappable_output() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.text.tabular(",
                "  on_unmappable_output = report,",
                ")",
                "simple = {",
                "  a : TEXT;",
                "};",
        });
        byte[] written = write(loaded, loaded.newModel("Simple")
                .setOption("a", new StringOption()));
        assertThat(text(written), is("\n"));
    }

    /**
     * w/ {@code on_unmappable_output}.
     * @throws Exception if failed
     */
    @Test
    public void on_unmappable_output_field() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.text.tabular",
                "simple = {",
                "  @directio.text.field(on_unmappable_output = report)",
                "  a : INT;",
                "  b : INT;",
                "};",
        });
        byte[] written = write(loaded, loaded.newModel("Simple")
                .setOption("a", new IntOption())
                .setOption("b", new IntOption(1)));
        assertThat(text(written), is("\t1\n"));

        writeError(loaded, loaded.newModel("Simple")
                .setOption("a", new IntOption(0))
                .setOption("b", new IntOption()));
    }

    /**
     * w/ {@code null_format}.
     * @throws Exception if failed
     */
    @Test
    public void null_format() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.text.tabular(",
                "  null_format = 'OK',",
                ")",
                "simple = {",
                "  a : TEXT;",
                "};",
        });
        byte[] contents = restore(loaded, loaded.newModel("Simple")
                .setOption("a", new StringOption()));
        assertThat(text(contents), is("OK\n"));
    }

    /**
     * w/ {@code null_format}.
     * @throws Exception if failed
     */
    @Test
    public void null_format_field() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.text.tabular(",
                "  null_format = 'OK',",
                ")",
                "simple = {",
                "  @directio.text.field(null_format = 'NULL')",
                "  a : TEXT;",
                "  b : TEXT;",
                "};",
        });
        byte[] contents = restore(loaded, loaded.newModel("Simple")
                .setOption("a", new StringOption())
                .setOption("b", new StringOption()));
        assertThat(text(contents), is("NULL\tOK\n"));
    }

    /**
     * w/ {@code null_format}.
     * @throws Exception if failed
     */
    @Test
    public void null_format_field_null() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.text.tabular(",
                "  null_format = 'OK',",
                ")",
                "simple = {",
                "  @directio.text.field(null_format = null)",
                "  a : TEXT;",
                "};",
        });
        writeError(loaded, loaded.newModel("Simple")
                .setOption("a", new StringOption()));
    }

    /**
     * w/ {@code true_format}.
     * @throws Exception if failed
     */
    @Test
    public void true_format() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.text.tabular(",
                "  true_format = 'A',",
                ")",
                "simple = {",
                "  a : BOOLEAN;",
                "  b : BOOLEAN;",
                "};",
        });
        byte[] contents = restore(loaded, loaded.newModel("Simple")
                .setOption("a", new BooleanOption(true))
                .setOption("b", new BooleanOption(false)));
        assertThat(text(contents), is("A\tfalse\n"));
    }

    /**
     * w/ {@code true_format}.
     * @throws Exception if failed
     */
    @Test
    public void true_format_field() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.text.tabular(",
                "  true_format = 'A',",
                ")",
                "simple = {",
                "  @directio.text.field(true_format = '_')",
                "  a : BOOLEAN;",
                "  b : BOOLEAN;",
                "};",
        });
        byte[] contents = restore(loaded, loaded.newModel("Simple")
                .setOption("a", new BooleanOption(true))
                .setOption("b", new BooleanOption(true)));
        assertThat(text(contents), is("_\tA\n"));
    }

    /**
     * w/ {@code true_format}.
     * @throws Exception if failed
     */
    @Test
    public void true_format_inconsistent_type() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.text.tabular",
                "simple = {",
                "  @directio.text.field(true_format = '_')",
                "  a : TEXT;",
                "};",
        });
        byte[] contents = restore(loaded, loaded.newModel("Simple")
                .setOption("a", new StringOption("Hello, world!")));
        assertThat(text(contents), is("Hello, world!\n"));
    }

    /**
     * w/ {@code false_format}.
     * @throws Exception if failed
     */
    @Test
    public void false_format() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.text.tabular(",
                "  false_format = 'B',",
                ")",
                "simple = {",
                "  a : BOOLEAN;",
                "  b : BOOLEAN;",
                "};",
        });
        byte[] contents = restore(loaded, loaded.newModel("Simple")
                .setOption("a", new BooleanOption(true))
                .setOption("b", new BooleanOption(false)));
        assertThat(text(contents), is("true\tB\n"));
    }

    /**
     * w/ {@code false_format}.
     * @throws Exception if failed
     */
    @Test
    public void false_format_field() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.text.tabular(",
                "  false_format = 'B',",
                ")",
                "simple = {",
                "  @directio.text.field(false_format = '_')",
                "  a : BOOLEAN;",
                "  b : BOOLEAN;",
                "};",
        });
        byte[] contents = restore(loaded, loaded.newModel("Simple")
                .setOption("a", new BooleanOption(false))
                .setOption("b", new BooleanOption(false)));
        assertThat(text(contents), is("_\tB\n"));
    }

    /**
     * w/ {@code false_format}.
     * @throws Exception if failed
     */
    @Test
    public void false_format_inconsistent_type() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.text.tabular",
                "simple = {",
                "  @directio.text.field(false_format = '_')",
                "  a : TEXT;",
                "};",
        });
        byte[] contents = restore(loaded, loaded.newModel("Simple")
                .setOption("a", new StringOption("Hello, world!")));
        assertThat(text(contents), is("Hello, world!\n"));
    }

    /**
     * w/ {@code number_format}.
     * @throws Exception if failed
     */
    @Test
    public void number_format() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.text.tabular(",
                "  number_format = '0.00',",
                ")",
                "simple = {",
                "  a : DECIMAL;",
                "};",
        });
        byte[] contents = write(loaded, loaded.newModel("Simple")
                .setOption("a", new DecimalOption(new BigDecimal("3.1415"))));
        read(contents, loaded, loaded.newModel("Simple")
                .setOption("a", new DecimalOption(new BigDecimal("3.14"))));
        assertThat(text(contents), is("3.14\n"));
    }

    /**
     * w/ {@code number_format}.
     * @throws Exception if failed
     */
    @Test
    public void number_format_integer() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.text.tabular(",
                "  number_format = '#,###;(#,###)',",
                ")",
                "simple = {",
                "  a : INT;",
                "  b : INT;",
                "};",
        });
        byte[] contents = restore(loaded, loaded.newModel("Simple")
                .setOption("a", new IntOption(12_345_678))
                .setOption("b", new IntOption(-12_345)));
        assertThat(text(contents), is("12,345,678\t(12,345)\n"));
    }

    /**
     * w/ {@code number_format} for fields.
     * @throws Exception if failed
     */
    @Test
    public void number_format_field() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.text.tabular(",
                "  number_format = '0',",
                ")",
                "simple = {",
                "  a : INT;",
                "  @directio.text.field(number_format = '#,###')",
                "  b : INT;",
                "};",
        });
        byte[] contents = restore(loaded, loaded.newModel("Simple")
                .setOption("a", new IntOption(12_345_678))
                .setOption("b", new IntOption(-12_345)));
        assertThat(text(contents), is("12345678\t-12,345\n"));
    }

    /**
     * w/ {@code number_format} for fields.
     * @throws Exception if failed
     */
    @Test
    public void number_format_field_null() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.text.tabular(",
                "  number_format = '#,###',",
                ")",
                "simple = {",
                "  a : INT;",
                "  @directio.text.field(number_format = null)",
                "  b : INT;",
                "};",
        });
        byte[] contents = restore(loaded, loaded.newModel("Simple")
                .setOption("a", new IntOption(12_345_678))
                .setOption("b", new IntOption(-12_345)));
        assertThat(text(contents), is("12,345,678\t-12345\n"));
    }

    /**
     * w/ {@code false_format}.
     * @throws Exception if failed
     */
    @Test
    public void number_format_inconsistent_type() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.text.tabular",
                "simple = {",
                "  @directio.text.field(number_format = '#,###')",
                "  a : TEXT;",
                "};",
        });
        byte[] contents = restore(loaded, loaded.newModel("Simple")
                .setOption("a", new StringOption("Hello, world!")));
        assertThat(text(contents), is("Hello, world!\n"));
    }

    /**
     * w/ {@code date_format}.
     * @throws Exception if failed
     */
    @Test
    public void date_format() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.text.tabular(",
                "  date_format = 'yyyy_MM_dd',",
                ")",
                "simple = {",
                "  a : DATE;",
                "};",
        });
        byte[] contents = restore(loaded, loaded.newModel("Simple")
                .setOption("a", new DateOption(new Date(2017, 1, 6))));
        assertThat(text(contents), is("2017_01_06\n"));
    }

    /**
     * w/ {@code date_format}.
     * @throws Exception if failed
     */
    @Test
    public void date_format_field() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.text.tabular",
                "simple = {",
                "  @directio.text.field(date_format = 'yyyy_MM_dd')",
                "  a : DATE;",
                "  b : DATE;",
                "};",
        });
        byte[] contents = restore(loaded, loaded.newModel("Simple")
                .setOption("a", new DateOption(new Date(2017, 1, 6)))
                .setOption("b", new DateOption(new Date(2017, 1, 6))));
        assertThat(text(contents), is("2017_01_06\t2017-01-06\n"));
    }

    /**
     * w/ {@code datetime_format}.
     * @throws Exception if failed
     */
    @Test
    public void datetime_format() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.text.tabular(",
                "  datetime_format = 'yyyy_MM_dd HH-mm-ss',",
                ")",
                "simple = {",
                "  a : DATETIME;",
                "};",
        });
        byte[] contents = restore(loaded, loaded.newModel("Simple")
                .setOption("a", new DateTimeOption(new DateTime(2017, 1, 6, 22, 10, 0))));
        assertThat(text(contents), is("2017_01_06 22-10-00\n"));
    }

    /**
     * w/ {@code datetime_format}.
     * @throws Exception if failed
     */
    @Test
    public void datetime_format_field() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.text.tabular",
                "simple = {",
                "  @directio.text.field(datetime_format = 'yyyy_MM_dd HH-mm-ss')",
                "  a : DATETIME;",
                "  b : DATETIME;",
                "};",
        });
        byte[] contents = restore(loaded, loaded.newModel("Simple")
                .setOption("a", new DateTimeOption(new DateTime(2017, 1, 6, 22, 10, 0)))
                .setOption("b", new DateTimeOption(new DateTime(2017, 1, 6, 22, 10, 0))));
        assertThat(text(contents), is("2017_01_06 22-10-00\t2017-01-06 22:10:00\n"));
    }

    /**
     * w/ {@code datetime_format}.
     * @throws Exception if failed
     */
    @Test
    public void datetime_format_inconsistent_type() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.text.tabular",
                "simple = {",
                "  @directio.text.field(datetime_format = '_')",
                "  a : TEXT;",
                "};",
        });
        byte[] contents = restore(loaded, loaded.newModel("Simple")
                .setOption("a", new StringOption("Hello, world!")));
        assertThat(text(contents), is("Hello, world!\n"));
    }

    /**
     * w/ {@code timezone}.
     * @throws Exception if failed
     */
    @Test
    public void timezone() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.text.tabular(",
                "  timezone = 'UTC',",
                ")",
                "simple = {",
                "  a : DATETIME;",
                "};",
        });
        byte[] contents = restore(loaded, loaded.newModel("Simple")
                .setOption("a", new DateTimeOption(new DateTime(2017, 1, 2, 12, 34, 56))));
        assertThat(text(contents), is("2017-01-02 20:34:56\n"));
    }

    /**
     * w/ {@code timezone}.
     * @throws Exception if failed
     */
    @Test
    public void timezone_field() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.text.tabular",
                "simple = {",
                "  a : DATETIME;",
                "  @directio.text.field(timezone = 'UTC')",
                "  b : DATETIME;",
                "};",
        });
        byte[] contents = restore(loaded, loaded.newModel("Simple")
                .setOption("a", new DateTimeOption(new DateTime(2017, 1, 2, 12, 34, 56)))
                .setOption("b", new DateTimeOption(new DateTime(2017, 1, 2, 12, 34, 56))));
        assertThat(text(contents), is("2017-01-02 12:34:56\t2017-01-02 20:34:56\n"));
    }

    /**
     * w/ {@code timezone}.
     * @throws Exception if failed
     */
    @Test
    public void timezone_null() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.text.tabular(",
                "  timezone = 'UTC',",
                ")",
                "simple = {",
                "  @directio.text.field(timezone = null)",
                "  a : DATETIME;",
                "  b : DATETIME;",
                "};",
        });
        byte[] contents = restore(loaded, loaded.newModel("Simple")
                .setOption("a", new DateTimeOption(new DateTime(2017, 1, 2, 12, 34, 56)))
                .setOption("b", new DateTimeOption(new DateTime(2017, 1, 2, 12, 34, 56))));
        assertThat(text(contents), is("2017-01-02 12:34:56\t2017-01-02 20:34:56\n"));
    }

    /**
     * w/ {@code timezone} for non datetime field.
     * @throws Exception if failed
     */
    @Test
    public void timezone_inconsistent_type() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.text.tabular",
                "simple = {",
                "  @directio.text.field(timezone = 'UTC')",
                "  a : TEXT;",
                "};",
        });
        byte[] contents = restore(loaded, loaded.newModel("Simple")
                .setOption("a", new StringOption("Hello, world!")));
        assertThat(text(contents), is("Hello, world!\n"));
    }

    /**
     * w/ {@code decimal_output_style}.
     * @throws Exception if failed
     */
    @Test
    public void decimal_output_style() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.text.tabular(",
                "  decimal_output_style = plain,",
                ")",
                "simple = {",
                "  a : DECIMAL;",
                "};",
        });
        ModelWrapper model = loaded.newModel("Simple")
                .setOption("a", new DecimalOption(new BigDecimal(BigInteger.valueOf(12), -10)));
        byte[] contents = write(loaded, model);
        assertThat(text(contents), is("120000000000\n"));
    }

    /**
     * w/ {@code decimal_output_style}.
     * @throws Exception if failed
     */
    @Test
    public void decimal_output_style_field() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.text.tabular",
                "simple = {",
                "  @directio.text.field(decimal_output_style = plain)",
                "  a : DECIMAL;",
                "  b : DECIMAL;",
                "};",
        });
        ModelWrapper model = loaded.newModel("Simple")
                .setOption("a", new DecimalOption(new BigDecimal(BigInteger.valueOf(12), -10)))
                .setOption("b", new DecimalOption(new BigDecimal(BigInteger.valueOf(12), -10)));
        byte[] contents = write(loaded, model);
        assertThat(text(contents), is("120000000000\t1.2E+11\n"));
    }

    /**
     * w/ {@code decimal_output_style}.
     * @throws Exception if failed
     */
    @Test
    public void decimal_output_style_inconsistent_type() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.text.tabular",
                "simple = {",
                "  @directio.text.field(decimal_output_style = plain)",
                "  a : TEXT;",
                "};",
        });
        byte[] contents = restore(loaded, loaded.newModel("Simple")
                .setOption("a", new StringOption("Hello, world!")));
        assertThat(text(contents), is("Hello, world!\n"));
    }

    /**
     * w/ {@code adapter}.
     * @throws Exception if failed
     */
    @Test
    public void adapter() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.text.tabular(",
                "  adapter = '" + MockFieldAdapter.class.getName() + "'",
                ")",
                "simple = {",
                "  a : TEXT;",
                "};",
        });
        ModelWrapper model = loaded.newModel("Simple")
                .setOption("a", new StringOption("hello"));
        byte[] contents = restore(loaded, model);
        assertThat(text(contents), is("HELLO\n"));
    }

    /**
     * w/ {@code adapter}.
     * @throws Exception if failed
     */
    @Test
    public void adapter_field() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.text.tabular",
                "simple = {",
                "  @directio.text.field(adapter = '" + MockFieldAdapter.class.getName() + "')",
                "  a : TEXT;",
                "  b : TEXT;",
                "};",
        });
        ModelWrapper model = loaded.newModel("Simple")
                .setOption("a", new StringOption("hello"))
                .setOption("b", new StringOption("hello"));
        byte[] contents = restore(loaded, model);
        assertThat(text(contents), is("HELLO\thello\n"));
    }

    /**
     * w/ {@code adapter}.
     * @throws Exception if failed
     */
    @Test
    public void adapter_override() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.text.tabular(",
                "  adapter = '" + MockFieldAdapter.class.getName() + "'",
                ")",
                "simple = {",
                "  a : TEXT;",
                "  @directio.text.field(adapter = null)",
                "  b : TEXT;",
                "};",
        });
        ModelWrapper model = loaded.newModel("Simple")
                .setOption("a", new StringOption("hello"))
                .setOption("b", new StringOption("hello"));
        byte[] contents = restore(loaded, model);
        assertThat(text(contents), is("HELLO\thello\n"));
    }

    /**
     * w/ {@code directio.text.field}.
     * @throws Exception if failed
     */
    @Test
    public void field_field() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.text.tabular",
                "simple = {",
                "  @directio.text.field",
                "  a : TEXT;",
                "};",
        });
        BinaryStreamFormat<?> support = (BinaryStreamFormat<?>) loaded.newObject("text", "SimpleTabularTextFormat");
        assertThat(support.getMinimumFragmentSize(), is(greaterThan(0L)));

        byte[] contents = write(loaded, loaded.newModel("Simple")
                .setOption("a", new StringOption("A")));
        read(contents, loaded, loaded.newModel("Simple")
                .setOption("a", new StringOption("A")));
        assertThat(text(contents), is("A\n"));
    }

    /**
     * w/ {@code directio.text.ignore}.
     * @throws Exception if failed
     */
    @Test
    public void field_ignore() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.text.tabular",
                "simple = {",
                "  a : TEXT;",
                "  @directio.text.ignore",
                "  b : TEXT;",
                "};",
        });
        BinaryStreamFormat<?> support = (BinaryStreamFormat<?>) loaded.newObject("text", "SimpleTabularTextFormat");
        assertThat(support.getMinimumFragmentSize(), is(greaterThan(0L)));

        byte[] contents = write(loaded, loaded.newModel("Simple")
                .setOption("a", new StringOption("A"))
                .setOption("b", new StringOption("B")));
        read(contents, loaded, loaded.newModel("Simple")
                .setOption("a", new StringOption("A"))
                .setOption("b", new StringOption()));
        assertThat(text(contents), is("A\n"));
    }

    /**
     * w/ {@code directio.text.file_name}.
     * @throws Exception if failed
     */
    @Test
    public void field_file_name() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.text.tabular",
                "simple = {",
                "  a : TEXT;",
                "  @directio.text.file_name",
                "  b : TEXT;",
                "};",
        });
        BinaryStreamFormat<?> support = (BinaryStreamFormat<?>) loaded.newObject("text", "SimpleTabularTextFormat");
        assertThat(support.getMinimumFragmentSize(), is(greaterThan(0L)));

        byte[] contents = write(loaded, loaded.newModel("Simple")
                .setOption("a", new StringOption("A"))
                .setOption("b", new StringOption("B")));
        read(contents, loaded, loaded.newModel("Simple")
                .setOption("a", new StringOption("A"))
                .setOption("b", new StringOption("testing")));
        assertThat(text(contents), is("A\n"));
    }

    /**
     * w/ {@code directio.text.line_number}.
     * @throws Exception if failed
     */
    @Test
    public void field_line_number() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.text.tabular(",
                "  input_transformer = '" + EmptyLineFilter.class.getName() + "'",
                ")",
                "simple = {",
                "  a : TEXT;",
                "  @directio.text.line_number",
                "  b : LONG;",
                "};",
        });
        BinaryStreamFormat<?> support = (BinaryStreamFormat<?>) loaded.newObject("text", "SimpleTabularTextFormat");
        assertThat(support.getMinimumFragmentSize(), is(-1L));

        byte[] contents = write(loaded, loaded.newModel("Simple")
                .setOption("a", new StringOption("A"))
                .setOption("b", new LongOption(-1)));
        assertThat(text(contents), is("A\n"));

        read(loaded, "\n\nHello, world!\n\n", wrapper -> {
            assertThat(wrapper.getOption("a"), is(new StringOption("Hello, world!")));
            assertThat(wrapper.getOption("b"), is(new LongOption(3)));
        });
    }

    /**
     * w/ {@code directio.text.line_number}.
     * @throws Exception if failed
     */
    @Test
    public void field_line_number_int() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.text.tabular(",
                "  input_transformer = '" + EmptyLineFilter.class.getName() + "'",
                ")",
                "simple = {",
                "  a : TEXT;",
                "  @directio.text.line_number",
                "  b : INT;",
                "};",
        });
        BinaryStreamFormat<?> support = (BinaryStreamFormat<?>) loaded.newObject("text", "SimpleTabularTextFormat");
        assertThat(support.getMinimumFragmentSize(), is(-1L));

        byte[] contents = write(loaded, loaded.newModel("Simple")
                .setOption("a", new StringOption("A"))
                .setOption("b", new IntOption(-1)));
        assertThat(text(contents), is("A\n"));

        read(loaded, "\n\nHello, world!\n\n", wrapper -> {
            assertThat(wrapper.getOption("a"), is(new StringOption("Hello, world!")));
            assertThat(wrapper.getOption("b"), is(new IntOption(3)));
        });
    }

    /**
     * w/ {@code directio.text.record_number}.
     * @throws Exception if failed
     */
    @Test
    public void field_record_number() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.text.tabular(",
                "  input_transformer = '" + EmptyLineFilter.class.getName() + "'",
                ")",
                "simple = {",
                "  a : TEXT;",
                "  @directio.text.record_number",
                "  b : LONG;",
                "};",
        });
        BinaryStreamFormat<?> support = (BinaryStreamFormat<?>) loaded.newObject("text", "SimpleTabularTextFormat");
        assertThat(support.getMinimumFragmentSize(), is(-1L));

        byte[] contents = write(loaded, loaded.newModel("Simple")
                .setOption("a", new StringOption("A"))
                .setOption("b", new LongOption(-1)));
        assertThat(text(contents), is("A\n"));

        read(loaded, "\n\nHello, world!\n\n", wrapper -> {
            assertThat(wrapper.getOption("a"), is(new StringOption("Hello, world!")));
            assertThat(wrapper.getOption("b"), is(new LongOption(1)));
        });
    }

    /**
     * w/ {@code directio.text.record_number}.
     * @throws Exception if failed
     */
    @Test
    public void field_record_number_int() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.text.tabular(",
                "  input_transformer = '" + EmptyLineFilter.class.getName() + "'",
                ")",
                "simple = {",
                "  a : TEXT;",
                "  @directio.text.record_number",
                "  b : INT;",
                "};",
        });
        BinaryStreamFormat<?> support = (BinaryStreamFormat<?>) loaded.newObject("text", "SimpleTabularTextFormat");
        assertThat(support.getMinimumFragmentSize(), is(-1L));

        byte[] contents = write(loaded, loaded.newModel("Simple")
                .setOption("a", new StringOption("A"))
                .setOption("b", new IntOption(-1)));
        assertThat(text(contents), is("A\n"));

        read(loaded, "\n\nHello, world!\n\n", wrapper -> {
            assertThat(wrapper.getOption("a"), is(new StringOption("Hello, world!")));
            assertThat(wrapper.getOption("b"), is(new IntOption(1)));
        });
    }

    /**
     * escape sequence contains duplicated keys.
     * @throws Exception if failed
     */
    @Test
    public void escape_conflict_key() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.text.tabular(",
                "  escape_character = '^',",
                "  escape_sequence = {",
                "    'a' : 'A',",
                "    'a' : 'B',",
                "  },",
                ")",
                "simple = {",
                "  a : TEXT;",
                "  b : TEXT;",
                "};",
        });
        byte[] contents = write(loaded, loaded.newModel("Simple")
                .setOption("a", new StringOption("1-A"))
                .setOption("b", new StringOption("2-B")));
        assertThat(text(contents), is("1-^a\t2-^a\n"));
    }

    /**
     * escape sequence contains duplicated values.
     * @throws Exception if failed
     */
    @Test
    public void escape_conflict_value() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.text.tabular(",
                "  escape_character = '^',",
                "  escape_sequence = {",
                "    'a' : 'A',",
                "    'b' : 'A',",
                "  },",
                ")",
                "simple = {",
                "  a : TEXT;",
                "  b : TEXT;",
                "};",
        });
        read(loaded, "1-^a\t2-^b\n", wrapper -> {
            assertThat(wrapper.getOption("a"), is(new StringOption("1-A")));
            assertThat(wrapper.getOption("b"), is(new StringOption("2-A")));
        });
    }

    /**
     * escape sequence contains duplicated values.
     * @throws Exception if failed
     */
    @Test
    public void escape_conflict_null() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.text.tabular(",
                "  escape_character = '^',",
                "  escape_sequence = {",
                "    'a' : null,",
                "    'b' : null,",
                "  },",
                ")",
                "simple = {",
                "  a : TEXT;",
                "  b : TEXT;",
                "};",
        });
        read(loaded, "^a\t^b\n", wrapper -> {
            assertThat(wrapper.getOption("a"), is(new StringOption()));
            assertThat(wrapper.getOption("b"), is(new StringOption()));
        });
    }

    /**
     * w/ unknown property.
     * @throws Exception if failed
     */
    @Test
    public void invalid_unknown_property() throws Exception {
        shouldSemanticErrorFromLines(new String[] {
                "@directio.text.tabular(",
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
                "@directio.text.tabular(",
                "  charset = '?',",
                ")",
                "simple = {",
                "  a : TEXT;",
                "};",
        });
    }

    /**
     * w/ unknown header.
     * @throws Exception if failed
     */
    @Test
    public void invalid_header_unknown() throws Exception {
        shouldSemanticErrorFromLines(new String[] {
                "@directio.text.tabular(",
                "  header = '?',",
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
                "@directio.text.tabular(",
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
                "@directio.text.tabular(",
                "  line_separator = '\\n',",
                ")",
                "simple = {",
                "  a : TEXT;",
                "};",
        });
    }

    /**
     * w/ malformed field_separator.
     * @throws Exception if failed
     */
    @Test
    public void invalid_field_separator_malformed() throws Exception {
        shouldSemanticErrorFromLines(new String[] {
                "@directio.text.tabular(",
                "  field_separator = '<>',",
                ")",
                "simple = {",
                "  a : TEXT;",
                "};",
        });
    }

    /**
     * field_separator conflict w/ line separator.
     * @throws Exception if failed
     */
    @Test
    public void invalid_field_separator_conflict() throws Exception {
        shouldSemanticErrorFromLines(new String[] {
                "@directio.text.tabular(",
                "  field_separator = '\\r',",
                ")",
                "simple = {",
                "  a : TEXT;",
                "};",
        });
        shouldSemanticErrorFromLines(new String[] {
                "@directio.text.tabular(",
                "  field_separator = '\\n',",
                ")",
                "simple = {",
                "  a : TEXT;",
                "};",
        });
    }

    /**
     * w/ malformed escape_character.
     * @throws Exception if failed
     */
    @Test
    public void invalid_escape_character_malformed() throws Exception {
        shouldSemanticErrorFromLines(new String[] {
                "@directio.text.tabular(",
                "  escape_character = '<>',",
                ")",
                "simple = {",
                "  a : TEXT;",
                "};",
        });
    }

    /**
     * w/ malformed escape_line_separator.
     * @throws Exception if failed
     */
    @Test
    public void invalid_escape_line_separator_malformed() throws Exception {
        shouldSemanticErrorFromLines(new String[] {
                "@directio.text.tabular(",
                "  escape_line_separator = unknown,",
                ")",
                "simple = {",
                "  a : TEXT;",
                "};",
        });
    }

    /**
     * escape_character conflict w/ line separator.
     * @throws Exception if failed
     */
    @Test
    public void invalid_escape_character_conflict_line_separator() throws Exception {
        shouldSemanticErrorFromLines(new String[] {
                "@directio.text.tabular(",
                "  escape_character = '\\r',",
                ")",
                "simple = {",
                "  a : TEXT;",
                "};",
        });
        shouldSemanticErrorFromLines(new String[] {
                "@directio.text.tabular(",
                "  escape_character = '\\n',",
                ")",
                "simple = {",
                "  a : TEXT;",
                "};",
        });
    }

    /**
     * escape_character conflict w/ line separator.
     * @throws Exception if failed
     */
    @Test
    public void invalid_escape_character_conflict_field_separator() throws Exception {
        shouldSemanticErrorFromLines(new String[] {
                "@directio.text.tabular(",
                "  field_separator = ':',",
                "  escape_character = ':',",
                ")",
                "simple = {",
                "  a : TEXT;",
                "};",
        });
        shouldSemanticErrorFromLines(new String[] {
                "@directio.text.tabular(",
                "  escape_character = '\\t',",
                ")",
                "simple = {",
                "  a : TEXT;",
                "};",
        });
    }

    /**
     * w/ malformed escape_sequence.
     * @throws Exception if failed
     */
    @Test
    public void invalid_escape_sequence_malformed() throws Exception {
        shouldSemanticErrorFromLines(new String[] {
                "@directio.text.tabular(",
                "  escape_character = '^',",
                "  escape_sequence = { 'n', '\\n' },",
                ")",
                "simple = {",
                "  a : TEXT;",
                "};",
        });
    }

    /**
     * escape_sequence w/ malformed key.
     * @throws Exception if failed
     */
    @Test
    public void invalid_escape_sequence_malformed_key() throws Exception {
        shouldSemanticErrorFromLines(new String[] {
                "@directio.text.tabular(",
                "  escape_character = '^',",
                "  escape_sequence = {",
                "    'malformed' : '\\n'",
                "  },",
                ")",
                "simple = {",
                "  a : TEXT;",
                "};",
        });
    }

    /**
     * escape_sequence w/ malformed value.
     * @throws Exception if failed
     */
    @Test
    public void invalid_escape_sequence_malformed_value() throws Exception {
        shouldSemanticErrorFromLines(new String[] {
                "@directio.text.tabular(",
                "  escape_character = '^',",
                "  escape_sequence = {",
                "    'n' : '\\r\\n'",
                "  },",
                ")",
                "simple = {",
                "  a : TEXT;",
                "};",
        });
    }

    /**
     * escape_sequence conflict w/ line separators.
     * @throws Exception if failed
     */
    @Test
    public void invalid_escape_sequence_conflict_line_separator() throws Exception {
        shouldSemanticErrorFromLines(new String[] {
                "@directio.text.tabular(",
                "  escape_character = '^',",
                "  escape_sequence = {",
                "    '\\n' : '\\n'",
                "  },",
                ")",
                "simple = {",
                "  a : TEXT;",
                "};",
        });
        shouldSemanticErrorFromLines(new String[] {
                "@directio.text.tabular(",
                "  escape_character = '^',",
                "  escape_sequence = {",
                "    '\\r' : '\\r'",
                "  },",
                ")",
                "simple = {",
                "  a : TEXT;",
                "};",
        });
    }

    /**
     * escape_sequence w/o escape character.
     * @throws Exception if failed
     */
    @Test
    public void invalid_escape_sequence_without_escape_character() throws Exception {
        shouldSemanticErrorFromLines(new String[] {
                "@directio.text.tabular(",
                "  escape_sequence = {",
                "    'N' : null",
                "  },",
                ")",
                "simple = {",
                "  a : TEXT;",
                "};",
        });
    }

    /**
     * escape_line_separator w/o escape character.
     * @throws Exception if failed
     */
    @Test
    public void invalid_escape_line_separator_without_escape_character() throws Exception {
        shouldSemanticErrorFromLines(new String[] {
                "@directio.text.tabular(",
                "  escape_line_separator = true,",
                ")",
                "simple = {",
                "  a : TEXT;",
                "};",
        });
    }

    /**
     * w/ malformed null_format.
     * @throws Exception if failed
     */
    @Test
    public void invalid_null_format_malformed() throws Exception {
        shouldSemanticErrorFromLines(new String[] {
                "@directio.text.tabular(",
                "  null_format = {},",
                ")",
                "simple = {",
                "  a : TEXT;",
                "};",
        });
    }

    /**
     * w/ malformed true_format.
     * @throws Exception if failed
     */
    @Test
    public void invalid_true_format_malformed() throws Exception {
        shouldSemanticErrorFromLines(new String[] {
                "@directio.text.tabular(",
                "  true_format = {},",
                ")",
                "simple = {",
                "  a : TEXT;",
                "};",
        });
    }

    /**
     * true_format conflict w/ null_format.
     * @throws Exception if failed
     */
    @Test
    public void invalid_true_format_conflict_null_format() throws Exception {
        shouldSemanticErrorFromLines(new String[] {
                "@directio.text.tabular(",
                "  null_format = 'NULL',",
                "  true_format = 'NULL',",
                ")",
                "simple = {",
                "  a : TEXT;",
                "};",
        });
    }

    /**
     * true_format conflict w/ null_format.
     * @throws Exception if failed
     */
    @Test
    public void invalid_true_format_conflict_null_format_inherited() throws Exception {
        shouldSemanticErrorFromLines(new String[] {
                "@directio.text.tabular(",
                "  null_format = 'a',",
                ")",
                "simple = {",
                "  @directio.text.field(true_format = 'a')",
                "  a : BOOLEAN;",
                "};",
        });
    }

    /**
     * w/ malformed false_format.
     * @throws Exception if failed
     */
    @Test
    public void invalid_false_format_malformed() throws Exception {
        shouldSemanticErrorFromLines(new String[] {
                "@directio.text.tabular(",
                "  false_format = {},",
                ")",
                "simple = {",
                "  a : TEXT;",
                "};",
        });
    }

    /**
     * false_format conflict w/ null_format.
     * @throws Exception if failed
     */
    @Test
    public void invalid_false_format_conflict_null_format() throws Exception {
        shouldSemanticErrorFromLines(new String[] {
                "@directio.text.tabular(",
                "  null_format = 'NULL',",
                "  false_format = 'NULL',",
                ")",
                "simple = {",
                "  a : TEXT;",
                "};",
        });
    }

    /**
     * false_format conflict w/ true_format.
     * @throws Exception if failed
     */
    @Test
    public void invalid_false_format_conflict_true_format() throws Exception {
        shouldSemanticErrorFromLines(new String[] {
                "@directio.text.tabular(",
                "  true_format = '_',",
                "  false_format = '_',",
                ")",
                "simple = {",
                "  a : TEXT;",
                "};",
        });
    }

    /**
     * false_format conflict w/ null_format.
     * @throws Exception if failed
     */
    @Test
    public void invalid_false_format_conflict_null_format_inherited() throws Exception {
        shouldSemanticErrorFromLines(new String[] {
                "@directio.text.tabular(",
                "  null_format = 'a',",
                ")",
                "simple = {",
                "  @directio.text.field(false_format = 'a')",
                "  a : BOOLEAN;",
                "};",
        });
    }

    /**
     * false_format conflict w/ true_format.
     * @throws Exception if failed
     */
    @Test
    public void invalid_false_format_conflict_true_format_inherited() throws Exception {
        shouldSemanticErrorFromLines(new String[] {
                "@directio.text.tabular(",
                "  true_format = 'a',",
                ")",
                "simple = {",
                "  @directio.text.field(false_format = 'a')",
                "  a : BOOLEAN;",
                "};",
        });
    }

    /**
     * malformed number_format.
     * @throws Exception if failed
     */
    @Test
    public void invalid_number_format_malformed() throws Exception {
        shouldSemanticErrorFromLines(new String[] {
                "@directio.text.tabular(",
                "  number_format = 'Hello, world!',",
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
                "@directio.text.tabular(",
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
                "@directio.text.tabular(",
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
                "@directio.text.tabular(",
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
                "@directio.text.tabular",
                "simple = {",
                "  @directio.text.ignore",
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
                "@directio.text.tabular",
                "simple = {",
                "  @directio.text.field",
                "  @directio.text.ignore",
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
                "@directio.text.tabular",
                "simple = {",
                "  @directio.text.field(undef = '?')",
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
                "@directio.text.tabular",
                "simple = {",
                "  @directio.text.file_name",
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
                "@directio.text.tabular",
                "simple = {",
                "  @directio.text.line_number",
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
                "@directio.text.tabular",
                "simple = {",
                "  @directio.text.record_number",
                "  a : TEXT;",
                "  b : TEXT;",
                "};",
        });
    }

    private byte[] write(ModelLoader loaded, ModelWrapper... objects) {
        String name = objects[0].getModelClass().getSimpleName();
        BinaryStreamFormat<Object> unsafe = unsafe(loaded.newObject("text", name + "TabularTextFormat"));
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

    private void writeError(ModelLoader loaded, ModelWrapper object) {
        String name = object.getModelClass().getSimpleName();
        BinaryStreamFormat<Object> unsafe = unsafe(loaded.newObject("text", name + "TabularTextFormat"));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (ModelOutput<Object> writer = unsafe.createOutput(unsafe.getSupportedType(), "testing", output)) {
            writer.write(object.unwrap());
            fail();
        } catch (IOException e) {
            // ok.
        } catch (InterruptedException e) {
            throw new AssertionError(e);
        }
    }

    private void read(ModelLoader loaded, String contents, Consumer<ModelWrapper> tester) {
        BinaryStreamFormat<Object> unsafe = unsafe(loaded.newObject("text", "SimpleTabularTextFormat"));
        ModelWrapper wrapper = loaded.newModel("Simple");
        Object buffer = wrapper.unwrap();
        try (ModelInput<Object> reader = unsafe.createInput(unsafe.getSupportedType(), "testing", in(contents))) {
            assertThat(reader.readTo(buffer), is(true));
            tester.accept(wrapper);

            assertThat(reader.readTo(buffer), is(false));
        } catch (IOException | InterruptedException e) {
            throw new AssertionError(e);
        }
    }

    private void read(
            byte[] contents, ModelLoader loaded, ModelWrapper... objects) throws IOException, InterruptedException {
        String name = objects[0].getModelClass().getSimpleName();
        BinaryStreamFormat<Object> unsafe = unsafe(loaded.newObject("text", name + "TabularTextFormat"));
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

    private void readError(ModelLoader loaded, String contents) {
        BinaryStreamFormat<Object> unsafe = unsafe(loaded.newObject("text", "SimpleTabularTextFormat"));
        ModelWrapper wrapper = loaded.newModel("Simple");
        Object buffer = wrapper.unwrap();
        try (ModelInput<Object> reader = unsafe.createInput(unsafe.getSupportedType(), "testing", in(contents))) {
            reader.readTo(buffer);
            fail();
        } catch (IOException e) {
            // ok.
        } catch (InterruptedException e1) {
            throw new AssertionError(e1);
        }
    }

    private byte[] restore(ModelLoader loaded, ModelWrapper... objects) throws IOException, InterruptedException {
        byte[] contents = write(loaded, objects);
        read(contents, loaded, objects);
        return contents;
    }

    @SuppressWarnings("unchecked")
    private BinaryStreamFormat<Object> unsafe(Object support) {
        return (BinaryStreamFormat<Object>) support;
    }

    private ByteArrayInputStream in(String contents) {
        return new ByteArrayInputStream(contents.getBytes(StandardCharsets.UTF_8));
    }

    private ByteArrayInputStream in(ByteArrayOutputStream output) {
        return new ByteArrayInputStream(output.toByteArray());
    }

    private String text(ByteArrayOutputStream output) {
        return text(output.toByteArray());
    }

    private String text(byte[] contents) {
        return new String(contents, StandardCharsets.UTF_8);
    }
}
