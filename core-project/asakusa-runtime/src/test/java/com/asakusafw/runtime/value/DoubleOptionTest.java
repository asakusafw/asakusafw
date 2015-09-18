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
 * Test for {@link DoubleOption}.
 */
@SuppressWarnings("deprecation")
public class DoubleOptionTest extends ValueOptionTestRoot {

    /**
     * test for initial state.
     */
    @Test
    public void init() {
        DoubleOption option = new DoubleOption();
        assertThat(option.isNull(), is(true));
    }

    /**
     * test for get.
     */
    @Test
    public void get() {
        DoubleOption option = new DoubleOption();
        option.modify(100);
        assertThat(option.get(), is(100.d));
        assertThat(option.isNull(), is(false));
    }

    /**
     * test for or w/ absent value.
     */
    @Test
    public void or() {
        DoubleOption option = new DoubleOption();
        assertThat(option.or(30), is(30.d));
        assertThat(option.isNull(), is(true));
    }

    /**
     * test for or w/ present value.
     */
    @Test
    public void orNotNull() {
        DoubleOption option = new DoubleOption();
        option.modify(100);
        assertThat(option.or(30), is(100.d));
    }

    /**
     * test for copyFrom.
     */
    @Test
    public void copy() {
        DoubleOption option = new DoubleOption();
        DoubleOption other = new DoubleOption();
        other.modify(50);
        option.copyFrom(other);
        assertThat(option.get(), is(50.d));

        option.modify(0);
        assertThat(other.get(), is(50.d));
    }

    /**
     * test for copyFrom w/ null.
     */
    @Test
    public void copyNull() {
        DoubleOption option = new DoubleOption();
        option.modify(100);

        DoubleOption other = new DoubleOption();
        option.copyFrom(other);
        assertThat(option.isNull(), is(true));
        option.modify(100);

        option.copyFrom(null);
        assertThat(option.isNull(), is(true));
    }

    /**
     * test for compare.
     */
    @Test
    public void compareTo() {
        DoubleOption a = new DoubleOption();
        DoubleOption b = new DoubleOption();
        DoubleOption c = new DoubleOption();
        DoubleOption d = new DoubleOption();
        DoubleOption e = new DoubleOption();

        a.modify(-10);
        b.modify(0);
        c.modify(50);
        d.modify(-10);
        e.modify(-30);

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
     * test for compare w/ null.
     */
    @Test
    public void compareNull() {
        DoubleOption a = new DoubleOption();
        DoubleOption b = new DoubleOption();
        DoubleOption c = new DoubleOption();

        a.modify(Double.NEGATIVE_INFINITY);

        assertThat(compare(a, b), greaterThan(0));
        assertThat(compare(b, a), lessThan(0));
        assertThat(compare(b, c), is(0));
    }

    /**
     * test for Writable.
     */
    @Test
    public void write() {
        DoubleOption option = new DoubleOption();
        option.modify(100);
        DoubleOption restored = restore(option);
        assertThat(restored.get(), is(option.get()));
    }

    /**
     * test for Writable w/ max.
     */
    @Test
    public void write_max() {
        DoubleOption option = new DoubleOption();
        option.modify(Double.POSITIVE_INFINITY);
        DoubleOption restored = restore(option);
        assertThat(restored.get(), is(option.get()));
    }

    /**
     * test for Writable w/ min.
     */
    @Test
    public void write_min() {
        DoubleOption option = new DoubleOption();
        option.modify(Double.NEGATIVE_INFINITY);
        DoubleOption restored = restore(option);
        assertThat(restored.get(), is(option.get()));
    }

    /**
     * test for Writable w/ null.
     */
    @Test
    public void writeNull() {
        DoubleOption option = new DoubleOption();
        DoubleOption restored = restore(option);
        assertThat(restored.isNull(), is(true));
    }
}
