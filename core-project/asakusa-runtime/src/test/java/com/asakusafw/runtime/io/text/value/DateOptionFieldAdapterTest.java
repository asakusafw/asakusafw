/**
 * Copyright 2011-2017 Asakusa Framework Team.
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

import org.junit.Test;

import com.asakusafw.runtime.value.Date;
import com.asakusafw.runtime.value.DateOption;

/**
 * Test for {@link DateOptionFieldAdapter}.
 */
public class DateOptionFieldAdapterTest {

    /**
     * parse.
     */
    @Test
    public void parse() {
        DateOptionFieldAdapter adapter = DateOptionFieldAdapter.builder().build();
        checkParse(adapter, "2016-12-31", new DateOption(new Date(2016, 12, 31)));
        checkParse(adapter, "2017-01-01", new DateOption(new Date(2017,  1,  1)));
        checkParse(adapter, "2017-01-02", new DateOption(new Date(2017,  1,  2)));
    }

    /**
     * parse - empty.
     */
    @Test
    public void parse_empty() {
        DateOptionFieldAdapter adapter = DateOptionFieldAdapter.builder().build();
        checkMalformed(adapter, "", new DateOption());
    }

    /**
     * parse - invalid.
     */
    @Test
    public void parse_invalid() {
        DateOptionFieldAdapter adapter = DateOptionFieldAdapter.builder().build();
        checkMalformed(adapter, "Hello, world!", new DateOption());
    }

    /**
     * parse - null.
     */
    @Test
    public void parse_null() {
        DateOptionFieldAdapter adapter = DateOptionFieldAdapter.builder()
                .withNullFormat("")
                .build();
        checkParse(adapter, "", new DateOption());
    }

    /**
     * emit.
     */
    @Test
    public void emit() {
        DateOptionFieldAdapter adapter = DateOptionFieldAdapter.builder().build();
        checkEmit(adapter, new DateOption(new Date(2016, 12, 31)), "2016-12-31");
        checkEmit(adapter, new DateOption(new Date(2017,  1,  1)), "2017-01-01");
        checkEmit(adapter, new DateOption(new Date(2017,  1,  2)), "2017-01-02");
    }

    /**
     * emit - null.
     */
    @Test
    public void emit_null() {
        DateOptionFieldAdapter adapter = DateOptionFieldAdapter.builder().build();
        checkEmit(adapter, new DateOption(), null);
    }

    /**
     * emit - null w/ format.
     */
    @Test
    public void emit_null_format() {
        DateOptionFieldAdapter adapter = DateOptionFieldAdapter.builder()
                .withNullFormat("")
                .build();
        checkEmit(adapter, new DateOption(), "");
    }

    /**
     * parse w/ custom format - default.
     */
    @Test
    public void parse_format_default() {
        DateOptionFieldAdapter adapter = DateOptionFieldAdapter.builder()
                .withDateFormat("yyyy'x'MM'x'dd")
                .build();
        equivalent(adapter, "2016x12x31", new DateOption(new Date(2016, 12, 31)));
        equivalent(adapter, "2017x01x01", new DateOption(new Date(2017,  1,  1)));
        equivalent(adapter, "2017x01x02", new DateOption(new Date(2017,  1,  2)));
    }

    /**
     * parse w/ custom format - direct.
     */
    @Test
    public void parse_format_direct() {
        DateOptionFieldAdapter adapter = DateOptionFieldAdapter.builder()
                .withDateFormat("yyyyMMdd")
                .build();
        equivalent(adapter, "20161231", new DateOption(new Date(2016, 12, 31)));
        equivalent(adapter, "20170101", new DateOption(new Date(2017,  1,  1)));
        equivalent(adapter, "20170102", new DateOption(new Date(2017,  1,  2)));
    }

    /**
     * parse w/ custom format - default.
     */
    @Test
    public void parse_format_custom() {
        DateOptionFieldAdapter adapter = DateOptionFieldAdapter.builder()
                .withDateFormat("yyyy -- MM -- dd")
                .build();
        equivalent(adapter, "2016 -- 12 -- 31", new DateOption(new Date(2016, 12, 31)));
        equivalent(adapter, "2017 -- 01 -- 01", new DateOption(new Date(2017,  1,  1)));
        equivalent(adapter, "2017 -- 01 -- 02", new DateOption(new Date(2017,  1,  2)));
    }
}
