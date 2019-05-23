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
package com.asakusafw.runtime.io.text.directio;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.GzipCodec;
import org.junit.Test;

import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.runtime.io.text.FieldReader;
import com.asakusafw.runtime.io.text.FieldWriter;
import com.asakusafw.runtime.io.text.TextFormat;
import com.asakusafw.runtime.io.text.driver.FieldDefinition;
import com.asakusafw.runtime.io.text.driver.HeaderType;
import com.asakusafw.runtime.io.text.driver.RecordDefinition;
import com.asakusafw.runtime.io.text.mock.MockFieldAdapter;
import com.asakusafw.runtime.io.text.mock.MockFieldReader;
import com.asakusafw.runtime.io.text.mock.MockFieldWriter;
import com.asakusafw.runtime.io.util.InputSplitter;
import com.asakusafw.runtime.io.util.InputSplitters;

/**
 * Test for {@link AbstractTextStreamFormat}.
 */
public class AbstractTextStreamFormatTest {

    /**
     * input - simple.
     * @throws Exception if failed
     */
    @Test
    public void input() throws Exception {
        MockFormat format = format(1);
        assertThat(format.getPreferredFragmentSize(), is(-1L));
        assertThat(format.getMinimumFragmentSize(), is(-1L));

        String[][] data = {
                { "Hello, world!" }
        };
        try (ModelInput<String[]> in = format.createInput(String[].class, "dummy", input(data))) {
            String[][] result = collect(1, in);
            assertThat(result, is(data));
        }
    }

    /**
     * input - w/ multiple rows/cols.
     * @throws Exception if failed
     */
    @Test
    public void input_multiple() throws Exception {
        MockFormat format = format(3);
        String[][] data = {
                { "A", "B", "C", },
                { "D", "E", "F", },
                { "G", "H", "I", },
        };
        try (ModelInput<String[]> in = format.createInput(String[].class, "dummy", input(data))) {
            String[][] result = collect(3, in);
            assertThat(result, is(data));
        }
    }

    /**
     * input - w/ splitter.
     * @throws Exception if failed
     */
    @Test
    public void input_splitter_whole() throws Exception {
        MockFormat format = format(3)
                .withInputSplitter(InputSplitters.byLineFeed());
        assertThat(format.getPreferredFragmentSize(), is(-1L));
        assertThat(format.getMinimumFragmentSize(), is(greaterThanOrEqualTo(0L)));

        String[][] data = {
                { "A", "B", "C", },
                { "D", "E", "F", },
                { "G", "H", "I", },
        };
        try (ModelInput<String[]> in = format.createInput(String[].class, "dummy", input(data))) {
            String[][] result = collect(3, in);
            assertThat(result, is(data));
        }
    }

    /**
     * input - w/ splitter.
     * @throws Exception if failed
     */
    @Test
    public void input_splitter_trim_lead() throws Exception {
        MockFormat format = format(3)
                .withInputSplitter(InputSplitters.byLineFeed());
        String[][] data = {
                { "A", "B", "C", },
                { "D", "E", "F", },
                { "G", "H", "I", },
        };
        try (ModelInput<String[]> in = format.createInput(String[].class, "dummy", input(data), 1, Long.MAX_VALUE)) {
            String[][] result = collect(3, in);
            assertThat(result, is(Arrays.copyOfRange(data, 1, 3)));
        }
    }

    /**
     * input - w/ splitter.
     * @throws Exception if failed
     */
    @Test
    public void input_splitter_trim_trail() throws Exception {
        MockFormat format = format(3)
                .withInputSplitter(InputSplitters.byLineFeed());
        String[][] data = {
                { "A", "B", "C", },
                { "D", "E", "F", },
                { "G", "H", "I", },
        };
        try (ModelInput<String[]> in = format.createInput(String[].class, "dummy", input(data), 0, 6)) {
            String[][] result = collect(3, in);
            assertThat(result, is(Arrays.copyOfRange(data, 0, 2)));
        }
    }

