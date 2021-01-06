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
package com.asakusafw.runtime.io.tsv;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Random;

import org.apache.hadoop.io.InputBuffer;
import org.junit.Test;

/**
 * Test for {@link DelimiterRangeInputStream}.
 */
public class DelimiterRangeInputStreamTest {

    /**
     * read byte without delimiter.
     * @throws Exception if failed
     */
    @Test
    public void readByte_nodelim() throws Exception {
        InputStream origin = bytes("Hello, world!");
        InputStream testee = new DelimiterRangeInputStream(origin, '|', 100, false);
        assertThat(readBytes(testee), is("Hello, world!"));
    }

    /**
     * read byte with delimiter with enough size.
     * @throws Exception if failed
     */
    @Test
    public void readByte_enough() throws Exception {
        InputStream origin = bytes("Hello|World|!");
        InputStream testee = new DelimiterRangeInputStream(origin, '|', 100, false);
        assertThat(readBytes(testee), is("Hello|World|!"));
    }

    /**
     * read byte with delimiter without enough size.
     * @throws Exception if failed
     */
    @Test
    public void readByte_delimited() throws Exception {
        InputStream origin = bytes("Hello|World|!");
        InputStream testee = new DelimiterRangeInputStream(origin, '|', 1, false);
        assertThat(readBytes(testee), is("Hello|"));
    }

    /**
     * read byte with delimiter without enough size.
     * @throws Exception if failed
     */
    @Test
    public void readByte_just_not_delimited() throws Exception {
        InputStream origin = bytes("Hello|World|!");
        InputStream testee = new DelimiterRangeInputStream(origin, '|', 5, false);
        assertThat(readBytes(testee), is("Hello|"));
    }

    /**
     * read byte with delimiter without enough size.
     * @throws Exception if failed
     */
    @Test
    public void readByte_just_delimited() throws Exception {
        InputStream origin = bytes("Hello|World|!");
        InputStream testee = new DelimiterRangeInputStream(origin, '|', 6, false);
        assertThat(readBytes(testee), is("Hello|World|"));
    }

    /**
     * read byte with skip first and is enough size.
     * @throws Exception if failed
     */
    @Test
    public void readByte_skip() throws Exception {
        InputStream origin = bytes("Hello|World|!");
        InputStream testee = new DelimiterRangeInputStream(origin, '|', 100, true);
        assertThat(readBytes(testee), is("World|!"));
    }

    /**
     * read byte with skip first and is not enough size.
     * @throws Exception if failed
     */
    @Test
    public void readByte_skip_too_small() throws Exception {
        InputStream origin = bytes("Hello|World|!");
        InputStream testee = new DelimiterRangeInputStream(origin, '|', 5, true);
        assertThat(readBytes(testee), is(""));
    }

    /**
     * read byte with skip first and is not enough size.
     * @throws Exception if failed
     */
    @Test
    public void readByte_skip_small() throws Exception {
        InputStream origin = bytes("Hello|World|!");
        InputStream testee = new DelimiterRangeInputStream(origin, '|', 6, true);
        assertThat(readBytes(testee), is("World|"));
    }

    /**
     * read byte with skip first, no delimiters, enough size.
     * @throws Exception if failed
     */
    @Test
    public void readByte_skip_nodelim() throws Exception {
        InputStream origin = bytes("Hello, world!");
        InputStream testee = new DelimiterRangeInputStream(origin, '|', 100, true);
        assertThat(readBytes(testee), is(""));
    }

    /**
     * read byte with skip first, no delimiters, not enough size.
     * @throws Exception if failed
     */
    @Test
    public void readByte_skip_nodelim_small() throws Exception {
        InputStream origin = bytes("Hello, world!");
        InputStream testee = new DelimiterRangeInputStream(origin, '|', 1, true);
        assertThat(readBytes(testee), is(""));
    }

    /**
     * all patterns for read byte.
     * @throws Exception if failed
     */
    @Test
    public void readByte_random() throws Exception {
        byte[] bytes = "ABC|D|EF".getBytes(StandardCharsets.US_ASCII);
        InputBuffer buffer = new InputBuffer();
        buffer.reset(bytes, bytes.length);
        Random random = new Random(12345);
        for (int i = 0; i < 100000; i++) {
            int[] bounds = new int[5];
            for (int j = 0; j < bounds.length; j++) {
                bounds[j] = random.nextInt(bytes.length + 1);
            }
            Arrays.sort(bounds);
            StringBuilder buf = new StringBuilder();
            int start = 0;
            for (int j = 0; j < bounds.length; j++) {
                int end = bounds[j];
                copy(buffer, buf, start, end);
                start = end;
            }
            copy(buffer, buf, start, bytes.length);
            assertThat(Arrays.toString(bounds), buf.toString(), is("ABC|D|EF"));
        }
    }

    /**
     * read byte without delimiter.
     * @throws Exception if failed
     */
    @Test
    public void readArray_nodelim() throws Exception {
        InputStream origin = bytes("Hello, world!");
        InputStream testee = new DelimiterRangeInputStream(origin, '|', 100, false);
        assertThat(readBytes(testee, 4), is("Hello, world!"));
    }

