/**
 * Copyright 2011 Asakusa Framework Team.
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

import com.asakusafw.runtime.value.Date;
import com.asakusafw.runtime.value.DateOption;

/**
 * Test for {@link DateOption}.
 */
@SuppressWarnings("deprecation")
public class DateOptionTest extends ValueOptionTestRoot {

    /**
     * 初期値。
     */
    @Test
    public void init() {
        DateOption option = new DateOption();
        assertThat("初期値はnull",
                option.isNull(), is(true));
    }

    /**
     * intの設定。
     */
    @Test
    public void setInt() {
        DateOption option = new DateOption();
        option.modify(500);
        assertThat(option.isNull(), is(false));
        assertThat(option.get().getElapsedDays(), is(500));
    }

    /**
     * intのor。
     */
    @Test
    public void intOr() {
        DateOption option = new DateOption();
        assertThat(option.or(100), is(100));

        assertThat(option.isNull(), is(true));
        option.modify(200);
        assertThat(option.or(300), is(200));
    }

    /**
     * Dateの設定。
     */
    @Test
    public void string() {
        DateOption option = new DateOption();
        option.modify(date(999));
        assertThat(option.isNull(), is(false));
        assertThat(option.get(), is(date(999)));
    }

    /**
     * Stringのor。
     */
    @Test
    public void stringOr() {
        DateOption option = new DateOption();
        assertThat(option.or(date(200)), is(date(200)));

        assertThat(option.isNull(), is(true));
        option.modify(date(300));
        assertThat(option.or(date(400)), is(date(300)));
    }

    /**
     * modifyにnullを設定。
     */
    @Test
    public void modifyNull() {
        DateOption option = new DateOption();
        option.modify(date(100));
        assertThat(option.isNull(), is(false));

        option.modify((Date) null);
        assertThat(option.isNull(), is(true));
    }

    /**
     * copyFromでコピー。
     */
    @Test
    public void copy() {
        DateOption option = new DateOption();
        DateOption other = new DateOption();
        other.modify(date(100));

        option.copyFrom(other);
        assertThat(option.get(), is(date(100)));

        option.modify(date(200));
        assertThat(other.get(), is(date(100)));
    }

    /**
     * copyFromでnullをコピー。
     */
    @Test
    public void copyNull() {
        DateOption option = new DateOption();
        option.modify(date(100));
        DateOption other = new DateOption();

        option.copyFrom(other);
        assertThat(option.isNull(), is(true));

        option.modify(date(200));
        assertThat(option.isNull(), is(false));

        option.copyFrom(null);
        assertThat(option.isNull(), is(true));
    }

    /**
     * 順序付けのテスト。
     */
    @Test
    public void compare() {
        DateOption a = new DateOption();
        DateOption b = new DateOption();
        DateOption c = new DateOption();
        DateOption d = new DateOption();

        a.modify(date(500));
        b.modify(date(200));
        c.modify(date(499));
        d.modify(date(500));

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
     * nullに関する順序付けのテスト。
     */
    @Test
    public void compareNull() {
        DateOption a = new DateOption();
        DateOption b = new DateOption();
        DateOption c = new DateOption();

        a.modify(date(0));

        assertThat(compare(a, b), greaterThan(0));
        assertThat(compare(b, a), lessThan(0));
        assertThat(compare(b, c), is(0));
    }

    /**
     * 最大値のテスト。
     */
    @Test
    public void max() {
        DateOption a = new DateOption();
        DateOption b = new DateOption();
        DateOption c = new DateOption();

        a.modify(date(200));
        b.modify(date(100));
        c.modify(date(300));

        a.max(b);
        assertThat(a.get(), is(date(200)));
        assertThat(b.get(), is(date(100)));

        a.max(c);
        assertThat(a.get(), is(date(300)));
        assertThat(b.get(), is(date(100)));
        assertThat(c.get(), is(date(300)));
    }

    /**
     * 最小値のテスト。
     */
    @Test
    public void min() {
        DateOption a = new DateOption();
        DateOption b = new DateOption();
        DateOption c = new DateOption();

        a.modify(date(300));
        b.modify(date(200));
        c.modify(date(400));

        a.min(b);
        assertThat(a.get(), is(date(200)));
        assertThat(b.get(), is(date(200)));

        a.min(c);
        assertThat(a.get(), is(date(200)));
        assertThat(b.get(), is(date(200)));
        assertThat(c.get(), is(date(400)));
    }

    /**
     * Writable対応のテスト。
     */
    @Test
    public void writable() {
        DateOption option = new DateOption();
        option.modify(date(100));
        DateOption restored = restore(option);
        assertThat(option.get(), is(restored.get()));
    }

    /**
     * Writable対応のテスト。
     */
    @Test
    public void writable_max() {
        DateOption option = new DateOption();
        option.modify(date(Integer.MAX_VALUE));
        DateOption restored = restore(option);
        assertThat(option.get(), is(restored.get()));
    }

    /**
     * Writable対応のテスト。
     */
    @Test
    public void writable_zero() {
        DateOption option = new DateOption();
        option.modify(date(0));
        DateOption restored = restore(option);
        assertThat(option.get(), is(restored.get()));
    }

    /**
     * null-Writable対応のテスト。
     */
    @Test
    public void writableOption() {
        DateOption option = new DateOption();
        DateOption restored = restore(option);
        assertThat(restored.isNull(), is(true));
    }

    private Date date(int elapsed) {
        Date date = new Date();
        date.setElapsedDays(elapsed);
        return date;
    }
}
