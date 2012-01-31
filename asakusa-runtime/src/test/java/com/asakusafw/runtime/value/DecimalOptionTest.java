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

import java.math.BigDecimal;

import org.junit.Test;

import com.asakusafw.runtime.value.DecimalOption;

/**
 * Test for {@link DecimalOption}.
 */
@SuppressWarnings("deprecation")
public class DecimalOptionTest extends ValueOptionTestRoot {

    /**
     * 初期状態のテスト。
     */
    @Test
    public void init() {
        DecimalOption option = new DecimalOption();
        assertThat(option.isNull(), is(true));
    }

    /**
     * 値の取得。
     */
    @Test
    public void get() {
        DecimalOption option = new DecimalOption();
        option.modify(decimal(100));
        assertThat(option.get(), is(decimal(100)));
        assertThat(option.isNull(), is(false));
    }

    /**
     * nullに対するor。
     */
    @Test
    public void or() {
        DecimalOption option = new DecimalOption();
        assertThat(option.or(decimal(30)), is(decimal(30)));
        assertThat(option.isNull(), is(true));
    }

    /**
     * すでに値が設定された状態のor。
     */
    @Test
    public void orNotNull() {
        DecimalOption option = new DecimalOption();
        option.modify(decimal(100));
        assertThat(option.or(decimal(30)), is(decimal(100)));
    }

    /**
     * copyFromのテスト。
     */
    @Test
    public void copy() {
        DecimalOption option = new DecimalOption();
        DecimalOption other = new DecimalOption();
        other.modify(decimal(50));
        option.copyFrom(other);
        assertThat(option.get(), is(decimal(50)));

        option.modify(decimal(0));
        assertThat(other.get(), is(decimal(50)));
    }

    /**
     * copyFromにnullを指定するテスト。
     */
    @Test
    public void copyNull() {
        DecimalOption option = new DecimalOption();
        option.modify(decimal(100));

        DecimalOption other = new DecimalOption();
        option.copyFrom(other);
        assertThat(option.isNull(), is(true));
        option.modify(decimal(100));

        option.copyFrom(null);
        assertThat(option.isNull(), is(true));
    }

    /**
     * 比較のテスト。
     */
    @Test
    public void compareTo() {
        DecimalOption a = new DecimalOption();
        DecimalOption b = new DecimalOption();
        DecimalOption c = new DecimalOption();
        DecimalOption d = new DecimalOption();
        DecimalOption e = new DecimalOption();

        a.modify(decimal(-10));
        b.modify(decimal(0));
        c.modify(decimal(50));
        d.modify(decimal(-10));
        e.modify(decimal(-30));

        assertThat(compare(a, b), lessThan(0));
        assertThat(compare(b, c), lessThan(0));
        assertThat(compare(c, a), greaterThan(0));
        assertThat(compare(a, c), lessThan(0));
        assertThat(compare(b, a), greaterThan(0));
        assertThat(compare(c, b), greaterThan(0));
        assertThat(compare(a, d), is(0));
        assertThat(compare(a, e), greaterThan(0));
    }

    /**
     * 比較のテスト。
     */
    @Test
    public void compareTo_scale() {
        DecimalOption a1 = new DecimalOption();
        DecimalOption a2 = new DecimalOption();
        DecimalOption b1 = new DecimalOption();
        DecimalOption b2 = new DecimalOption();
        DecimalOption c1 = new DecimalOption();
        DecimalOption c2 = new DecimalOption();
        DecimalOption d1 = new DecimalOption();
        DecimalOption d2 = new DecimalOption();

        a1.modify(decimal("10000000."));
        a2.modify(decimal("10000000.0"));

        b1.modify(decimal("1."));
        b2.modify(decimal("1.0000000"));

        c1.modify(decimal("0.0000001"));
        c2.modify(decimal("0.00000010"));

        d1.modify(decimal("-1."));
        d2.modify(decimal("-1.0000000"));

        assertThat(compare(a1, a2), equalTo(0));
        assertThat(compare(b1, b2), equalTo(0));
        assertThat(compare(c1, c2), equalTo(0));
        assertThat(compare(d1, d2), equalTo(0));

        assertThat(compare(a1, b1), greaterThan(0));
        assertThat(compare(a1, b2), greaterThan(0));
        assertThat(compare(a1, c1), greaterThan(0));
        assertThat(compare(a1, c2), greaterThan(0));
        assertThat(compare(b1, c1), greaterThan(0));
        assertThat(compare(b1, c2), greaterThan(0));

        assertThat(compare(b1, a1), lessThan(0));
        assertThat(compare(b1, a2), lessThan(0));
        assertThat(compare(c1, a1), lessThan(0));
        assertThat(compare(c1, a2), lessThan(0));
        assertThat(compare(c1, b1), lessThan(0));
        assertThat(compare(c1, b2), lessThan(0));
    }

    /**
     * nullに関する順序付けのテスト。
     */
    @Test
    public void compareNull() {
        DecimalOption a = new DecimalOption();
        DecimalOption b = new DecimalOption();
        DecimalOption c = new DecimalOption();

        a.modify(decimal(Long.MIN_VALUE));

        assertThat(compare(a, b), greaterThan(0));
        assertThat(compare(b, a), lessThan(0));
        assertThat(compare(b, c), is(0));
    }

    /**
     * Writableのテスト。
     */
    @Test
    public void write() {
        DecimalOption option = new DecimalOption();
        option.modify(decimal("3.14"));
        DecimalOption restored = restore(option);
        assertThat(restored.get(), is(option.get()));
    }

    /**
     * Writableのテスト。
     */
    @Test
    public void write_max() {
        DecimalOption option = new DecimalOption();
        option.modify(decimal(Long.MAX_VALUE).add(decimal(Long.MAX_VALUE)));
        DecimalOption restored = restore(option);
        assertThat(restored.get(), is(option.get()));
    }

    /**
     * Writableのテスト。
     */
    @Test
    public void write_min() {
        DecimalOption option = new DecimalOption();
        option.modify(decimal(Long.MIN_VALUE).add(decimal(Long.MAX_VALUE)));
        DecimalOption restored = restore(option);
        assertThat(restored.get(), is(option.get()));
    }

    /**
     * null-Writableのテスト。
     */
    @Test
    public void writeNull() {
        DecimalOption option = new DecimalOption();
        DecimalOption restored = restore(option);
        assertThat(restored.isNull(), is(true));
    }

    private BigDecimal decimal(long value) {
        return new BigDecimal(value);
    }

    private BigDecimal decimal(String value) {
        return new BigDecimal(value);
    }
}
