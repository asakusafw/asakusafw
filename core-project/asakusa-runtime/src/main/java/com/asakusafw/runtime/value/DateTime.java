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
 * 日付時刻に関する軽量クラス。
 */
public class DateTime implements Comparable<DateTime> {

    /**
     * 日付時刻のフォーマット。
     */
    public static final String FORMAT = "yyyy-MM-dd HH:mm:ss";

    private long elapsedSeconds = 0L;

    /**
     * 0001/01/01 00:00:00 を表すインスタンスを生成する。
     */
    public DateTime() {
        this(0L);
    }

    /**
     * インスタンスを生成する。
     * @param elapsedSeconds 0001/01/01 00:00:00 からの経過日数(0起算)
     */
    public DateTime(long elapsedSeconds) {
        this.elapsedSeconds = elapsedSeconds;
    }

    /**
     * インスタンスを生成する。
     * @param year 年 (1-...)
     * @param month 月 (1-12)
     * @param day 日 (1-31)
     * @param hour 時 (0-23)
     * @param minute 分 (0-59)
     * @param second 秒 (0-59)
     */
    public DateTime(
            int year, int month, int day,
            int hour, int minute, int second) {
        int date = DateUtil.getDayFromDate(year, month, day);
        int secondsInDay = DateUtil.getSecondFromTime(hour, minute, second);
        this.elapsedSeconds = (long) date * 86400 + secondsInDay;
    }

    /**
     * 0001/01/01 00:00:00 からの経過秒数を返す。
     * @return 0001/01/01 00:00:00 からの経過秒数(0起算)
     */
    public long getElapsedSeconds() {
        return elapsedSeconds;
    }

    /**
     * 0001/01/01 00:00:00 からの経過秒数を変更する。
     * @param elapsed 設定する経過秒数 (0起算)
     */
    public void setElapsedSeconds(long elapsed) {
        this.elapsedSeconds = elapsed;
    }

    /**
     * この日付の西暦年(1-)を返す。
     * @return この日付の西暦年
     */
    public int getYear() {
        int days = DateUtil.getDayFromSeconds(elapsedSeconds);
        return DateUtil.getYearFromDay(days);
    }

    /**
     * この日付の月(1-12)を返す。
     * @return この日付の月
     */
    public int getMonth() {
        int days = DateUtil.getDayFromSeconds(elapsedSeconds);
        int year = getYear();
        int dayInYear = days - DateUtil.getDayFromYear(year);
        return DateUtil.getMonthOfYear(dayInYear, DateUtil.isLeap(year));
    }

    /**
     * この日付の日(1-31)を返す。
     * @return この日付の日
     */
    public int getDay() {
        int year = getYear();
        int days = DateUtil.getDayFromSeconds(elapsedSeconds);
        int dayInYear = days - DateUtil.getDayFromYear(year);
        return DateUtil.getDayOfMonth(dayInYear, DateUtil.isLeap(year));
    }

    /**
     * この時刻の時間(0-23)を返す。
     * @return この時刻の時間
     */
    public int getHour() {
        int sec = DateUtil.getSecondOfDay(elapsedSeconds);
        return sec / (60 * 60);
    }

    /**
     * この時刻の分(0-59)を返す。
     * @return この時刻の分
     */
    public int getMinute() {
        int sec = DateUtil.getSecondOfDay(elapsedSeconds);
        return sec / 60 % 60;
    }

    /**
     * この時刻の秒(0-59)を返す。
     * @return この時刻の秒
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
        long a = elapsedSeconds;
        long b = o.elapsedSeconds;
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
                "%04d-%02d-%02d %02d:%02d:%02d",
                getYear(),
                getMonth(),
                getDay(),
                getHour(),
                getMinute(),
                getSecond());
    }

    /**
     * 指定の文字列を指定のフォーマットで解析し、対応する時刻を返す。
     * @param timeString 解析対象の文字列
     * @param format フォーマット形式
     * @return 対応する時刻
     * @throws IllegalArgumentException 引数が時刻を表さない場合
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
     * 指定の文字列を指定のフォーマットで解析し、対応する時刻を返す。
     * @param timeString 解析対象の文字列
     * @param format フォーマット形式
     * @return 対応する時刻
     * @throws IllegalArgumentException 引数が時刻を表さない場合
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
     * 日付のフォーマット。
     */
    public enum Format {

        /**
         * {@code YYYYMMDDhhmmss}の形式 (24時間表記)。
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
                            "YYYYMMDDhhmmss"));
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
        ;

        /**
         * 指定の文字列をこのフォーマットで解析し、0001/01/01 00:00:00からの経過秒数を返す。
         * @param timeString 対象の文字列
         * @return 0001/01/01 00:00:00からの経過秒数
         * @throws IllegalArgumentException 引数に不正な文字列が指定された場合
         */
        public abstract long parse(String timeString);

        static int get(String string, int from, int to) {
            return Integer.parseInt(string.substring(from, to));
        }
    }
}
