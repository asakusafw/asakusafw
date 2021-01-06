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
package com.asakusafw.dmdl.directio.text.csv;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.zip.GZIPInputStream;

import org.junit.Before;
import org.junit.Test;

import com.asakusafw.dmdl.directio.common.driver.GeneratorTesterRoot;
import com.asakusafw.dmdl.directio.text.mock.EmptyLineFilter;
import com.asakusafw.dmdl.directio.text.mock.UpperCaseTransformer;
import com.asakusafw.dmdl.directio.text.tabular.TabularTextEmitterTest;
import com.asakusafw.dmdl.java.emitter.driver.ObjectDriver;
import com.asakusafw.runtime.directio.BinaryStreamFormat;
import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.runtime.value.IntOption;
import com.asakusafw.runtime.value.LongOption;
import com.asakusafw.runtime.value.StringOption;

/**
 * Test for {@link CsvTextEmitter}.
 * @see TabularTextEmitterTest
 */
public class CsvTextEmitterTest extends GeneratorTesterRoot {

    /**
     * Initializes the test.
     * @throws Exception if some errors were occurred
     */
    @Before
    public void setUp() throws Exception {
        emitDrivers.add(new CsvTextEmitter());
        emitDrivers.add(new ObjectDriver());
    }

    /**
     * simple case.
     * @throws Exception if failed
     */
    @Test
    public void simple() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.text.csv",
                "simple = { value : TEXT; };",
        });
        ModelWrapper model = loaded.newModel("Simple")
                .setOption("value", new StringOption("Hello, world!"));

        BinaryStreamFormat<?> support = (BinaryStreamFormat<?>) loaded.newObject("text", "SimpleCsvTextFormat");
        assertThat(support.getSupportedType(), is((Object) model.unwrap().getClass()));
        assertThat(support.getMinimumFragmentSize(), is(greaterThan(0L)));

        BinaryStreamFormat<Object> unsafe = unsafe(support);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (ModelOutput<Object> writer = unsafe.createOutput(unsafe.getSupportedType(), "hello", output)) {
            writer.write(model.unwrap());
        }

        assertThat(text(output), is("\"Hello, world!\"\r\n"));

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
                "@directio.text.csv",
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
        assertThat(text(contents), is("0a,0b,0c\r\n1a,1b,1c\r\n2a,2b,2c\r\n"));
    }

    /**
     * w/ {@code charset}.
     * @throws Exception if failed
     */
    @Test
    public void charset() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.text.csv(",
                "  charset = 'UTF-16LE',",
                ")",
                "simple = {",
                "  a : TEXT;",
                "};",
        });
        BinaryStreamFormat<?> support = (BinaryStreamFormat<?>) loaded.newObject("text", "SimpleCsvTextFormat");
        assertThat(support.getMinimumFragmentSize(), is(-1L));

        ModelWrapper model = loaded.newModel("Simple")
                .setOption("a", new StringOption("Hello, world!"));
        byte[] contents = write(loaded, model);
        assertThat(new String(contents, StandardCharsets.UTF_16LE), is("\"Hello, world!\"\r\n"));
    }

    /**
     * w/ {@code compression}.
     * @throws Exception if failed
     */
    @Test
    public void compression() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.text.csv(",
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
            assertThat(s.nextLine(), is("\"Hello, world!\""));

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
                "@directio.text.csv(",
                "  line_separator = unix,",
                ")",
                "simple = {",
                "  a : TEXT;",
                "};",
        });
        ModelWrapper model = loaded.newModel("Simple")
                .setOption("a", new StringOption("Hello, world!"));
        byte[] contents = restore(loaded, model);
        assertThat(text(contents), is("\"Hello, world!\"\n"));
    }

    /**
     * w/ {@code field_separator}.
     * @throws Exception if failed
     */
    @Test
    public void field_separator() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.text.csv(",
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
        assertThat(text(contents), is("A:B:C\r\n"));
    }

    /**
     * w/ {@code quote_character}.
     * @throws Exception if failed
     */
    @Test
    public void quote_character() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.text.csv(",
                "  quote_character = '%',",
                ")",
                "simple = {",
                "  a : TEXT;",
                "};",
        });
        ModelWrapper model = loaded.newModel("Simple")
                .setOption("a", new StringOption("Hello, world!"));
        byte[] contents = restore(loaded, model);
        assertThat(text(contents), is("%Hello, world!%\r\n"));
    }

    /**
     * w/ {@code allow_linefeed}.
     * @throws Exception if failed
     */
    @Test
    public void allow_linefeed() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.text.csv(",
                "  allow_linefeed = true,",
                ")",
                "simple = {",
                "  a : TEXT;",
                "};",
        });
        BinaryStreamFormat<?> support = (BinaryStreamFormat<?>) loaded.newObject("text", "SimpleCsvTextFormat");
        assertThat(support.getMinimumFragmentSize(), is(-1L));

        ModelWrapper model = loaded.newModel("Simple")
                .setOption("a", new StringOption("Hello\nworld!"));
        byte[] contents = restore(loaded, model);
        assertThat(text(contents), is("\"Hello\nworld!\"\r\n"));
    }

    /**
     * w/ {@code quote_style}.
     * @throws Exception if failed
     */
    @Test
    public void quote_style() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.text.csv(",
                "  quote_style = always,",
                ")",
                "simple = {",
                "  a : TEXT;",
                "};",
        });
        ModelWrapper model = loaded.newModel("Simple")
                .setOption("a", new StringOption("Hello!"));
        byte[] contents = restore(loaded, model);
        assertThat(text(contents), is("\"Hello!\"\r\n"));
    }

    /**
     * w/ {@code quote_style}.
     * @throws Exception if failed
     */
    @Test
    public void quote_style_field() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.text.csv",
                "simple = {",
                "  a : TEXT;",
                "  @directio.text.field(quote_style = always)",
                "  b : TEXT;",
                "};",
        });
        ModelWrapper model = loaded.newModel("Simple")
                .setOption("a", new StringOption("A"))
                .setOption("b", new StringOption("B"));
        byte[] contents = restore(loaded, model);
        assertThat(text(contents), is("A,\"B\"\r\n"));
    }

    /**
     * w/ {@code header}.
     * @throws Exception if failed
     */
    @Test
    public void header() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.text.csv(",
                "  header = force,",
                ")",
                "simple = {",
                "  @directio.text.field(name = 'Hello, world!')",
                "  a : TEXT;",
                "  b : TEXT;",
                "};",
        });
        byte[] contents = restore(loaded, loaded.newModel("Simple")
                .setOption("a", new StringOption("A"))
                .setOption("b", new StringOption("B")));
        assertThat(text(contents), is("\"Hello, world!\",b\r\nA,B\r\n"));
    }

    /**
     * w/ {@code header_quote_style}.
     * @throws Exception if failed
     */
    @Test
    public void header_quote_style() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.text.csv(",
                "  header = force,",
                "  header_quote_style = always,",
                ")",
                "simple = {",
                "  a : TEXT;",
                "  b : TEXT;",
                "};",
        });
        byte[] contents = restore(loaded, loaded.newModel("Simple")
                .setOption("a", new StringOption("A"))
                .setOption("b", new StringOption("B")));
        assertThat(text(contents), is("\"a\",\"b\"\r\nA,B\r\n"));
    }

    /**
     * w/ {@code quote_style} inherits to header.
     * @throws Exception if failed
     */
    @Test
    public void quote_style_inherit_header() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.text.csv(",
                "  header = force,",
                "  quote_style = always,",
                ")",
                "simple = {",
                "  a : TEXT;",
                "  b : TEXT;",
                "};",
        });
        byte[] contents = restore(loaded, loaded.newModel("Simple")
                .setOption("a", new StringOption("A"))
                .setOption("b", new StringOption("B")));
        assertThat(text(contents), is("\"a\",\"b\"\r\n\"A\",\"B\"\r\n"));
    }

    /**
     * w/ {@code header_quote_style}.
     * @throws Exception if failed
     */
    @Test
    public void header_quote_style_override() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.text.csv(",
                "  header = force,",
                "  quote_style = always,",
                "  header_quote_style = never,",
                ")",
                "simple = {",
                "  @directio.text.field(quote_style = always)",
                "  a : TEXT;",
                "  b : TEXT;",
                "};",
        });
        byte[] contents = restore(loaded, loaded.newModel("Simple")
                .setOption("a", new StringOption("A"))
                .setOption("b", new StringOption("B")));
        assertThat(text(contents), is("a,b\r\n\"A\",\"B\"\r\n"));
    }

    /**
     * w/ {@code input_transformer}.
     * @throws Exception if failed
     */
    @Test
    public void input_transformer() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.text.csv(",
                "  input_transformer = '" + EmptyLineFilter.class.getName() + "'",
                ")",
                "simple = {",
                "  a : TEXT;",
                "};",
        });
        read(loaded, "\n\nHello!\n\n", wrapper -> {
            assertThat(wrapper.getOption("a"), is(new StringOption("Hello!")));
        });
    }

    /**
     * w/ {@code output_transformer}.
     * @throws Exception if failed
     */
    @Test
    public void output_transformer() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.text.csv(",
                "  output_transformer = '" + UpperCaseTransformer.class.getName() + "'",
                ")",
                "simple = {",
                "  a : TEXT;",
                "};",
        });
        ModelWrapper model = loaded.newModel("Simple")
                .setOption("a", new StringOption("hello"));
        byte[] contents = write(loaded, model);
        assertThat(text(contents), is("HELLO\r\n"));
    }

    /**
     * w/ {@code directio.text.field}.
     * @throws Exception if failed
     */
    @Test
    public void field_field() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.text.csv",
                "simple = {",
                "  @directio.text.field",
                "  a : TEXT;",
                "};",
        });
        BinaryStreamFormat<?> support = (BinaryStreamFormat<?>) loaded.newObject("text", "SimpleCsvTextFormat");
        assertThat(support.getMinimumFragmentSize(), is(greaterThan(0L)));

        byte[] contents = write(loaded, loaded.newModel("Simple")
                .setOption("a", new StringOption("A")));
        read(contents, loaded, loaded.newModel("Simple")
                .setOption("a", new StringOption("A")));
        assertThat(text(contents), is("A\r\n"));
    }

    /**
     * w/ {@code directio.text.ignore}.
     * @throws Exception if failed
     */
    @Test
    public void field_ignore() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.text.csv",
                "simple = {",
                "  a : TEXT;",
                "  @directio.text.ignore",
                "  b : TEXT;",
                "};",
        });
        BinaryStreamFormat<?> support = (BinaryStreamFormat<?>) loaded.newObject("text", "SimpleCsvTextFormat");
        assertThat(support.getMinimumFragmentSize(), is(greaterThan(0L)));

        byte[] contents = write(loaded, loaded.newModel("Simple")
                .setOption("a", new StringOption("A"))
                .setOption("b", new StringOption("B")));
        read(contents, loaded, loaded.newModel("Simple")
                .setOption("a", new StringOption("A"))
                .setOption("b", new StringOption()));
        assertThat(text(contents), is("A\r\n"));
    }

    /**
     * w/ {@code directio.text.file_name}.
     * @throws Exception if failed
     */
    @Test
    public void field_file_name() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.text.csv",
                "simple = {",
                "  a : TEXT;",
                "  @directio.text.file_name",
                "  b : TEXT;",
                "};",
        });
        BinaryStreamFormat<?> support = (BinaryStreamFormat<?>) loaded.newObject("text", "SimpleCsvTextFormat");
        assertThat(support.getMinimumFragmentSize(), is(greaterThan(0L)));

        byte[] contents = write(loaded, loaded.newModel("Simple")
                .setOption("a", new StringOption("A"))
                .setOption("b", new StringOption("B")));
        read(contents, loaded, loaded.newModel("Simple")
                .setOption("a", new StringOption("A"))
                .setOption("b", new StringOption("testing")));
        assertThat(text(contents), is("A\r\n"));
    }

    /**
     * w/ {@code directio.text.line_number}.
     * @throws Exception if failed
     */
    @Test
    public void field_line_number() throws Exception {
        ModelLoader loaded = generateJavaFromLines(new String[] {
                "@directio.text.csv(",
                "  input_transformer = '" + EmptyLineFilter.class.getName() + "'",
                ")",
                "simple = {",
                "  a : TEXT;",
                "  @directio.text.line_number",
                "  b : LONG;",
                "};",
        });
        BinaryStreamFormat<?> support = (BinaryStreamFormat<?>) loaded.newObject("text", "SimpleCsvTextFormat");
        assertThat(support.getMinimumFragmentSize(), is(-1L));

        byte[] contents = write(loaded, loaded.newModel("Simple")
                .setOption("a", new StringOption("A"))
                .setOption("b", new LongOption(-1)));
        assertThat(text(contents), is("A\r\n"));

        read(loaded, "\n\nHello!\n\n", wrapper -> {
            assertThat(wrapper.getOption("a"), is(new StringOption("Hello!")));
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
                "@directio.text.csv(",
                "  input_transformer = '" + EmptyLineFilter.class.getName() + "'",
                ")",
                "simple = {",
                "  a : TEXT;",
                "  @directio.text.line_number",
                "  b : INT;",
                "};",
        });
        BinaryStreamFormat<?> support = (BinaryStreamFormat<?>) loaded.newObject("text", "SimpleCsvTextFormat");
        assertThat(support.getMinimumFragmentSize(), is(-1L));

        byte[] contents = write(loaded, loaded.newModel("Simple")
                .setOption("a", new StringOption("A"))
                .setOption("b", new IntOption(-1)));
        assertThat(text(contents), is("A\r\n"));

        read(loaded, "\n\nHello!\n\n", wrapper -> {
            assertThat(wrapper.getOption("a"), is(new StringOption("Hello!")));
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
                "@directio.text.csv(",
                "  input_transformer = '" + EmptyLineFilter.class.getName() + "'",
                ")",
                "simple = {",
                "  a : TEXT;",
                "  @directio.text.record_number",
                "  b : LONG;",
                "};",
        });
        BinaryStreamFormat<?> support = (BinaryStreamFormat<?>) loaded.newObject("text", "SimpleCsvTextFormat");
        assertThat(support.getMinimumFragmentSize(), is(-1L));

        byte[] contents = write(loaded, loaded.newModel("Simple")
                .setOption("a", new StringOption("A"))
                .setOption("b", new LongOption(-1)));
        assertThat(text(contents), is("A\r\n"));

        read(loaded, "\n\nHello!\n\n", wrapper -> {
            assertThat(wrapper.getOption("a"), is(new StringOption("Hello!")));
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
                "@directio.text.csv(",
                "  input_transformer = '" + EmptyLineFilter.class.getName() + "'",
                ")",
                "simple = {",
                "  a : TEXT;",
                "  @directio.text.record_number",
                "  b : INT;",
                "};",
        });
        BinaryStreamFormat<?> support = (BinaryStreamFormat<?>) loaded.newObject("text", "SimpleCsvTextFormat");
        assertThat(support.getMinimumFragmentSize(), is(-1L));

        byte[] contents = write(loaded, loaded.newModel("Simple")
                .setOption("a", new StringOption("A"))
                .setOption("b", new IntOption(-1)));
        assertThat(text(contents), is("A\r\n"));

        read(loaded, "\n\nHello!\n\n", wrapper -> {
            assertThat(wrapper.getOption("a"), is(new StringOption("Hello!")));
            assertThat(wrapper.getOption("b"), is(new IntOption(1)));
        });
    }

    /**
     * w/ malformed quote_character.
     * @throws Exception if failed
     */
    @Test
    public void invalid_quote_character_malformed() throws Exception {
        shouldSemanticErrorFromLines(new String[] {
                "@directio.text.csv(",
                "  quote_character = '<>',",
                ")",
                "simple = {",
                "  a : TEXT;",
                "};",
        });
    }

    /**
     * quote_character conflict w/ line separator.
     * @throws Exception if failed
     */
    @Test
    public void invalid_quote_character_conflict_line_separator() throws Exception {
        shouldSemanticErrorFromLines(new String[] {
                "@directio.text.csv(",
                "  quote_character = '\\r',",
                ")",
                "simple = {",
                "  a : TEXT;",
                "};",
        });
        shouldSemanticErrorFromLines(new String[] {
                "@directio.text.csv(",
                "  quote_character = '\\n',",
                ")",
                "simple = {",
                "  a : TEXT;",
                "};",
        });
    }

    /**
     * quote_character conflict w/ line separator.
     * @throws Exception if failed
     */
    @Test
    public void invalid_quote_character_conflict_field_separator() throws Exception {
        shouldSemanticErrorFromLines(new String[] {
                "@directio.text.csv(",
                "  field_separator = ':',",
                "  quote_character = ':',",
                ")",
                "simple = {",
                "  a : TEXT;",
                "};",
        });
        shouldSemanticErrorFromLines(new String[] {
                "@directio.text.csv(",
                "  quote_character = ',',",
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
                "@directio.text.csv(",
                "  null_format = 'a',",
                ")",
                "simple = {",
                "  @directio.text.field(true_format = 'a')",
                "  a : BOOLEAN;",
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
                "@directio.text.csv(",
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
                "@directio.text.csv(",
                "  true_format = 'a',",
                ")",
                "simple = {",
                "  @directio.text.field(false_format = 'a')",
                "  a : BOOLEAN;",
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
                "@directio.text.csv",
                "simple = {",
                "  @directio.text.ignore",
                "  a : TEXT;",
                "};",
        });
    }

    private static byte[] write(ModelLoader loaded, ModelWrapper... objects) {
        String name = objects[0].getModelClass().getSimpleName();
        BinaryStreamFormat<Object> unsafe = unsafe(loaded.newObject("text", name + "CsvTextFormat"));
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

    private static void read(ModelLoader loaded, String contents, Consumer<ModelWrapper> tester) {
        BinaryStreamFormat<Object> unsafe = unsafe(loaded.newObject("text", "SimpleCsvTextFormat"));
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

    private static void read(
            byte[] contents, ModelLoader loaded, ModelWrapper... objects) throws IOException, InterruptedException {
        String name = objects[0].getModelClass().getSimpleName();
        BinaryStreamFormat<Object> unsafe = unsafe(loaded.newObject("text", name + "CsvTextFormat"));
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

    private byte[] restore(ModelLoader loaded, ModelWrapper... objects) throws IOException, InterruptedException {
        byte[] contents = write(loaded, objects);
        read(contents, loaded, objects);
        return contents;
    }

    @SuppressWarnings("unchecked")
    private static BinaryStreamFormat<Object> unsafe(Object support) {
        return (BinaryStreamFormat<Object>) support;
    }

    private static ByteArrayInputStream in(String contents) {
        return new ByteArrayInputStream(contents.getBytes(StandardCharsets.UTF_8));
    }

    private static ByteArrayInputStream in(ByteArrayOutputStream output) {
        return new ByteArrayInputStream(output.toByteArray());
    }

    private static String text(ByteArrayOutputStream output) {
        return text(output.toByteArray());
    }

    private static String text(byte[] contents) {
        return new String(contents, StandardCharsets.UTF_8);
    }
}
