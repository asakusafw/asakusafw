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
package com.asakusafw.runtime.io.json.value;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.asakusafw.runtime.value.DateUtil;

abstract class DateAdapter {

    private static final Factory[] BUILTIN = new Factory[] {
        pattern -> {
            if (pattern.equals(Direct.PATTERN)) {
                return new Direct();
            }
            return null;
        },
        pattern -> Standard.of(pattern),
        pattern -> new Default(new SimpleDateFormat(pattern)),
    };

    abstract String getPattern();

    abstract int parse(CharSequence sequence);

    abstract void emit(int elapsedDate, StringBuilder output);

    static DateAdapter newInstance(String pattern) {
        for (Factory f : BUILTIN) {
            DateAdapter formatter = f.of(pattern);
            if (formatter != null) {
                return formatter;
            }
        }
        throw new AssertionError(pattern);
    }

    @FunctionalInterface
    private interface Factory {
        DateAdapter of(String pattern);
    }

    private static final class Default extends DateAdapter {

        private final SimpleDateFormat format;

        private final Calendar calendarBuffer = Calendar.getInstance();

        private final ParsePosition parsePositionBuffer = new ParsePosition(0);

        Default(SimpleDateFormat format) {
            assert format != null;
            this.format = format;
        }

        @Override
        String getPattern() {
            return format.toPattern();
        }

        @Override
        int parse(CharSequence sequence) {
            ParsePosition pos = parsePositionBuffer;
            pos.setIndex(0);
            pos.setErrorIndex(-1);
            java.util.Date parsed = format.parse(sequence.toString(), pos);
            if (pos.getIndex() != sequence.length() || pos.getErrorIndex() >= 0) {
                return -1;
            }
            calendarBuffer.setTime(parsed);
            return DateUtil.getDayFromCalendar(calendarBuffer);
        }

        @Override
        void emit(int elapsedDate, StringBuilder output) {
            DateUtil.setDayToCalendar(elapsedDate, calendarBuffer);
            output.append(format.format(calendarBuffer.getTime()));
        }
    }

    private static final class Standard extends DateAdapter {

        private static final Pattern META_PATTERN = Pattern.compile(
                "yyyy([ \\-\\._/]|'[a-zA-Z \\-\\._/]')MM\\1dd"); //$NON-NLS-1$

        private final DateAdapter next;

        private final char separator;

        private Standard(String pattern, char separator) {
            this.separator = separator;
            this.next = new Default(new SimpleDateFormat(pattern));
        }

        static Standard of(String pattern) {
            Matcher matcher = META_PATTERN.matcher(pattern);
            if (matcher.matches()) {
                char separator = extract(matcher, 1);
                return new Standard(pattern, separator);
            }
            return null;
        }

        private static char extract(Matcher matcher, int group) {
            String value = matcher.group(group);
            if (value.length() == 1) {
                return value.charAt(0);
            }
            if (value.length() == 3) {
                if (value.charAt(0) != '\'' && value.charAt(2) != '\'') {
                    throw new IllegalStateException();
                }
                return value.charAt(1);
            }
            throw new IllegalStateException();
        }

        @Override
        String getPattern() {
            return next.getPattern();
        }

        @Override
        int parse(CharSequence sequence) {
            int value = DateUtil.parseDate(sequence, separator);
            if (value >= 0) {
                return value;
            }
            return next.parse(sequence);
        }

        @Override
        void emit(int elapsedDate, StringBuilder output) {
            DateUtil.toDateString(elapsedDate, separator, output);
        }
    }

    private static final class Direct extends DateAdapter {

        private static final String PATTERN = "yyyyMMdd"; //$NON-NLS-1$

        private static final int POS_YEAR = 0;

        private static final int POS_MONTH = 4;

        private static final int POS_DAY = 6;

        private static final int LENGTH = 8;

        private final DateAdapter next;

        Direct() {
            this.next = new Default(new SimpleDateFormat(PATTERN));
        }

        @Override
        String getPattern() {
            return next.getPattern();
        }

        @Override
        int parse(CharSequence sequence) {
            if (sequence.length() != LENGTH) {
                return next.parse(sequence);
            }
            int year = getNumericValue(sequence, POS_YEAR, 4);
            int month = getNumericValue(sequence, POS_MONTH, 2);
            int day = getNumericValue(sequence, POS_DAY, 2);
            if (year < 0 || month < 0 || day < 0) {
                return next.parse(sequence);
            }
            return DateUtil.getDayFromDate(year, month, day);
        }

        @Override
        void emit(int elapsedDate, StringBuilder output) {
            int year = DateUtil.getYearFromDay(elapsedDate);
            int dayInYear = elapsedDate - DateUtil.getDayFromYear(year);
            int month = DateUtil.getMonthOfYear(dayInYear, DateUtil.isLeap(year));
            int day = DateUtil.getDayOfMonth(dayInYear, DateUtil.isLeap(year));
            append(output, year, 4);
            append(output, month, 2);
            append(output, day, 2);
        }
    }

    static int getNumericValue(CharSequence sequence, int from, int length) {
        int to = from + length;
        int result = 0;
        for (int i = from; i < to; i++) {
            char c = (char) (sequence.charAt(i) - '0');
            if (c > 9) {
                return -1;
            }
            result = result * 10 + c;
        }
        return result;
    }

    static void append(StringBuilder buf, int value, int columns) {
        if (value < 0) {
            throw new IllegalArgumentException();
        }
        int required = countColumns(value);
        for (int i = required; i < columns; i++) {
            buf.append('0');
        }
        buf.append(value);
    }

    private static int countColumns(int value) {
        assert value >= 0;
        if (value == 0) {
            return 1;
        }
        double log = Math.log10(value);
        return (int) Math.floor(log) + 1;
    }
}
