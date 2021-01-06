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
package com.asakusafw.dmdl.directio.csv.driver;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.zip.GZIPInputStream;

import org.apache.hadoop.io.Text;
import org.junit.Before;
import org.junit.Test;

import com.asakusafw.dmdl.directio.common.driver.GeneratorTesterRoot;
import com.asakusafw.dmdl.java.emitter.driver.ObjectDriver;
import com.asakusafw.runtime.directio.BinaryStreamFormat;
import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.runtime.io.csv.CsvConfiguration;
import com.asakusafw.runtime.io.csv.CsvFormatException;
import com.asakusafw.runtime.io.csv.CsvParser;
import com.asakusafw.runtime.io.util.LineFeedDelimitedInputStream;
import com.asakusafw.runtime.value.Date;
import com.asakusafw.runtime.value.DateTime;
import com.asakusafw.runtime.value.IntOption;
import com.asakusafw.runtime.value.LongOption;
import com.asakusafw.runtime.value.StringOption;

/**
 * Test for {@link CsvFormatEmitter}.
 */
public class CsvFormatEmitterTest extends GeneratorTesterRoot {

    private static final Charset DEFAULT_ENCODING = StandardCharsets.UTF_8;

    /**
     * Initializes the test.
     * @throws Exception if some errors were occurred
     */
    @Before
    public void setUp() throws Exception {
        emitDrivers.add(new CsvFormatEmitter());
        emitDrivers.add(new ObjectDriver());
    }

    /**
     * A simple case.
     * @throws Exception if failed
     */
    @Test
    public void simple() throws Exception {
        dump(true);
        ModelLoader loaded = generateJava("simple");
        ModelWrapper model = loaded.newModel("Simple");
        BinaryStreamFormat<?> support = (BinaryStreamFormat<?>) loaded.newObject("csv", "SimpleCsvFormat");

        assertThat(support.getSupportedType(), is((Object) model.unwrap().getClass()));

        BinaryStreamFormat<Object> unsafe = unsafe(support);

        model.set("value", new Text("hello-world"));

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (ModelOutput<Object> writer = unsafe.createOutput(model.unwrap().getClass(), "hello", output)) {
            writer.write(model.unwrap());
        }

        assertThat(scan(output.toByteArray()), is(Arrays.asList("hello-world")));

        Object buffer = loaded.newModel("Simple").unwrap();
        try (ModelInput<Object> reader = unsafe.createInput(model.unwrap().getClass(), "hello", in(output),
                0, size(output))) {
            assertThat(reader.readTo(buffer), is(true));
            assertThat(buffer, is(model.unwrap()));
            assertThat(reader.readTo(buffer), is(false));
        }
    }

    /**
     * All types.
     * @throws Exception if failed
     */
    @Test
    public void types() throws Exception {
        ModelLoader loaded = generateJava("types");
        ModelWrapper model = loaded.newModel("Types");
        BinaryStreamFormat<?> support = (BinaryStreamFormat<?>) loaded.newObject("csv", "TypesCsvFormat");
        assertThat(support.getSupportedType(), is((Object) model.unwrap().getClass()));

        ModelWrapper empty = loaded.newModel("Types");

        ModelWrapper all = loaded.newModel("Types");
        all.set("c_int", 100);
        all.set("c_text", new Text("Hello, DMDL world!"));
        all.set("c_boolean", true);
        all.set("c_byte", (byte) 64);
        all.set("c_short", (short) 1023);
        all.set("c_long", 100000L);
        all.set("c_float", 1.5f);
        all.set("c_double", 2.5f);
        all.set("c_decimal", new BigDecimal("3.1415"));
        all.set("c_date", new Date(2011, 9, 1));
        all.set("c_datetime", new DateTime(2011, 12, 31, 23, 59, 59));

        BinaryStreamFormat<Object> unsafe = unsafe(support);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (ModelOutput<Object> writer = unsafe.createOutput(model.unwrap().getClass(), "hello", output)) {
            writer.write(empty.unwrap());
            writer.write(all.unwrap());
        }

        Object buffer = loaded.newModel("Types").unwrap();
        try (ModelInput<Object> reader = unsafe.createInput(model.unwrap().getClass(), "hello", in(output),
                0, size(output))) {
            assertThat(reader.readTo(buffer), is(true));
            assertThat(buffer, is(empty.unwrap()));
            assertThat(reader.readTo(buffer), is(true));
            assertThat(buffer, is(all.unwrap()));
            assertThat(reader.readTo(buffer), is(false));
        }
    }

