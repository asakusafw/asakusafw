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
package com.asakusafw.runtime.io.text.tabular;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/**
 * Test for {@code LineCursor}.
 */
public class LineCursorTest {

    /**
     * simple case.
     */
    @Test
    public void simple() {
        List<String> results = parse("Hello, world!");
        assertThat(results, contains("Hello, world!"));
    }

    /**
     * w/ LF.
     */
    @Test
    public void lf() {
        List<String> results = parse("hello\nworld\n\n!\n");
        assertThat(results, contains("hello", "world", "", "!"));
    }

    /**
     * w/ CR.
     */
    @Test
    public void cr() {
        List<String> results = parse("hello\rworld\r\r!\r");
        assertThat(results, contains("hello", "world", "", "!"));
    }

    /**
     * w/ CRLF.
     */
    @Test
    public void crlf() {
        List<String> results = parse("hello\r\nworld\r\n\r\n!\r\n");
        assertThat(results, contains("hello", "world", "", "!"));
    }

    /**
     * w/ escape.
     */
    @Test
    public void escape() {
        List<String> results = parse('\\', false, "Hello\\, world!");
        assertThat(results, contains("Hello\\, world!"));
    }

    /**
     * w/ escape + LF.
     */
    @Test
    public void escape_lf() {
        List<String> results = parse('\\', false, "Hello\\\nworld");
        assertThat(results, contains("Hello\\\nworld"));
    }

    /**
     * w/ escape + CR.
     */
    @Test
    public void escape_cr() {
        List<String> results = parse('\\', false, "Hello\\\rworld");
        assertThat(results, contains("Hello\\\rworld"));
    }

    /**
     * w/ escape + CRLF.
     */
    @Test
    public void escape_crlf() {
        List<String> results = parse('\\', false, "Hello\\\r\nworld");
        assertThat(results, contains("Hello\\\r\nworld"));
    }

    /**
     * w/ escape, escape, LF w/o meta-escape.
     */
    @Test
    public void escape_escape_lf() {
        List<String> results = parse('\\', false, "Hello\\\\\nworld");
        assertThat(results, contains("Hello\\\\\nworld"));
    }

    /**
     * w/ escape, escape, CR w/o meta-escape.
     */
    @Test
    public void escape_escape_cr() {
        List<String> results = parse('\\', false, "Hello\\\\\rworld");
        assertThat(results, contains("Hello\\\\\rworld"));
    }

    /**
     * w/ escape, escape, CRLF w/o meta-escape.
     */
    @Test
    public void escape_escape_crlf() {
        List<String> results = parse('\\', false, "Hello\\\\\r\nworld");
        assertThat(results, contains("Hello\\\\\r\nworld"));
    }

    /**
     * w/ escape, escape, LF w/ meta-escape.
     */
    @Test
    public void escape_escape_lf_metaescape() {
        List<String> results = parse('\\', true, "Hello\\\\\nworld");
        assertThat(results, contains("Hello\\\\", "world"));
    }

    /**
     * w/ escape, escape, CR w/ meta-escape.
     */
    @Test
    public void escape_escape_cr_metaescape() {
        List<String> results = parse('\\', true, "Hello\\\\\rworld");
        assertThat(results, contains("Hello\\\\", "world"));
    }

    /**
     * w/ escape, escape, CRLF w/ meta-escape.
     */
    @Test
    public void escape_escape_crlf_metaescape() {
        List<String> results = parse('\\', true, "Hello\\\\\r\nworld");
        assertThat(results, contains("Hello\\\\", "world"));
    }

    /**
     * w/ escape, EOF.
     */
    @Test
    public void escape_eof() {
        List<String> results = parse('\\', false, "Hello\\");
        assertThat(results, contains("Hello\\"));
    }

    /**
     * w/ escape, CR, EOF.
     */
    @Test
    public void escape_cr_eof() {
        List<String> results = parse('\\', false, "Hello\\\r");
        assertThat(results, contains("Hello\\\r"));
    }

    /**
     * w/ escape, CR, escape, LF.
     */
    @Test
    public void escape_cr_escape_lf() {
        List<String> results = parse('\\', false, "Hello\\\r\\\nworld");
        assertThat(results, contains("Hello\\\r\\\nworld"));
    }

    /**
     * w/ escape, CR, CR.
     */
    @Test
    public void escape_cr_cr() {
        List<String> results = parse('\\', false, "Hello\\\r\rworld");
        assertThat(results, contains("Hello\\\r", "world"));
    }

