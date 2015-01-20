/**
 * Copyright 2011-2015 Asakusa Framework Team.
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
package com.asakusafw.utils.java.internal.model.util;

import java.math.BigInteger;

/**
 * リテラルを解析する。
 */
public final class LiteralAnalyzer {

    private static final BigInteger MAX_INT = BigInteger.valueOf(Integer.MAX_VALUE).add(BigInteger.ONE);
    private static final BigInteger MAX_LONG = BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.ONE);

    private LiteralAnalyzer() {
        return;
    }

    /**
     * 指定の文字列をリテラルとして解析し、解析結果のトークンを返す。
     * @param literal 解析する文字列
     * @return 解析結果のトークン
     * @throws IllegalArgumentException 引数に{@code null}を指定した場合
     */
    public static LiteralToken parse(String literal) {
        if (literal == null) {
            throw new IllegalArgumentException("literal must not be null"); //$NON-NLS-1$
        }
        LiteralTokenKind kind = LiteralParser.scan(literal);
        Object value = valueOf(kind, literal);
        return new LiteralToken(literal, kind, value);
    }

    private static Object valueOf(LiteralTokenKind kind, String literal) {
        switch (kind) {
        case BOOLEAN:
            return booleanValueOf(literal);
        case CHAR:
            return charValueOf(literal);
        case DOUBLE:
            return doubleValueOf(literal);
        case FLOAT:
            return floatValueOf(literal);
        case INT:
            return intValueOf(literal);
        case LONG:
            return longValueOf(literal);
        case NULL:
            return null;
        case STRING:
            return stringValueOf(literal);
        case UNKNOWN:
            return LiteralTokenKind.UNKNOWN;
        default:
            throw new AssertionError(literal);
        }
    }

    /**
     * 指定のリテラルが表現する{@code boolean}型の値を返す。
     * @param literal 対象のリテラル文字列
     * @return 指定のリテラルが表現する値
     * @throws IllegalArgumentException リテラルの形式が不正である場合
     */
    public static boolean booleanValueOf(String literal) {
        if (LiteralToken.TOKEN_TRUE.equals(literal)) {
            return true;
        } else if (LiteralToken.TOKEN_FALSE.equals(literal)) {
            return false;
        } else {
            throw new IllegalArgumentException(literal);
        }
    }

    /**
     * 指定のリテラルが表現する{@code char}型の値を返す。
     * @param literal 対象のリテラル文字列
     * @return 指定のリテラルが表現する値
     * @throws IllegalArgumentException リテラルの形式が不正である場合
     */
    public static char charValueOf(String literal) {
        int length = literal.length();
        if (length < 3 || literal.charAt(0) != '\'' || literal.charAt(length - 1) != '\'') {
            throw new IllegalArgumentException(literal);
        }
        String unescaped = JavaEscape.unescape(literal.substring(1, length - 1));
        if (unescaped.length() != 1) {
            throw new IllegalArgumentException(literal);
        }
        return unescaped.charAt(0);
    }

    /**
     * 指定のリテラルが表現する{@code double}型の値を返す。
     * @param literal 対象のリテラル文字列
     * @return 指定のリテラルが表現する値
     * @throws NumberFormatException リテラルの形式が不正である場合
     */
    public static double doubleValueOf(String literal) {
        return Double.parseDouble(literal);
    }

    /**
     * 指定のリテラルが表現する{@code float}型の値を返す。
     * @param literal 対象のリテラル文字列
     * @return 指定のリテラルが表現する値
     * @throws NumberFormatException リテラルの形式が不正である場合
     */
    public static float floatValueOf(String literal) {
        return Float.parseFloat(literal);
    }

    /**
     * 指定のリテラルが表現する{@code int}型の値を返す。
     * @param literal 対象のリテラル文字列
     * @return 指定のリテラルが表現する値
     * @throws NumberFormatException リテラルの形式が不正である場合
     */
    public static int intValueOf(String literal) {
        IntegerHolder h = parseInteger(literal);

        BigInteger number = h.toBigInteger();
        if (h.radix != 10) {
            if (number.bitLength() > 32) {
                throw new NumberFormatException(literal);
            }
        } else {
            if (number.bitLength() > 31 && !number.equals(MAX_INT)) {
                throw new NumberFormatException(literal);
            }
        }
        return number.intValue();
    }

    /**
     * 指定のリテラルが表現する{@code long}型の値を返す。
     * @param literal 対象のリテラル文字列
     * @return 指定のリテラルが表現する値
     * @throws NumberFormatException リテラルの形式が不正である場合
     */
    public static long longValueOf(String literal) {
        String target;
        if (literal.endsWith("l") || literal.endsWith("L")) { //$NON-NLS-1$ //$NON-NLS-2$
            target = literal.substring(0, literal.length() - 1);
        } else {
            target = literal;
        }
        IntegerHolder h = parseInteger(target);

        BigInteger number = h.toBigInteger();
        if (h.radix != 10) {
            if (number.bitLength() > 64) {
                throw new NumberFormatException(literal);
            }
        } else {
            if (number.bitLength() > 63 && !number.equals(MAX_LONG)) {
                throw new NumberFormatException(literal);
            }
        }
        return number.longValue();
    }


    /**
     * 指定のリテラルが表現する{@link String}型の値を返す。
     * @param literal 対象のリテラル文字列
     * @return 指定のリテラルが表現する値
     * @throws IllegalArgumentException リテラルの形式が不正である場合
     */
    public static String stringValueOf(String literal) {
        int length = literal.length();
        if (length < 2 || literal.charAt(0) != '\"' || literal.charAt(length - 1) != '\"') {
            throw new IllegalArgumentException(literal);
        }
        return JavaEscape.unescape(literal.substring(1, length - 1));
    }

    /**
     * 指定の値を表現可能なリテラル文字列を返す。
     * @param value リテラル化可能な値
     * @return 対象の値を表現するリテラル文字列
     * @throws IllegalArgumentException リテラル化可能な値でない場合
     */
    public static String literalOf(Object value) {
        if (value == null) {
            return nullLiteral();
        }
        Class<? extends Object> klass = value.getClass();
        if (klass == Boolean.class) {
            return booleanLiteralOf((Boolean) value);
        } else if (klass == Character.class) {
            return charLiteralOf((Character) value);
        } else if (klass == Double.class) {
            return doubleLiteralOf((Double) value);
        } else if (klass == Float.class) {
            return floatLiteralOf((Float) value);
        } else if (klass == Integer.class) {
            return intLiteralOf((Integer) value);
        } else if (klass == Long.class) {
            return longLiteralOf((Long) value);
        } else if (klass == String.class) {
            return stringLiteralOf((String) value);
        } else {
            throw new IllegalArgumentException(value.toString());
        }
    }

    /**
     * 指定の{@code boolean}型の値を表現可能なリテラル文字列を返す。
     * @param value 対象の値
     * @return 対象の値を表現するリテラル文字列
     */
    public static String booleanLiteralOf(boolean value) {
        return String.valueOf(value);
    }

    /**
     * 指定の{@code char}型の値を表現可能なリテラル文字列を返す。
     * @param value 対象の値
     * @return 対象の値を表現するリテラル文字列
     */
    public static String charLiteralOf(char value) {
        return '\'' + JavaEscape.escape(String.valueOf(value), true, false) + '\'';
    }

    /**
     * 指定の{@code double}型の値を表現可能なリテラル文字列を返す。
     * @param value 対象の値
     * @return 対象の値を表現するリテラル文字列
     */
    public static String doubleLiteralOf(double value) {
        return String.valueOf(value);
    }

    /**
     * 指定の{@code float}型の値を表現可能なリテラル文字列を返す。
     * @param value 対象の値
     * @return 対象の値を表現するリテラル文字列
     */
    public static String floatLiteralOf(float value) {
        if (Float.isInfinite(value) || Float.isNaN(value)) {
            return String.valueOf(value);
        } else {
            return String.valueOf(value) + "f"; //$NON-NLS-1$
        }
    }

    /**
     * 指定の{@code int}型の値を表現可能なリテラル文字列を返す。
     * @param value 対象の値
     * @return 対象の値を表現するリテラル文字列
     */
    public static String intLiteralOf(int value) {
        return String.valueOf(value);
    }

    /**
     * 指定の{@code long}型の値を表現可能なリテラル文字列を返す。
     * @param value 対象の値
     * @return 対象の値を表現するリテラル文字列
     */
    public static String longLiteralOf(long value) {
        return String.valueOf(value) + 'L';
    }

    /**
     * 指定の{@link String}型の値を表現可能なリテラル文字列を返す。
     * @param value 対象の値
     * @return 対象の値を表現するリテラル文字列
     */
    public static String stringLiteralOf(String value) {
        return '\"' + JavaEscape.escape(value, false, false) + '\"';
    }

    /**
     * 指定の{@code null}型の値を表現可能なリテラル文字列を返す。
     * @return 対象の値を表現するリテラル文字列
     */
    public static String nullLiteral() {
        return LiteralToken.TOKEN_NULL;
    }

    /**
     * リテラル文字列を、先頭の基数修飾を加味して解釈し、対応する整数を返す。
     * @param literal 処理対象の文字列
     * @return 対応する整数
     */
    private static IntegerHolder parseInteger(String literal) {
        assert literal != null;
        String target;
        boolean positive;
        if (literal.startsWith("-")) { //$NON-NLS-1$
            target = literal.substring(1).trim();
            positive = false;
        } else {
            target = literal;
            positive = true;
        }
        if (target.length() > 2 && (target.startsWith("0x") || target.startsWith("0X"))) { //$NON-NLS-1$ //$NON-NLS-2$
            return new IntegerHolder(positive, target.substring(2), 16);
        } else if (target.length() > 1 && target.startsWith("0")) { //$NON-NLS-1$
            return new IntegerHolder(positive, target.substring(1), 8);
        } else {
            return new IntegerHolder(positive, target, 10);
        }
    }

    private static class IntegerHolder {
        private final boolean positive;
        private final String literal;
        final int radix;

        IntegerHolder(boolean positive, String literal, int radix) {
            this.positive = positive;
            this.literal = literal;
            this.radix = radix;
        }

        BigInteger toBigInteger() {
            BigInteger bint = new BigInteger(this.literal, this.radix);
            if (this.positive) {
                return bint;
            } else {
                return bint.negate();
            }
        }
    }
}
