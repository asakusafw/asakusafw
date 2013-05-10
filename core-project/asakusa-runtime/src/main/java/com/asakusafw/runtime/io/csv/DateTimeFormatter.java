/**
 * Copyright 2011-2013 Asakusa Framework Team.
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
package com.asakusafw.runtime.io.csv;

import java.nio.CharBuffer;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.asakusafw.runtime.value.DateUtil;

/**
 * DateTime formatter.
 * @since 0.4.0
 */
abstract class DateTimeFormatter {

    private static final DateTimeFormatter[] BUILTIN = new DateTimeFormatter[] {
        new Direct(),
    };

    abstract String getPattern();

    abstract long parse(CharSequence sequence);

    abstract CharSequence format(long elapsedSeconds);

    static DateTimeFormatter newInstance(String pattern) {
        for (DateTimeFormatter f : BUILTIN) {
            if (f.getPattern().equals(pattern)) {
                return f;
            }
        }
        return new Default(new SimpleDateFormat(pattern));
    }

    private static final class Default extends DateTimeFormatter {

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
        long parse(CharSequence sequence) {
            parsePositionBuffer.setIndex(0);
            parsePositionBuffer.setErrorIndex(-1);
            java.util.Date parsed = format.parse(sequence.toString(), parsePositionBuffer);
            if (parsePositionBuffer.getIndex() == 0) {
                return -1;
            }
            calendarBuffer.setTime(parsed);
            return DateUtil.getSecondFromCalendar(calendarBuffer);
        }

        @Override
        String format(long elapsedSeconds) {
            DateUtil.setSecondToCalendar(elapsedSeconds, calendarBuffer);
            return format.format(calendarBuffer.getTime());
        }
    }

    private static final class Direct extends DateTimeFormatter {

        private static final int POS_YEAR = 0;

        private static final int POS_MONTH = 4;

        private static final int POS_DAY = 6;

        private static final int POS_HOUR = 8;

        private static final int POS_MINUTE = 10;

        private static final int POS_SECOND = 12;

        private static final int LENGTH = 14;

        private final CharBuffer buffer;

        Direct() {
            buffer = CharBuffer.allocate(LENGTH);
        }

        @Override
        String getPattern() {
            return "yyyyMMddHHmmss";
        }

        @Override
        CharSequence format(long elapsedSeconds) {
            int elapsedDate = DateUtil.getDayFromSeconds(elapsedSeconds);
            int year = DateUtil.getYearFromDay(elapsedDate);
            int dayInYear = elapsedDate - DateUtil.getDayFromYear(year);
            int month = DateUtil.getMonthOfYear(dayInYear, DateUtil.isLeap(year));
            int day = DateUtil.getDayOfMonth(dayInYear, DateUtil.isLeap(year));

            int secondOfDay = DateUtil.getSecondOfDay(elapsedSeconds);
            int hour = secondOfDay / (60 * 60);
            int minute = secondOfDay / 60 % 60;
            int second = secondOfDay % 60;

            putStringValue(buffer, year, POS_YEAR, 4);
            putStringValue(buffer, month, POS_MONTH, 2);
            putStringValue(buffer, day, POS_DAY, 2);
            putStringValue(buffer, hour, POS_HOUR, 2);
            putStringValue(buffer, minute, POS_MINUTE, 2);
            putStringValue(buffer, second, POS_SECOND, 2);
            return buffer;
        }

        @Override
        long parse(CharSequence sequence) {
            if (sequence.length() != LENGTH) {
                return -1;
            }
            int year = getNumericValue(sequence, POS_YEAR, 4);
            int month = getNumericValue(sequence, POS_MONTH, 2);
            int day = getNumericValue(sequence, POS_DAY, 2);
            int hour = getNumericValue(sequence, POS_HOUR, 2);
            int minute = getNumericValue(sequence, POS_MINUTE, 2);
            int second = getNumericValue(sequence, POS_SECOND, 2);
            if (year < 0 || month < 0 || day < 0 || hour < 0 || minute < 0 || second < 0) {
                return -1;
            }
            int date = DateUtil.getDayFromDate(year, month, day);
            int secondsInDay = DateUtil.getSecondFromTime(hour, minute, second);
            return (long) date * 86400 + secondsInDay;
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

    static void putStringValue(CharBuffer buffer, int value, int from, int length) {
        int to = from + length;
        int current = value;
        for (int i = to - 1; i >= from; i--) {
            char c = (char) (current % 10 + '0');
            current = current / 10;
            buffer.put(i, c);
        }
    }
}