    /**
     * with attributes.
     * @throws Exception if failed
     */
    @Test
    public void attributes() throws Exception {
        ModelLoader loaded = generateJava("attributes");
        ModelWrapper model = loaded.newModel("Model");
        model.set("text_value", new Text("\u3042\u3044\u3046\u3048\u304a"));
        model.set("true_value", true);
        model.set("false_value", false);
        model.set("date_value", new Date(2011, 10, 11));
        model.set("date_time_value", new DateTime(2011, 1, 2, 13, 14, 15));
        BinaryStreamFormat<Object> support = unsafe(loaded.newObject("csv", "ModelCsvFormat"));

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (ModelOutput<Object> writer = support.createOutput(model.unwrap().getClass(), "hello", output)) {
            writer.write(model.unwrap());
        }

        String[][] results = parse(5, new String(dump(new GZIPInputStream(new ByteArrayInputStream(output.toByteArray()))), "ISO-2022-jp"));
        assertThat(results, is(new String[][] {
                {"text_value", "true_value", "false_value", "date_value", "date_time_value"},
                {"\u3042\u3044\u3046\u3048\u304a", "T", "F", "2011/10/11", "2011/01/02+13:14:15"},
        }));
    }

    /**
     * quoted.
     * @throws Exception if failed
     */
    @Test
    public void quote() throws Exception {
        ModelLoader loaded = generateJava("quote");
        ModelWrapper model = loaded.newModel("Simple");
        BinaryStreamFormat<?> support = (BinaryStreamFormat<?>) loaded.newObject("csv", "SimpleCsvFormat");
        BinaryStreamFormat<Object> unsafe = unsafe(support);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (ModelOutput<Object> writer = unsafe.createOutput(model.unwrap().getClass(), "hello", output)) {
            model.set("value", new Text("hello-world"));
            writer.write(model.unwrap());
            model.set("value", new Text("hello,world"));
            writer.write(model.unwrap());
        }
        assertThat(scan(output.toByteArray()), contains("\"hello-world\"", "\"hello,world\""));
    }

    /**
     * With compression.
     * @throws Exception if failed
     */
    @Test
    public void compression() throws Exception {
        ModelLoader loaded = generateJava("compress");
        ModelWrapper model = loaded.newModel("Compress");
        BinaryStreamFormat<?> support = (BinaryStreamFormat<?>) loaded.newObject("csv", "CompressCsvFormat");

        assertThat(support.getSupportedType(), is((Object) model.unwrap().getClass()));

        BinaryStreamFormat<Object> unsafe = unsafe(support);
        model.set("value", new Text("hello"));

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (ModelOutput<Object> writer = unsafe.createOutput(model.unwrap().getClass(), "hello", output)) {
            writer.write(model.unwrap());
        }

        assertThat(
                scan(dump(new GZIPInputStream(new ByteArrayInputStream(output.toByteArray())))),
                is(Arrays.asList("hello")));

        Object buffer = loaded.newModel("Compress").unwrap();
        try (ModelInput<Object> reader = unsafe.createInput(model.unwrap().getClass(), "hello", in(output),
                0, size(output))) {
            assertThat(reader.readTo(buffer), is(true));
            assertThat(buffer, is(model.unwrap()));
            assertThat(reader.readTo(buffer), is(false));
        }
    }

