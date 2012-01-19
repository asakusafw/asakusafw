/**
 * Copyright 2012 Asakusa Framework Team.
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
package com.asakusafw.compiler.directio;

import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.asakusafw.compiler.flow.DataClass;
import com.asakusafw.compiler.flow.DataClass.Property;
import com.asakusafw.runtime.stage.directio.StringTemplate.Format;
import com.asakusafw.runtime.value.DateOption;
import com.asakusafw.runtime.value.DateTimeOption;
import com.asakusafw.vocabulary.directio.DirectFileOutputDescription;

/**
 * Processes patterns in {@link DirectFileOutputDescription}.
 * @since 0.2.5
 */
public class OutputPattern {

    static final int CHAR_BLOCK_OPEN = '{';

    static final int CHAR_BLOCK_CLOSE = '}';

    static final int CHAR_SEPARATE_IN_BLOCK = ':';

    static final int CHAR_VARIABLE_START = '$';

    static final BitSet CHAR_MAP_META = new BitSet();
    static {
        CHAR_MAP_META.set(0, 0x20);
        CHAR_MAP_META.set('\\');
        CHAR_MAP_META.set('*');
        CHAR_MAP_META.set('?');
        CHAR_MAP_META.set('#');
        CHAR_MAP_META.set('|');
        CHAR_MAP_META.set('{');
        CHAR_MAP_META.set('}');
        CHAR_MAP_META.set('[');
        CHAR_MAP_META.set(']');
    }

    private static final Pattern PATTERN_ORDER = Pattern.compile(
            "\\s*("
            + "(\\w+)"                              //  2 - asc
            + "|" + "(\\+\\s*(\\w+))"               //  4 - asc
            + "|" + "(-\\s*(\\w+))"                 //  6 - desc
            + "|" + "((\\w+)\\s+[Aa][Ss][Cc])"      //  8 - asc
            + "|" + "((\\w+)\\s+[Dd][Ee][Ss][Cc])"  // 10 - desc
            + ")\\s*"
            );

    private static final int[] ORDER_GROUP_INDEX = { 2, 4, 6, 8, 10 };

    private static final boolean[] ASC_MAP = { true, true, false, true, false };

    /**
     * Compiles the resource pattern for the output.
     * @param pattern the pattern string
     * @param dataType target data type
     * @return the compiled objects
     * @throws IllegalArgumentException if pattern is invalid
     * @see DirectFileOutputDescription#getResourcePattern()
     */
    public static List<CompiledResourcePattern> compileResourcePattern(String pattern, DataClass dataType) {
        if (pattern == null) {
            throw new IllegalArgumentException("pattern must not be null"); //$NON-NLS-1$
        }
        if (dataType == null) {
            throw new IllegalArgumentException("dataType must not be null"); //$NON-NLS-1$
        }
        List<CompiledResourcePattern> results = new ArrayList<CompiledResourcePattern>();
        Cursor cursor = new Cursor(pattern);
        while (cursor.isEof() == false) {
            if (cursor.isLiteral()) {
                String literal = cursor.consumeLiteral();
                results.add(new CompiledResourcePattern(literal));
            } else if (cursor.isPlaceHolder()) {
                String[] ph = cursor.consumePlaceHolder();
                DataClass.Property property = findProperty(dataType, ph[0]);
                if (property == null) {
                    cursor.rewind();
                    throw new IllegalArgumentException(MessageFormat.format(
                            "Unknown property \"{1}\": {0}",
                            cursor,
                            ph[0]));
                }
                String argument = ph[1];
                Format format = findFormat(property, argument);
                if (format== null) {
                    cursor.rewind();
                    throw new IllegalArgumentException(MessageFormat.format(
                            "Invalid format \"{1}\": {0}",
                            cursor,
                            argument == null ? "" : argument));
                }
                try {
                    format.check(property.getType(), argument);
                } catch (IllegalArgumentException e) {
                    cursor.rewind();
                    throw new IllegalArgumentException(MessageFormat.format(
                            "Invalid format \"{1}\": {0}",
                            cursor,
                            argument == null ? "" : argument), e);
                }
                results.add(new CompiledResourcePattern(property, format, argument));
            } else {
                throw new IllegalArgumentException(MessageFormat.format(
                        "Invalid character: {0}",
                        cursor));
            }
        }
        return results;
    }

