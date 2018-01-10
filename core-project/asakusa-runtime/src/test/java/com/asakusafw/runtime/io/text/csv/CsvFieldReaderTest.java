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
package com.asakusafw.runtime.io.text.csv;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

import org.junit.Test;

import com.asakusafw.runtime.io.text.FieldReader;
import com.asakusafw.runtime.io.text.TextFormatException;

/**
 * Test for {@link CsvFieldReader}.
 */
public class CsvFieldReaderTest {

    private boolean allowLineFeed = true;

    /**
     * simple case.
     */
    @Test
    public void simple() {
        String[][] result = read("this is a test");
        assertThat(result, is(new String[][] {
            { "this is a test" },
        }));
    }

    /**
     * w/ field separator.
     */
    @Test
    public void field_separator() {
        String[][] result = read("this,is,a,test");
        assertThat(result, is(new String[][] {
            { "this", "is", "a", "test" },
        }));
    }

    /**
     * w/ record separator.
     */
    @Test
    public void record_separator() {
        String[][] result = read("this\nis\na\ntest\r\n");
        assertThat(result, is(new String[][] {
            { "this" },
            { "is" },
            { "a" },
            { "test" },
        }));
    }

    /**
     * w/ quoted field.
     */
    @Test
    public void quote() {
        String[][] result = read("'this,is','a''test\n'");
        assertThat(result, is(new String[][] {
            { "this,is", "a'test\n" },
        }));
    }

    /**
     * begin_field - c.
     */
    @Test
    public void begin_field_c() {
        String[][] result = read("c");
        assertThat(result, is(new String[][] {
            { "c" },
        }));
    }

    /**
     * begin_field - quote.
     */
    @Test
    public void begin_field_quote() {
        String[][] result = read("''");
        assertThat(result, is(new String[][] {
            { "" },
        }));
    }

    /**
     * begin_field - field_separator.
     */
    @Test
    public void begin_field_field_separator() {
        String[][] result = read(",");
        assertThat(result, is(new String[][] {
            { "", "" },
        }));
    }

    /**
     * begin_field - EOF.
     */
    @Test
    public void begin_field_eof() {
        String[][] result = read("");
        assertThat(result, is(new String[0][]));
    }

    /**
     * bare_body - c.
     */
    @Test
    public void bare_body_c() {
        String[][] result = read("cc");
        assertThat(result, is(new String[][] {
            { "cc" },
        }));
    }

    /**
     * bare_body - quote.
     */
    @Test
    public void bare_body_quote() {
        String[][] result = read("c'");
        assertThat(result, is(new String[][] {
            { "c'" },
        }));
    }

    /**
     * bare_body - field_separator.
     */
    @Test
    public void bare_body_field_separator() {
        String[][] result = read("c,");
        assertThat(result, is(new String[][] {
            { "c", "" },
        }));
    }

    /**
     * bare_body - EOF.
     */
    @Test
    public void bare_body_eof() {
        String[][] result = read("c");
        assertThat(result, is(new String[][] {
            { "c" },
        }));
    }

    /**
     * quote_body - c.
     */
    @Test
    public void quote_body_c() {
        String[][] result = read("'c'");
        assertThat(result, is(new String[][] {
            { "c" },
        }));
    }

    /**
     * quote_body - quote.
     */
    @Test
    public void quote_body_quote() {
        String[][] result = read("''''");
        assertThat(result, is(new String[][] {
            { "'" },
        }));
    }

    /**
     * quote_body - field_separator.
     */
    @Test
    public void quote_body_field_separator() {
        String[][] result = read("','");
        assertThat(result, is(new String[][] {
            { "," },
        }));
    }

    /**
     * quote_body - EOF.
     */
    @Test
    public void quote_body_eof() {
        String[][] result = read("'");
        assertThat(result, is(new String[][] {
            { "" },
        }));
    }

    /**
     * quote_body - LF.
     */
    @Test
    public void quote_body_lf_allow() {
        String[][] result = read("'\n'");
        assertThat(result, is(new String[][] {
            { "\n" },
        }));
    }

    /**
     * quote_body - LF.
     */
    @Test(expected = TextFormatException.class)
    public void quote_body_lf_deny() {
        allowLineFeed = false;
        read("'\n'");
    }

    /**
     * quote_body_saw_quote - c.
     */
    @Test
    public void quote_body_saw_quote_c() {
        String[][] result = read("'c'c");
        assertThat(result, is(new String[][] {
            { "c'c" },
        }));
    }

    /**
     * quote_body_saw_quote - quote.
     */
    @Test
    public void quote_body_saw_quote_quote() {
        String[][] result = read("'c'''");
        assertThat(result, is(new String[][] {
            { "c'" },
        }));
    }

    /**
     * quote_body_saw_quote - field_separator.
     */
    @Test
    public void quote_body_saw_quote_field_separator() {
        String[][] result = read("'c',");
        assertThat(result, is(new String[][] {
            { "c", "" },
        }));
    }

    /**
     * quote_body_saw_quote - EOF.
     */
    @Test
    public void quote_body_saw_quote_eof() {
        String[][] result = read("'c'");
        assertThat(result, is(new String[][] {
            { "c" },
        }));
    }

    /**
     * transform w/ filter.
     */
    @Test
    public void filter() {
        String[][] result = read(s -> s.toString().equals("B") ? null : s, "A\nB\nC\n");
        assertThat(result, is(new String[][] {
            { "A" },
            { "C" },
        }));
    }

    /**
     * transform w/ edit.
     */
    @Test
    public void transform() {
        String[][] result = read(s -> s + "!", "A\nB\nC\n");
        assertThat(result, is(new String[][] {
            { "A!" },
            { "B!" },
            { "C!" },
        }));
    }

    private String[][] read(String... lines) {
        return read(null, lines);
    }

    private String[][] read(UnaryOperator<CharSequence> transformer, String... lines) {
        try (FieldReader reader = reader(transformer, lines)) {
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

    private FieldReader reader(UnaryOperator<CharSequence> transformer, String... lines) {
        return new CsvFieldReader(
                new StringReader(String.join("\n", lines)), ',', '\'',
                allowLineFeed, transformer);
    }
}