    /**
     * with header.
     * @throws Exception if failed
     */
    @Test
    public void header() throws Exception {
        ModelLoader loaded = generateJava("field_name");
        ModelWrapper model = loaded.newModel("Model");
        BinaryStreamFormat<Object> support = unsafe(loaded.newObject("csv", "ModelCsvFormat"));

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (ModelOutput<Object> writer = support.createOutput(model.unwrap().getClass(), "hello", output)) {
            model.set("value", new Text("Hello, world!"));
            writer.write(model.unwrap());
        }

        String[][] results = parse(1, new String(output.toByteArray(), "UTF-8"));
        assertThat(results, is(new String[][] {
                {"title"},
                {"Hello, world!"},
        }));
    }

    /**
     * with force header.
     * @throws Exception if failed
     */
    @Test
    public void force_header() throws Exception {
        ModelLoader loaded = generateJava("force_header");
        ModelWrapper model = loaded.newModel("Model");
        BinaryStreamFormat<Object> support = unsafe(loaded.newObject("csv", "ModelCsvFormat"));

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (ModelOutput<Object> writer = support.createOutput(model.unwrap().getClass(), "hello", output)) {
            model.set("value", new Text("Hello, world!"));
            writer.write(model.unwrap());
        }

        String[][] results = parse(1, new String(output.toByteArray(), "UTF-8"));
        assertThat(results, is(new String[][] {
                {"title"},
                {"Hello, world!"},
        }));
    }

    /**
     * with implicit field.
     * @throws Exception if failed
     */
    @Test
    public void implicit_field_name() throws Exception {
        ModelLoader loaded = generateJava("implicit_field_name");
        ModelWrapper model = loaded.newModel("Model");
        BinaryStreamFormat<Object> support = unsafe(loaded.newObject("csv", "ModelCsvFormat"));

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (ModelOutput<Object> writer = support.createOutput(model.unwrap().getClass(), "hello", output)) {
            model.set("value", new Text("Hello, world!"));
            writer.write(model.unwrap());
        }

        String[][] results = parse(1, new String(output.toByteArray(), "UTF-8"));
        assertThat(results, is(new String[][] {
                {"value"},
                {"Hello, world!"},
        }));
    }

    /**
     * with file name.
     * @throws Exception if failed
     */
    @Test
    public void file_name() throws Exception {
        ModelLoader loaded = generateJava("file_name");
        ModelWrapper model = loaded.newModel("Model");
        ModelWrapper buffer = loaded.newModel("Model");
        BinaryStreamFormat<Object> support = unsafe(loaded.newObject("csv", "ModelCsvFormat"));
        assertThat(support.getMinimumFragmentSize(), is(greaterThan(0L)));

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (ModelOutput<Object> writer = support.createOutput(model.unwrap().getClass(), "hello", output)) {
            model.set("value", new Text("Hello, world!"));
            writer.write(model.unwrap());
        }

        try (ModelInput<Object> reader = support.createInput(model.unwrap().getClass(), "testing", in(output),
                0, size(output))) {
            assertThat(reader.readTo(buffer.unwrap()), is(true));
            assertThat(buffer.getOption("value"), is((Object) new StringOption("Hello, world!")));
            assertThat(buffer.getOption("path"), is((Object) new StringOption("testing")));
            assertThat(reader.readTo(buffer.unwrap()), is(false));
        }
    }

    /**
     * with line number.
     * @throws Exception if failed
     */
    @Test
    public void line_number() throws Exception {
        ModelLoader loaded = generateJava("line_number");
        ModelWrapper model = loaded.newModel("Model");
        model.set("value", new Text("Hello\nworld!"));
        ModelWrapper buffer = loaded.newModel("Model");
        BinaryStreamFormat<Object> support = unsafe(loaded.newObject("csv", "ModelCsvFormat"));

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (ModelOutput<Object> writer = support.createOutput(model.unwrap().getClass(), "hello", output)) {
            writer.write(model.unwrap());
            writer.write(model.unwrap());
        }

        try (ModelInput<Object> reader = support.createInput(model.unwrap().getClass(), "testing", in(output),
                0, size(output))) {
            assertThat(reader.readTo(buffer.unwrap()), is(true));
            assertThat(buffer.getOption("value"), is((Object) new StringOption("Hello\nworld!")));
            assertThat(buffer.getOption("number"), is((Object) new IntOption(1)));
            assertThat(reader.readTo(buffer.unwrap()), is(true));
            assertThat(buffer.getOption("value"), is((Object) new StringOption("Hello\nworld!")));
            assertThat(buffer.getOption("number"), is((Object) new IntOption(3)));
            assertThat(reader.readTo(buffer.unwrap()), is(false));
        }
    }

