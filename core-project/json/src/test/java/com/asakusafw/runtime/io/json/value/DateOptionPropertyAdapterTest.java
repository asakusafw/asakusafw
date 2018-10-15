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
package com.asakusafw.runtime.io.json.value;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

import com.asakusafw.runtime.io.json.PropertyAdapter;
import com.asakusafw.runtime.value.Date;
import com.asakusafw.runtime.value.DateOption;

/**
 * Test for {@link DateOptionPropertyAdapter}.
 */
public class DateOptionPropertyAdapterTest {

    final DateOptionPropertyAdapter.Builder builder = DateOptionPropertyAdapter.builder();

    /**
     * read.
     * @throws Exception if failed
     */
    @Test
    public void read() throws Exception {
        DateOptionPropertyAdapter adapter = builder.build();
        DateOption option = new DateOption();
        adapter.read(new MockValue("2018-12-31"), option);
        assertThat(option, is(new DateOption(new Date(2018, 12, 31))));
    }

    /**
     * read w/ invalid input.
     * @throws Exception if failed
     */
    @Test(expected = RuntimeException.class)
    public void read_invalid() throws Exception {
        DateOptionPropertyAdapter adapter = builder.build();
        DateOption option = new DateOption();
        adapter.read(new MockValue("OUT A TIME"), option);
    }

    /**
     * write.
     * @throws Exception if failed
     */
    @Test
    public void write() throws Exception {
        DateOptionPropertyAdapter adapter = builder.build();
        MockValue writer = new MockValue();

        adapter.write(new DateOption(new Date(2019, 1, 1)), writer);
        assertThat(writer.get(), is("2019-01-01"));
    }

    /**
     * parse w/ default format.
     * @throws Exception if failed
     */
    @Test
    public void format() throws Exception {
        DateOptionPropertyAdapter adapter = builder.build();
        equivalent(adapter, "2016-12-31", new DateOption(new Date(2016, 12, 31)));
        equivalent(adapter, "2017-01-01", new DateOption(new Date(2017,  1,  1)));
        equivalent(adapter, "2017-01-02", new DateOption(new Date(2017,  1,  2)));
    }

    /**
     * parse w/ custom format - standard.
     * @throws Exception if failed
     */
    @Test
    public void format_standard() throws Exception {
        DateOptionPropertyAdapter adapter = builder
                .withDateFormat("yyyy'x'MM'x'dd")
                .build();
        equivalent(adapter, "2016x12x31", new DateOption(new Date(2016, 12, 31)));
        equivalent(adapter, "2017x01x01", new DateOption(new Date(2017,  1,  1)));
        equivalent(adapter, "2017x01x02", new DateOption(new Date(2017,  1,  2)));
    }

    /**
     * parse w/ custom format - direct.
     * @throws Exception if failed
     */
    @Test
    public void format_direct() throws Exception {
        DateOptionPropertyAdapter adapter = builder
                .withDateFormat("yyyyMMdd")
                .build();
        equivalent(adapter, "20161231", new DateOption(new Date(2016, 12, 31)));
        equivalent(adapter, "20170101", new DateOption(new Date(2017,  1,  1)));
        equivalent(adapter, "20170102", new DateOption(new Date(2017,  1,  2)));
    }

    /**
     * parse w/ custom format - default.
     * @throws Exception if failed
     */
    @Test
    public void format_custom() throws Exception {
        DateOptionPropertyAdapter adapter = builder
                .withDateFormat("yyyy -- MM -- dd")
                .build();
        equivalent(adapter, "2016 -- 12 -- 31", new DateOption(new Date(2016, 12, 31)));
        equivalent(adapter, "2017 -- 01 -- 01", new DateOption(new Date(2017,  1,  1)));
        equivalent(adapter, "2017 -- 01 -- 02", new DateOption(new Date(2017,  1,  2)));
    }

    <T> void equivalent(PropertyAdapter<T> adapter, Object value, T property) throws IOException {
        MockValue x = new MockValue(value);
        adapter.read(x, property);
        x.set("__INVALID__");
        adapter.write(property, x);
        assertThat(x.get(), is(value));
    }
}