    /**
     * input - w/ splitter.
     * @throws Exception if failed
     */
    @Test
    public void input_splitter_trim_around() throws Exception {
        MockFormat format = format(3)
                .withInputSplitter(InputSplitters.byLineFeed());
        String[][] data = {
                { "A", "B", "C", },
                { "D", "E", "F", },
                { "G", "H", "I", },
        };
        try (ModelInput<String[]> in = format.createInput(String[].class, "dummy", input(data), 1, 6)) {
            String[][] result = collect(3, in);
            assertThat(result, is(Arrays.copyOfRange(data, 1, 2)));
        }
    }

    /**
     * input - w/ header.
     * @throws Exception if failed
     */
    @Test
    public void input_hedaer() throws Exception {
        MockFormat format = format(3, HeaderType.FORCE);
        String[][] data = {
                { "A", "B", "C", },
                { "D", "E", "F", },
                { "G", "H", "I", },
        };
        try (ModelInput<String[]> in = format.createInput(String[].class, "dummy", input(data))) {
            String[][] result = collect(3, in);
            assertThat(result, is(Arrays.copyOfRange(data, 1, 3)));
        }
    }

    /**
     * input - w/ header.
     * @throws Exception if failed
     */
    @Test
    public void input_hedaer_split_first() throws Exception {
        MockFormat format = format(3, HeaderType.FORCE)
                .withInputSplitter(InputSplitters.byLineFeed());
        String[][] data = {
                { "A", "B", "C", },
                { "D", "E", "F", },
                { "G", "H", "I", },
        };
        try (ModelInput<String[]> in = format.createInput(String[].class, "dummy", input(data), 0, 1)) {
            String[][] result = collect(3, in);
            assertThat(result, is(new String[0][]));
        }
    }

    /**
     * input - w/ header.
     * @throws Exception if failed
     */
    @Test
    public void input_hedaer_split_rest() throws Exception {
        MockFormat format = format(3, HeaderType.FORCE)
                .withInputSplitter(InputSplitters.byLineFeed());
        String[][] data = {
                { "A", "B", "C", },
                { "D", "E", "F", },
                { "G", "H", "I", },
        };
        try (ModelInput<String[]> in = format.createInput(String[].class, "dummy", input(data), 1, Long.MAX_VALUE)) {
            String[][] result = collect(3, in);
            assertThat(result, is(Arrays.copyOfRange(data, 1, 3)));
        }
    }

    /**
     * input - w/ compression.
     * @throws Exception if failed
     */
    @Test
    public void input_compression() throws Exception {
        MockFormat format = format(1)
                .withCodecClass(GzipCodec.class);
        String[][] data = {
                { "Hello, world!" }
        };
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        try (InputStream in = input(data); OutputStream out = new GZIPOutputStream(buf)) {
            IOUtils.copy(in, out);
        }
        try (ModelInput<String[]> in = format.createInput(String[].class, "dummy", new ByteArrayInputStream(buf.toByteArray()))) {
            String[][] result = collect(1, in);
            assertThat(result, is(data));
        }
    }

    /**
     * output - simple.
     * @throws Exception if failed
     */
    @Test
    public void output() throws Exception {
        MockFormat format = format(1);
        String[][] data = {
                { "Hello, world!" }
        };
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (ModelOutput<String[]> out = format.createOutput(String[].class, "dummy", output)) {
            dump(out, data);
        }
        assertThat(deserialize(output.toByteArray()), is(data));
    }

    /**
     * output - w/ header.
     * @throws Exception if failed
     */
    @Test
    public void output_header() throws Exception {
        MockFormat format = format(HeaderType.FORCE, "a", "b", "c");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (ModelOutput<String[]> out = format.createOutput(String[].class, "dummy", output)) {
            dump(out, new String[][] {
                { "A", "B", "C", },
                { "D", "E", "F", },
            });
        }
        assertThat(deserialize(output.toByteArray()), is(new String[][] {
            { "a", "b", "c", },
            { "A", "B", "C", },
            { "D", "E", "F", },
        }));
    }

    /**
     * output - w/ compression.
     * @throws Exception if failed
     */
    @Test
    public void output_compression() throws Exception {
        MockFormat format = format(1)
                .withCodecClass(GzipCodec.class);
        String[][] data = {
                { "Hello, world!" }
        };
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (ModelOutput<String[]> out = format.createOutput(String[].class, "dummy", output)) {
            dump(out, data);
        }
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        try (InputStream in = new GZIPInputStream(new ByteArrayInputStream(output.toByteArray()))) {
            IOUtils.copy(in, buf);
        }
        assertThat(deserialize(buf.toByteArray()), is(data));
    }

