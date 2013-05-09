/**
 * Copyright 2011-2013 Asakusa Framework Team.
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
 * Test for {@link LongOption}.
 */
@SuppressWarnings("deprecation")
public class LongOptionTest extends ValueOptionTestRoot {

    /**
     * 初期状態のテスト。
     */
    @Test
    public void init() {
        LongOption option = new LongOption();
        assertThat(option.isNull(), is(true));
    }

    /**
     * 値の取得。
     */
    @Test
    public void get() {
        LongOption option = new LongOption();
        option.modify(100);
        assertThat(option.get(), is(100L));
        assertThat(option.isNull(), is(false));
    }

    /**
     * nullに対するor。
     */
    @Test
    public void or() {
        LongOption option = new LongOption();
        assertThat(option.or(30), is(30L));
        assertThat(option.isNull(), is(true));
    }

    /**
     * すでに値が設定された状態のor。
     */
    @Test
    public void orNotNull() {
        LongOption option = new LongOption();
        option.modify(100);
        assertThat(option.or(30), is(100L));
    }

    /**
     * copyFromのテスト。
     */
    @Test
    public void copy() {
        LongOption option = new LongOption();
        LongOption other = new LongOption();
        other.modify(50);
        option.copyFrom(other);
        assertThat(option.get(), is(50L));

        option.modify(0);
        assertThat(other.get(), is(50L));
    }

    /**
     * copyFromにnullを指定するテスト。
     */
    @Test
    public void copyNull() {
        LongOption option = new LongOption();
        option.modify(100);

        LongOption other = new LongOption();
        option.copyFrom(other);
        assertThat(option.isNull(), is(true));
        option.modify(100);

        option.copyFrom(null);
        assertThat(option.isNull(), is(true));
    }

    /**
     * 比較のテスト。
     */
    @Test
    public void compareTo() {
        LongOption a = new LongOption();
        LongOption b = new LongOption();
        LongOption c = new LongOption();
        LongOption d = new LongOption();

        a.modify(Long.MIN_VALUE);
        b.modify(0);
        c.modify(50);
        d.modify(Long.MIN_VALUE);

        assertThat(compare(a, b), lessThan(0));
        assertThat(compare(b, c), lessThan(0));
        assertThat(compare(c, a), greaterThan(0));
        assertThat(compare(a, c), lessThan(0));
        assertThat(compare(b, a), greaterThan(0));
        assertThat(compare(c, b), greaterThan(0));
        assertThat(compare(a, d), is(0));
    }

    /**
     * nullに関する順序付けのテスト。
     */
    @Test
    public void compareNull() {
        LongOption a = new LongOption();
        LongOption b = new LongOption();
        LongOption c = new LongOption();

        a.modify(Long.MIN_VALUE);

        assertThat(compare(a, b), greaterThan(0));
        assertThat(compare(b, a), lessThan(0));
        assertThat(compare(b, c), is(0));
    }

    /**
     * Writableのテスト。
     */
    @Test
    public void write() {
        LongOption option = new LongOption();
        option.modify(100);
        LongOption restored = restore(option);
        assertThat(restored.get(), is(option.get()));
    }

    /**
     * Writableのテスト。
     */
    @Test
    public void write_max() {
        LongOption option = new LongOption();
        option.modify(Long.MAX_VALUE);
        LongOption restored = restore(option);
        assertThat(restored.get(), is(option.get()));
    }

    /**
     * Writableのテスト。
     */
    @Test
    public void write_min() {
        LongOption option = new LongOption();
        option.modify(Long.MIN_VALUE);
        LongOption restored = restore(option);
        assertThat(restored.get(), is(option.get()));
    }

    /**
     * null-Writableのテスト。
     */
    @Test
    public void writeNull() {
        LongOption option = new LongOption();
        LongOption restored = restore(option);
        assertThat(restored.isNull(), is(true));
    }
}
