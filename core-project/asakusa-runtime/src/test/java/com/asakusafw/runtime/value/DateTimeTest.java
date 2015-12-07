/**
 * Copyright 2011-2015 Asakusa Framework Team.
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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.junit.Test;

/**
 * Test for {@link DateTime}.
 */
public class DateTimeTest {

    /**
     * check fields between 1583-1600.
     */
    @Test
    public void get1583_1600() {
        checkCalendar(1583, 1600);
    }

    /**
     * check fields between 1601-1700.
     */
    @Test
    public void get1601_1700() {
        checkCalendar(1601, 1700);
    }

    /**
     * check fields between 1701-1800.
     */
    @Test
    public void get1701_1800() {
        checkCalendar(1701, 1800);
    }

    /**
     * check fields between 1801-1900.
     */
    @Test
    public void get1801_1900() {
        checkCalendar(1801, 1900);
    }

    /**
     * check fields between 1901-2000.
     */
    @Test
    public void get1901_2000() {
        checkCalendar(1901, 2000);
    }

    /**
     * check fields between 2001-2100.
     */
    @Test
    public void get2001_2100() {
        checkCalendar(2001, 2100);
    }

    /**
     * check fields between 2101-2200.
     */
    @Test
    public void get2101_2200() {
        checkCalendar(2101, 2200);
    }

    /**
     * check fields between 2201-2300.
     */
    @Test
    public void get2201_2300() {
        checkCalendar(2201, 2300);
    }

    /**
     * check fields between 2301-2400.
     */
    @Test
    public void get2301_2400() {
        checkCalendar(2301, 2400);
    }

    /**
     * check fields between 2401-2500.
     */
    @Test
    public void get2401_2500() {
        checkCalendar(2401, 2500);
    }

    /**
     * test for parse.
     */
    @Test
    public void parse() {
        DateTime time = DateTime.valueOf("12340102123456", DateTime.Format.SIMPLE);
        assertThat(time.getYear(), is(1234));
        assertThat(time.getMonth(), is(1));
        assertThat(time.getDay(), is(2));
        assertThat(time.getHour(), is(12));
        assertThat(time.getMinute(), is(34));
        assertThat(time.getSecond(), is(56));
    }

    /**
     * parses standard date-time string.
     */
    @Test
    public void parse_standard() {
        DateTime time = DateTime.valueOf("1234-01-02 12:34:56", DateTime.Format.STANDARD);
        assertThat(time.getYear(), is(1234));
        assertThat(time.getMonth(), is(1));
        assertThat(time.getDay(), is(2));
        assertThat(time.getHour(), is(12));
        assertThat(time.getMinute(), is(34));
        assertThat(time.getSecond(), is(56));
    }

    /**
     * parses standard date-time string.
     */
    @Test
    public void parse_standard_w_zeros() {
        DateTime time = DateTime.valueOf("0001-01-01 00:00:00", DateTime.Format.STANDARD);
        assertThat(time.getYear(), is(1));
        assertThat(time.getMonth(), is(1));
        assertThat(time.getDay(), is(1));
        assertThat(time.getHour(), is(0));
        assertThat(time.getMinute(), is(0));
        assertThat(time.getSecond(), is(0));
    }

    /**
     * parse epoch date.
     */
    @Test
    public void parse_zero() {
        DateTime time = DateTime.valueOf("00010101000000", DateTime.Format.SIMPLE);
        assertThat(time.getYear(), is(1));
        assertThat(time.getMonth(), is(1));
        assertThat(time.getDay(), is(1));
        assertThat(time.getHour(), is(0));
        assertThat(time.getMinute(), is(0));
        assertThat(time.getSecond(), is(0));
    }

    /**
     * parse long-term future date.
     */
    @Test
    public void parse_big() {
        DateTime time = DateTime.valueOf("29991231235959", DateTime.Format.SIMPLE);
        assertThat(time.getYear(), is(2999));
        assertThat(time.getMonth(), is(12));
        assertThat(time.getDay(), is(31));
        assertThat(time.getHour(), is(23));
        assertThat(time.getMinute(), is(59));
        assertThat(time.getSecond(), is(59));
    }

    /**
     * parse w/ option value.
     */
    @Test
    public void parse_option() {
        StringOption option = new StringOption("20100615112233");
        DateTime time = DateTime.valueOf(option, DateTime.Format.SIMPLE);
        assertThat(time.getYear(), is(2010));
        assertThat(time.getMonth(), is(6));
        assertThat(time.getDay(), is(15));
        assertThat(time.getHour(), is(11));
        assertThat(time.getMinute(), is(22));
        assertThat(time.getSecond(), is(33));
    }

    /**
     * parse w/ null.
     */
    @Test
    public void parse_null() {
        StringOption option = new StringOption(null);
        DateTime time = DateTime.valueOf(option, DateTime.Format.SIMPLE);
        assertThat(time, is(nullValue()));
    }

    void checkCalendar(int start, int end) {
        GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        calendar.clear();

        calendar.set(Calendar.YEAR, start);
        calendar.set(Calendar.MONTH, Calendar.JANUARY);
        calendar.set(Calendar.DATE, 1);

        DateTime time = new DateTime();
        time.setElapsedSeconds((long) DateUtil.getDayFromDate(start, 1, 1) * 86400);
        while (calendar.get(Calendar.YEAR) <= end) {

            String calString = calendar.toString();
            assertThat(
                    "Year: " + calString,
                    time.getYear(), is(calendar.get(Calendar.YEAR)));
            assertThat(
                    "Month: " + calString,
                    time.getMonth(), is(calendar.get(Calendar.MONTH) + 1));
            assertThat(
                    "Date: " + calString,
                    time.getDay(), is(calendar.get(Calendar.DATE)));
            assertThat(
                    "Hour: " + calString,
                    time.getHour(), is(calendar.get(Calendar.HOUR_OF_DAY)));
            assertThat(
                    "Minute: " + calString,
                    time.getMinute(), is(calendar.get(Calendar.MINUTE)));
            assertThat(
                    "Second: " + calString,
                    time.getSecond(), is(calendar.get(Calendar.SECOND)));

            calendar.add(Calendar.SECOND, 65537);
            time.setElapsedSeconds(time.getElapsedSeconds() + 65537);
        }
    }
}
