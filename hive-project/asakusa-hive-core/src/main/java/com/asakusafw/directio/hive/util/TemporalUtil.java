/**
 * Copyright 2011-2014 Asakusa Framework Team.
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
package com.asakusafw.directio.hive.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.BitSet;

import com.asakusafw.runtime.value.Date;
import com.asakusafw.runtime.value.DateTime;
import com.asakusafw.runtime.value.DateUtil;

/**
 * date and date-time utilities.
 * @since 0.7.0
 */
public final class TemporalUtil {

    private static final ThreadLocal<DateFormat> DATE_FORMAT_CACHE = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd");
        }
    };

    private static final ThreadLocal<DateFormat> TIMESTAMP_FORMAT_CACHE = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }
    };

    private static final ThreadLocal<StringBuilder> STRING_BUILDER_CACHE = new ThreadLocal<StringBuilder>() {
        @Override
        protected StringBuilder initialValue() {
            return new StringBuilder(32);
        }
    };

    private static final char DATE_SEGMENT_SEPARATOR = '-';

    private static final char DATE_TIME_SEPARATOR = ' ';

    private static final char TIME_SEGMENT_SEPARATOR = ':';

    private static final BitSet SEPARATOR_CHAR = new BitSet();
    static {
        SEPARATOR_CHAR.set(' ');
        SEPARATOR_CHAR.set('-');
        SEPARATOR_CHAR.set('/');
        SEPARATOR_CHAR.set(':');
        SEPARATOR_CHAR.set('\'');
        SEPARATOR_CHAR.set('.');
    }

    private static final int COL_YEAR_BEGIN = 0;

    private static final int COL_YEAR_END = COL_YEAR_BEGIN + 4;

    private static final int COL_MONTH_BEGIN = COL_YEAR_END + 1;

    private static final int COL_MONTH_END = COL_MONTH_BEGIN + 2;

    private static final int COL_DAY_BEGIN = COL_MONTH_END + 1;

    private static final int COL_DAY_END = COL_DAY_BEGIN + 2;

    private static final int COL_HOUR_BEGIN = COL_DAY_END + 1;

    private static final int COL_HOUR_END = COL_HOUR_BEGIN + 2;

    private static final int COL_MINUTE_BEGIN = COL_HOUR_END + 1;

    private static final int COL_MINUTE_END = COL_MINUTE_BEGIN + 2;

    private static final int COL_SECOND_BEGIN = COL_MINUTE_END + 1;

    private static final int COL_SECOND_END = COL_SECOND_BEGIN + 2;

    /**
     * Parses a {@code date} value.
     * @param value the date value
     * @return the days for {@link Date} object
     */
    public static int parseDate(String value) {
        int length = value.length();
        if (length >= COL_DAY_END
                && isSeparator(value, COL_YEAR_END)
                && isSeparator(value, COL_MONTH_END)
                && (length == COL_DAY_END || isSeparator(value, COL_DAY_END))) {
            int year = parse(value, COL_YEAR_BEGIN, COL_YEAR_END);
            int month = parse(value, COL_MONTH_BEGIN, COL_MONTH_END);
            int day = parse(value, COL_DAY_BEGIN, COL_DAY_END);
            if (year > 0 && month > 0 && day > 0) {
                return DateUtil.getDayFromDate(year, month, day);
            }
        }
        try {
            java.util.Date date = DATE_FORMAT_CACHE.get().parse(value);
            return DateUtil.getDayFromDate(date);
        } catch (ParseException e) {
            return -1;
        }
    }

    /**
     * Returns a string representation of {@link Date}.
     * @param elapsedDays the elapsed days from 0001/01/01
     * @return string representation
     */
    public static String toDateString(int elapsedDays) {
        StringBuilder buf = STRING_BUILDER_CACHE.get();
        buf.setLength(0);
        appendDateString(buf, elapsedDays);
        return buf.toString();
    }

    /**
     * Returns a string representation of {@link DateTime}.
     * @param elapsedSeconds the elapsed seconds from 0001/01/01 00:00:00
     * @return string representation
     */
    public static String toTimestampString(long elapsedSeconds) {
        StringBuilder buf = STRING_BUILDER_CACHE.get();
        buf.setLength(0);
        appendDateString(buf, DateUtil.getDayFromSeconds(elapsedSeconds));
        buf.append(DATE_TIME_SEPARATOR);
        appendTimeString(buf, DateUtil.getSecondOfDay(elapsedSeconds));
        return buf.toString();
    }

    private static void appendDateString(StringBuilder buf, int days) {
        int year = DateUtil.getYearFromDay(days);
        boolean leap = DateUtil.isLeap(year);
        int dayInYear = days - DateUtil.getDayFromYear(year);
        int month = DateUtil.getMonthOfYear(dayInYear, leap);
        int day = DateUtil.getDayOfMonth(dayInYear, DateUtil.isLeap(year));

        fill(buf, 4, year);
        buf.append(DATE_SEGMENT_SEPARATOR);
        fill(buf, 2, month);
        buf.append(DATE_SEGMENT_SEPARATOR);
        fill(buf, 2, day);
    }

    private static void appendTimeString(StringBuilder buf, int seconds) {
        int hour = seconds / (60 * 60);
        int minute = seconds / 60 % 60;
        int second = seconds % 60;
        fill(buf, 2, hour);
        buf.append(TIME_SEGMENT_SEPARATOR);
        fill(buf, 2, minute);
        buf.append(TIME_SEGMENT_SEPARATOR);
        fill(buf, 2, second);
    }

    private static void fill(StringBuilder buf, int columns, int value) {
        int required = countColumns(value);
        for (int i = required; i < columns; i++) {
            buf.append('0');
        }
        buf.append(value);
    }

    private static int countColumns(int value) {
        if (value == 0) {
            return 1;
        } else if (value < 0) {
            return 1 + countColumns(-value);
        }
        double log = Math.log10(value);
        return (int) Math.floor(log) + 1;
    }

    /**
     * Parses a {@code timestamp} value.
     * @param value the timestamp value
     * @return the seconds for {@link DateTime} object
     */
    public static long parseTimestamp(String value) {
        int length = value.length();
        if (length >= COL_SECOND_END
                && isSeparator(value, COL_YEAR_END)
                && isSeparator(value, COL_MONTH_END)
                && isSeparator(value, COL_DAY_END)
                && isSeparator(value, COL_HOUR_END)
                && isSeparator(value, COL_MINUTE_END)
                && (length == COL_SECOND_END || isSeparator(value, COL_SECOND_END))) {
            int year = parse(value, COL_YEAR_BEGIN, COL_YEAR_END);
            int month = parse(value, COL_MONTH_BEGIN, COL_MONTH_END);
            int day = parse(value, COL_DAY_BEGIN, COL_DAY_END);
            int hour = parse(value, COL_HOUR_BEGIN, COL_HOUR_END);
            int minute = parse(value, COL_MINUTE_BEGIN, COL_MINUTE_END);
            int second = parse(value, COL_SECOND_BEGIN, COL_SECOND_END);
            if (year > 0 && month > 0 && day > 0
                    && hour > 0 && minute > 0 && second > 0) {
                long result = DateUtil.getDayFromDate(year, month, day) * 86400L;
                result += DateUtil.getSecondFromTime(hour, minute, second);
                return result;
            }
        }
        try {
            java.util.Date date = TIMESTAMP_FORMAT_CACHE.get().parse(value);
            return DateUtil.getSecondFromDate(date);
        } catch (ParseException e) {
            return -1;
        }
    }

    private static boolean isSeparator(String string, int column) {
        char c = string.charAt(column);
        return SEPARATOR_CHAR.get(c);
    }

    private static int parse(String string, int begin, int end) {
        int result = 0;
        for (int i = begin; i < end; i++) {
            char c = string.charAt(i);
            if (c < '0' || '9' < c) {
                return -1;
            }
            result = (result * 10) + (c - '0');
        }
        return result;
    }


    private TemporalUtil() {
        return;
    }
}