    /**
     * read byte with delimiter with enough size.
     * @throws Exception if failed
     */
    @Test
    public void readArray_enough() throws Exception {
        InputStream origin = bytes("Hello|World|!");
        InputStream testee = new DelimiterRangeInputStream(origin, '|', 100, false);
        assertThat(readBytes(testee, 4), is("Hello|World|!"));
    }

    /**
     * read byte with delimiter without enough size.
     * @throws Exception if failed
     */
    @Test
    public void readArray_delimited() throws Exception {
        InputStream origin = bytes("Hello|World|!");
        InputStream testee = new DelimiterRangeInputStream(origin, '|', 1, false);
        assertThat(readBytes(testee, 4), is("Hello|"));
    }

    /**
     * read byte with delimiter without enough size.
     * @throws Exception if failed
     */
    @Test
    public void readArray_just_not_delimited() throws Exception {
        InputStream origin = bytes("Hello|World|!");
        InputStream testee = new DelimiterRangeInputStream(origin, '|', 5, false);
        assertThat(readBytes(testee, 4), is("Hello|"));
    }

    /**
     * read byte with delimiter without enough size.
     * @throws Exception if failed
     */
    @Test
    public void readArray_just_delimited() throws Exception {
        InputStream origin = bytes("Hello|World|!");
        InputStream testee = new DelimiterRangeInputStream(origin, '|', 6, false);
        assertThat(readBytes(testee, 4), is("Hello|World|"));
    }

    /**
     * read byte with skip first and is enough size.
     * @throws Exception if failed
     */
    @Test
    public void readArray_skip() throws Exception {
        InputStream origin = bytes("Hello|World|!");
        InputStream testee = new DelimiterRangeInputStream(origin, '|', 100, true);
        assertThat(readBytes(testee, 4), is("World|!"));
    }

    /**
     * read byte with skip first and is not enough size.
     * @throws Exception if failed
     */
    @Test
    public void readArray_skip_too_small() throws Exception {
        InputStream origin = bytes("Hello|World|!");
        InputStream testee = new DelimiterRangeInputStream(origin, '|', 5, true);
        assertThat(readBytes(testee, 4), is(""));
    }

    /**
     * read byte with skip first and is not enough size.
     * @throws Exception if failed
     */
    @Test
    public void readArray_skip_small() throws Exception {
        InputStream origin = bytes("Hello|World|!");
        InputStream testee = new DelimiterRangeInputStream(origin, '|', 6, true);
        assertThat(readBytes(testee, 4), is("World|"));
    }

    /**
     * read byte with skip first, no delimiters, enough size.
     * @throws Exception if failed
     */
    @Test
    public void readArray_skip_nodelim() throws Exception {
        InputStream origin = bytes("Hello, world!");
        InputStream testee = new DelimiterRangeInputStream(origin, '|', 100, true);
        assertThat(readBytes(testee, 4), is(""));
    }

    /**
     * read byte with skip first, no delimiters, not enough size.
     * @throws Exception if failed
     */
    @Test
    public void readArray_skip_nodelim_small() throws Exception {
        InputStream origin = bytes("Hello, world!");
        InputStream testee = new DelimiterRangeInputStream(origin, '|', 1, true);
        assertThat(readBytes(testee, 4), is(""));
    }

    /**
     * all patterns for read byte.
     * @throws Exception if failed
     */
    @Test
    public void readArray_random() throws Exception {
        byte[] bytes = "ABC|D|EF".getBytes(StandardCharsets.US_ASCII);
        InputBuffer buffer = new InputBuffer();
        buffer.reset(bytes, bytes.length);
        Random random = new Random(12345);
        for (int i = 0; i < 100000; i++) {
            int[] bounds = new int[5];
            for (int j = 0; j < bounds.length; j++) {
                bounds[j] = random.nextInt(bytes.length + 1);
            }
            Arrays.sort(bounds);
            StringBuilder buf = new StringBuilder();
            int start = 0;
            for (int j = 0; j < bounds.length; j++) {
                int end = bounds[j];
                copy(buffer, buf, start, end, 4);
                start = end;
            }
            copy(buffer, buf, start, bytes.length, 4);
            assertThat(Arrays.toString(bounds), buf.toString(), is("ABC|D|EF"));
        }
    }

    private InputStream bytes(String content) {
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.US_ASCII));
    }

    private String readBytes(InputStream in) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        while (true) {
            int c = in.read();
            if (c < 0) {
                break;
            }
            output.write(c);
        }
        return new String(output.toByteArray(), StandardCharsets.US_ASCII);
    }

    private String readBytes(InputStream in, int size) throws IOException {
        byte[] buf = new byte[size];
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        while (true) {
            int read = in.read(buf);
            if (read < 0) {
                break;
            }
            output.write(buf, 0, read);
        }
        return new String(output.toByteArray(), StandardCharsets.US_ASCII);
    }

    private void copy(InputBuffer source, StringBuilder sink, int start, int end) throws IOException {
        source.reset();
        assertThat(source.skip(start), is((long) start));
        InputStream testee = new DelimiterRangeInputStream(source, '|', end - start, start > 0);
        sink.append(readBytes(testee));
    }

    private void copy(InputBuffer source, StringBuilder sink, int start, int end, int size) throws IOException {
        source.reset();
        assertThat(source.skip(start), is((long) start));
        InputStream testee = new DelimiterRangeInputStream(source, '|', end - start, start > 0);
        sink.append(readBytes(testee, size));
    }
}
