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

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.UnaryOperator;

import org.junit.Test;

import com.asakusafw.runtime.io.text.LineSeparator;
import com.asakusafw.runtime.io.text.driver.BasicFieldOutput;

/**
 * Test for {@link TabularTextFormat}.
 */
public class TabularTextFormatTest {

    /**
     * input.
     */
    @Test
    public void input() {
        TabularTextFormat format = TabularTextFormat.builder()
                .build();
        String[][] results = read(format, new String[] {
                "Hello, world!",
        });
        assertThat(results, is(new String[][] {
            { "Hello, world!" },
        }));
    }

    /**
     * input - delimited.
     */
    @Test
    public void input_delimited() {
        TabularTextFormat format = TabularTextFormat.builder()
                .build();
        String[][] results = read(format, new String[] {
                "A\tB",
                "C\tD",
        });
        assertThat(results, is(new String[][] {
            { "A", "B", },
            { "C", "D", },
        }));
    }

    /**
     * input - charset.
     */
    @Test
    public void input_charset() {
        TabularTextFormat format = TabularTextFormat.builder()
                .withCharset("US-ASCII")
                .build();
        String[][] results = read(format, new String[] {
                "A\tB",
                "C\tD",
        });
        assertThat(results, is(new String[][] {
            { "A", "B", },
            { "C", "D", },
        }));
    }

    /**
     * input - w/ field separator.
     */
    @Test
    public void input_field_separator() {
        TabularTextFormat format = TabularTextFormat.builder()
                .withFieldSeparator(',')
                .build();
        String[][] results = read(format, new String[] {
                "A,B",
                "C,D",
        });
        assertThat(results, is(new String[][] {
            { "A", "B", },
            { "C", "D", },
        }));
    }

    /**
     * input - w/ escape sequence.
     */
    @Test
    public void input_escape_sequence() {
        TabularTextFormat format = TabularTextFormat.builder()
                .withEscapeSequence(EscapeSequence.builder('\\')
                        .addMapping('t', '\t')
                        .build())
                .build();
        String[][] results = read(format, new String[] {
                "A\\t\t\\tB",
                "C\\t\t\\tD",
        });
        assertThat(results, is(new String[][] {
            { "A\t", "\tB", },
            { "C\t", "\tD", },
        }));
    }

    /**
     * input - w/ transformer.
     */
    @Test
    public void input_transformer() {
        TabularTextFormat format = TabularTextFormat.builder()
                .withInputTransformer(LowerCaseTransformer.class)
                .build();
        String[][] results = read(format, new String[] {
                "A\tB",
                "C\tD",
        });
        assertThat(results, is(new String[][] {
            { "a", "b", },
            { "c", "d", },
        }));
    }

    /**
     * output.
     */
    @Test
    public void output() {
        TabularTextFormat format = TabularTextFormat.builder()
                .build();
        String[] results = write(format, new String[][] {
            { "Hello, world!", },
        });
        assertThat(results, is(new String[] {
                "Hello, world!",
        }));
    }

    /**
     * output - delimited.
     */
    @Test
    public void output_delimited() {
        TabularTextFormat format = TabularTextFormat.builder()
                .build();
        String[] results = write(format, new String[][] {
            { "A", "B", },
            { "C", "D", },
        });
        assertThat(results, is(new String[] {
                "A\tB",
                "C\tD",
        }));
    }

    /**
     * output - charset.
     */
    @Test
    public void output_charset() {
        TabularTextFormat format = TabularTextFormat.builder()
                .withCharset("US-ASCII")
                .build();
        String[] results = write(format, new String[][] {
            { "A", "B", },
            { "C", "D", },
        });
        assertThat(results, is(new String[] {
                "A\tB",
                "C\tD",
        }));
    }

    /**
     * output - w/ line separator.
     */
    @Test
    public void output_line_separator() {
        TabularTextFormat format = TabularTextFormat.builder()
                .withLineSeparator(LineSeparator.WINDOWS)
                .build();
        String[] results = write(format, new String[][] {
            { "A", "B", },
            { "C", "D", },
        });
        assertThat(results, is(new String[] {
                "A\tB",
                "C\tD",
        }));
    }

    /**
     * output - w/ field separator.
     */
    @Test
    public void output_field_separator() {
        TabularTextFormat format = TabularTextFormat.builder()
                .withFieldSeparator(',')
                .build();
        String[] results = write(format, new String[][] {
            { "A", "B", },
            { "C", "D", },
        });
        assertThat(results, is(new String[] {
                "A,B",
                "C,D",
        }));
    }

    /**
     * output - w/ escape sequence.
     */
    @Test
    public void output_escape_sequence() {
        TabularTextFormat format = TabularTextFormat.builder()
                .withEscapeSequence(EscapeSequence.builder('\\')
                        .addMapping('t', '\t')
                        .build())
                .build();
        String[] results = write(format, new String[][] {
            { "A\t", "\tB", },
            { "C\t", "\tD", },
        });
        assertThat(results, is(new String[] {
                "A\\t\t\\tB",
                "C\\t\t\\tD",
        }));
    }

    /**
     * output - w/ transformer.
     */
    @Test
    public void output_transformer() {
        TabularTextFormat format = TabularTextFormat.builder()
                .withOutputTransformer(LowerCaseTransformer.class)
                .build();
        String[] results = write(format, new String[][] {
            { "A", "B", },
            { "C", "D", },
        });
        assertThat(results, is(new String[] {
                "a\tb",
                "c\td",
        }));
    }

    private String[][] read(TabularTextFormat format, String... lines) {
        StringBuilder buffer = new StringBuilder();
        for (String line : lines) {
            buffer.append(line);
            buffer.append(format.getLineSeparator().getSequence());
        }
        byte[] bytes = buffer.toString().getBytes(format.getCharset());
        try (TabularFieldReader reader = format.open(new ByteArrayInputStream(bytes))) {
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

    private String[] write(TabularTextFormat format, String[][] fields) {
        BasicFieldOutput fout = new BasicFieldOutput();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (TabularFieldWriter writer = format.open(output)) {
            for (String[] row : fields) {
                for (String field : row) {
                    writer.putField(fout.set(field));
                }
                writer.putEndOfRecord();
            }
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        String lines = new String(output.toByteArray(), format.getCharset());
        return Arrays.stream(lines.split(format.getLineSeparator().getSequence()))
                .filter(s -> s.isEmpty() == false)
                .toArray(String[]::new);
    }

    @SuppressWarnings("javadoc")
    public static class LowerCaseTransformer implements UnaryOperator<CharSequence> {
        @Override
        public CharSequence apply(CharSequence t) {
            return t.toString().toLowerCase(Locale.ENGLISH);
        }
    }
}