    /**
     * with record number.
     * @throws Exception if failed
     */
    @Test
    public void record_number() throws Exception {
        ModelLoader loaded = generateJava("record_number");
        ModelWrapper model = loaded.newModel("Model");
        model.set("value", new Text("Hello\nworld!"));
        ModelWrapper buffer = loaded.newModel("Model");
        BinaryStreamFormat<Object> support = unsafe(loaded.newObject("csv", "ModelCsvFormat"));

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (ModelOutput<Object> writer = support.createOutput(model.unwrap().getClass(), "hello", output)) {
            writer.write(model.unwrap());
            writer.write(model.unwrap());
        }
        try (ModelInput<Object> reader = support.createInput(model.unwrap().getClass(), "testing", in(output),
                0, size(output))) {
            assertThat(reader.readTo(buffer.unwrap()), is(true));
            assertThat(buffer.getOption("value"), is((Object) new StringOption("Hello\nworld!")));
            assertThat(buffer.getOption("number"), is((Object) new LongOption(1)));
            assertThat(reader.readTo(buffer.unwrap()), is(true));
            assertThat(buffer.getOption("value"), is((Object) new StringOption("Hello\nworld!")));
            assertThat(buffer.getOption("number"), is((Object) new LongOption(2)));
            assertThat(reader.readTo(buffer.unwrap()), is(false));
        }
    }

    /**
     * with ignored property.
     * @throws Exception if failed
     */
    @Test
    public void ignore() throws Exception {
        ModelLoader loaded = generateJava("ignore");
        ModelWrapper model = loaded.newModel("Model");
        model.set("value", new Text("Hello, world!"));
        model.set("ignored", new Text("ignored"));
        ModelWrapper buffer = loaded.newModel("Model");
        BinaryStreamFormat<Object> support = unsafe(loaded.newObject("csv", "ModelCsvFormat"));

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (ModelOutput<Object> writer = support.createOutput(model.unwrap().getClass(), "hello", output)) {
            writer.write(model.unwrap());
        }
        try (ModelInput<Object> reader = support.createInput(model.unwrap().getClass(), "testing", in(output),
                0, size(output))) {
            assertThat(reader.readTo(buffer.unwrap()), is(true));
            assertThat(buffer.getOption("value"), is((Object) new StringOption("Hello, world!")));
            assertThat(buffer.getOption("ignored"), is((Object) new StringOption()));
            assertThat(reader.readTo(buffer.unwrap()), is(false));
        }
    }

    /**
     * fragmentation support.
     * @throws Exception if failed
     */
    @Test
    public void fragmentation() throws Exception {
        ModelLoader loaded = generateJava("fragmentation");
        Random random = new Random(12345);
        for (int i = 0; i < 10; i++) {
            fragmentation_attempt(loaded, random);
        }
    }

    /**
     * fragmentation support with hedaer.
     * @throws Exception if failed
     */
    @Test
    public void fragmentation_header() throws Exception {
        ModelLoader loaded = generateJava("fragmentation_header");
        Random random = new Random(123456);
        for (int i = 0; i < 10; i++) {
            fragmentation_attempt(loaded, random);
        }
    }

