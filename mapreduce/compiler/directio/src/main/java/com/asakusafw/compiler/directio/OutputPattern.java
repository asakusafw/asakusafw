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
package com.asakusafw.compiler.directio;

import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.asakusafw.compiler.flow.DataClass;
import com.asakusafw.compiler.flow.DataClass.Property;
import com.asakusafw.runtime.stage.directio.StringTemplate.Format;
import com.asakusafw.runtime.value.ByteOption;
import com.asakusafw.runtime.value.DateOption;
import com.asakusafw.runtime.value.DateTimeOption;
import com.asakusafw.runtime.value.DecimalOption;
import com.asakusafw.runtime.value.DoubleOption;
import com.asakusafw.runtime.value.FloatOption;
import com.asakusafw.runtime.value.IntOption;
import com.asakusafw.runtime.value.LongOption;
import com.asakusafw.runtime.value.ShortOption;
import com.asakusafw.vocabulary.directio.DirectFileOutputDescription;

/**
 * Processes patterns in {@link DirectFileOutputDescription}.
 * @since 0.2.5
 * @version 0.9.1
 */
public final class OutputPattern {

    static final int CHAR_BRACE_OPEN = '{';

    static final int CHAR_BRACE_CLOSE = '}';

    static final int CHAR_BLOCK_OPEN = '[';

    static final int CHAR_BLOCK_CLOSE = ']';

