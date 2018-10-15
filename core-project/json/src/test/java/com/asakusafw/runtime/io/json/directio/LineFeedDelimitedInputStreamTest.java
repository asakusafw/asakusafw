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
package com.asakusafw.runtime.io.json.directio;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

/**
 * Test for {@link LineFeedDelimitedInputStream}.
 */
public class LineFeedDelimitedInputStreamTest {

    /**
     * simple case.
     */
    @Test
    public void simple() {
        String result = process("Hello, world!", true);
        assertThat(result, is("Hello, world!"));
    }

    /**
     * skip leading.
     */
    @Test
    public void skip_lead() {
        String result = process("__INVALID__\nHello, world!", false);
        assertThat(result, is("Hello, world!"));
    }

    /**
     * skip trail.
     */
    @Test
    public void skip_trail() {
        String result = process("Hello|, world!\n__INVALID__", true);
        assertThat(result, is("Hello, world!\n"));
    }

    /**
     * skip lead/trail.
     */
    @Test
    public void skip_around() {
        String result = process("__INVALID__\nHello|, world!\n__INVALID__", false);
        assertThat(result, is("Hello, world!\n"));
    }

    /**
     * skip lead/trail.
     */
    @Test
    public void skip_single() {
        String result = process("__INVALID__|", false);
        assertThat(result, is(""));
    }

    /**
     * skip lead/trail.
     */
    @Test
    public void skip_out() {
        String result = process("__INVALID__|\n__INVALID__\n__INVALID__", false);
        assertThat(result, is(""));
    }

    /**
     * skip lead/trail.
     */
    @Test
    public void skip_rest_head() {
        String result = process("__INVALID__\n|Hello, world!\n__INVALID__", false);
        assertThat(result, is("Hello, world!\n"));
    }

    /**
     * take multiple lines.
     */
    @Test
    public void multiple_lines() {
        String result = process("xxx\nAAA\nBBB\nCCC\nDDD\nE|EE\nyyy", false);
        assertThat(result, is("AAA\nBBB\nCCC\nDDD\nEEE\n"));
    }

    /**
     * take multiple lines.
     */
    @Test
    public void split_after_eol() {
        String result = process("xxx\nAAA\nBBB\nCCC\nDDD\n|EEE\nyyy", false);
        assertThat(result, is("AAA\nBBB\nCCC\nDDD\nEEE\n"));
    }

    /**
     * take multiple lines.
     */
    @Test
    public void split_before_eol() {
        String result = process("xxx\nAAA\nBBB\nCCC\nDDD\nEEE|\nyyy", false);
        assertThat(result, is("AAA\nBBB\nCCC\nDDD\nEEE\n"));
    }

    /**
     * w/ long record.
     */
    @Test
    public void long_record() {
        String result = process("A012345789012345789\n|B012345789012345789\nC012345789012345789\n", false);
        assertThat(result, is("B012345789012345789\n"));
    }

    private String process(String text, boolean head) {
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            long length;
            int split = text.indexOf('|');
            if (split < 0) {
                output.write(text.getBytes(StandardCharsets.UTF_8));
                length = Long.MAX_VALUE;
            } else {
                byte[] first = text.substring(0, split).getBytes(StandardCharsets.UTF_8);
                byte[] last = text.substring(split + 1).getBytes(StandardCharsets.UTF_8);
                output.write(first);
                output.write(last);
                length = first.length;
            }
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] bytes = output.toByteArray();
            try (InputStream in = wrap(bytes, head, length)) {
                byte[] buf = new byte[10];
                while (true) {
                    int read = in.read(buf);
                    if (read < 0) {
                        break;
                    }
                    buffer.write(buf, 0, read);
                }
            }
            return new String(buffer.toByteArray(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    private LineFeedDelimitedInputStream wrap(byte[] bytes, boolean head, long length) {
        return new LineFeedDelimitedInputStream(new ByteArrayInputStream(bytes), head ? 0 : 1, length);
    }
}
