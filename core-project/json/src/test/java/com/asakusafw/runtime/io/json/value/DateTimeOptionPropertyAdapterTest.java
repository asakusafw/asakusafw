/**
 * Copyright 2011-2019 Asakusa Framework Team.
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
package com.asakusafw.runtime.io.json.value;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

import com.asakusafw.runtime.io.json.PropertyAdapter;
import com.asakusafw.runtime.value.DateTime;
import com.asakusafw.runtime.value.DateTimeOption;

/**
 * Test for {@link DateTimeOptionPropertyAdapter}.
 */
public class DateTimeOptionPropertyAdapterTest {

    final DateTimeOptionPropertyAdapter.Builder builder = DateTimeOptionPropertyAdapter.builder();

    /**
     * read.
     * @throws Exception if failed
     */
    @Test
    public void read() throws Exception {
        DateTimeOptionPropertyAdapter adapter = builder.build();
        DateTimeOption option = new DateTimeOption();

        adapter.read(new MockValue("2018-12-31 23:59:59"), option);
        assertThat(option, is(new DateTimeOption(new DateTime(2018, 12, 31, 23, 59, 59))));
    }

    /**
     * read w/ invalid input.
     * @throws Exception if failed
     */
    @Test(expected = RuntimeException.class)
    public void read_invalid() throws Exception {
        DateTimeOptionPropertyAdapter adapter = builder.build();
        DateTimeOption option = new DateTimeOption();

        adapter.read(new MockValue("OUT A TIME"), option);
    }

    /**
     * write.
     * @throws Exception if failed
     */
    @Test
    public void write() throws Exception {
        DateTimeOptionPropertyAdapter adapter = builder.build();
        MockValue writer = new MockValue();

        adapter.write(new DateTimeOption(new DateTime(2019, 1, 1, 0, 0, 0)), writer);
        assertThat(writer.get(), is("2019-01-01 00:00:00"));
    }

    /**
     * parse w/ custom format - default.
     * @throws Exception if failed
     */
    @Test
    public void format() throws Exception {
        DateTimeOptionPropertyAdapter adapter = builder.build();
        equivalent(adapter, "2016-12-31 12:34:56", new DateTimeOption(dt(2016, 12, 31, 12, 34, 56)));
        equivalent(adapter, "2017-01-01 12:34:56", new DateTimeOption(dt(2017,  1,  1, 12, 34, 56)));
        equivalent(adapter, "2017-01-02 12:34:56", new DateTimeOption(dt(2017,  1,  2, 12, 34, 56)));
    }

    /**
     * parse w/ custom format - standard.
     * @throws Exception if failed
     */
    @Test
    public void format_standard() throws Exception {
        DateTimeOptionPropertyAdapter adapter = builder
                .withDateTimeFormat("yyyy'x'MM'x'dd'y'HH'z'mm'z'ss")
                .build();
        equivalent(adapter, "2016x12x31y12z34z56", new DateTimeOption(dt(2016, 12, 31, 12, 34, 56)));
        equivalent(adapter, "2017x01x01y12z34z56", new DateTimeOption(dt(2017,  1,  1, 12, 34, 56)));
        equivalent(adapter, "2017x01x02y12z34z56", new DateTimeOption(dt(2017,  1,  2, 12, 34, 56)));
    }

    /**
     * parse w/ custom format - direct.
     * @throws Exception if failed
     */
    @Test
    public void format_direct() throws Exception {
        DateTimeOptionPropertyAdapter adapter = builder
                .withDateTimeFormat("yyyyMMddHHmmss")
                .build();
        equivalent(adapter, "20161231123456", new DateTimeOption(dt(2016, 12, 31, 12, 34, 56)));
        equivalent(adapter, "20170101123456", new DateTimeOption(dt(2017,  1,  1, 12, 34, 56)));
        equivalent(adapter, "20170102123456", new DateTimeOption(dt(2017,  1,  2, 12, 34, 56)));
    }

    /**
     * parse w/ custom format - default.
     * @throws Exception if failed
     */
    @Test
    public void format_custom() throws Exception {
        DateTimeOptionPropertyAdapter adapter = builder
                .withDateTimeFormat("yyyy -- MM -- dd + HH * mm * ss")
                .build();
        equivalent(adapter, "2016 -- 12 -- 31 + 12 * 34 * 56", new DateTimeOption(dt(2016, 12, 31, 12, 34, 56)));
        equivalent(adapter, "2017 -- 01 -- 01 + 12 * 34 * 56", new DateTimeOption(dt(2017,  1,  1, 12, 34, 56)));
        equivalent(adapter, "2017 -- 01 -- 02 + 12 * 34 * 56", new DateTimeOption(dt(2017,  1,  2, 12, 34, 56)));
    }

    /**
     * w/ timezone.
     * @throws Exception if failed
     */
    @Test
    public void timezone() throws Exception {
        DateTimeOptionPropertyAdapter adapter = builder
                .withTimeZone("UTC")
                .build();
        equivalent(adapter, "2017-01-02 12:34:56", new DateTimeOption(dt(2017, 1, 2, 12 - 8, 34, 56)));
    }

    private static DateTime dt(int year, int month, int day, int hour, int minute, int second) {
        return new DateTime(year, month, day, hour, minute, second);
    }

    <T> void equivalent(PropertyAdapter<T> adapter, Object value, T property) throws IOException {
        MockValue x = new MockValue(value);
        adapter.read(x, property);
        x.set("__INVALID__");
        adapter.write(property, x);
        assertThat(x.get(), is(value));
    }
}
