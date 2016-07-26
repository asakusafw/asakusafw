/**
 * Copyright 2011-2016 Asakusa Framework Team.
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

import java.util.Calendar;

/**
 * Utilities about date and time.
 * @since 0.1.0
 * @version 0.7.0
 */
public final class DateUtil {

    private static final ThreadLocal<Calendar> CALENDAR_CACHE = ThreadLocal.withInitial(Calendar::getInstance);

    private static final int DAYS_YEAR = 365;

    private static final int DAYS_JANUARY = 31;
    private static final int DAYS_FEBRUARY = DAYS_JANUARY + 28;
    private static final int DAYS_MARCH = DAYS_FEBRUARY + 31;
    private static final int DAYS_APRIL = DAYS_MARCH + 30;
    private static final int DAYS_MAY = DAYS_APRIL + 31;
    private static final int DAYS_JUNE = DAYS_MAY + 30;
    private static final int DAYS_JULY = DAYS_JUNE + 31;
    private static final int DAYS_AUGUST = DAYS_JULY + 31;
    private static final int DAYS_SEPTEMBER = DAYS_AUGUST + 30;
    private static final int DAYS_OCTOBER = DAYS_SEPTEMBER + 31;
    private static final int DAYS_NOVEMBER = DAYS_OCTOBER + 30;

    private static final int[] DAYS_MONTH = {
        0,
        DAYS_JANUARY,
        DAYS_FEBRUARY,
        DAYS_MARCH,
        DAYS_APRIL,
        DAYS_MAY,
        DAYS_JUNE,
        DAYS_JULY,
        DAYS_AUGUST,
        DAYS_SEPTEMBER,
        DAYS_OCTOBER,
        DAYS_NOVEMBER,
    };

    private static final int YEARS_LEAP_CYCLE = 400;

    private static final int DAYS_LEAP_CYCLE =
        DAYS_YEAR * YEARS_LEAP_CYCLE
        + (YEARS_LEAP_CYCLE / 4)
        - (YEARS_LEAP_CYCLE / 100)
        + (YEARS_LEAP_CYCLE / 400);

    private static final int YEARS_CENTURY = 100;

    private static final int DAYS_CENTURY =
        DAYS_YEAR * YEARS_CENTURY
        + (YEARS_CENTURY / 4)
        - (YEARS_CENTURY / 100)
        + (YEARS_CENTURY / 400);

    private static final int YEARS_LEAP = 4;

    private static final int DAYS_LEAP =
        DAYS_YEAR * YEARS_LEAP
        + (YEARS_LEAP / 4)
        - (YEARS_LEAP / 100)
        + (YEARS_LEAP / 400);

    /**
     * Returns a date as the number of elapsed days from {@code 0001/01/01 (YYYY/MM/DD)}.
     * @param year year
     * @param month month (1-12)
     * @param day day (1-31)
     * @return the number of elapsed days (0-origin)
     */
    public static int getDayFromDate(int year, int month, int day) {
        int result = 0;
        result += getDayFromYear(year);
        result += DAYS_MONTH[month - 1];
        result += day - 1;
        if (month >= 3 && isLeap(year)) {
            result += 1;
        }
        return result;
    }

    /**
     * Returns a date as the number of elapsed days from {@code 0001/01/01 (YYYY/MM/DD)}.
     * @param calendar the target calendar object
     * @return the number of elapsed days (0-origin)
     * @since 0.2.2
     */
    public static int getDayFromCalendar(Calendar calendar) {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        return getDayFromDate(year, month, day);
    }

    /**
     * Returns a date as the number of elapsed days from {@code 0001/01/01 (YYYY/MM/DD)}.
     * @param date the target date object
     * @return the number of elapsed days (0-origin)
     * @since 0.7.0
     */
    public static int getDayFromDate(java.util.Date date) {
        return getDayFromCalendar(toCachedCalendar(date));
    }