    static final int CHAR_WILDCARD = '*';

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
            "\\s*(" //$NON-NLS-1$
            + "(\\w+)"                              //  2 - asc //$NON-NLS-1$
            + "|" + "(\\+\\s*(\\w+))"               //  4 - asc //$NON-NLS-1$ //$NON-NLS-2$
            + "|" + "(-\\s*(\\w+))"                 //  6 - desc //$NON-NLS-1$ //$NON-NLS-2$
            + "|" + "((\\w+)\\s+[Aa][Ss][Cc])"      //  8 - asc //$NON-NLS-1$ //$NON-NLS-2$
            + "|" + "((\\w+)\\s+[Dd][Ee][Ss][Cc])"  // 10 - desc //$NON-NLS-1$ //$NON-NLS-2$
            + ")\\s*" //$NON-NLS-1$
            );

    private static final int[] ORDER_GROUP_INDEX = { 2, 4, 6, 8, 10 };

    private static final boolean[] ASC_MAP = { true, true, false, true, false };

    private static final Map<Class<?>, Format> FORMATS;
    static {
        Map<Class<?>, Format> map = new HashMap<>();
        map.put(ByteOption.class, Format.BYTE);
        map.put(ShortOption.class, Format.SHORT);
        map.put(IntOption.class, Format.INT);
        map.put(LongOption.class, Format.LONG);
        map.put(FloatOption.class, Format.FLOAT);
        map.put(DoubleOption.class, Format.DOUBLE);
        map.put(DecimalOption.class, Format.DECIMAL);
        map.put(DateOption.class, Format.DATE);
        map.put(DateTimeOption.class, Format.DATETIME);
        FORMATS = Collections.unmodifiableMap(map);
    }

    private OutputPattern() {
        return;
    }

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
        List<CompiledResourcePattern> results = new ArrayList<>();
        Cursor cursor = new Cursor(pattern);
        while (cursor.isEof() == false) {
            if (cursor.isLiteral()) {
                String literal = cursor.consumeLiteral();
                results.add(new CompiledResourcePattern(literal));
            } else if (cursor.isPlaceHolder()) {
                Formatted ph = cursor.consumePlaceHolder();
                DataClass.Property property = findProperty(dataType, (String) ph.original);
                if (property == null) {
                    cursor.rewind();
                    throw new IllegalArgumentException(MessageFormat.format(
                            Messages.getString("OutputPattern.errorUndefinedProperty"), //$NON-NLS-1$
                            cursor,
                            ph.original));
                }
                String argument = ph.formatString;
                Format format = findFormat(property, argument);
                if (format == null) {
                    cursor.rewind();
                    throw new IllegalArgumentException(MessageFormat.format(
                            Messages.getString("OutputPattern.errorInvalidPlaceholderFormat"), //$NON-NLS-1$
                            cursor,
                            argument == null ? "" : argument)); //$NON-NLS-1$
                }
                try {
                    format.check(property.getType(), argument);
                } catch (IllegalArgumentException e) {
                    cursor.rewind();
                    throw new IllegalArgumentException(MessageFormat.format(
                            Messages.getString("OutputPattern.errorInvalidPlaceholderFormatArgument"), //$NON-NLS-1$
                            cursor,
                            argument == null ? "" : argument), e); //$NON-NLS-1$
                }
                results.add(new CompiledResourcePattern(property, format, argument));
            } else if (cursor.isRandomNumber()) {
                Formatted rand = cursor.consumeRandomNumber();
                RandomNumber source = (RandomNumber) rand.original;
                results.add(new CompiledResourcePattern(source, Format.NATURAL, null));
            } else if (cursor.isWildcard()) {
                cursor.consumeWildcard();
                results.add(new CompiledResourcePattern());
            } else {
                throw new IllegalArgumentException(MessageFormat.format(
                        Messages.getString("OutputPattern.errorInvalidCharacter"), //$NON-NLS-1$
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
        Set<String> saw = new HashSet<>();
        List<CompiledOrder> results = new ArrayList<>();
        for (String order : orders) {
            boolean asc = false;
            String name = null;
            Matcher matcher = PATTERN_ORDER.matcher(order.trim());
            if (matcher.matches() == false) {
                throw new IllegalArgumentException(MessageFormat.format(
                        Messages.getString("OutputPattern.errorInvalidOrder"), //$NON-NLS-1$
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
                        Messages.getString("OutputPattern.errorUndefinedOrderProperty"), //$NON-NLS-1$
                        order,
                        name));
            }
            if (saw.contains(property.getName())) {
                throw new IllegalArgumentException(MessageFormat.format(
                        Messages.getString("OutputPattern.errorDuplicateOrderProperty"), //$NON-NLS-1$
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
        return FORMATS.get(type);
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
            return cbuf[position] == CHAR_BRACE_OPEN;
        }

        boolean isRandomNumber() {
            if (isEof()) {
                return false;
            }
            return cbuf[position] == CHAR_BLOCK_OPEN;
        }

        boolean isWildcard() {
            if (isEof()) {
                return false;
            }
            return cbuf[position] == CHAR_WILDCARD;
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
            if (isEof() || cbuf[position] != CHAR_BRACE_OPEN) {
                return;
            }
            advance();
            while (true) {
                if (isEof()) {
                    position = start;
                    throw new IllegalArgumentException(MessageFormat.format(
                            Messages.getString("OutputPattern.errorUnclosedVariable"), //$NON-NLS-1$
                            this));
                }
                char c = cbuf[position];
                if (c == CHAR_BRACE_CLOSE) {
                    break;
                }
                advance();
            }
            advance();
        }

        Formatted consumePlaceHolder() {
            assert isPlaceHolder();
            this.lastSegmentPosition = position;
            int start = position + 1;
            String propertyName;
            String formatString;
            advance();
            while (true) {
                if (isEof()) {
                    position = start;
                    throw new IllegalArgumentException(MessageFormat.format(
                            Messages.getString("OutputPattern.errorUnclosedPlaceholder"), //$NON-NLS-1$
                            this));
                }
                char c = cbuf[position];
                if (c == CHAR_BRACE_CLOSE || c == CHAR_SEPARATE_IN_BLOCK) {
                    break;
                }
                advance();
            }
            propertyName = String.valueOf(cbuf, start, position - start);
            if (cbuf[position] == CHAR_SEPARATE_IN_BLOCK) {
                advance();
                int formatStart = position;
                while (true) {
                    if (isEof()) {
                        position = start;
                        throw new IllegalArgumentException(MessageFormat.format(
                                Messages.getString("OutputPattern.errorUnclosedPlaceholder"), //$NON-NLS-1$
                                this));
                    }
                    char c = cbuf[position];
                    if (c == CHAR_BRACE_CLOSE) {
                        break;
                    }
                    advance();
                }
                formatString = String.valueOf(cbuf, formatStart, position - formatStart);
            } else {
                formatString = null;
            }
            assert cbuf[position] == CHAR_BRACE_CLOSE;
            advance();
            return new Formatted(propertyName, formatString);
        }

        private static final Pattern RNG = Pattern.compile("(\\d+)\\.{2,3}(\\d+)(:(.*))?"); //$NON-NLS-1$
        Formatted consumeRandomNumber() {
            assert isRandomNumber();
            this.lastSegmentPosition = position;
            int start = position + 1;
            while (true) {
                if (isEof()) {
                    position = start;
                    throw new IllegalArgumentException(MessageFormat.format(
                            Messages.getString("OutputPattern.errorUnclosedRandomNumber"), //$NON-NLS-1$
                            this));
                }
                char c = cbuf[position];
                if (c == CHAR_BLOCK_CLOSE) {
                    break;
                }
                advance();
            }
            String content = String.valueOf(cbuf, start, position - start);
            Matcher matcher = RNG.matcher(content);
            if (matcher.matches() == false) {
                position = start;
                throw new IllegalArgumentException(MessageFormat.format(
                        Messages.getString("OutputPattern.errorInvalidRandomNumber"), //$NON-NLS-1$
                        this));
            }
            int lower;
            try {
                lower = Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                position = start + matcher.start(1);
                throw new IllegalArgumentException(MessageFormat.format(
                        Messages.getString("OutputPattern.errorInvalidRandomNumberArgument"), //$NON-NLS-1$
                        this), e);
            }
            int upper;
            try {
                upper = Integer.parseInt(matcher.group(2));
            } catch (NumberFormatException e) {
                position = start + matcher.start(2);
                throw new IllegalArgumentException(MessageFormat.format(
                        Messages.getString("OutputPattern.errorInvalidRandomNumberArgument"), //$NON-NLS-1$
                        this), e);
            }
            if (lower >= upper) {
                position = start + matcher.start(1);
                throw new IllegalArgumentException(MessageFormat.format(
                        Messages.getString("OutputPattern.errorInvalidRandomNumberRange"), //$NON-NLS-1$
                        this));
            }

            String format = matcher.group(4);
            advance();
            return new Formatted(new RandomNumber(lower, upper), format);
        }

        void consumeWildcard() {
            assert isWildcard();
            advance();
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
            buf.append(" >> "); //$NON-NLS-1$
            for (int i = position, n = cbuf.length; i < n; i++) {
                buf.append(cbuf[i]);
            }
            buf.append(']');
            return buf.toString();
        }
    }

    private static class Formatted {

        final Object original;

        final String formatString;

        Formatted(Object original, String formatString) {
            this.original = original;
            this.formatString = formatString;
        }
    }

    /**
     * The compiled resource pattern.
     * @since 0.2.5
     */
    public static final class CompiledResourcePattern {

        private final SourceKind kind;

        private final Object source;

        private final Format format;

        private final String argument;

        /**
         * Creates a new wildcard.
         * @since 0.4.0
         */
        public CompiledResourcePattern() {
            this.kind = SourceKind.ENVIRONMENT;
            this.source = null;
            this.format = Format.PLAIN;
            this.argument = null;
        }

        /**
         * Creates a new literal.
         * @param string literal value
         * @throws IllegalArgumentException if some parameters were {@code null}
         */
        public CompiledResourcePattern(String string) {
            if (string == null) {
                throw new IllegalArgumentException("string must not be null"); //$NON-NLS-1$
            }
            this.kind = SourceKind.NOTHING;
            this.source = null;
            this.format = Format.PLAIN;
            this.argument = string;
        }

        /**
         * Creates a new instance.
         * @param target target property
         * @param format format kind
         * @param argument format argument (nullable)
         * @throws IllegalArgumentException if some parameters were {@code null}
         */
        public CompiledResourcePattern(DataClass.Property target, Format format, String argument) {
            if (target == null) {
                throw new IllegalArgumentException("target must not be null"); //$NON-NLS-1$
            }
            if (format == null) {
                throw new IllegalArgumentException("format must not be null"); //$NON-NLS-1$
            }
            this.kind = SourceKind.PROPERTY;
            this.source = target;
            this.format = format;
            this.argument = argument;
            format.check(target.getType(), argument);
        }

        /**
         * Creates a new instance.
         * @param source the source object
         * @param format format kind
         * @param argument format argument (nullable)
         * @throws IllegalArgumentException if some parameters were {@code null}
         */
        public CompiledResourcePattern(RandomNumber source, Format format, String argument) {
            if (source == null) {
                throw new IllegalArgumentException("source must not be null"); //$NON-NLS-1$
            }
            if (format == null) {
                throw new IllegalArgumentException("format must not be null"); //$NON-NLS-1$
            }
            this.kind = SourceKind.RANDOM;
            this.source = source;
            this.format = format;
            this.argument = argument;
        }

        /**
         * Returns the kind of the souce of this fragment.
         * @return the kind
         * @since 0.2.6
         */
        public SourceKind getKind() {
            return kind;
        }

        /**
         * Returns the source of this fragment.
         * @return the source, or {@code null} if the source is not specified
         * @since 0.2.6
         */
        public Object getSource() {
            return source;
        }

        /**
         * Returns the target property.
         * @return the target property, or {@code null} if the source is not a property
         * @see #getSource()
         */
        public DataClass.Property getTarget() {
            if (kind != SourceKind.PROPERTY) {
                return null;
            }
            return (DataClass.Property) source;
        }

        /**
         * Returns the random number specification.
         * @return the random number spec, or {@code null} if the source is not a random number
         * @see #getSource()
         * @since 0.2.6
         */
        public RandomNumber getRandomNumber() {
            if (kind != SourceKind.RANDOM) {
                return null;
            }
            return (RandomNumber) source;
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

    /**
     * The source kind.
     * @since 0.2.6
     */
    public enum SourceKind {

        /**
         * Source is nothing (for literals).
         */
        NOTHING,

        /**
         * Source is a property.
         * @see com.asakusafw.compiler.flow.DataClass.Property
         */
        PROPERTY,

        /**
         * Source is a random number generator.
         * @see RandomNumber
         */
        RANDOM,

        /**
         * Source is from current Environment ID.
         * @since 0.4.0
         */
        ENVIRONMENT,
    }

    /**
     * Represents a random number.
     * @since 0.2.6
     */
    public static class RandomNumber {

        private final int lowerBound;

        private final int upperBound;

        /**
         * Creates a new instance.
         * @param lowerBound the lower bound (inclusive)
         * @param upperBound the upper bound (inclusive)
         */
        public RandomNumber(int lowerBound, int upperBound) {
            this.lowerBound = lowerBound;
            this.upperBound = upperBound;
        }

        /**
         * Returns the lower bound of this random number.
         * @return the lower bound (inclusive)
         */
        public int getLowerBound() {
            return lowerBound;
        }

        /**
         * Returns the upper bound of this random number.
         * @return the upper bound (inclusive)
         */
        public int getUpperBound() {
            return upperBound;
        }
    }
}
