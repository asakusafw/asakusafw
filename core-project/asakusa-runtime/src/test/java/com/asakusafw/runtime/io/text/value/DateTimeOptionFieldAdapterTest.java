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
package com.asakusafw.runtime.io.text.value;

import static com.asakusafw.runtime.io.text.value.TestUtil.*;

import java.util.TimeZone;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExternalResource;

import com.asakusafw.runtime.value.DateTime;
import com.asakusafw.runtime.value.DateTimeOption;

/**
 * Test for {@link DateTimeOptionFieldAdapter}.
 */
public class DateTimeOptionFieldAdapterTest {

    /**
     * Escapes time zone.
     */
    @Rule
    public final ExternalResource escapeTimeZone = new ExternalResource() {
        TimeZone escape;
        @Override
        protected void before() {
            escape = TimeZone.getDefault();
            TimeZone.setDefault(TimeZone.getTimeZone("PST"));
        }
        @Override
        protected void after() {
            if (escape != null) {
                TimeZone.setDefault(escape);
            }
        }
    };

    /**
     * parse.
     */
    @Test
    public void parse() {
        DateTimeOptionFieldAdapter adapter = DateTimeOptionFieldAdapter.builder().build();
        checkParse(adapter, "2016-12-31 12:34:56", new DateTimeOption(dt(2016, 12, 31, 12, 34, 56)));
        checkParse(adapter, "2017-01-01 12:34:56", new DateTimeOption(dt(2017,  1,  1, 12, 34, 56)));
        checkParse(adapter, "2017-01-02 12:34:56", new DateTimeOption(dt(2017,  1,  2, 12, 34, 56)));
    }

    /**
     * parse - empty.
     */
    @Test
    public void parse_empty() {
        DateTimeOptionFieldAdapter adapter = DateTimeOptionFieldAdapter.builder().build();
        checkMalformed(adapter, "", new DateTimeOption());
    }

    /**
     * parse - invalid.
     */
    @Test
    public void parse_invalid() {
        DateTimeOptionFieldAdapter adapter = DateTimeOptionFieldAdapter.builder().build();
        checkMalformed(adapter, "Hello, world!", new DateTimeOption());
    }

    /**
     * parse - null.
     */
    @Test
    public void parse_null() {
        DateTimeOptionFieldAdapter adapter = DateTimeOptionFieldAdapter.builder()
                .withNullFormat("")
                .build();
        checkParse(adapter, "", new DateTimeOption());
    }

    /**
     * emit.
     */
    @Test
    public void emit() {
        DateTimeOptionFieldAdapter adapter = DateTimeOptionFieldAdapter.builder().build();
        checkEmit(adapter, new DateTimeOption(dt(2016, 12, 31, 12, 34, 56)), "2016-12-31 12:34:56");
        checkEmit(adapter, new DateTimeOption(dt(2017,  1,  1, 12, 34, 56)), "2017-01-01 12:34:56");
        checkEmit(adapter, new DateTimeOption(dt(2017,  1,  2, 12, 34, 56)), "2017-01-02 12:34:56");
    }

    /**
     * emit - null.
     */
    @Test
    public void emit_null() {
        DateTimeOptionFieldAdapter adapter = DateTimeOptionFieldAdapter.builder().build();
        checkEmit(adapter, new DateTimeOption(), null);
    }

    /**
     * emit - null w/ format.
     */
    @Test
    public void emit_null_format() {
        DateTimeOptionFieldAdapter adapter = DateTimeOptionFieldAdapter.builder()
                .withNullFormat("")
                .build();
        checkEmit(adapter, new DateTimeOption(), "");
    }

    /**
     * parse w/ custom format - default.
     */
    @Test
    public void parse_format_default() {
        DateTimeOptionFieldAdapter adapter = DateTimeOptionFieldAdapter.builder()
                .withDateTimeFormat("yyyy'x'MM'x'dd'y'HH'z'mm'z'ss")
                .build();
        equivalent(adapter, "2016x12x31y12z34z56", new DateTimeOption(dt(2016, 12, 31, 12, 34, 56)));
        equivalent(adapter, "2017x01x01y12z34z56", new DateTimeOption(dt(2017,  1,  1, 12, 34, 56)));
        equivalent(adapter, "2017x01x02y12z34z56", new DateTimeOption(dt(2017,  1,  2, 12, 34, 56)));
    }

    /**
     * parse w/ custom format - direct.
     */
    @Test
    public void parse_format_direct() {
        DateTimeOptionFieldAdapter adapter = DateTimeOptionFieldAdapter.builder()
                .withDateTimeFormat("yyyyMMddHHmmss")
                .build();
        equivalent(adapter, "20161231123456", new DateTimeOption(dt(2016, 12, 31, 12, 34, 56)));
        equivalent(adapter, "20170101123456", new DateTimeOption(dt(2017,  1,  1, 12, 34, 56)));
        equivalent(adapter, "20170102123456", new DateTimeOption(dt(2017,  1,  2, 12, 34, 56)));
    }

    /**
     * parse w/ custom format - default.
     */
    @Test
    public void parse_format_custom() {
        DateTimeOptionFieldAdapter adapter = DateTimeOptionFieldAdapter.builder()
                .withDateTimeFormat("yyyy -- MM -- dd + HH * mm * ss")
                .build();
        equivalent(adapter, "2016 -- 12 -- 31 + 12 * 34 * 56", new DateTimeOption(dt(2016, 12, 31, 12, 34, 56)));
        equivalent(adapter, "2017 -- 01 -- 01 + 12 * 34 * 56", new DateTimeOption(dt(2017,  1,  1, 12, 34, 56)));
        equivalent(adapter, "2017 -- 01 -- 02 + 12 * 34 * 56", new DateTimeOption(dt(2017,  1,  2, 12, 34, 56)));
    }

    /**
     * w/ timezone.
     */
    @Test
    public void timezone() {
        DateTimeOptionFieldAdapter adapter = DateTimeOptionFieldAdapter.builder()
                .withTimeZone("UTC")
                .build();
        equivalent(adapter, "2017-01-02 12:34:56", new DateTimeOption(dt(2017, 1, 2, 12 - 8, 34, 56)));
    }

    private static DateTime dt(int year, int month, int day, int hour, int minute, int second) {
        return new DateTime(year, month, day, hour, minute, second);
    }
}
