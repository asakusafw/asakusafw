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
package com.asakusafw.runtime.value;

import java.text.MessageFormat;

/**
 * 日付に関する軽量クラス。
 */
public class Date implements Comparable<Date> {

    /**
     * 日付のフォーマット。
     */
    public static final String FORMAT = "yyyy-MM-dd"; //$NON-NLS-1$

    private int elapsed = 0;

    /**
     * 0001/01/01を表すインスタンスを生成する。
     */
    public Date() {
        this(0);
    }

    /**
     * インスタンスを生成する。
     * @param year 年 (1-...)
     * @param month 月 (1-12)
     * @param day 日 (1-31)
     */
    public Date(int year, int month, int day) {
        this(DateUtil.getDayFromDate(year, month, day));
    }

    /**
     * インスタンスを生成する。
     * @param elapsedDays 表す日付の0001/01/01からの経過日数(0起算)
     */
    public Date(int elapsedDays) {
        this.elapsed = elapsedDays;
    }

    /**
     * 0001/01/01 からの経過日数を返す。
     * @return 0001/01/01 からの経過日数(0起算)
     */
    public int getElapsedDays() {
        return elapsed;
    }

    /**
     * 0001/01/01 からの経過日数を変更する。
     * @param days 設定する経過日数(0起算)
     */
    public void setElapsedDays(int days) {
        this.elapsed = days;
    }

    /**
     * この日付の西暦年(1-)を返す。
     * @return この日付の西暦年
     */
    public int getYear() {
        return DateUtil.getYearFromDay(elapsed);
    }

    /**
     * この日付の月(1-12)を返す。
     * @return この日付の月
     */
    public int getMonth() {
        int year = getYear();
        int dayInYear = elapsed - DateUtil.getDayFromYear(year);
        return DateUtil.getMonthOfYear(dayInYear, DateUtil.isLeap(year));
    }

    /**
     * この日付の日(1-31)を返す。
     * @return この日付の日
     */
    public int getDay() {
        int year = getYear();
        int dayInYear = elapsed - DateUtil.getDayFromYear(year);
        return DateUtil.getDayOfMonth(dayInYear, DateUtil.isLeap(year));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + elapsed;
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
        if (elapsed != other.elapsed) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(Date o) {
        int a = elapsed;
        int b = o.elapsed;
        if (a == b) {
            return 0;
        }
        if (a < b) {
            return -1;
        }
        return +1;
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
     * 指定の文字列を指定のフォーマットで解析し、対応する日付を返す。
     * @param dateString 解析対象の文字列
     * @param format フォーマット形式
     * @return 対応する日付、文字列が{@code null}を表す場合は{@code null}
     * @throws IllegalArgumentException 引数が日付を表さない場合
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
     * 指定の文字列を指定のフォーマットで解析し、対応する日付を返す。
     * @param dateString 解析対象の文字列
     * @param format フォーマット形式
     * @return 対応する日付
     * @throws IllegalArgumentException 引数が日付を表さない場合
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
     * 日付のフォーマット。
     * @since 0.1.0
     * @version 0.7.0
     */
    public enum Format {

        /**
         * {@code YYYYMMDD}の形式。
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
         * {@code YYYY-MM-DD}の形式。
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
         * 指定の文字列をこのフォーマットで解析し、0001/01/01からの経過日数を返す。
         * @param dateString 対象の文字列
         * @return 0001/01/01からの経過日数
         * @throws IllegalArgumentException 引数に不正な文字列が指定された場合
         */
        public abstract int parse(String dateString);

        static int get(String string, int from, int to) {
            return Integer.parseInt(string.substring(from, to));
        }
    }
}
