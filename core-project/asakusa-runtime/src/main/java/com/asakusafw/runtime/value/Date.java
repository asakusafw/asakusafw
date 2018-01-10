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
 * A light weight class about date.
 * @see DateUtil
 * @see DateOption
 */
public class Date implements Comparable<Date> {

    /**
     * The default date format.
     */
    public static final String FORMAT = "yyyy-MM-dd"; //$NON-NLS-1$

    private int elapsedDays = 0;

    /**
     * Creates a new instance which represents {@code 0001/01/01 (YYYY/MM/DD)}.
     */
    public Date() {
        this(0);
    }

    /**
     * Creates a new instance.
     * @param year year (1-...)
     * @param month month (1-12)
     * @param day day (1-31)
     */
    public Date(int year, int month, int day) {
        this(DateUtil.getDayFromDate(year, month, day));
    }

    /**
     * Creates a new instance.
     * @param elapsedDays the number of elapsed days from {@code 0001/01/01 (YYYY/MM/DD)} (0-origin)
     */
    public Date(int elapsedDays) {
        this.elapsedDays = elapsedDays;
    }

    /**
     * Returns the number of elapsed days from {@code 0001/01/01 (YYYY/MM/DD)}.
     * @return the number of elapsed days (0-origin)
     */
    public int getElapsedDays() {
        return elapsedDays;
    }

    /**
     * Sets the date as number of elapsed days from {@code 0001/01/01}.
     * @param elapsed the elapsed days (0-origin)
     */
    public void setElapsedDays(int elapsed) {
        this.elapsedDays = elapsed;
    }

    /**
     * Returns the year of this date.
     * @return the year (1-)
     */
    public int getYear() {
        return DateUtil.getYearFromDay(elapsedDays);
    }

    /**
     * Returns the month of this date.
     * @return the month (1-12)
     */
    public int getMonth() {
        int year = getYear();
        int dayInYear = elapsedDays - DateUtil.getDayFromYear(year);
        return DateUtil.getMonthOfYear(dayInYear, DateUtil.isLeap(year));
    }

    /**
     * Returns the day of this date.
     * @return the day (1-31)
     */
    public int getDay() {
        int year = getYear();
        int dayInYear = elapsedDays - DateUtil.getDayFromYear(year);
        return DateUtil.getDayOfMonth(dayInYear, DateUtil.isLeap(year));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + elapsedDays;
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
        Date other = (Date) obj;
        if (elapsedDays != other.elapsedDays) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(Date o) {
        return Integer.compare(elapsedDays, o.elapsedDays);
    }

    @Override
    public String toString() {
        return String.format(
                "%04d-%02d-%02d", //$NON-NLS-1$
                getYear(),
                getMonth(),
                getDay());
    }

    /**
     * Parses the target string and returns the corresponding date as the elapsed days from
     * {@code 0001/01/01 (YYYY/MM/DD)}.
     * @param dateString the target string
     * @param format the format kind
     * @return the elapsed days (0-origin)
     * @throws IllegalArgumentException if the target string is malformed
     */
    public static Date valueOf(StringOption dateString, Date.Format format) {
        if (dateString == null) {
            throw new IllegalArgumentException("dateString must not be null"); //$NON-NLS-1$
        }
        if (format == null) {
            throw new IllegalArgumentException("format must not be null"); //$NON-NLS-1$
        }
        if (dateString.isNull()) {
            return null;
        }
        return valueOf(dateString.getAsString(), format);
    }

    /**
     * Parses the target string and returns the corresponding date object.
     * @param dateString the target string
     * @param format the format kind
     * @return the corresponding date object
     * @throws IllegalArgumentException if the target string is malformed
     */
    public static Date valueOf(String dateString, Date.Format format) {
        if (dateString == null) {
            throw new IllegalArgumentException("dateString must not be null"); //$NON-NLS-1$
        }
        if (format == null) {
            throw new IllegalArgumentException("format must not be null"); //$NON-NLS-1$
        }
        Date date = new Date();
        date.setElapsedDays(format.parse(dateString));
        return date;
    }

    /**
     * Represents kinds of date formats.
     * @since 0.1.0
     * @version 0.7.0
     */
    public enum Format {

        /**
         * {@code YYYYMMDD}.
         */
        SIMPLE {
            @Override
            public int parse(String dateString) {
                if (dateString == null) {
                    throw new IllegalArgumentException("dateString must not be null"); //$NON-NLS-1$
                }
                if (dateString.length() != 8) {
                    throw new IllegalArgumentException(MessageFormat.format(
                            "\"{0}\" is not form of \"{1}\"",
                            dateString,
                            "YYYYMMDD")); //$NON-NLS-1$
                }
                int year = get(dateString, 0, 4);
                int month = get(dateString, 4, 6);
                int day = get(dateString, 6, 8);
                return DateUtil.getDayFromDate(year, month, day);
            }
        },

        /**
         * {@code YYYY-MM-DD}.
         * @since 0.7.0
         */
        STANDARD {
            @Override
            public int parse(String dateString) {
                if (dateString == null) {
                    throw new IllegalArgumentException("dateString must not be null"); //$NON-NLS-1$
                }
                int value = DateUtil.parseDate(dateString, '-');
                if (value < 0) {
                    throw new IllegalArgumentException(MessageFormat.format(
                            "\"{0}\" is not form of \"{1}\"",
                            dateString,
                            "YYYY-MM-DD")); //$NON-NLS-1$
                }
                return value;
            }
        },
        ;

        /**
         * Parses the target string and returns the corresponding date as the elapsed days from
         * {@code 0001/01/01 (YYYY/MM/DD)}.
         * @param dateString the target string
         * @return the elapsed days (0-origin)
         * @throws IllegalArgumentException if the target string is malformed
         */
        public abstract int parse(String dateString);

        static int get(String string, int from, int to) {
            return Integer.parseInt(string.substring(from, to));
        }
    }
}
