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
package com.asakusafw.runtime.io.json.directio;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.GzipCodec;
import org.junit.Test;

import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.runtime.io.json.JsonFormat.Builder;
import com.asakusafw.runtime.io.json.PropertyAdapter;
import com.asakusafw.runtime.io.json.PropertyDefinition;
import com.asakusafw.runtime.io.json.ValueReader;
import com.asakusafw.runtime.io.json.ValueWriter;
import com.asakusafw.runtime.io.util.InputSplitter;
import com.asakusafw.runtime.io.util.InputSplitters;

/**
 * Test for {@link AbstractJsonStreamFormat}.
 */
public class AbstractJsonStreamFormatTest {

    /**
     * input - simple.
     * @throws Exception if failed
     */
    @Test
    public void input() throws Exception {
        MockFormat format = format(1);
        assertThat(format.getPreferredFragmentSize(), is(-1L));
        assertThat(format.getMinimumFragmentSize(), is(-1L));

        int[][] data = {
                { 100 }
        };
        try (ModelInput<int[]> in = format.createInput(int[].class, "dummy", input(data))) {
            int[][] result = collect(1, in);
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
        int[][] data = {
                { 11, 12, 13, },
                { 22, 22, 23, },
                { 33, 32, 33, },
        };
        try (ModelInput<int[]> in = format.createInput(int[].class, "dummy", input(data))) {
            int[][] result = collect(3, in);
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

        int[][] data = {
                { 11, 12, 13, },
                { 22, 22, 23, },
                { 33, 32, 33, },
        };
        try (ModelInput<int[]> in = format.createInput(int[].class, "dummy", input(data))) {
            int[][] result = collect(3, in);
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
        int[][] data = {
                { 11, 12, 13, },
                { 22, 22, 23, },
                { 33, 32, 33, },
        };
        try (ModelInput<int[]> in = format.createInput(int[].class, "dummy", input(data), 1, Long.MAX_VALUE)) {
            int[][] result = collect(3, in);
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
        int[][] data = {
                { 11, 12, 13, },
                { 22, 22, 23, },
                { 33, 32, 33, },
        };
        int objsz = 2 + (4 + 2) * 3 + 1;
        try (ModelInput<int[]> in = format.createInput(int[].class, "dummy", input(data), 0, objsz * 2 - 1)) {
            int[][] result = collect(3, in);
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
        int[][] data = {
                { 11, 12, 13, },
                { 22, 22, 23, },
                { 33, 32, 33, },
        };
        int objsz = 2 + (4 + 2) * 3 + 1;
        try (ModelInput<int[]> in = format.createInput(int[].class, "dummy", input(data), 1, objsz * 2 - 1)) {
            int[][] result = collect(3, in);
            assertThat(result, is(Arrays.copyOfRange(data, 1, 2)));
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
        int[][] data = {
                { 100 }
        };
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        try (InputStream in = input(data); OutputStream out = new GZIPOutputStream(buf)) {
            IOUtils.copy(in, out);
        }
        try (ModelInput<int[]> in = format.createInput(int[].class, "dummy", new ByteArrayInputStream(buf.toByteArray()))) {
            int[][] result = collect(1, in);
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
        int[][] data = {
                { 100 }
        };
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (ModelOutput<int[]> out = format.createOutput(int[].class, "dummy", output)) {
            dump(out, data);
        }
        assertThat(deserialize(output.toByteArray()), is(data));
    }

    /**
     * output - w/ compression.
     * @throws Exception if failed
     */
    @Test
    public void output_compression() throws Exception {
        MockFormat format = format(1)
                .withCodecClass(GzipCodec.class);
        int[][] data = {
                { 100 }
        };
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (ModelOutput<int[]> out = format.createOutput(int[].class, "dummy", output)) {
            dump(out, data);
        }
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        try (InputStream in = new GZIPInputStream(new ByteArrayInputStream(output.toByteArray()))) {
            IOUtils.copy(in, buf);
        }
        assertThat(deserialize(buf.toByteArray()), is(data));
    }

    private int[][] collect(int columns, ModelInput<int[]> input) throws IOException {
        List<int[]> results = new ArrayList<>();
        while (true) {
            int[] row = new int[columns];
            if (input.readTo(row)) {
                results.add(row);
            } else {
                break;
            }
        }
        return results.toArray(new int[results.size()][]);
    }

    private void dump(ModelOutput<int[]> output, int[][] fields) throws IOException {
        for (int[] row : fields) {
            output.write(row);
        }
    }

    private MockFormat format(int columns) {
        return new MockFormat(columns);
    }

    static ByteArrayInputStream input(int[][] input) {
        StringBuilder buffer = new StringBuilder();
        for (int[] row : input) {
            buffer.append("{");
            for (int col = 0; col < row.length; col++) {
                if (col != 0) {
                    buffer.append(",");
                }
                buffer.append(String.format("'%d':%d", col, row[col]));
            }
            buffer.append("}\n");
        }
        return new ByteArrayInputStream(buffer.toString().getBytes(StandardCharsets.UTF_8));
    }

    static int[][] deserialize(byte[] data) {
        String file = new String(data, StandardCharsets.UTF_8);
        Pattern object = Pattern.compile("\\{(.*?)\\}");
        Pattern field = Pattern.compile("\"(\\d+)\"\\s*:\\s*(\\d+),?");

        Matcher omatch = object.matcher(file);
        int ostart = 0;
        List<int[]> results = new ArrayList<>();
        while (omatch.find(ostart)) {
            Matcher fmatch = field.matcher(omatch.group(1));
            int fstart = 0;
            int[] buf = new int[256];
            int sz = 0;
            while (fmatch.find(fstart)) {
                int key = Integer.parseInt(fmatch.group(1));
                int value = Integer.parseInt(fmatch.group(2));
                buf[key] = value;
                sz = Math.max(sz, key + 1);
                fstart = fmatch.end();
            }
            results.add(Arrays.copyOfRange(buf, 0, sz));
            ostart = omatch.end();
        }
        return results.stream().toArray(int[][]::new);
    }

    private static class MockAdapter implements PropertyAdapter<int[]> {

        private final int index;

        MockAdapter(int index) {
            this.index = index;
        }

        @Override
        public void absent(int[] property) {
            throw new IllegalStateException();
        }

        @Override
        public void read(ValueReader reader, int[] property) throws IOException {
            property[index] = reader.readInt();
        }

        @Override
        public void write(int[] property, ValueWriter writer) throws IOException {
            writer.writeInt(property[index]);
        }
    }

    private static class MockFormat extends AbstractJsonStreamFormat<int[]> {

        private final int columns;

        private Class<? extends CompressionCodec> codecClass;

        private InputSplitter inputSplitter;

        MockFormat(int columns) {
            this.columns = columns;
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
        public Class<int[]> getSupportedType() {
            return int[].class;
        }

        @Override
        protected void configureJsonFormat(Builder<int[]> builder) {
            for (int i = 0, n = columns; i < n; i++) {
                String name = String.valueOf(i);
                MockAdapter adapter = new MockAdapter(i);
                builder.withProperty(it -> it, PropertyDefinition.builder(name, () -> adapter).build());
            }
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
