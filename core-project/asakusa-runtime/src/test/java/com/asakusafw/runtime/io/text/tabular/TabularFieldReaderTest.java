/**
 * Copyright 2011-2017 Asakusa Framework Team.
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

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

import org.junit.Test;

/**
 * Test for {@link TabularFieldReader}.
 */
public class TabularFieldReaderTest {

    private EscapeSequence escape = EscapeSequence.builder('\\')
            .addMapping('\\', '\\')
            .addMapping('t', '\t')
            .addMapping('n', '\n')
            .addMapping('r', '\r')
            .addNullMapping('N')
            .build();

    /**
     * simple case.
     */
    @Test
    public void simple() {
        String[][] result = read("Hello, world!");
        assertThat(result, is(new String[][] {
            { "Hello, world!" },
        }));
    }

    /**
     * w/ field separator.
     */
    @Test
    public void separator() {
        String[][] result = read("Hello\tworld\t\t!");
        assertThat(result, is(new String[][] {
            { "Hello", "world", "", "!" },
        }));
    }

    /**
     * w/ field separator.
     */
    @Test
    public void separator_eof() {
        String[][] result = read("Hello\t");
        assertThat(result, is(new String[][] {
            { "Hello", "" },
        }));
    }

    /**
     * w/ empty file.
     */
    @Test
    public void empty_file() {
        String[][] result = read();
        assertThat(result, is(new String[0][0]));
    }

    /**
     * w/o escape settings.
     */
    @Test
    public void no_escape() {
        escape = null;
        String[][] result = read("Hello\tworld\t\\\t!");
        assertThat(result, is(new String[][] {
            { "Hello", "world", "\\", "!" },
        }));
    }

    /**
     * w/ escape character.
     */
    @Test
    public void escape() {
        String[][] result = read("Hello\\\\\\t\\r\\n!");
        assertThat(result, is(new String[][] {
            { "Hello\\\t\r\n!" },
        }));
    }

    /**
     * w/ escape character.
     */
    @Test
    public void escape_eof() {
        String[][] result = read("Hello\\");
        assertThat(result, is(new String[][] {
            { "Hello\\" },
        }));
    }

    /**
     * w/ escape LF.
     */
    @Test
    public void escape_bare_lf() {
        escape = EscapeSequence.builder('^').addLineSeparator().build();
        String[][] result = read("Hello^\nworld!");
        assertThat(result, is(new String[][] {
            { "Hello\nworld!" },
        }));
    }

    /**
     * w/ escape CR-LF.
     */
    @Test
    public void escape_bare_cr_lf() {
        escape = EscapeSequence.builder('^').addLineSeparator().build();
        String[][] result = read("Hello^\r\nworld!");
        assertThat(result, is(new String[][] {
            { "Hello\r\nworld!" },
        }));
    }

    /**
     * w/ escape CR-c.
     */
    @Test
    public void escape_bare_cr_c() {
        escape = EscapeSequence.builder('^').addLineSeparator().build();
        String[][] result = read("Hello^\rworld!");
        assertThat(result, is(new String[][] {
            { "Hello\rworld!" },
        }));
    }

    /**
     * w/ unknown escape character.
     */
    @Test
    public void escape_unknown() {
        escape = EscapeSequence.builder('\\').build();
        String[][] result = read("Hello\\nworld!");
        assertThat(result, is(new String[][] {
            { "Hello\\nworld!" },
        }));
    }

    /**
     * w/ unknown escape character.
     */
    @Test
    public void escape_unknown_escape() {
        escape = EscapeSequence.builder('\\')
                .addMapping('t', '\t')
                .build();
        String[][] result = read("Hello\\\\tworld!");
        assertThat(result, is(new String[][] {
            { "Hello\\\tworld!" },
        }));
    }

    /**
     * w/ unknown escape character.
     */
    @Test
    public void escape_unknown_separator() {
        escape = EscapeSequence.builder('\\').build();
        String[][] result = read("Hello\\\tworld!");
        assertThat(result, is(new String[][] {
            { "Hello\\", "world!" },
        }));
    }

    /**
     * w/ null sequence.
     */
    @Test
    public void escape_null() {
        String[][] result = read("\\N");
        assertThat(result, is(new String[][] {
            { null },
        }));
    }

    /**
     * w/ null sequence.
     */
    @Test
    public void escape_null_leading() {
        String[][] result = read("a\\N");
        assertThat(result, is(new String[][] {
            { "a\\N" },
        }));
    }

    /**
     * w/ null sequence.
     */
    @Test
    public void escape_null_following() {
        String[][] result = read("\\Nb");
        assertThat(result, is(new String[][] {
            { "\\Nb" },
        }));
    }

    /**
     * w/ null sequence.
     */
    @Test
    public void escape_null_around() {
        String[][] result = read("a\\Nb");
        assertThat(result, is(new String[][] {
            { "a\\Nb" },
        }));
    }

    /**
     * w/ null sequence.
     */
    @Test
    public void escape_null_separator() {
        String[][] result = read("\\N\t");
        assertThat(result, is(new String[][] {
            { null, "" },
        }));
    }