    private void fragmentation_attempt(ModelLoader loaded, Random random) throws Exception {
        ModelWrapper model = loaded.newModel("Tuple");
        BinaryStreamFormat<?> support = (BinaryStreamFormat<?>) loaded.newObject("csv", "TupleCsvFormat");

        assertThat(support.getSupportedType(), is((Object) model.unwrap().getClass()));

        BinaryStreamFormat<Object> unsafe = unsafe(support);

        List<Object> expected = new ArrayList<>();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (ModelOutput<Object> writer = unsafe.createOutput(model.unwrap().getClass(), "hello", output)) {
            for (int line = 0; line < 100; line++) {
                ModelWrapper buffer = loaded.newModel("Tuple");
                buffer.set("f1", new Text("f1:" + (line * 1)));
                buffer.set("f2", new Text("f2:" + random.nextInt()));
                buffer.set("f3", new Text("f3:" + random.nextInt()));
                writer.write(buffer.unwrap());
                expected.add(buffer.unwrap());
            }
        }
        byte[] bytes = output.toByteArray();

        for (int attempt = 0; attempt < 100; attempt++) {
            List<Object> actual = new ArrayList<>();
            int[] fragment = new int[random.nextInt(100) + 2];
            fragment[0] = output.size();
            for (int i = 1; i < fragment.length; i++) {
                fragment[i] = random.nextInt(output.size() + 1);
            }
            Arrays.sort(fragment);
            int start = 0;
            for (int i = 0; i < fragment.length; i++) {
                int offset = start;
                int length = fragment[i] - offset;
                if (length == 0) {
                    continue;
                }
                InputStream in = new ByteArrayInputStream(bytes, offset, bytes.length - offset);
                in.mark(bytes.length - offset);
                try (ModelInput<Object> reader = unsafe.createInput(model.unwrap().getClass(), "hello", in,
                        offset, length)) {
                    while (true) {
                        Object buffer = loaded.newModel("Tuple").unwrap();
                        if (reader.readTo(buffer) == false) {
                            break;
                        }
                        actual.add(buffer);
                    }
                } catch (CsvFormatException e) {
                    try (InputStream reIn = new ByteArrayInputStream(bytes);
                            InputStream copy = new LineFeedDelimitedInputStream(reIn, offset, length)) {
                        System.out.println(copy.read());
                    }
                    throw new IOException(MessageFormat.format(
                            "attempt={0}, f-offset={1}, f-size={2}, total={3}: [[{4}]]",
                            attempt,
                            offset,
                            length,
                            bytes.length,
                            new String(bytes, offset, length, "UTF-8")), e);
                }
                start = fragment[i];
            }
            assertThat(
                    String.format("attempt:%d", attempt),
                    actual,
                    hasSize(expected.size()));
            for (int i = 0, n = actual.size(); i < n; i++) {
                assertThat(actual.get(i), is(expected.get(i)));
            }
        }
    }

    /**
     * fragmentation is restricted.
     * @throws Exception if failed
     */
    @Test
    public void fragmentation_restricted() throws Exception {
        ModelLoader loaded = generateJava("fragmentation_restricted");
        ModelWrapper model = loaded.newModel("Tuple");
        BinaryStreamFormat<?> support = (BinaryStreamFormat<?>) loaded.newObject("csv", "TupleCsvFormat");
        BinaryStreamFormat<Object> unsafe = unsafe(support);

        model.set("f1", new Text("Hello1"));
        model.set("f2", new Text("Hello1"));
        model.set("f3", new Text("Hello1"));

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (ModelOutput<Object> writer = unsafe.createOutput(model.unwrap().getClass(), "hello", output)) {
            writer.write(model.unwrap());
        }
        try {
            unsafe.createInput(model.unwrap().getClass(), "hello", in(output), 1, size(output));
            fail();
        } catch (Exception e) {
            // ok.
        }
    }

