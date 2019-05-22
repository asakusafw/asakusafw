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
package com.asakusafw.runtime.io.text;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.CharBuffer;

import org.junit.Test;

/**
 * Test for {@link TextUtil}.
 */
public class TextUtilTest {

    private static final String CP_BEER = new StringBuilder().appendCodePoint(0x1f37a).toString();

    /**
     * quote.
     */
    @Test
    public void quote() {
        assertThat(TextUtil.quote("Hello, world!"), is("\"Hello, world!\""));
    }

    /**
     * quote w/ escape sequence.
     */
    @Test
    public void quote_escape() {
        assertThat(TextUtil.quote("say \"hello\"\n"), is("\"say \\\"hello\\\"\\n\""));
    }

    /**
     * quote w/ unicode.
     */
    @Test
    public void quote_unicode() {
        assertThat(TextUtil.quote("\000\001"), is("\"\\u0000\\u0001\""));
    }

    /**
     * quote w/ surrogate pair.
     */
    @Test
    public void quote_surrogate_pair() {
        assertThat(TextUtil.quote(CP_BEER), is('"' + CP_BEER + '"'));
    }

    /**
     * quote w/ surrogate pair.
     */
    @Test
    public void quote_surrogate_broken() {
        char hi = CP_BEER.charAt(0);
        char lo = CP_BEER.charAt(1);
        assertThat(TextUtil.quote(String.valueOf(hi)), is(String.format("\"\\u%04x\"", (int) hi)));
        assertThat(TextUtil.quote(String.valueOf(lo)), is(String.format("\"\\u%04x\"", (int) lo)));
        assertThat(
                TextUtil.quote(new StringBuilder().append(hi).append('a')),
                is(String.format("\"\\u%04xa\"", (int) hi)));
    }

    /**
     * parse byte.
     */
    @Test
    public void parse_byte() {
        assertThat(parse(TextUtil::parseByte, 0), is((byte) 0));
        assertThat(parse(TextUtil::parseByte, 1), is((byte) 1));
        assertThat(parse(TextUtil::parseByte, "+1"), is((byte) 1));
        assertThat(parse(TextUtil::parseByte, -1), is((byte) -1));
        assertThat(parse(TextUtil::parseByte, Byte.MAX_VALUE), is(Byte.MAX_VALUE));
        assertThat(parse(TextUtil::parseByte, Byte.MIN_VALUE), is(Byte.MIN_VALUE));

        invalid(TextUtil::parseByte, Byte.MAX_VALUE + 1);
        invalid(TextUtil::parseByte, Byte.MIN_VALUE - 1);
    }

    /**
     * parse short.
     */
    @Test
    public void parse_short() {
        assertThat(parse(TextUtil::parseShort, 0), is((short) 0));
        assertThat(parse(TextUtil::parseShort, 1), is((short) 1));
        assertThat(parse(TextUtil::parseShort, "+1"), is((short) 1));
        assertThat(parse(TextUtil::parseShort, -1), is((short) -1));
        assertThat(parse(TextUtil::parseShort, Short.MAX_VALUE), is(Short.MAX_VALUE));
        assertThat(parse(TextUtil::parseShort, Short.MIN_VALUE), is(Short.MIN_VALUE));

        invalid(TextUtil::parseShort, Short.MAX_VALUE + 1);
        invalid(TextUtil::parseShort, Short.MIN_VALUE - 1);
    }

    /**
     * parse int.
     */
    @Test
    public void parse_int() {
        assertThat(parse(TextUtil::parseInt, 0), is(0));
        assertThat(parse(TextUtil::parseInt, 1), is(1));
        assertThat(parse(TextUtil::parseInt, "+1"), is(1));
        assertThat(parse(TextUtil::parseInt, -1), is(-1));
        assertThat(parse(TextUtil::parseInt, Integer.MAX_VALUE), is(Integer.MAX_VALUE));
        assertThat(parse(TextUtil::parseInt, Integer.MIN_VALUE), is(Integer.MIN_VALUE));

        invalid(TextUtil::parseInt, BigInteger.valueOf(Integer.MAX_VALUE).add(BigInteger.valueOf(1)));
        invalid(TextUtil::parseInt, BigInteger.valueOf(Integer.MIN_VALUE).add(BigInteger.valueOf(-1)));
        invalid(TextUtil::parseInt, BigInteger.valueOf(Integer.MAX_VALUE)
                .add(BigInteger.valueOf(1))
                .multiply(BigInteger.valueOf(10)));
        invalid(TextUtil::parseInt, BigInteger.valueOf(Integer.MIN_VALUE)
                .add(BigInteger.valueOf(-1))
                .multiply(BigInteger.valueOf(10)));
    }

