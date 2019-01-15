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
package com.asakusafw.runtime.io.text.csv;

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

    private final boolean allowLineFeed = true;

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
     * w/ quote.
     */
    @Test
    public void quote() {
        List<String> results = parse("'Hello, world!'\n");
        assertThat(results, contains("'Hello, world!'"));
    }

    /**
     * w/ field separator.
     */
    @Test
    public void comma() {
        List<String> results = parse("Hello, world!\n");
        assertThat(results, contains("Hello, world!"));
    }

    /**
     * invalid quote.
     */
    @Test
    public void invalid_quote() {
        List<String> results = parse("Hello' world!\n");
        assertThat(results, contains("Hello' world!"));
    }

    /**
     * empty file.
     */
    @Test
    public void begin_eof() {
        List<String> results = parse("");
        assertThat(results, hasSize(0));
    }

    /**
     * field separator follows EOF.
     */
    @Test
    public void comma_eof() {
        List<String> results = parse(",");
        assertThat(results, contains(","));
    }

    /**
     * empty file.
     */
    @Test
    public void begin_lf() {
        List<String> results = parse("\n");
        assertThat(results, contains(""));
    }

    /**
     * quote w/ quote.
     */
    @Test
    public void quoted_quote() {
        List<String> results = parse("'Hello'' world!'\n");
        assertThat(results, contains("'Hello'' world!'"));
    }

    /**
     * quote w/ field separator.
     */
    @Test
    public void quoted_comma() {
        List<String> results = parse("',',','\n");
        assertThat(results, contains("',',','"));
    }

    /**
     * quoted field ends with EOF.
     */
    @Test
    public void quoted_quote_eof() {
        List<String> results = parse("'Hello, world!'");
        assertThat(results, contains("'Hello, world!'"));
    }

    /**
     * quote w/ LF.
     */
    @Test
    public void quoted_lf() {
        List<String> results = parse("'\n'\n");
        assertThat(results, contains("'\n'"));
    }

    /**
     * quote w/ EOF.
     */
    @Test
    public void quoted_eof() {
        List<String> results = parse("'");
        assertThat(results, contains("'"));
    }

    /**
     * quote w/ CRLF.
     */
    @Test
    public void quoted_cr_lf() {
        List<String> results = parse("'\r\n'\n");
        assertThat(results, contains("'\r\n'"));
    }

    /**
     * quote w/ CR - c.
     */
    @Test
    public void quoted_cr_c() {
        List<String> results = parse("'\rc'\n");
        assertThat(results, contains("'\rc'"));
    }

    /**
     * quote w/ CR - quote.
     */
    @Test
    public void quoted_cr_quote() {
        List<String> results = parse("'\r'\n");
        assertThat(results, contains("'\r'"));
    }

    /**
     * quote w/ CR - CR.
     */
    @Test
    public void quoted_cr_cr() {
        List<String> results = parse("'\r\r'\n");
        assertThat(results, contains("'\r\r'"));
    }

    /**
     * quote w/ CR - EOF.
     */
    @Test
    public void quoted_cr_eof() {
        List<String> results = parse("'\r");
        assertThat(results, contains("'\r"));
    }

    /**
     * quote w/ quote - c.
     */
    @Test
    public void quoted_quote_c() {
        List<String> results = parse("'Hello' world!'\n");
        assertThat(results, contains("'Hello' world!'"));
    }

    /**
     * quoted field ends with CRLF.
     */
    @Test
    public void quoted_field_cr_lf() {
        List<String> results = parse("'Hello, world!'\r\n");
        assertThat(results, contains("'Hello, world!'"));
    }

    private List<String> parse(String contents) {
        return parse('\'', ',', contents);
    }

    private List<String> parse(char quote, char field, String contents) {
        try (LineCursor cursor = new LineCursor(new StringReader(contents), quote, field, allowLineFeed)) {
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
