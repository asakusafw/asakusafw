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
package com.asakusafw.runtime.io.text.csv;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import org.junit.Test;

import com.asakusafw.runtime.io.text.FieldWriter;
import com.asakusafw.runtime.io.text.LineSeparator;
import com.asakusafw.runtime.io.text.UnmappableOutput.ErrorCode;
import com.asakusafw.runtime.io.text.UnmappableOutputException;
import com.asakusafw.runtime.io.text.driver.BasicFieldOutput;
import com.asakusafw.runtime.io.text.driver.FieldOutput;
import com.asakusafw.runtime.io.text.driver.StandardFieldOutputOption;

/**
 * Test for {@link CsvFieldWriter}.
 */
public class CsvFieldWriterTest {

    private LineSeparator lineSeparator = LineSeparator.UNIX;

    private boolean allowLineFeed = true;

    /**
     * simple case.
     */
    @Test
    public void simple() {
        String result = emit(null, w -> {
            w.putField(wrap("this is a test"));
            w.putEndOfRecord();
        });
        assertThat(result, is("this is a test\n"));
    }

    /**
     * w/ windows style line separator.
     */
    @Test
    public void windows() {
        lineSeparator = LineSeparator.WINDOWS;
        String result = emit(null, w -> {
            w.putField(wrap("this is a test"));
            w.putEndOfRecord();
        });
        assertThat(result, is("this is a test\r\n"));
    }

    /**
     * w/ multiple columns.
     */
    @Test
    public void multiple_columns() {
        String result = emit(null, w -> {
            w.putField(wrap("A"));
            w.putField(wrap("B"));
            w.putField(wrap("C"));
            w.putEndOfRecord();
        });
        assertThat(result, is(lines(new String[][] {
            { "A", "B", "C", },
        })));
    }

    /**
     * w/ multiple rows.
     */
    @Test
    public void multiple_rows() {
        String result = emit(null, w -> {
            w.putField(wrap("A"));
            w.putEndOfRecord();
            w.putField(wrap("B"));
            w.putEndOfRecord();
            w.putField(wrap("C"));
            w.putEndOfRecord();
        });
        assertThat(result, is(lines(new String[][] {
            { "A", },
            { "B", },
            { "C", },
        })));
    }

    /**
     * w/ empty fields.
     */
    @Test
    public void empty_field() {
        String result = emit(null, w -> {
            w.putField(wrap(""));
            w.putField(wrap(""));
            w.putEndOfRecord();
        });
        assertThat(result, is(lines(new String[][] {
            { "", "", },
        })));
    }

    /**
     * w/ empty records.
     */
    @Test
    public void empty_record() {
        String result = emit(null, w -> {
            expectUnmappable(w, ErrorCode.EXTRA_EMPTY_FIELD);
        });
        assertThat(result, is(lines(new String[][] {
            { "", },
        })));
    }

    /**
     * auto quote w/ CR.
     */
    @Test
    public void quote_auto_cr() {
        String result = emit(null, w -> {
            w.putField(wrap("\r"));
            w.putEndOfRecord();
        });
        assertThat(result, is("'\r'\n"));
    }

    /**
     * auto quote w/ LF.
     */
    @Test
    public void quote_auto_lf() {
        String result = emit(null, w -> {
            w.putField(wrap("\n"));
            w.putEndOfRecord();
        });
        assertThat(result, is("'\n'\n"));
    }

    /**
     * auto quote w/ CR LF.
     */
    @Test
    public void quote_auto_crlf() {
        String result = emit(null, w -> {
            w.putField(wrap("\r\n"));
            w.putEndOfRecord();
        });
        assertThat(result, is("'\r\n'\n"));
    }

    /**
     * auto quote w/ CR.
     */
    @Test
    public void quote_auto_field_separator() {
        String result = emit(null, w -> {
            w.putField(wrap(","));
            w.putEndOfRecord();
        });
        assertThat(result, is("','\n"));
    }

    /**
     * auto quote w/ quote character.
     */
    @Test
    public void quote_auto_quote_character() {
        String result = emit(null, w -> {
            w.putField(wrap("'"));
            w.putEndOfRecord();
        });
        assertThat(result, is("''''\n"));
    }

    /**
     * auto quote w/ LF.
     */
    @Test
    public void quote_lf_deny() {
        allowLineFeed = false;
        String result = emit(null, w -> {
            w.putField(wrap("\n"));
            expectUnmappable(w, ErrorCode.RESTRICTED_SEQUENCE);
        });
        assertThat(result, is("'\n'\n"));
    }

    /**
     * auto quote w/ CR LF.
     */
    @Test
    public void quote_crlf_deny() {
        allowLineFeed = false;
        String result = emit(null, w -> {
            w.putField(wrap("\r\n"));
            expectUnmappable(w, ErrorCode.RESTRICTED_SEQUENCE);
        });
        assertThat(result, is("'\r\n'\n"));
    }

    /**
     * as header.
     */
    @Test
    public void header() {
        String result = emit(null, w -> {
            w.putField(wrap("hello").addOption(StandardFieldOutputOption.HEADER));
            w.putEndOfRecord();
        });
        assertThat(result, is("'hello'\n"));
    }