    /**
     * Compiled the ordering for the output.
     * @param orders the each order representation
     * @param dataType target data type
     * @return the compiled objects
     * @throws IllegalArgumentException if pattern is invalid
     * @see DirectFileOutputDescription#getOrder()
     */
    public static List<CompiledOrder> compileOrder(List<String> orders, DataClass dataType) {
        if (orders == null) {
            throw new IllegalArgumentException("orders must not be null"); //$NON-NLS-1$
        }
        if (dataType == null) {
            throw new IllegalArgumentException("dataType must not be null"); //$NON-NLS-1$
        }
        Set<String> saw = new HashSet<String>();
        List<CompiledOrder> results = new ArrayList<CompiledOrder>();
        for (String order : orders) {
            boolean asc = false;
            String name = null;
            Matcher matcher = PATTERN_ORDER.matcher(order.trim());
            if (matcher.matches() == false) {
                throw new IllegalArgumentException(MessageFormat.format(
                        "Invalid order format: {0}",
                        order));
            }
            for (int i = 0; i < ORDER_GROUP_INDEX.length; i++) {
                int groupIndex = ORDER_GROUP_INDEX[i];
                if (matcher.group(groupIndex) != null) {
                    asc = ASC_MAP[i];
                    name = matcher.group(groupIndex);
                    break;
                }
            }
            assert name != null;
            DataClass.Property property = findProperty(dataType, name);
            if (property == null) {
                throw new IllegalArgumentException(MessageFormat.format(
                        "Unknown property \"{1}\": {0}",
                        order,
                        name));
            }
            if (saw.contains(property.getName())) {
                throw new IllegalArgumentException(MessageFormat.format(
                        "Duplicate property \"{1}\": {0}",
                        order,
                        name));
            }
            saw.add(property.getName());
            results.add(new CompiledOrder(property, asc));
        }
        return results;
    }

    private static Property findProperty(DataClass dataType, String name) {
        assert dataType != null;
        assert name != null;
        return dataType.findProperty(name);
    }

    private static Format findFormat(Property property, String argument) {
        if (argument == null) {
            return Format.NATURAL;
        }
        Type type = property.getType();
        if (type == DateOption.class) {
            return Format.DATE;
        }
        if (type == DateTimeOption.class) {
            return Format.DATETIME;
        }
        return null;
    }

    private static final class Cursor {

        private final char[] cbuf;

        private int lastSegmentPosition;

        private int position;

        Cursor(String value) {
            assert value != null;
            this.cbuf = value.toCharArray();
            this.position = 0;
        }

        boolean isEof() {
            return cbuf.length == position;
        }

        boolean isLiteral() {
            if (isEof()) {
                return false;
            }
            return CHAR_MAP_META.get(cbuf[position]) == false;
        }

        boolean isPlaceHolder() {
            if (isEof()) {
                return false;
            }
            return cbuf[position] == CHAR_BLOCK_OPEN;
        }

        void rewind() {
            this.position = lastSegmentPosition;
        }

        String consumeLiteral() {
            assert isLiteral();
            this.lastSegmentPosition = position;
            int start = position;
            while (isLiteral()) {
                char c = cbuf[position];
                if (c == CHAR_VARIABLE_START) {
                    skipVariable();
                } else if (CHAR_MAP_META.get(c) == false) {
                    advance();
                } else {
                    throw new AssertionError(c);
                }
            }
            return String.valueOf(cbuf, start, position - start);
        }

        private void skipVariable() {
            int start = position;
            assert cbuf[position] == CHAR_VARIABLE_START;
            advance();
            if (isEof() || cbuf[position] != CHAR_BLOCK_OPEN) {
                return;
            }
            advance();
            while (true) {
                if (isEof()) {
                    position = start;
                    throw new IllegalArgumentException(MessageFormat.format(
                            "Variable is not closed: {0}",
                            this));
                }
                char c = cbuf[position];
                if (c == CHAR_BLOCK_CLOSE) {
                    break;
                }
                advance();
            }
            advance();
        }