    /**
     * parse long.
     */
    @Test
    public void parse_long() {
        assertThat(parse(TextUtil::parseLong, 0), is((long) 0));
        assertThat(parse(TextUtil::parseLong, 1), is((long) 1));
        assertThat(parse(TextUtil::parseLong, "+1"), is((long) 1));
        assertThat(parse(TextUtil::parseLong, -1), is((long) -1));
        assertThat(parse(TextUtil::parseLong, Long.MAX_VALUE), is(Long.MAX_VALUE));
        assertThat(parse(TextUtil::parseLong, Long.MIN_VALUE), is(Long.MIN_VALUE));

        invalid(TextUtil::parseLong, BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.valueOf(1)));
        invalid(TextUtil::parseLong, BigInteger.valueOf(Long.MIN_VALUE).add(BigInteger.valueOf(-1)));
        invalid(TextUtil::parseLong, BigInteger.valueOf(Long.MIN_VALUE)
                .add(BigInteger.valueOf(1))
                .multiply(BigInteger.valueOf(10)));
        invalid(TextUtil::parseLong, BigInteger.valueOf(Long.MIN_VALUE)
                .add(BigInteger.valueOf(-1))
                .multiply(BigInteger.valueOf(10)));
    }

    /**
     * parse decimal.
     */
    @Test
    public void parse_decimal() {
        assertThat(parse(TextUtil::parseDecimal, "0"), is(new BigDecimal("0")));
        assertThat(parse(TextUtil::parseDecimal, "1"), is(new BigDecimal("1")));
        assertThat(parse(TextUtil::parseDecimal, "+1"), is(new BigDecimal("+1")));
        assertThat(parse(TextUtil::parseDecimal, "-1"), is(new BigDecimal("-1")));
        assertThat(parse(TextUtil::parseDecimal, "3.14"), is(new BigDecimal("3.14")));

        assertThat(
                parse(TextUtil::parseDecimal, new StringBuilder("3.14")),
                is(new BigDecimal("3.14")));
        assertThat(
                parse(TextUtil::parseDecimal, CharBuffer.wrap("3.14")),
                is(new BigDecimal("3.14")));
        assertThat(
                parse(TextUtil::parseDecimal, CharBuffer.wrap("3.14".toCharArray())),
                is(new BigDecimal("3.14")));
    }

    /**
     * parse float.
     */
    @Test
    public void parse_float() {
        assertThat(parse(TextUtil::parseFloat, "0"), is(0f));
        assertThat(parse(TextUtil::parseFloat, "1"), is(1f));
        assertThat(parse(TextUtil::parseFloat, "+1"), is(1f));
        assertThat(parse(TextUtil::parseFloat, "-1"), is(-1f));
        assertThat(parse(TextUtil::parseFloat, "1.25"), is(1.25f));
    }

    /**
     * parse double.
     */
    @Test
    public void parse_double() {
        assertThat(parse(TextUtil::parseDouble, "0"), is(0d));
        assertThat(parse(TextUtil::parseDouble, "1"), is(1d));
        assertThat(parse(TextUtil::parseDouble, "+1"), is(1d));
        assertThat(parse(TextUtil::parseDouble, "-1"), is(-1d));
        assertThat(parse(TextUtil::parseDouble, "1.25"), is(1.25d));
    }

    /**
     * parse int.
     */
    @Test
    public void parse_int_invalid() {
        invalid(TextUtil::parseInt, "A");
        invalid(TextUtil::parseInt, " ");
        invalid(TextUtil::parseInt, "2a03");
    }

    /**
     * parse int.
     */
    @Test
    public void parse_long_invalid() {
        invalid(TextUtil::parseLong, "A");
        invalid(TextUtil::parseLong, " ");
        invalid(TextUtil::parseLong, "2a03");
    }

    private static <T> T parse(Parser<T> parser, Object value) {
        CharSequence s = value instanceof CharSequence ? (CharSequence) value : value.toString();
        return parser.parse(s, 0, s.length());
    }

    private static void invalid(Parser<?> parser, Object value) {
        String s = value.toString();
        try {
            Object r = parser.parse(s, 0, s.length());
            fail(String.format("%s -> %s", s, r));
        } catch (NumberFormatException e) {
            // ok
        }
    }

    @FunctionalInterface
    private interface Parser<T> {
        T parse(CharSequence cs, int start, int end);
    }
}