    /**
     * w/ spilling.
     */
    @Test
    public void spill() {
        int chunkSize = (int) (LineCursor.READ_BUFFER_SIZE * 1.3);
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < chunkSize; i++) {
            buf.append((char) ('0' + i % 10));
        }
        String chunk = buf.toString();
        List<String> results = parse(new StringBuilder()
                .append(chunk)
                .append("\n")
                .append(chunk)
                .append("\n")
                .append(chunk)
                .append("\n")
                .toString());
        assertThat(results, contains(chunk, chunk, chunk));
    }

    /**
     * w/ spilling.
     * {@code ... <spill> \n ! }.
     */
    @Test
    public void spill_align_lf() {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < LineCursor.READ_BUFFER_SIZE; i++) {
            buf.append((char) ('0' + i % 10));
        }
        String chunk = buf.toString();
        List<String> results = parse(chunk + "\n!");
        assertThat(results, contains(chunk, "!"));
    }

    /**
     * w/ spilling.
     * {@code ... \r <spill> \r ! }.
     */
    @Test
    public void spill_cross_crlf() {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < LineCursor.READ_BUFFER_SIZE - 1; i++) {
            buf.append((char) ('0' + i % 10));
        }
        String chunk = buf.toString();
        List<String> results = parse(chunk + "\r\n!");
        assertThat(results, contains(chunk, "!"));
    }

    /**
     * w/ line numbers.
     * @throws Exception if failed
     */
    @Test
    public void line_number() throws Exception {
        String contents = new StringBuilder()
                .append("0\n")
                .append("1\r\n")
                .append("2\r")
                .append("3\\\n4\n")
                .append("5\\\r\n6\n")
                .append("7\\\r8\n")
                .append("9\r\r11\n")
                .append("12\\\r\r14\n")
                .toString();
        try (LineCursor cursor = new LineCursor(new StringReader(contents), '\\', true, true)) {
            assertThat(cursor.next(), is(true));
            assertThat(cursor.getLineNumber(), is(0L));
            assertThat(cursor.getContent().toString(), is("0"));

            assertThat(cursor.next(), is(true));
            assertThat(cursor.getLineNumber(), is(1L));
            assertThat(cursor.getContent().toString(), is("1"));

            assertThat(cursor.next(), is(true));
            assertThat(cursor.getLineNumber(), is(2L));
            assertThat(cursor.getContent().toString(), is("2"));

            assertThat(cursor.next(), is(true));
            assertThat(cursor.getLineNumber(), is(3L));
            assertThat(cursor.getContent().toString(), is("3\\\n4"));

            assertThat(cursor.next(), is(true));
            assertThat(cursor.getLineNumber(), is(5L));
            assertThat(cursor.getContent().toString(), is("5\\\r\n6"));

            assertThat(cursor.next(), is(true));
            assertThat(cursor.getLineNumber(), is(7L));
            assertThat(cursor.getContent().toString(), is("7\\\r8"));

            assertThat(cursor.next(), is(true));
            assertThat(cursor.getLineNumber(), is(9L));
            assertThat(cursor.getContent().toString(), is("9"));

            assertThat(cursor.next(), is(true));
            assertThat(cursor.getLineNumber(), is(10L));
            assertThat(cursor.getContent().toString(), is(""));

            assertThat(cursor.next(), is(true));
            assertThat(cursor.getLineNumber(), is(11L));
            assertThat(cursor.getContent().toString(), is("11"));

            assertThat(cursor.next(), is(true));
            assertThat(cursor.getLineNumber(), is(12L));
            assertThat(cursor.getContent().toString(), is("12\\\r"));

            assertThat(cursor.next(), is(true));
            assertThat(cursor.getLineNumber(), is(14L));
            assertThat(cursor.getContent().toString(), is("14"));

            assertThat(cursor.next(), is(false));
            assertThat(cursor.getLineNumber(), is(lessThan(0L)));
        }
    }

    private static List<String> parse(String contents) {
        return parse((char) 0, false, false, contents);
    }

    private static List<String> parse(char meta, boolean escapeMeta, String contents) {
        return parse(meta, true, escapeMeta, contents);
    }

    private static List<String> parse(char meta, boolean escape, boolean escapeMeta, String contents) {
        try (LineCursor cursor = new LineCursor(new StringReader(contents), meta, escape, escapeMeta)) {
            List<String> results = new ArrayList<>();
            while (cursor.next()) {
                results.add(cursor.getContent().toString());
            }
            return results;
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }
}
