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

import org.junit.Test;

/**
 * Test for {@link DateTimeOption}.
 */
@SuppressWarnings("deprecation")
public class DateTimeOptionTest extends ValueOptionTestRoot {

    /**
     * test for initial state.
     */
    @Test
    public void init() {
        DateTimeOption option = new DateTimeOption();
        assertThat(option.isNull(), is(true));
    }

    /**
     * test for set elapsed seconds.
     */
    @Test
    public void setLong() {
        DateTimeOption option = new DateTimeOption();
        option.modify(500);
        assertThat(option.isNull(), is(false));
        assertThat(option.get(), is(time(500)));
    }

    /**
     * test for or w/ int.
     */
    @Test
    public void intOr() {
        DateTimeOption option = new DateTimeOption();
        assertThat(option.or(100), is(100L));

        assertThat(option.isNull(), is(true));
        option.modify(200);
        assertThat(option.or(300), is(200L));
    }

    /**
     * test for date-time type.
     */
    @Test
    public void string() {
        DateTimeOption option = new DateTimeOption();
        option.modify(time(999));
        assertThat(option.isNull(), is(false));
        assertThat(option.get(), is(time(999)));
    }

    /**
     * test for or w/ date-time type.
     */
    @Test
    public void stringOr() {
        DateTimeOption option = new DateTimeOption();
        assertThat(option.or(time(200)), is(time(200)));

        assertThat(option.isNull(), is(true));
        option.modify(time(300));
        assertThat(option.or(time(400)), is(time(300)));
    }

    /**
     * test for modify w/ null.
     */
    @Test
    public void modifyNull() {
        DateTimeOption option = new DateTimeOption();
        option.modify(time(100));
        assertThat(option.isNull(), is(false));

        option.modify((DateTime) null);
        assertThat(option.isNull(), is(true));
    }

    /**
     * test for copyFrom.
     */
    @Test
    public void copy() {
        DateTimeOption option = new DateTimeOption();
        DateTimeOption other = new DateTimeOption();
        other.modify(time(100));

        option.copyFrom(other);
        assertThat(option.get(), is(time(100)));

        option.modify(time(200));
        assertThat(other.get(), is(time(100)));
    }

    /**
     * test for copyFrom w/ null.
     */
    @Test
    public void copyNull() {
        DateTimeOption option = new DateTimeOption();
        option.modify(time(100));
        DateTimeOption other = new DateTimeOption();

        option.copyFrom(other);
        assertThat(option.isNull(), is(true));

        option.modify(time(200));
        assertThat(option.isNull(), is(false));

        option.copyFrom(null);
        assertThat(option.isNull(), is(true));
    }

    /**
     * test for compare.
     */
    @Test
    public void compare() {
        DateTimeOption a = new DateTimeOption();
        DateTimeOption b = new DateTimeOption();
        DateTimeOption c = new DateTimeOption();
        DateTimeOption d = new DateTimeOption();

        a.modify(time(500));
        b.modify(time(200));
        c.modify(time(499));
        d.modify(time(500));

        assertThat(compare(a, b), greaterThan(0));
        assertThat(compare(b, c), lessThan(0));
        assertThat(compare(c, a), lessThan(0));
        assertThat(compare(a, c), greaterThan(0));
        assertThat(compare(b, a), lessThan(0));
        assertThat(compare(c, b), greaterThan(0));
        assertThat(compare(a, a), is(0));
        assertThat(compare(a, d), is(0));
    }

    /**
     * test for compare w/ null.
     */
    @Test
    public void compareNull() {
        DateTimeOption a = new DateTimeOption();
        DateTimeOption b = new DateTimeOption();
        DateTimeOption c = new DateTimeOption();

        a.modify(time(0));

        assertThat(compare(a, b), greaterThan(0));
        assertThat(compare(b, a), lessThan(0));
        assertThat(compare(b, c), is(0));
    }

    /**
     * test for max.
     */
    @Test
    public void max() {
        DateTimeOption a = new DateTimeOption();
        DateTimeOption b = new DateTimeOption();
        DateTimeOption c = new DateTimeOption();

        a.modify(time(200));
        b.modify(time(100));
        c.modify(time(300));

        a.max(b);
        assertThat(a.get(), is(time(200)));
        assertThat(b.get(), is(time(100)));

        a.max(c);
        assertThat(a.get(), is(time(300)));
        assertThat(b.get(), is(time(100)));
        assertThat(c.get(), is(time(300)));
    }

    /**
     * test for min.
     */
    @Test
    public void min() {
        DateTimeOption a = new DateTimeOption();
        DateTimeOption b = new DateTimeOption();
        DateTimeOption c = new DateTimeOption();

        a.modify(time(300));
        b.modify(time(200));
        c.modify(time(400));

        a.min(b);
        assertThat(a.get(), is(time(200)));
        assertThat(b.get(), is(time(200)));

        a.min(c);
        assertThat(a.get(), is(time(200)));
        assertThat(b.get(), is(time(200)));
        assertThat(c.get(), is(time(400)));
    }

    /**
     * test for Writable.
     */
    @Test
    public void writable() {
        DateTimeOption option = new DateTimeOption();
        option.modify(time(100));
        DateTimeOption restored = restore(option);
        assertThat(option.get(), is(restored.get()));
    }

    /**
     * test for Writable w/ max.
     */
    @Test
    public void writable_max() {
        DateTimeOption option = new DateTimeOption();
        option.modify(time(Long.MAX_VALUE));
        DateTimeOption restored = restore(option);
        assertThat(option.get(), is(restored.get()));
    }

    /**
     * test for Writable w/ min.
     */
    @Test
    public void writable_0() {
        DateTimeOption option = new DateTimeOption();
        option.modify(time(0));
        DateTimeOption restored = restore(option);
        assertThat(option.get(), is(restored.get()));
    }

    /**
     * test for Writable w/ null.
     */
    @Test
    public void writableOption() {
        DateTimeOption option = new DateTimeOption();
        DateTimeOption restored = restore(option);
        assertThat(restored.isNull(), is(true));
    }

    private DateTime time(long elapsed) {
        DateTime date = new DateTime();
        date.setElapsedSeconds(elapsed);
        return date;
    }
}
