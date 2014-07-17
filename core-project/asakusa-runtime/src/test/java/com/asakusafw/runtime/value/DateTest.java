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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.junit.Test;

/**
 * Test for {@link Date}.
 */
public class DateTest {

    /**
     * 西暦1583-1600の範囲で、日付からyear, month, dayを正しく計算できるか確認。
     */
    @Test
    public void get1583_1600() {
        checkCalendar(1583, 1600);
    }

    /**
     * 西暦1601-1700の範囲で、日付からyear, month, dayを正しく計算できるか確認。
     */
    @Test
    public void get1601_1700() {
        checkCalendar(1601, 1700);
    }

    /**
     * 西暦1701-1800の範囲で、日付からyear, month, dayを正しく計算できるか確認。
     */
    @Test
    public void get1701_1800() {
        checkCalendar(1701, 1800);
    }

    /**
     * 西暦1801-1900の範囲で、日付からyear, month, dayを正しく計算できるか確認。
     */
    @Test
    public void get1801_1900() {
        checkCalendar(1801, 1900);
    }

    /**
     * 西暦1901-2000の範囲で、日付からyear, month, dayを正しく計算できるか確認。
     */
    @Test
    public void get1901_2000() {
        checkCalendar(1901, 2000);
    }

    /**
     * 西暦2001-2100の範囲で、日付からyear, month, dayを正しく計算できるか確認。
     */
    @Test
    public void get2001_2100() {
        checkCalendar(2001, 2100);
    }

    /**
     * 西暦2101-2200の範囲で、日付からyear, month, dayを正しく計算できるか確認。
     */
    @Test
    public void get2101_2200() {
        checkCalendar(2101, 2200);
    }

    /**
     * 西暦2201-2300の範囲で、日付からyear, month, dayを正しく計算できるか確認。
     */
    @Test
    public void get2201_2300() {
        checkCalendar(2201, 2300);
    }

    /**
     * 西暦2301-2400の範囲で、日付からyear, month, dayを正しく計算できるか確認。
     */
    @Test
    public void get2301_2400() {
        checkCalendar(2301, 2400);
    }

    /**
     * 西暦2401-2500の範囲で、日付からyear, month, dayを正しく計算できるか確認。
     */
    @Test
    public void get2401_2500() {
        checkCalendar(2401, 2500);
    }

    /**
     * 単純な文字列解析。
     */
    @Test
    public void parse() {
        Date date = Date.valueOf("12340102", Date.Format.SIMPLE);
        assertThat(date.getYear(), is(1234));
        assertThat(date.getMonth(), is(1));
        assertThat(date.getDay(), is(2));
    }

    /**
     * parses standard date string.
     */
    @Test
    public void parse_standard() {
        Date date = Date.valueOf("1234-01-02", Date.Format.STANDARD);
        assertThat(date.getYear(), is(1234));
        assertThat(date.getMonth(), is(1));
        assertThat(date.getDay(), is(2));
    }

    /**
     * 最初の日付の文字列解析。
     */
    @Test
    public void parse_zero() {
        Date date = Date.valueOf("00010101", Date.Format.SIMPLE);
        assertThat(date.getYear(), is(1));
        assertThat(date.getMonth(), is(1));
        assertThat(date.getDay(), is(1));
    }

    /**
     * 大きな日付の文字列解析。
     */
    @Test
    public void parse_big() {
        Date date = Date.valueOf("29991231", Date.Format.SIMPLE);
        assertThat(date.getYear(), is(2999));
        assertThat(date.getMonth(), is(12));
        assertThat(date.getDay(), is(31));
    }

    /**
     * optionの解析。
     */
    @Test
    public void parse_option() {
        StringOption option = new StringOption("20100615");
        Date date = Date.valueOf(option, Date.Format.SIMPLE);
        assertThat(date.getYear(), is(2010));
        assertThat(date.getMonth(), is(6));
        assertThat(date.getDay(), is(15));
    }

    /**
     * nullの解析。
     */
    @Test
    public void parse_null() {
        StringOption option = new StringOption(null);
        Date date = Date.valueOf(option, Date.Format.SIMPLE);
        assertThat(date, is(nullValue()));
    }

    void checkCalendar(int start, int end) {
        GregorianCalendar calendar =
            new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        calendar.clear();

        calendar.set(Calendar.YEAR, start);
        calendar.set(Calendar.MONTH, Calendar.JANUARY);
        calendar.set(Calendar.DATE, 1);

        Date date = new Date();
        date.setElapsedDays(DateUtil.getDayFromDate(start, 1, 1));
        while (calendar.get(Calendar.YEAR) <= end) {

            String calString = calendar.toString();
            assertThat(
                    "Year: " + calString,
                    date.getYear(), is(calendar.get(Calendar.YEAR)));
            assertThat(
                    "Month: " + calString,
                    date.getMonth(), is(calendar.get(Calendar.MONTH) + 1));
            assertThat(
                    "Date: " + calString,
                    date.getDay(), is(calendar.get(Calendar.DATE)));

            calendar.add(Calendar.DATE, 1);
            date.setElapsedDays(date.getElapsedDays() + 1);
        }
    }
}
