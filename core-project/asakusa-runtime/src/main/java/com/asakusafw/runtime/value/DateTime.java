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

import java.text.MessageFormat;

/**
 * A light weight class about date and time.
 * @see DateUtil
 * @see DateTimeOption
 */
public class DateTime implements Comparable<DateTime> {

    /**
     * The default date and time format.
     */
    public static final String FORMAT = "yyyy-MM-dd HH:mm:ss"; //$NON-NLS-1$

    private long elapsedSeconds = 0L;

    /**
     * Creates a new instance which represents {@code 0001/01/01 (YYYY/MM/DD) 00:00:00}.
     */
    public DateTime() {
        this(0L);
    }

    /**
     * Creates a new instance.
     * @param elapsedSeconds the number of elapsed seconds from {@code 0001/01/01 (YYYY/MM/DD) 00:00:00} (0-origin)
     */
    public DateTime(long elapsedSeconds) {
        this.elapsedSeconds = elapsedSeconds;
    }

    /**
     * Creates a new instance.
     * @param year year (1-...)
     * @param month month (1-12)
     * @param day day (1-31)
     * @param hour hour (0-23)
     * @param minute minute (0-59)
     * @param second second (0-59)
     */
    public DateTime(int year, int month, int day, int hour, int minute, int second) {
        int date = DateUtil.getDayFromDate(year, month, day);
        int secondsInDay = DateUtil.getSecondFromTime(hour, minute, second);
        this.elapsedSeconds = (long) date * 86400 + secondsInDay;
    }

    /**
     * Returns the number of elapsed seconds from {@code 0001/01/01 (YYYY/MM/DD) 00:00:00}.
     * @return the number of elapsed seconds (0-origin)
     */
    public long getElapsedSeconds() {
        return elapsedSeconds;
    }

    /**
     * Sets the date and time as number of elapsed seconds from {@code 0001/01/01 (YYYY/MM/DD) 00:00:00}.
     * @param elapsed the elapsed seconds (0-origin)
     */
    public void setElapsedSeconds(long elapsed) {
        this.elapsedSeconds = elapsed;
    }

    /**
     * Returns the year of this date and time.
     * @return the year (1-)
     */
    public int getYear() {
        int days = DateUtil.getDayFromSeconds(elapsedSeconds);
        return DateUtil.getYearFromDay(days);
    }

    /**
     * Returns the month of this date and time.
     * @return the month (1-12)
     */
    public int getMonth() {
        int days = DateUtil.getDayFromSeconds(elapsedSeconds);
        int year = getYear();
        int dayInYear = days - DateUtil.getDayFromYear(year);
        return DateUtil.getMonthOfYear(dayInYear, DateUtil.isLeap(year));
    }

    /**
     * Returns the day of this date and time.
     * @return the day (1-31)
     */
    public int getDay() {
        int year = getYear();
        int days = DateUtil.getDayFromSeconds(elapsedSeconds);
        int dayInYear = days - DateUtil.getDayFromYear(year);
        return DateUtil.getDayOfMonth(dayInYear, DateUtil.isLeap(year));
    }

    /**
     * Returns the hour of this date time.
     * @return the hour (0-23)
     */
    public int getHour() {
        int sec = DateUtil.getSecondOfDay(elapsedSeconds);
        return sec / (60 * 60);
    }

    /**
     * Returns the minute of this date time.
     * @return the minute (0-59)
     */
    public int getMinute() {
        int sec = DateUtil.getSecondOfDay(elapsedSeconds);
        return sec / 60 % 60;
    }