    private String[][] collect(int columns, ModelInput<String[]> input) throws IOException {
        List<String[]> results = new ArrayList<>();
        while (true) {
            String[] row = new String[columns];
            if (input.readTo(row)) {
                results.add(row);
            } else {
                break;
            }
        }
        return results.toArray(new String[results.size()][]);
    }

    private void dump(ModelOutput<String[]> output, String[][] fields) throws IOException {
        for (String[] row : fields) {
            output.write(row);
        }
    }

    private MockFormat format(int columns) {
        return format(columns, null);
    }

    private MockFormat format(int columns, HeaderType headerType) {
        return format(headerType, IntStream.range(0, columns)
                .mapToObj(i -> "p" + i)
                .toArray(String[]::new));
    }

    private MockFormat format(HeaderType headerType, String... header) {
        RecordDefinition.Builder<String[]> builder = RecordDefinition.builder(String[].class);
        if (headerType != null) {
            builder.withHeaderType(headerType);
        }
        for (int i = 0; i < header.length; i++) {
            FieldDefinition<String[]> field = FieldDefinition.builder(header[i], MockFieldAdapter.supplier(i))
                    .build();
            builder.withField(UnaryOperator.identity(), field);
        }
        MockFormat format = new MockFormat(builder.build());
        format.setConf(new Configuration());
        return format;
    }

    static ByteArrayInputStream input(String[][] input) {
        return new ByteArrayInputStream(serialize(input));
    }

    static byte[] serialize(String[][] fields) {
        return Arrays.stream(fields)
                .map(ss -> String.join(":", ss))
                .collect(Collectors.joining("\n"))
                .getBytes(StandardCharsets.UTF_8);
    }

    static String[][] deserialize(byte[] data) {
        String file = new String(data, StandardCharsets.UTF_8);
        return Arrays.stream(file.split("\n"))
                .map(s -> s.split(":"))
                .toArray(String[][]::new);
    }

    private static class MockFormat extends AbstractTextStreamFormat<String[]> {

        private final RecordDefinition<String[]> definition;

        private Class<? extends CompressionCodec> codecClass;

        private InputSplitter inputSplitter;

        MockFormat(RecordDefinition<String[]> definition) {
            this.definition = definition;
        }

        MockFormat withCodecClass(Class<? extends CompressionCodec> aClass) {
            this.codecClass = aClass;
            return this;
        }

        MockFormat withInputSplitter(InputSplitter splitter) {
            this.inputSplitter = splitter;
            return this;
        }

        @Override
        public Class<String[]> getSupportedType() {
            return String[].class;
        }

        @Override
        protected TextFormat createTextFormat() {
            return new TextFormat() {
                @Override
                public FieldReader open(InputStream input) throws IOException {
                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                    IOUtils.copy(input, buffer);
                    return new MockFieldReader(deserialize(buffer.toByteArray())) {
                        @Override
                        public void close() throws IOException {
                            input.close();
                        }
                    };
                }
                @Override
                public FieldWriter open(OutputStream output) throws IOException {
                    return new MockFieldWriter() {
                        @Override
                        public void close() throws IOException {
                            try {
                                output.write(serialize(get()));
                            } catch (IOException e) {
                                throw new AssertionError(e);
                            } finally {
                                output.close();
                            }
                        }
                    };
                }
                @Override
                public FieldReader open(Reader input) throws IOException {
                    throw new UnsupportedOperationException();
                }
                @Override
                public FieldWriter open(Writer output) throws IOException {
                    throw new UnsupportedOperationException();
                }
            };
        }

        @Override
        protected RecordDefinition<String[]> createRecordDefinition() {
            return definition;
        }

        @Override
        protected Class<? extends CompressionCodec> getCompressionCodecClass() {
            return codecClass == null ? super.getCompressionCodecClass() : codecClass;
        }

        @Override
        protected InputSplitter getInputSplitter() {
            return inputSplitter == null ? super.getInputSplitter() : inputSplitter;
        }
    }
}