    /**
     * Converts the number of elapsed days from {@code 0001/01/01 (YYYY/MM/DD)} to the corresponded date,
     * and set it to the target calendar object.
     * Note that, the hour, minute, second and millisecond fields will be set to {@code 0}.
     * @param days the number of elapsed days from {@code 0001/01/01 (YYYY/MM/DD)}
     * @param calendar the target calendar object
     * @since 0.2.2
     */
    public static void setDayToCalendar(int days, Calendar calendar) {
        int year = getYearFromDay(days);
        int daysInYear = days - getDayFromYear(year);
        boolean leap = isLeap(year);
        int month = getMonthOfYear(daysInYear, leap);
        int day = getDayOfMonth(daysInYear, leap);
        calendar.set(year, month - 1, day, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    /**
     * Converts the time as the number of elapsed seconds from {@code 00:00:00}.
     * @param hour hour
     * @param minute minute
     * @param second second
     * @return the number of elapsed seconds (0-origin)
     */
    public static int getSecondFromTime(int hour, int minute, int second) {
        int result = 0;
        result += hour * 60 * 60;
        result += minute * 60;
        result += second;
        return result;
    }

    /**
     * Converts the date and time as the number of elapsed seconds from {@code 0001/01/01 (YYYY/MM/DD) 00:00:00}.
     * @param calendar the target calendar object
     * @return the number of elapsed seconds (0-origin)
     * @since 0.2.2
     */
    public static long getSecondFromCalendar(Calendar calendar) {
        int days = getDayFromCalendar(calendar);
        long result = (long) days * 86400;
        result += calendar.get(Calendar.HOUR_OF_DAY) * 60 * 60;
        result += calendar.get(Calendar.MINUTE) * 60;
        result += calendar.get(Calendar.SECOND);
        return result;
    }

    /**
     * Converts the date and time as the number of elapsed seconds from {@code 0001/01/01 (YYYY/MM/DD) 00:00:00}.
     * @param date the target date object
     * @return the number of elapsed seconds (0-origin)
     * @since 0.7.0
     */
    public static long getSecondFromDate(java.util.Date date) {
        return getSecondFromCalendar(toCachedCalendar(date));
    }

    private static Calendar toCachedCalendar(java.util.Date date) {
        Calendar work = CALENDAR_CACHE.get();
        work.setTime(date);
        return work;
    }

    /**
     * Converts the number of elapsed seconds from {@code 0001/01/01 (YYYY/MM/DD) 00:00:00} to the corresponded date,
     * and set it to the target calendar object.
     * Note that, the millisecond fields will be set to {@code 0}.
     * @param seconds the number of elapsed seconds
     * @param calendar the target calendar object
     * @since 0.2.2
     */
    public static void setSecondToCalendar(long seconds, Calendar calendar) {
        int days = getDayFromSeconds(seconds);
        int year = getYearFromDay(days);
        int daysInYear = days - getDayFromYear(year);
        boolean leap = isLeap(year);
        int month = getMonthOfYear(daysInYear, leap);
        int day = getDayOfMonth(daysInYear, leap);

        int rest = getSecondOfDay(seconds);
        int hour = rest / (60 * 60);
        int minute = rest / 60 % 60;
        int second = rest % 60;

        calendar.set(year, month - 1, day, hour, minute, second);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    /**
     * Converts the number of elapsed days from {@code 0001/01/01 (YYYY/MM/DD)} to the corresponding year.
     * @param days the number of elapsed days
     * @return the corresponded year
     */
    public static int getYearFromDay(int days) {

        // the number of leap year cycles (400years)
        int cycles = days / DAYS_LEAP_CYCLE;
        int cycleRest = days % DAYS_LEAP_CYCLE;

        // the century offset in the current leap year cycle (0-3)
        int centInCycle = cycleRest / DAYS_CENTURY;
        int centRest = cycleRest % DAYS_CENTURY;
        centRest += DAYS_CENTURY * (centInCycle / (YEARS_LEAP_CYCLE / YEARS_CENTURY));
        centInCycle -= (centInCycle / (YEARS_LEAP_CYCLE / YEARS_CENTURY));

        // the leap year offset in the current century (0-24)
        int leapInCent = centRest / DAYS_LEAP;
        int leapRest = centRest % DAYS_LEAP;

        // the year offset since the last leap year (0-3)
        int yearInLeap = leapRest / DAYS_YEAR;
        yearInLeap -= (yearInLeap / YEARS_LEAP);

        // compute the year
        int year = YEARS_LEAP_CYCLE * cycles
            + YEARS_CENTURY * centInCycle
            + YEARS_LEAP * leapInCent
            + yearInLeap
            + 1;

        return year;
    }

    /**
     * Whether the target year is leap year or not.
     * @param year the target year
     * @return {@code true} if the target year is leap year, otherwise {@code false}
     */
    public static boolean isLeap(int year) {
        if (year % 4 != 0) {
            return false;
        }
        return (year % 100) != 0 || (year % 400) == 0;
    }

    /**
     * Converts the year to the number of elapsed days from {@code 0001/01/01 (YYYY/MM/DD)} about
     * the first day of the specified year.
     * For example, {@code getDayFromYear(1)} returns just {@code 0}.
     * @param year the target year
     * @return the number of elapsed days about the first day of the specified year
     */
    public static int getDayFromYear(int year) {
        int y = year - 1;
        return DAYS_YEAR * y + (y / 4) - (y / 100) + (y / 400);
    }

    /**
     * Converts the number of elapsed days from beginning of the year to the corresponding month.
     * @param dayOfYear the number of elapsed days from beginning of the year
     * @param leap whether the target year is leap year or not
     * @return the month including the target day (<em>1-origin</em>)
     */
    public static int getMonthOfYear(int dayOfYear, boolean leap) {
        int d = dayOfYear;
        if (d < DAYS_JANUARY) {
            return 1;
        }
        if (leap) {
            d--;
        }
        if (d < DAYS_FEBRUARY) {
            return 2;
        }
        if (d < DAYS_MARCH) {
            return 3;
        }
        if (d < DAYS_APRIL) {
            return 4;
        }
        if (d < DAYS_MAY) {
            return 5;
        }
        if (d < DAYS_JUNE) {
            return 6;
        }
        if (d < DAYS_JULY) {
            return 7;
        }
        if (d < DAYS_AUGUST) {
            return 8;
        }
        if (d < DAYS_SEPTEMBER) {
            return 9;
        }
        if (d < DAYS_OCTOBER) {
            return 10;
        }
        if (d < DAYS_NOVEMBER) {
            return 11;
        }
        return 12;
    }

    /**
     * Converts the number of elapsed days from beginning of the year to the date in the current month.
     * @param dayOfYear the number of elapsed days from beginning of the year
     * @param leap whether the target year is leap year or not
     * @return the date in the current month (1-origin)
     */
    public static int getDayOfMonth(int dayOfYear, boolean leap) {
        int d = dayOfYear;
        if (d < DAYS_JANUARY) {
            return d + 1;
        }
        if (d < DAYS_FEBRUARY) {
            return d - (DAYS_JANUARY - 1);
        }
        if (leap) {
            if (d == DAYS_FEBRUARY) {
                return 29;
            }
            d--;
        }
        if (d < DAYS_MARCH) {
            return d - (DAYS_FEBRUARY - 1);
        }
        if (d < DAYS_APRIL) {
            return d - (DAYS_MARCH - 1);
        }
        if (d < DAYS_MAY) {
            return d - (DAYS_APRIL - 1);
        }
        if (d < DAYS_JUNE) {
            return d - (DAYS_MAY - 1);
        }
        if (d < DAYS_JULY) {
            return d - (DAYS_JUNE - 1);
        }
        if (d < DAYS_AUGUST) {
            return d - (DAYS_JULY - 1);
        }
        if (d < DAYS_SEPTEMBER) {
            return d - (DAYS_AUGUST - 1);
        }
        if (d < DAYS_OCTOBER) {
            return d - (DAYS_SEPTEMBER - 1);
        }
        if (d < DAYS_NOVEMBER) {
            return d - (DAYS_OCTOBER - 1);
        }
        return d - (DAYS_NOVEMBER - 1);
    }

    /**
     * Converts the number of elapsed seconds from {@code 0001/01/01 (YYYY/MM/DD) 00:00:00} to
     * the corresponded date as the number of elapsed days.
     * @param seconds the number of elapsed seconds (0-origin)
     * @return the corresponded number of elapsed days (0-origin)
     * @see #getSecondOfDay(long)
     */
    public static int getDayFromSeconds(long seconds) {
        return (int) (seconds / 86400);
    }

    /**
     * Converts the number of elapsed seconds from {@code 0001/01/01 (YYYY/MM/DD) 00:00:00} to
     * the number of elapsed seconds from beginning of the corresponding date.
     * @param seconds the number of elapsed seconds (0-origin)
     * @return the number of elapsed seconds of the day (0-origin)
     * @see #getDayFromSeconds(long)
     */
    public static int getSecondOfDay(long seconds) {
        return (int) (seconds % 86400);
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
     * Parses a {@code Date} value.
     * @param value the date value
     * @param dateSegmentSeparator the separator char between each date segment
     * @return the days for {@link Date} object
     * @since 0.7.0
     */
    public static int parseDate(
            CharSequence value,
            char dateSegmentSeparator) {
        int length = value.length();
        if (length == COL_DAY_END
                && is(value, COL_YEAR_END, dateSegmentSeparator)
                && is(value, COL_MONTH_END, dateSegmentSeparator)) {
            int year = parse(value, COL_YEAR_BEGIN, COL_YEAR_END);
            int month = parse(value, COL_MONTH_BEGIN, COL_MONTH_END);
            int day = parse(value, COL_DAY_BEGIN, COL_DAY_END);
            if (year > 0 && month > 0 && day > 0) {
                return DateUtil.getDayFromDate(year, month, day);
            }
        }
        return -1;
    }

    /**
     * Parses a {@code DateTime} value.
     * @param value the date-time value
     * @param dateSegmentSeparator the separator char between each date segment
     * @param dateTimeSeparator the separator char between date and time
     * @param timeSegmentSeparator the separator char between each time segment
     * @return the seconds for {@link DateTime} object
     * @since 0.7.0
     */
    public static long parseDateTime(
            CharSequence value,
            char dateSegmentSeparator,
            char dateTimeSeparator,
            char timeSegmentSeparator) {
        int length = value.length();
        if (length >= COL_SECOND_END
                && is(value, COL_YEAR_END, dateSegmentSeparator)
                && is(value, COL_MONTH_END, dateSegmentSeparator)
                && is(value, COL_DAY_END, dateTimeSeparator)
                && is(value, COL_HOUR_END, timeSegmentSeparator)
                && is(value, COL_MINUTE_END, timeSegmentSeparator)) {
            int year = parse(value, COL_YEAR_BEGIN, COL_YEAR_END);
            int month = parse(value, COL_MONTH_BEGIN, COL_MONTH_END);
            int day = parse(value, COL_DAY_BEGIN, COL_DAY_END);
            int hour = parse(value, COL_HOUR_BEGIN, COL_HOUR_END);
            int minute = parse(value, COL_MINUTE_BEGIN, COL_MINUTE_END);
            int second = parse(value, COL_SECOND_BEGIN, COL_SECOND_END);
            if (year > 0 && month > 0 && day > 0
                    && hour >= 0 && minute >= 0 && second >= 0) {
                long result = DateUtil.getDayFromDate(year, month, day) * 86400L;
                result += DateUtil.getSecondFromTime(hour, minute, second);
                return result;
            }
        }
        return -1;
    }

    private static boolean is(CharSequence string, int column, char value) {
        return string.charAt(column) == value;
    }

    private static int parse(CharSequence string, int begin, int end) {
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


    /**
     * Appends a string representation of {@link Date}.
     * @param elapsedDays the number of elapsed days from 0001/01/01
     * @param dateSegmentSeparator the separator char between each date segment
     * @param target the target buffer
     * @since 0.7.0
     */
    public static void toDateString(
            int elapsedDays,
            char dateSegmentSeparator,
            StringBuilder target) {
        appendDateString(target, elapsedDays, dateSegmentSeparator);
    }

    /**
     * Appends a string representation of {@link DateTime}.
     * @param elapsedSeconds the number of elapsed seconds from 0001/01/01 00:00:00
     * @param dateSegmentSeparator the separator char between each date segment
     * @param dateTimeSeparator the separator char between date and time
     * @param timeSegmentSeparator the separator char between each time segment
     * @param target the target buffer
     * @since 0.7.0
     */
    public static void toDateTimeString(
            long elapsedSeconds,
            char dateSegmentSeparator,
            char dateTimeSeparator,
            char timeSegmentSeparator,
            StringBuilder target) {
        appendDateString(target, DateUtil.getDayFromSeconds(elapsedSeconds), dateSegmentSeparator);
        target.append(dateTimeSeparator);
        appendTimeString(target, DateUtil.getSecondOfDay(elapsedSeconds), timeSegmentSeparator);
    }

    private static void appendDateString(StringBuilder buf, int days, char separator) {
        int year = DateUtil.getYearFromDay(days);
        boolean leap = DateUtil.isLeap(year);
        int dayInYear = days - DateUtil.getDayFromYear(year);
        int month = DateUtil.getMonthOfYear(dayInYear, leap);
        int day = DateUtil.getDayOfMonth(dayInYear, DateUtil.isLeap(year));

        fill(buf, 4, year);
        buf.append(separator);
        fill(buf, 2, month);
        buf.append(separator);
        fill(buf, 2, day);
    }

    private static void appendTimeString(StringBuilder buf, int seconds, char separator) {
        int hour = seconds / (60 * 60);
        int minute = seconds / 60 % 60;
        int second = seconds % 60;
        fill(buf, 2, hour);
        buf.append(separator);
        fill(buf, 2, minute);
        buf.append(separator);
        fill(buf, 2, second);
    }

    private static void fill(StringBuilder buf, int columns, int value) {
        if (value < 0) {
            buf.append('-');
            if (value == Integer.MIN_VALUE) {
                fill(buf, columns - 2, -(value / 10));
                buf.append('8');
            } else {
                fill(buf, columns - 1, -value);
            }
        } else {
            int required = countColumns(value);
            for (int i = required; i < columns; i++) {
                buf.append('0');
            }
            buf.append(value);
        }
    }

    private static int countColumns(int value) {
        assert value >= 0;
        if (value == 0) {
            return 1;
        }
        double log = Math.log10(value);
        return (int) Math.floor(log) + 1;
    }

    private DateUtil() {
        throw new AssertionError();
    }
}