    /**
     * unlimited range.
     * @throws Exception if failed
     */
    @Test
    public void unlimited() throws Exception {
        ModelLoader loaded = generateJava("simple");
        ModelWrapper model = loaded.newModel("Simple");
        BinaryStreamFormat<?> support = (BinaryStreamFormat<?>) loaded.newObject("csv", "SimpleCsvFormat");

        assertThat(support.getSupportedType(), is((Object) model.unwrap().getClass()));

        BinaryStreamFormat<Object> unsafe = unsafe(support);

        model.set("value", new Text("hello-world"));

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (ModelOutput<Object> writer = unsafe.createOutput(model.unwrap().getClass(), "hello", output)) {
            writer.write(model.unwrap());
        }

        assertThat(scan(output.toByteArray()), is(Arrays.asList("hello-world")));

        Object buffer = loaded.newModel("Simple").unwrap();
        try (ModelInput<Object> reader = unsafe.createInput(model.unwrap().getClass(), "hello", in(output),
                0, -1)) {
            assertThat(reader.readTo(buffer), is(true));
            assertThat(buffer, is(model.unwrap()));
            assertThat(reader.readTo(buffer), is(false));
        }
    }

    /**
     * Compile with no attributes.
     * @throws Exception if failed
     */
    @Test
    public void no_attributes() throws Exception {
        ModelLoader loaded = generateJava("no_attributes");
        assertThat(loaded.exists("csv", "NoAttributesCsvFormat"), is(false));
    }

    /**
     * with invalid field.
     * @throws Exception if failed
     */
    @Test
    public void invalid_file_name() throws Exception {
        shouldSemanticError("invalid_file_name");
    }

    /**
     * with invalid line number.
     * @throws Exception if failed
     */
    @Test
    public void invalid_line_number() throws Exception {
        shouldSemanticError("invalid_line_number");
    }

    /**
     * with invalid record number.
     * @throws Exception if failed
     */
    @Test
    public void invalid_record_number() throws Exception {
        shouldSemanticError("invalid_record_number");
    }

    private String[][] parse(int columns, String string) {
        CsvConfiguration conf = new CsvConfiguration(
                CsvConfiguration.DEFAULT_CHARSET,
                CsvConfiguration.DEFAULT_HEADER_CELLS,
                CsvConfiguration.DEFAULT_TRUE_FORMAT,
                CsvConfiguration.DEFAULT_FALSE_FORMAT,
                CsvConfiguration.DEFAULT_DATE_FORMAT,
                CsvConfiguration.DEFAULT_DATE_TIME_FORMAT);
        List<String[]> results = new ArrayList<>();
        ByteArrayInputStream input = new ByteArrayInputStream(string.getBytes(conf.getCharset()));
        try (CsvParser parser = new CsvParser(input, string, conf)) {
            StringOption buffer = new StringOption();
            while (parser.next()) {
                String[] line = new String[columns];
                for (int i = 0; i < columns; i++) {
                    parser.fill(buffer);
                    line[i] = buffer.or((String) null);
                }
                parser.endRecord();
                results.add(line);
            }
        } catch (Exception e) {
            throw new AssertionError(e);
        }
        return results.toArray(new String[results.size()][]);
    }

    /**
     * Compile with invalid attribute.
     * @throws Exception if failed
     */
    @Test
    public void invalid_attribute() throws Exception {
        shouldSemanticError("invalid_attribute");
    }

    @SuppressWarnings("unchecked")
    private BinaryStreamFormat<Object> unsafe(Object support) {
        return (BinaryStreamFormat<Object>) support;
    }

    private ByteArrayInputStream in(ByteArrayOutputStream output) {
        return new ByteArrayInputStream(output.toByteArray());
    }

    private long size(ByteArrayOutputStream output) {
        return output.size();
    }

    private byte[] dump(InputStream input) throws IOException {
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            while (true) {
                int read = input.read(buf);
                if (read < 0) {
                    break;
                }
                output.write(buf, 0, read);
            }
            return output.toByteArray();
        } finally {
            input.close();
        }
    }

    private List<String> scan(byte[] bytes) {
        try (Scanner scanner = new Scanner(new ByteArrayInputStream(bytes), DEFAULT_ENCODING.name())) {
            List<String> results = new ArrayList<>();
            while (scanner.hasNextLine()) {
                results.add(scanner.nextLine());
            }
            return results;
        }
    }
}
