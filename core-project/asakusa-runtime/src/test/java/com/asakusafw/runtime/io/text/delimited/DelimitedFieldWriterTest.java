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
package com.asakusafw.runtime.io.text.delimited;

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

/**
 * Test for {@link DelimitedFieldWriter}.
 */
public class DelimitedFieldWriterTest {

    private LineSeparator lineSeparator = LineSeparator.UNIX;

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
        String result = emit(null, w -> {
            w.putField("Hello, world!");
            w.putEndOfRecord();
        });
        assertThat(result, is("Hello, world!\n"));
    }

    /**
     * w/ windows style line separator.
     */
    @Test
    public void windows() {
        lineSeparator = LineSeparator.WINDOWS;
        String result = emit(null, w -> {
            w.putField("Hello, world!");
            w.putEndOfRecord();
        });
        assertThat(result, is("Hello, world!\r\n"));
    }

    /**
     * w/ multiple columns.
     */
    @Test
    public void multiple_columns() {
        String result = emit(null, w -> {
            w.putField("A");
            w.putField("B");
            w.putField("C");
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
            w.putField("A");
            w.putEndOfRecord();
            w.putField("B");
            w.putEndOfRecord();
            w.putField("C");
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
            w.putField("");
            w.putField("");
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

    private void expectUnmappable(FieldWriter w, ErrorCode code) throws IOException {
        try {
            w.putEndOfRecord();
            fail("expected: " + code);
        } catch (UnmappableOutputException e) {
            assertThat(e.getEntries(), hasSize(1));
            assertThat(e.getEntries().get(0).getErrorCode(), is(code));
        }
    }

    /**
     * escape - escape character.
     */
    @Test
    public void escape_escape() {
        String result = emit(null, w -> {
            w.putField("\\");
            w.putEndOfRecord();
        });
        assertThat(result, is(lines(new String[][] {
            { "\\\\" },
        })));
    }

    /**
     * escape - field separator.
     */
    @Test
    public void escape_field_separator() {
        String result = emit(null, w -> {
            w.putField("\t");
            w.putEndOfRecord();
        });
        assertThat(result, is(lines(new String[][] {
            { "\\t" },
        })));
    }

    /**
     * escape - LF.
     */
    @Test
    public void escape_lf() {
        String result = emit(null, w -> {
            w.putField("\n");
            w.putEndOfRecord();
        });
        assertThat(result, is(lines(new String[][] {
            { "\\n" },
        })));
    }

    /**
     * escape - CRLF.
     */
    @Test
    public void escape_crlf() {
        String result = emit(null, w -> {
            w.putField("\r\n");
            w.putEndOfRecord();
        });
        assertThat(result, is(lines(new String[][] {
            { "\\r\\n" },
        })));
    }

    /**
     * escape - CR.
     */
    @Test
    public void escape_cr() {
        String result = emit(null, w -> {
            w.putField("\r");
            w.putEndOfRecord();
        });
        assertThat(result, is(lines(new String[][] {
            { "\\r" },
        })));
    }

    /**
     * escape - LF.
     */
    @Test
    public void escape_line_separator_lf() {
        escape = EscapeSequence.builder('\\').addLineSeparator().build();
        String result = emit(null, w -> {
            w.putField("\n");
            w.putEndOfRecord();
        });
        assertThat(result, is(lines(new String[][] {
            { "\\\n" },
        })));
    }

    /**
     * escape - CRLF.
     */
    @Test
    public void escape_line_separator_crlf() {
        escape = EscapeSequence.builder('\\').addLineSeparator().build();
        String result = emit(null, w -> {
            w.putField("\r\n");
            w.putEndOfRecord();
        });
        assertThat(result, is(lines(new String[][] {
            { "\\\r\n" },
        })));
    }

    /**
     * escape - CR w/o trailing characters.
     */
    @Test
    public void escape_line_separator_cr_cont() {
        escape = EscapeSequence.builder('\\').addLineSeparator().build();
        String result = emit(null, w -> {
            w.putField("\rC");
            w.putEndOfRecord();
        });
        assertThat(result, is(lines(new String[][] {
            { "\\\rC" },
        })));
    }

    /**
     * escape - CR w/ trailing characters.
     */
    @Test
    public void escape_line_separator_cr_eof() {
        escape = EscapeSequence.builder('\\').addLineSeparator().build();
        String result = emit(null, w -> {
            w.putField("\r");
            w.putEndOfRecord();
        });
        assertThat(result, is(lines(new String[][] {
            { "\\\r" },
        })));
    }

    /**
     * w/ null.
     */
    @Test
    public void null_sequence() {
        String result = emit(null, w -> {
            w.putField(null);
            w.putEndOfRecord();
        });
        assertThat(result, is(lines(new String[][] {
            { "\\N" },
        })));
    }

    /**
     * undefined escape - field separator.
     */
    @Test
    public void undefined_escape_field_separator() {
        escape = EscapeSequence.builder('\\')
                .addNullMapping('N')
                .build();
        String result = emit(null, w -> {
            w.putField("\t");
            expectUnmappable(w, ErrorCode.EXTRA_FIELD_SEPARATOR);
        });
        assertThat(result, is(lines(new String[][] {
            { "\t" },
        })));
    }

    /**
     * undefined escape - record separator LF.
     */
    @Test
    public void undefined_escape_record_separator_lf() {
        escape = EscapeSequence.builder('\\')
                .addNullMapping('N')
                .build();
        String result = emit(null, w -> {
            w.putField("\n");
            expectUnmappable(w, ErrorCode.EXTRA_RECORD_SEPARATOR);
        });
        assertThat(result, is(lines(new String[][] {
            { "\n" },
        })));
    }

    /**
     * undefined escape - record separator CRLF.
     */
    @Test
    public void undefined_escape_record_separator_crlf() {
        escape = EscapeSequence.builder('\\')
                .addNullMapping('N')
                .build();
        String result = emit(null, w -> {
            w.putField("\r\n");
            expectUnmappable(w, ErrorCode.EXTRA_RECORD_SEPARATOR);
        });
        assertThat(result, is(lines(new String[][] {
            { "\r\n" },
        })));
    }

    /**
     * undefined escape - record separator CR w/ trailing characters.
     */
    @Test
    public void undefined_escape_record_separator_cr_c() {
        escape = EscapeSequence.builder('\\')
                .addNullMapping('N')
                .build();
        String result = emit(null, w -> {
            w.putField("\r");
            expectUnmappable(w, ErrorCode.EXTRA_RECORD_SEPARATOR);
        });
        assertThat(result, is(lines(new String[][] {
            { "\r" },
        })));
    }

    /**
     * undefined escape - record separator CR w/o trailing characters.
     */
    @Test
    public void undefined_escape_record_separator_cr_eof() {
        escape = EscapeSequence.builder('\\')
                .addNullMapping('N')
                .build();
        String result = emit(null, w -> {
            w.putField("\r");
            expectUnmappable(w, ErrorCode.EXTRA_RECORD_SEPARATOR);
        });
        assertThat(result, is(lines(new String[][] {
            { "\r" },
        })));
    }

    /**
     * undefined escape - bare escape character follows C.
     */
    @Test
    public void undefined_escape_escape_c() {
        escape = EscapeSequence.builder('\\')
                .addNullMapping('N')
                .build();
        String result = emit(null, w -> {
            w.putField("\\c");
            w.putEndOfRecord();
        });
        assertThat(result, is(lines(new String[][] {
            { "\\c" },
        })));
    }

    /**
     * undefined escape - bare escape character follows encodable character.
     */
    @Test
    public void undefined_escape_escape_encodable() {
        escape = EscapeSequence.builder('\\')
                .addMapping('t', '\t')
                .build();
        String result = emit(null, w -> {
            w.putField("\\\t");
            w.putEndOfRecord();
        });
        assertThat(result, is(lines(new String[][] {
            { "\\\\t" },
        })));
    }

    /**
     * undefined escape - bare escape character follows encodable LF.
     */
    @Test
    public void undefined_escape_escape_encodable_lf() {
        escape = EscapeSequence.builder('\\')
                .addLineSeparator()
                .build();
        String result = emit(null, w -> {
            w.putField("\\\n");
            w.putEndOfRecord();
        });
        assertThat(result, is(lines(new String[][] {
            { "\\\n" },
        })));
    }

    /**
     * undefined escape - bare escape character follows encodable CRLF.
     */
    @Test
    public void undefined_escape_escape_encodable_crlf() {
        escape = EscapeSequence.builder('\\')
                .addLineSeparator()
                .build();
        String result = emit(null, w -> {
            w.putField("\\\r\n");
            w.putEndOfRecord();
        });
        assertThat(result, is(lines(new String[][] {
            { "\\\r\n" },
        })));
    }

    /**
     * undefined escape - bare escape character follows encodable CR w/ trailing characters.
     */
    @Test
    public void undefined_escape_escape_encodable_cr_c() {
        escape = EscapeSequence.builder('\\')
                .addLineSeparator()
                .build();
        String result = emit(null, w -> {
            w.putField("\\\rc");
            w.putEndOfRecord();
        });
        assertThat(result, is(lines(new String[][] {
            { "\\\rc" },
        })));
    }

    /**
     * undefined escape - bare escape character follows encodable CR w/o trailing characters.
     */
    @Test
    public void undefined_escape_escape_encodable_cr_eof() {
        escape = EscapeSequence.builder('\\')
                .addLineSeparator()
                .build();
        String result = emit(null, w -> {
            w.putField("\\\r");
            w.putEndOfRecord();
        });
        assertThat(result, is(lines(new String[][] {
            { "\\\r" },
        })));
    }

    /**
     * undefined escape - bare escape character follows conflict character.
     */
    @Test
    public void undefined_escape_escape_conflict() {
        escape = EscapeSequence.builder('\\')
                .addMapping('t', '\t')
                .build();
        String result = emit(null, w -> {
            w.putField("\\t");
            expectUnmappable(w, ErrorCode.CONFLICT_ESCAPE_SEQUENCE);
        });
        assertThat(result, is(lines(new String[][] {
            { "\\t" },
        })));
    }

    /**
     * undefined escape - bare escape character follows conflict character.
     */
    @Test
    public void undefined_escape_escape_conflict_null() {
        escape = EscapeSequence.builder('\\')
                .addNullMapping('N')
                .build();
        String result = emit(null, w -> {
            w.putField("\\N");
            expectUnmappable(w, ErrorCode.CONFLICT_ESCAPE_SEQUENCE);
        });
        assertThat(result, is(lines(new String[][] {
            { "\\N" },
        })));
    }

    /**
     * undefined escape - bare escape character follows another escape character.
     */
    @Test
    public void undefined_escape_escape_escape() {
        escape = EscapeSequence.builder('\\')
                .addMapping('t', '\t')
                .build();
        String result = emit(null, w -> {
            w.putField("\\\\t");
            expectUnmappable(w, ErrorCode.CONFLICT_ESCAPE_SEQUENCE);
        });
        assertThat(result, is(lines(new String[][] {
            { "\\\\t" },
        })));
    }

    /**
     * undefined escape - bare escape character follows field separator.
     */
    @Test
    public void undefined_escape_escape_field_separator() {
        escape = EscapeSequence.builder('\\')
                .addNullMapping('N')
                .build();
        String result = emit(null, w -> {
            w.putField("\\\t");
            expectUnmappable(w, ErrorCode.EXTRA_FIELD_SEPARATOR);
        });
        assertThat(result, is(lines(new String[][] {
            { "\\\t" },
        })));
    }

    /**
     * undefined escape - bare escape character follows LF.
     */
    @Test
    public void undefined_escape_escape_lf() {
        escape = EscapeSequence.builder('\\')
                .addNullMapping('N')
                .build();
        String result = emit(null, w -> {
            w.putField("\\\n");
            expectUnmappable(w, ErrorCode.EXTRA_RECORD_SEPARATOR);
        });
        assertThat(result, is(lines(new String[][] {
            { "\\\n" },
        })));
    }

    /**
     * undefined escape - bare escape character follows CRLF.
     */
    @Test
    public void undefined_escape_escape_crlf() {
        escape = EscapeSequence.builder('\\')
                .addNullMapping('N')
                .build();
        String result = emit(null, w -> {
            w.putField("\\\r\n");
            expectUnmappable(w, ErrorCode.EXTRA_RECORD_SEPARATOR);
        });
        assertThat(result, is(lines(new String[][] {
            { "\\\r\n" },
        })));
    }

    /**
     * undefined escape - bare escape character follows CR w/ trailing characters.
     */
    @Test
    public void undefined_escape_escape_cr_c() {
        escape = EscapeSequence.builder('\\')
                .addNullMapping('N')
                .build();
        String result = emit(null, w -> {
            w.putField("\\\rc");
            expectUnmappable(w, ErrorCode.EXTRA_RECORD_SEPARATOR);
        });
        assertThat(result, is(lines(new String[][] {
            { "\\\rc" },
        })));
    }

    /**
     * undefined escape - bare escape character follows CR w/o trailing characters.
     */
    @Test
    public void undefined_escape_escape_cr_eof() {
        escape = EscapeSequence.builder('\\')
                .addNullMapping('N')
                .build();
        String result = emit(null, w -> {
            w.putField("\\\r");
            expectUnmappable(w, ErrorCode.EXTRA_RECORD_SEPARATOR);
        });
        assertThat(result, is(lines(new String[][] {
            { "\\\r" },
        })));
    }

    /**
     * undefined escape - field ends with bare escape character.
     */
    @Test
    public void undefined_escape_escape_end_of_field() {
        escape = EscapeSequence.builder('\\')
                .addNullMapping('N')
                .build();
        String result = emit(null, w -> {
            w.putField("\\");
            w.putField("");
            w.putEndOfRecord();
        });
        assertThat(result, is(lines(new String[][] {
            { "\\", "" },
        })));
    }

    /**
     * undefined escape - field ends with bare escape character.
     */
    @Test
    public void undefined_escape_escape_end_of_field_loss() {
        escape = EscapeSequence.builder('\\')
                .addMapping('\t', '\t')
                .build();
        String result = emit(null, w -> {
            w.putField("\\");
            w.putField("");
            expectUnmappable(w, ErrorCode.LOST_FIELD_SEPARATOR);
        });
        assertThat(result, is(lines(new String[][] {
            { "\\", "" },
        })));
    }

    /**
     * undefined escape - field ends with bare escape character.
     */
    @Test
    public void undefined_escape_escape_end_of_record() {
        escape = EscapeSequence.builder('\\')
                .addNullMapping('N')
                .build();
        String result = emit(null, w -> {
            w.putField("\\");
            w.putEndOfRecord();
            w.putField("");
            w.putEndOfRecord();
        });
        assertThat(result, is(lines(new String[][] {
            { "\\", },
            { "", },
        })));
    }

    /**
     * undefined escape - field ends with bare escape character.
     */
    @Test
    public void undefined_escape_escape_end_of_record_loss() {
        escape = EscapeSequence.builder('\\')
                .addLineSeparator()
                .build();
        String result = emit(null, w -> {
            w.putField("\\");
            expectUnmappable(w, ErrorCode.LOST_RECORD_SEPARATOR);
            w.putField("");
            w.putEndOfRecord();
        });
        assertThat(result, is(lines(new String[][] {
            { "\\", },
            { "", },
        })));
    }

    /**
     * w/ null - undefined.
     */
    @Test
    public void undefined_null_sequence() {
        escape = null;
        String result = emit(null, w -> {
            w.putField(null);
            expectUnmappable(w, ErrorCode.UNDEFINED_NULL_SEQUENCE);
        });
        assertThat(result, is(lines(new String[][] {
            { "" },
        })));
    }

    /**
     * w/ large record.
     */
    @Test
    public void large() {
        String[] fields = new String[1024];
        Arrays.fill(fields, "Hello, world!");
        String result = emit(null, w -> {
            for (String field : fields) {
                w.putField(field);
            }
            w.putEndOfRecord();
        });
        assertThat(result, is(lines(new String[][] {
                fields,
        })));
    }

    /**
     * w/ transformer.
     */
    @Test
    public void transform() {
        String result = emit(s -> s.toString().trim(), w -> {
            w.putField(" A ");
            w.putField(" B ");
            w.putField(" C ");
            w.putEndOfRecord();
        });
        assertThat(result, is(lines(new String[][] {
            { "A ", " B ", " C" },
        })));
    }

    /**
     * w/ transformer.
     */
    @Test
    public void transform_filter() {
        String result = emit(s -> s.toString().equals("B") ? null : s, w -> {
            w.putField("A");
            w.putEndOfRecord();
            w.putField("B");
            w.putEndOfRecord();
            w.putField("C");
            w.putEndOfRecord();
        });
        assertThat(result, is(lines(new String[][] {
            { "A" },
            { "C" },
        })));
    }

    private String emit(UnaryOperator<CharSequence> transformer, Action action) {
        StringWriter writer = new StringWriter();
        try (DelimitedFieldWriter w = new DelimitedFieldWriter(writer, lineSeparator, '\t', escape, transformer)) {
            action.perform(w);
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        return writer.toString();
    }

    private String lines(String[][] lines) {
        return Arrays.stream(lines)
                .map(fs -> String.join("\t", fs))
                .collect(Collectors.joining(
                        lineSeparator.getSequence(),
                        "", lineSeparator.getSequence()));
    }

    @FunctionalInterface
    private interface Action {
        void perform(FieldWriter writer) throws IOException;
    }
}