    /**
     * always quote.
     */
    @Test
    public void quote_always() {
        String result = emit(null, w -> {
            w.putField(wrap("hello").addOption(QuoteStyle.ALWAYS));
            w.putEndOfRecord();
        });
        assertThat(result, is("'hello'\n"));
    }

    /**
     * always quote.
     */
    @Test
    public void quote_always_field_separator() {
        String result = emit(null, w -> {
            w.putField(wrap("Hello, world!").addOption(QuoteStyle.ALWAYS));
            w.putEndOfRecord();
        });
        assertThat(result, is("'Hello, world!'\n"));
    }

    /**
     * never quote.
     */
    @Test
    public void quote_never() {
        String result = emit(null, w -> {
            w.putField(wrap("hello").addOption(QuoteStyle.NEVER));
            w.putEndOfRecord();
        });
        assertThat(result, is("hello\n"));
    }

    /**
     * never quote w/ CR.
     */
    @Test
    public void quote_never_cr() {
        String result = emit(null, w -> {
            w.putField(wrap("\r").addOption(QuoteStyle.NEVER));
            expectUnmappable(w, ErrorCode.EXTRA_RECORD_SEPARATOR);
        });
        assertThat(result, is("\r\n"));
    }

    /**
     * never quote w/ LF.
     */
    @Test
    public void quote_never_lf() {
        String result = emit(null, w -> {
            w.putField(wrap("\n").addOption(QuoteStyle.NEVER));
            expectUnmappable(w, ErrorCode.EXTRA_RECORD_SEPARATOR);
        });
        assertThat(result, is("\n\n"));
    }

    /**
     * never quote w/ LF.
     */
    @Test
    public void quote_never_crlf() {
        String result = emit(null, w -> {
            w.putField(wrap("\r\n").addOption(QuoteStyle.NEVER));
            expectUnmappable(w, ErrorCode.EXTRA_RECORD_SEPARATOR);
        });
        assertThat(result, is("\r\n\n"));
    }

    /**
     * never quote w/ field separator.
     */
    @Test
    public void quote_never_field_separator() {
        String result = emit(null, w -> {
            w.putField(wrap(",").addOption(QuoteStyle.NEVER));
            expectUnmappable(w, ErrorCode.EXTRA_FIELD_SEPARATOR);
        });
        assertThat(result, is(",\n"));
    }

    /**
     * never quote w/ quote.
     */
    @Test
    public void quote_never_quote_character() {
        String result = emit(null, w -> {
            w.putField(wrap("'").addOption(QuoteStyle.NEVER));
            expectUnmappable(w, ErrorCode.RESTRICTED_SEQUENCE);
        });
        assertThat(result, is("'\n"));
    }

    /**
     * invalid null.
     */
    @Test
    public void invalid_null() {
        String result = emit(null, w -> {
            w.putField(wrap(null));
            expectUnmappable(w, ErrorCode.UNDEFINED_NULL_SEQUENCE);
        });
        assertThat(result, is("\n"));
    }

    /**
     * w/ filter.
     */
    @Test
    public void filter() {
        String result = emit(s -> "B".contentEquals(s) ? null : s, w -> {
            w.putField(wrap("A"));
            w.putEndOfRecord();
            w.putField(wrap("B"));
            w.putEndOfRecord();
            w.putField(wrap("C"));
            w.putEndOfRecord();
        });
        assertThat(result, is(lines(new String[][] {
            { "A", },
            { "C", },
        })));
    }

    /**
     * w/ transform.
     */
    @Test
    public void transform() {
        String result = emit(s -> s.toString().toLowerCase(), w -> {
            w.putField(wrap("A"));
            w.putField(wrap("B"));
            w.putField(wrap("C"));
            w.putEndOfRecord();
        });
        assertThat(result, is(lines(new String[][] {
            { "a", "b", "c", },
        })));
    }

    private static void expectUnmappable(FieldWriter w, ErrorCode code) throws IOException {
        try {
            w.putEndOfRecord();
            fail("expected: " + code);
        } catch (UnmappableOutputException e) {
            assertThat(e.getEntries(), hasSize(1));
            assertThat(e.getEntries().get(0).getErrorCode(), is(code));
        }
    }

    private static FieldOutput wrap(CharSequence contents) {
        return new BasicFieldOutput().set(contents);
    }

    private String emit(UnaryOperator<CharSequence> transformer, Action action) {
        StringWriter writer = new StringWriter();
        try (FieldWriter w = new CsvFieldWriter(
                writer,
                lineSeparator, ',', '\'',
                allowLineFeed, QuoteStyle.NEEDED, QuoteStyle.ALWAYS, transformer)) {
            action.perform(w);
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        return writer.toString();
    }

    private String lines(String[][] lines) {
        return Arrays.stream(lines)
                .map(fs -> String.join(",", fs))
                .collect(Collectors.joining(
                        lineSeparator.getSequence(),
                        "", lineSeparator.getSequence()));
    }

    @FunctionalInterface
    private interface Action {
        void perform(FieldWriter writer) throws IOException;
    }
}