    /**
     * Returns the second of this date time.
     * @return the second (0-59)
     */
    public int getSecond() {
        int sec = DateUtil.getSecondOfDay(elapsedSeconds);
        return sec % 60;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (elapsedSeconds ^ (elapsedSeconds >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        DateTime other = (DateTime) obj;
        if (elapsedSeconds != other.elapsedSeconds) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(DateTime o) {
        return Long.compare(elapsedSeconds, o.elapsedSeconds);
    }

    @Override
    public String toString() {
        return String.format(
                "%04d-%02d-%02d %02d:%02d:%02d", //$NON-NLS-1$
                getYear(),
                getMonth(),
                getDay(),
                getHour(),
                getMinute(),
                getSecond());
    }

    /**
     * Parses the target string using the specified format, and returns the corresponding date and time as the elapsed
     * seconds from {@code 0001/01/01 (YYYY/MM/DD) 00:00:00}.
     * @param timeString the target string
     * @param format the format kind
     * @return the elapsed seconds (0-origin)
     * @throws IllegalArgumentException if the target string is malformed
     */
    public static DateTime valueOf(StringOption timeString, DateTime.Format format) {
        if (timeString == null) {
            throw new IllegalArgumentException("timeString must not be null"); //$NON-NLS-1$
        }
        if (format == null) {
            throw new IllegalArgumentException("format must not be null"); //$NON-NLS-1$
        }
        if (timeString.isNull()) {
            return null;
        }
        return valueOf(timeString.getAsString(), format);
    }

    /**
     * Parses the target string using the specified format, and returns the corresponding date and time object.
     * @param timeString the target string
     * @param format the format kind
     * @return the corresponding date and time object
     * @throws IllegalArgumentException if the target string is malformed
     */
    public static DateTime valueOf(String timeString, DateTime.Format format) {
        if (timeString == null) {
            throw new IllegalArgumentException("timeString must not be null"); //$NON-NLS-1$
        }
        if (format == null) {
            throw new IllegalArgumentException("format must not be null"); //$NON-NLS-1$
        }
        DateTime time = new DateTime();
        time.setElapsedSeconds(format.parse(timeString));
        return time;
    }

    /**
     * Represents kinds of date and time formats.
     * @since 0.1.0
     * @version 0.7.0
     */
    public enum Format {

        /**
         * {@code YYYYMMDDhhmmss}.
         */
        SIMPLE {
            @Override
            public long parse(String timeString) {
                if (timeString == null) {
                    throw new IllegalArgumentException("dateString must not be null"); //$NON-NLS-1$
                }
                if (timeString.length() != 14) {
                    throw new IllegalArgumentException(MessageFormat.format(
                            "\"{0}\" is not form of \"{1}\"",
                            timeString,
                            "YYYYMMDDhhmmss")); //$NON-NLS-1$
                }
                int year = get(timeString, 0, 4);
                int month = get(timeString, 4, 6);
                int day = get(timeString, 6, 8);
                int hour = get(timeString, 8, 10);
                int minute = get(timeString, 10, 12);
                int second = get(timeString, 12, 14);

                int date = DateUtil.getDayFromDate(year, month, day);
                long seconds = (long) date * 86400 + DateUtil.getSecondFromTime(hour, minute, second);
                return seconds;
            }
        },
        /**
         * {@code YYYY-MM-DD hh:mm:ss}.
         * @since 0.7.0
         */
        STANDARD {
            @Override
            public long parse(String timeString) {
                if (timeString == null) {
                    throw new IllegalArgumentException("dateString must not be null"); //$NON-NLS-1$
                }
                long value = DateUtil.parseDateTime(timeString, '-', ' ', ':');
                if (value < 0) {
                    throw new IllegalArgumentException(MessageFormat.format(
                            "\"{0}\" is not form of \"{1}\"",
                            timeString,
                            "YYYY-MM-DD hh:mm:ss")); //$NON-NLS-1$
                }
                return value;
            }
        }
        ;

        /**
         * Parses the target string and returns the corresponding date and time as the elapsed seconds from
         * {@code 0001/01/01 (YYYY/MM/DD) 00:00:00}.
         * @param timeString the target string
         * @return the elapsed seconds (0-origin)
         * @throws IllegalArgumentException if the target string is malformed
         */
        public abstract long parse(String timeString);

        static int get(String string, int from, int to) {
            return Integer.parseInt(string.substring(from, to));
        }
    }
}
