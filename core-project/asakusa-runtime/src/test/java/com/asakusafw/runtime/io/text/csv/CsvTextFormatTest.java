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
package com.asakusafw.runtime.io.text.csv;

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
 * Test for {@link CsvTextFormat}.
 */
public class CsvTextFormatTest {

    /**
     * input.
     */
    @Test
    public void input() {
        CsvTextFormat format = CsvTextFormat.builder()
                .build();
        String[][] results = read(format, new String[] {
                "Hello!",
        });
        assertThat(results, is(new String[][] {
            { "Hello!" },
        }));
    }

    /**
     * input - quoted.
     */
    @Test
    public void input_quoted() {
        CsvTextFormat format = CsvTextFormat.builder()
                .build();
        String[][] results = read(format, new String[] {
                "\"Hello, world!\"",
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
        CsvTextFormat format = CsvTextFormat.builder()
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
     * input - charset.
     */
    @Test
    public void input_charset() {
        CsvTextFormat format = CsvTextFormat.builder()
                .withCharset("US-ASCII")
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
     * input - w/ field separator.
     */
    @Test
    public void input_field_separator() {
        CsvTextFormat format = CsvTextFormat.builder()
                .withFieldSeparator('\t')
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
     * input - w/ quote character.
     */
    @Test
    public void input_quote_character() {
        CsvTextFormat format = CsvTextFormat.builder()
                .withQuoteCharacter('%')
                .build();
        String[][] results = read(format, new String[] {
                "%Hello, world!%",
        });
        assertThat(results, is(new String[][] {
            { "Hello, world!", },
        }));
    }

    /**
     * input - w/ allow line feed in field.
     */
    @Test
    public void input_allow_line_feed() {
        CsvTextFormat format = CsvTextFormat.builder()
                .withAllowLineFeedInField(true)
                .build();
        String[][] results = read(format, new String[] {
                "\"A\nB\nC\n\"",
        });
        assertThat(results, is(new String[][] {
            { "A\nB\nC\n", },
        }));
    }

    /**
     * input - w/ transformer.
     */
    @Test
    public void input_transformer() {
        CsvTextFormat format = CsvTextFormat.builder()
                .withInputTransformer(LowerCaseTransformer.class)
                .build();
        String[][] results = read(format, new String[] {
                "A,B",
                "C,D",
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
        CsvTextFormat format = CsvTextFormat.builder()
                .build();
        String[] results = write(format, new String[][] {
            { "Hello!", },
        });
        assertThat(results, is(new String[] {
                "Hello!",
        }));
    }

    /**
     * output - quote.
     */
    @Test
    public void output_quoted() {
        CsvTextFormat format = CsvTextFormat.builder()
                .build();
        String[] results = write(format, new String[][] {
            { "Hello, world!!", },
        });
        assertThat(results, is(new String[] {
                "\"Hello, world!!\"",
        }));
    }

    /**
     * output - delimited.
     */
    @Test
    public void output_delimited() {
        CsvTextFormat format = CsvTextFormat.builder()
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
     * output - charset.
     */
    @Test
    public void output_charset() {
        CsvTextFormat format = CsvTextFormat.builder()
                .withCharset("US-ASCII")
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
     * output - w/ line separator.
     */
    @Test
    public void output_line_separator() {
        CsvTextFormat format = CsvTextFormat.builder()
                .withLineSeparator(LineSeparator.UNIX)
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
     * output - w/ field separator.
     */
    @Test
    public void output_field_separator() {
        CsvTextFormat format = CsvTextFormat.builder()
                .withFieldSeparator('\t')
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
     * output - w/ quote character.
     */
    @Test
    public void output_quote_character() {
        CsvTextFormat format = CsvTextFormat.builder()
                .withQuoteCharacter('%')
                .build();
        String[] results = write(format, new String[][] {
            { "Hello, world!", },
        });
        assertThat(results, is(new String[] {
                "%Hello, world!%",
        }));
    }

    /**
     * output - w/ allow line feed in field.
     */
    @Test
    public void output_allow_line_feed_in_field() {
        CsvTextFormat format = CsvTextFormat.builder()
                .withAllowLineFeedInField(true)
                .build();
        String[] results = write(format, new String[][] {
            { "A\nB\nC\n", },
        });
        assertThat(results, is(new String[] {
                "\"A\nB\nC\n\"",
        }));
    }

    /**
     * output - w/ default quote style.
     */
    @Test
    public void output_default_quote_style() {
        CsvTextFormat format = CsvTextFormat.builder()
                .withDefaultQuoteStyle(QuoteStyle.ALWAYS)
                .build();
        String[] results = write(format, new String[][] {
            { "Hello!", },
        });
        assertThat(results, is(new String[] {
                "\"Hello!\"",
        }));
    }

    /**
     * output - w/ header quote style.
     */
    @Test
    public void output_header_quote_style() {
        CsvTextFormat format = CsvTextFormat.builder()
                .withHeaderQuoteStyle(QuoteStyle.ALWAYS)
                .build();
        String[] results = write(format, new String[][] {
            { "Hello!", },
        });
        assertThat(results, is(new String[] {
                "Hello!",
        }));
    }

    /**
     * output - w/ transformer.
     */
    @Test
    public void output_transformer() {
        CsvTextFormat format = CsvTextFormat.builder()
                .withOutputTransformer(LowerCaseTransformer.class)
                .build();
        String[] results = write(format, new String[][] {
            { "A", "B", },
            { "C", "D", },
        });
        assertThat(results, is(new String[] {
                "a,b",
                "c,d",
        }));
    }

    private static String[][] read(CsvTextFormat format, String... lines) {
        StringBuilder buffer = new StringBuilder();
        for (String line : lines) {
            buffer.append(line);
            buffer.append(format.getLineSeparator().getSequence());
        }
        byte[] bytes = buffer.toString().getBytes(format.getCharset());
        try (CsvFieldReader reader = format.open(new ByteArrayInputStream(bytes))) {
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

    private static String[] write(CsvTextFormat format, String[][] fields) {
        BasicFieldOutput fout = new BasicFieldOutput();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (CsvFieldWriter writer = format.open(output)) {
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