        String[] consumePlaceHolder() {
            assert isPlaceHolder();
            this.lastSegmentPosition = position;
            int start = position + 1;
            String[] results = new String[2];
            advance();
            while (true) {
                if (isEof()) {
                    position = start;
                    throw new IllegalArgumentException(MessageFormat.format(
                            "Placeholder is not closed: {0}",
                            this));
                }
                char c = cbuf[position];
                if (c == CHAR_BLOCK_CLOSE || c == CHAR_SEPARATE_IN_BLOCK) {
                    break;
                }
                advance();
            }
            results[0] = String.valueOf(cbuf, start, position - start);
            if (cbuf[position] == CHAR_SEPARATE_IN_BLOCK) {
                advance();
                int formatStart = position;
                while (true) {
                    if (isEof()) {
                        position = start;
                        throw new IllegalArgumentException(MessageFormat.format(
                                "Placeholder is not closed: {0}",
                                this));
                    }
                    char c = cbuf[position];
                    if (c == CHAR_BLOCK_CLOSE) {
                        break;
                    }
                    advance();
                }
                results[1] = String.valueOf(cbuf, formatStart, position - formatStart);
            }
            assert cbuf[position] == CHAR_BLOCK_CLOSE;
            advance();
            return results;
        }

        private void advance() {
            position = Math.min(position + 1, cbuf.length);
        }

        @Override
        public String toString() {
            StringBuilder buf = new StringBuilder();
            buf.append('[');
            for (int i = 0, n = position; i < n; i++) {
                buf.append(cbuf[i]);
            }
            buf.append(" >> ");
            for (int i = position, n = cbuf.length; i < n; i++) {
                buf.append(cbuf[i]);
            }
            buf.append(']');
            return buf.toString();
        }
    }

    /**
     * The compiled resource pattern.
     * @since 0.2.5
     */
    public static final class CompiledResourcePattern {

        private final DataClass.Property target;

        private final Format format;

        private final String argument;

        /**
         * Creates a new literal.
         * @param string literal value
         * @throws IllegalArgumentException if some parameters were {@code null}
         */
        public CompiledResourcePattern(String string) {
            if (string == null) {
                throw new IllegalArgumentException("string must not be null"); //$NON-NLS-1$
            }
            this.target = null;
            this.format = Format.PLAIN;
            this.argument = string;
        }

        /**
         * Creates a new instance.
         * @param target target
         * @param format
         * @param argument
         * @throws IllegalArgumentException if some parameters were {@code null}
         */
        public CompiledResourcePattern(DataClass.Property target, Format format, String argument) {
            if (target == null) {
                throw new IllegalArgumentException("target must not be null"); //$NON-NLS-1$
            }
            if (format == null) {
                throw new IllegalArgumentException("format must not be null"); //$NON-NLS-1$
            }
            this.target = target;
            this.format = format;
            this.argument = argument;
            format.check(target.getType(), argument);
        }

        /**
         * Returns the target property.
         * @return the target property, or {@code null} if this is a literal
         */
        public DataClass.Property getTarget() {
            return target;
        }

        /**
         * Returns the format kind.
         * @return the format
         */
        public Format getFormat() {
            return format;
        }

        /**
         * Returns the argument for the format.
         * @return the argument, or {@code null} if not defined
         */
        public String getArgument() {
            return argument;
        }
    }


    /**
     * The compiled ordering pattern.
     * @since 0.2.5
     */
    public static final class CompiledOrder {

        private final DataClass.Property target;

        private final boolean ascend;

        /**
         * Creates a new instance.
         * @param target target property
         * @param ascend whether the ordering is ascend
         * @throws IllegalArgumentException if some parameters were {@code null}
         */
        public CompiledOrder(DataClass.Property target, boolean ascend) {
            if (target == null) {
                throw new IllegalArgumentException("target must not be null"); //$NON-NLS-1$
            }
            this.target = target;
            this.ascend = ascend;
        }

        /**
         * Returns the target property.
         * @return the target property
         */
        public DataClass.Property getTarget() {
            return target;
        }

        /**
         * Returns whether the ordering is ascend.
         * @return {@code true} for ascend, or {@code false} for descend
         */
        public boolean isAscend() {
            return ascend;
        }
    }
}