    /**
     * w/ null sequence.
     */
    @Test
    public void escape_null_escape() {
        String[][] result = read("\\N\\t");
        assertThat(result, is(new String[][] {
            { "\\N\t" },
        }));
    }

    /**
     * transform.
     */
    @Test
    public void transform() {
        String[][] result = read(s -> s.toString().trim(), "  Hello, world!  ");
        assertThat(result, is(new String[][] {
            { "Hello, world!" },
        }));
    }

    /**
     * transform.
     */
    @Test
    public void transform_filter() {
        String[][] result = read(s -> s.toString().equals("b") ? null : s, new String[] {
                "a",
                "b",
                "c",
        });
        assertThat(result, is(new String[][] {
            { "a" },
            { "c" },
        }));
    }

    /**
     * indices.
     * @throws Exception if failed
     */
    @Test
    public void indices() throws Exception {
        try (TabularFieldReader reader = reader(null, "A\tB\tC")) {
            assertThat(reader.nextRecord(), is(true));

            assertThat(reader.nextField(), is(true));
            assertThat(reader.getRecordLineNumber(), is(0L));
            assertThat(reader.getRecordIndex(), is(0L));
            assertThat(reader.getFieldIndex(), is(0L));

            assertThat(reader.nextField(), is(true));
            assertThat(reader.getRecordLineNumber(), is(0L));
            assertThat(reader.getRecordIndex(), is(0L));
            assertThat(reader.getFieldIndex(), is(1L));

            assertThat(reader.nextField(), is(true));
            assertThat(reader.getRecordLineNumber(), is(0L));
            assertThat(reader.getRecordIndex(), is(0L));
            assertThat(reader.getFieldIndex(), is(2L));

            assertThat(reader.nextField(), is(false));

            assertThat(reader.nextRecord(), is(false));
        }
    }

    /**
     * indices.
     * @throws Exception if failed
     */
    @Test
    public void indices_multiline() throws Exception {
        try (TabularFieldReader reader = reader(null, "A", "B", "C")) {
            assertThat(reader.nextRecord(), is(true));

            assertThat(reader.nextField(), is(true));
            assertThat(reader.getRecordLineNumber(), is(0L));
            assertThat(reader.getRecordIndex(), is(0L));
            assertThat(reader.getFieldIndex(), is(0L));

            assertThat(reader.nextField(), is(false));

            assertThat(reader.nextRecord(), is(true));

            assertThat(reader.nextField(), is(true));
            assertThat(reader.getRecordLineNumber(), is(1L));
            assertThat(reader.getRecordIndex(), is(1L));
            assertThat(reader.getFieldIndex(), is(0L));

            assertThat(reader.nextField(), is(false));

            assertThat(reader.nextRecord(), is(true));

            assertThat(reader.nextField(), is(true));
            assertThat(reader.getRecordLineNumber(), is(2L));
            assertThat(reader.getRecordIndex(), is(2L));
            assertThat(reader.getFieldIndex(), is(0L));

            assertThat(reader.nextField(), is(false));

            assertThat(reader.nextRecord(), is(false));
        }
    }

    /**
     * indices.
     * @throws Exception if failed
     */
    @Test
    public void indices_filter() throws Exception {
        try (TabularFieldReader reader = reader(s -> s.toString().equals("B") ? null : s, "A", "B", "C")) {
            assertThat(reader.nextRecord(), is(true));

            assertThat(reader.nextField(), is(true));
            assertThat(reader.getRecordLineNumber(), is(0L));
            assertThat(reader.getRecordIndex(), is(0L));
            assertThat(reader.getFieldIndex(), is(0L));

            assertThat(reader.nextField(), is(false));

            assertThat(reader.nextRecord(), is(true));

            assertThat(reader.nextField(), is(true));
            assertThat(reader.getRecordLineNumber(), is(2L));
            assertThat(reader.getRecordIndex(), is(1L));
            assertThat(reader.getFieldIndex(), is(0L));

            assertThat(reader.nextField(), is(false));

            assertThat(reader.nextRecord(), is(false));
        }
    }

    private String[][] read(String... lines) {
        return read(null, lines);
    }

    private String[][] read(UnaryOperator<CharSequence> transformer, String... lines) {
        try (TabularFieldReader reader = reader(transformer, lines)) {
            List<List<String>> results = new ArrayList<>();
            while (reader.nextRecord()) {
                List<String> row = new ArrayList<>();
                while (reader.nextField()) {
                    CharSequence content = reader.getContent();
                    row.add(content == null ? null : content.toString());
                }
                results.add(row);
            }
            return results.stream()
                    .map(s -> s.stream().toArray(String[]::new))
                    .toArray(String[][]::new);
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    private TabularFieldReader reader(UnaryOperator<CharSequence> transformer, String... lines) {
        return new TabularFieldReader(
                new StringReader(String.join("\n", lines)), '\t', escape, transformer);
    }
}
