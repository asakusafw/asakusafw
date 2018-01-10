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
package com.asakusafw.runtime.value;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.function.Consumer;
import java.util.function.Function;

import org.junit.Test;

/**
 * Test for {@link StringOptionUtil}.
 */
public class StringOptionUtilTest {

    /**
     * test for as reader.
     */
    @Test
    public void reader() {
        String read = dump(new StringOption("Hello, world!"));
        assertThat(read, is("Hello, world!"));
    }

    /**
     * trim - nothing.
     */
    @Test
    public void trim_nothing() {
        StringOption value = new StringOption("Hello, world!");
        StringOptionUtil.trim(value);
        assertThat(value, is(new StringOption("Hello, world!")));
    }

    /**
     * trim - leading.
     */
    @Test
    public void trim_leading() {
        StringOption value = new StringOption("    Hello, world!");
        StringOptionUtil.trim(value);
        assertThat(value, is(new StringOption("Hello, world!")));
    }

    /**
     * trim - nothing.
     */
    @Test
    public void trim_trailing() {
        StringOption value = new StringOption("Hello, world!    ");
        StringOptionUtil.trim(value);
        assertThat(value, is(new StringOption("Hello, world!")));
    }

    /**
     * trim - rounding.
     */
    @Test
    public void trim_rounding() {
        StringOption value = new StringOption("  Hello, world!  ");
        StringOptionUtil.trim(value);
        assertThat(value, is(new StringOption("Hello, world!")));
    }

    /**
     * append - string option.
     */
    @Test
    public void append_option() {
        StringOption value = new StringOption("Hello");
        StringOptionUtil.append(value, new StringOption(", world!"));
        StringOptionUtil.trim(value);
        assertThat(value, is(new StringOption("Hello, world!")));
    }

    /**
     * append - string.
     */
    @Test
    public void append_string() {
        StringOption value = new StringOption("Hello");
        StringOptionUtil.append(value, ", world!");
        StringOptionUtil.trim(value);
        assertThat(value, is(new StringOption("Hello, world!")));
    }

    /**
     * count code points.
     */
    @Test
    public void countCodePoints() {
        Consumer<String> tester = s -> assertThat(
                StringOptionUtil.countCodePoints(new StringOption(s)), is((int) s.codePoints().count()));
        tester.accept("");
        tester.accept("a");
        tester.accept("Hello, world!");
        tester.accept("\u00c0");
        tester.accept(StringOptionTest.HELLO_JP);
        tester.accept(new StringBuilder()
                .appendCodePoint(0x1f37a)
                .appendCodePoint(0x1f363)
                .toString());
    }

    /**
     * append - string builder.
     */
    @Test
    public void append_string_builder() {
        StringBuilder buf = new StringBuilder();
        buf.append("<");
        StringOptionUtil.append(buf, new StringOption("Hello, world!"));
        buf.append(">");
        assertThat(buf.toString(), is("<Hello, world!>"));
    }

    /**
     * parse int.
     */
    @Test
    public void parseInt() {
        Function<Object, Integer> parser = s -> StringOptionUtil.parseInt(new StringOption(String.valueOf(s)));
        assertThat(parser.apply("0"), is(0));
        assertThat(parser.apply("1"), is(1));
        assertThat(parser.apply("+100"), is(+100));
        assertThat(parser.apply("-100"), is(-100));
        assertThat(parser.apply(Integer.MAX_VALUE), is(Integer.MAX_VALUE));
        assertThat(parser.apply(Integer.MIN_VALUE), is(Integer.MIN_VALUE));

        raise(() -> parser.apply(""));
        raise(() -> parser.apply("Hello, world!"));
        raise(() -> parser.apply(Integer.MAX_VALUE + 1L));
        raise(() -> parser.apply(Integer.MIN_VALUE - 1L));
    }

    /**
     * parse long.
     */
    @Test
    public void parseLong() {
        Function<Object, Long> parser = s -> StringOptionUtil.parseLong(new StringOption(String.valueOf(s)));
        assertThat(parser.apply("0"), is(0L));
        assertThat(parser.apply("1"), is(1L));
        assertThat(parser.apply("+100"), is(+100L));
        assertThat(parser.apply("-100"), is(-100L));
        assertThat(parser.apply(Integer.MAX_VALUE), is((long) Integer.MAX_VALUE));
        assertThat(parser.apply(Integer.MIN_VALUE), is((long) Integer.MIN_VALUE));
        assertThat(parser.apply(Long.MAX_VALUE), is(Long.MAX_VALUE));
        assertThat(parser.apply(Long.MIN_VALUE), is(Long.MIN_VALUE));

        raise(() -> parser.apply(""));
        raise(() -> parser.apply("Hello, world!"));
        raise(() -> parser.apply(BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.ONE)));
        raise(() -> parser.apply(BigInteger.valueOf(Long.MIN_VALUE).subtract(BigInteger.ONE)));
    }

    /**
     * parse decimal.
     */
    @Test
    public void parseDecimal() {
        Function<Object, BigDecimal> parser = s -> StringOptionUtil.parseDecimal(new StringOption(String.valueOf(s)));
        assertThat(parser.apply("0"), is(new BigDecimal("0")));
        assertThat(parser.apply("1"), is(new BigDecimal("1")));
        assertThat(parser.apply("+100"), is(new BigDecimal("100")));
        assertThat(parser.apply("-100"), is(new BigDecimal("-100")));
        assertThat(parser.apply("+3.14"), is(new BigDecimal("3.14")));
        assertThat(parser.apply("-3.14"), is(new BigDecimal("-3.14")));
        assertThat(parser.apply(Long.MAX_VALUE), is(new BigDecimal(Long.MAX_VALUE)));
        assertThat(parser.apply(Long.MIN_VALUE), is(new BigDecimal(Long.MIN_VALUE)));

        raise(() -> parser.apply(""));
        raise(() -> parser.apply("Hello, world!"));
    }

    private static void raise(Runnable r) {
        try {
            r.run();
            fail();
        } catch (RuntimeException e) {
            // ok.
        }
    }

    private static String dump(StringOption option) {
        try (Reader reader = StringOptionUtil.asReader(option)) {
            char[] buf = new char[256];
            StringBuffer results = new StringBuffer();
            while (true) {
                int read = reader.read(buf);
                if (read < 0) {
                    break;
                }
                results.append(buf, 0, read);
            }
            return results.toString();
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }
}
