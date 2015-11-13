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
 * Test for {@link FloatOption}.
 */
@SuppressWarnings("deprecation")
public class FloatOptionTest extends ValueOptionTestRoot {

    /**
     * test for initial state.
     */
    @Test
    public void init() {
        FloatOption option = new FloatOption();
        assertThat(option.isNull(), is(true));
    }

    /**
     * test for get.
     */
    @Test
    public void get() {
        FloatOption option = new FloatOption();
        option.modify(100);
        assertThat(option.get(), is(100.f));
        assertThat(option.isNull(), is(false));
    }

    /**
     * test for or w/ absent value.
     */
    @Test
    public void or() {
        FloatOption option = new FloatOption();
        assertThat(option.or(30), is(30.f));
        assertThat(option.isNull(), is(true));
    }

    /**
     * test for or w/ present value.
     */
    @Test
    public void orNotNull() {
        FloatOption option = new FloatOption();
        option.modify(100);
        assertThat(option.or(30), is(100.f));
    }

    /**
     * test for copyFrom.
     */
    @Test
    public void copy() {
        FloatOption option = new FloatOption();
        FloatOption other = new FloatOption();
        other.modify(50);
        option.copyFrom(other);
        assertThat(option.get(), is(50.f));

        option.modify(0);
        assertThat(other.get(), is(50.f));
    }

    /**
     * test for copyFrom w/ null.
     */
    @Test
    public void copyNull() {
        FloatOption option = new FloatOption();
        option.modify(100);

        FloatOption other = new FloatOption();
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
        FloatOption a = new FloatOption();
        FloatOption b = new FloatOption();
        FloatOption c = new FloatOption();
        FloatOption d = new FloatOption();
        FloatOption e = new FloatOption();

        a.modify(-10);
        b.modify(0);
        c.modify(30);
        d.modify(-10);
        e.modify(-30);

        assertThat(compare(a, b), lessThan(0));
        assertThat(compare(b, c), lessThan(0));
        assertThat(compare(c, a), greaterThan(0));
        assertThat(compare(a, c), lessThan(0));
        assertThat(compare(b, a), greaterThan(0));
        assertThat(compare(c, b), greaterThan(0));
        assertThat(compare(a, d), is(0));
        assertThat(compare(d, e), greaterThan(0));
    }

    /**
     * test for comapre w/ null.
     */
    @Test
    public void compareNull() {
        FloatOption a = new FloatOption();
        FloatOption b = new FloatOption();
        FloatOption c = new FloatOption();

        a.modify(Float.NEGATIVE_INFINITY);

        assertThat(compare(a, b), greaterThan(0));
        assertThat(compare(b, a), lessThan(0));
        assertThat(compare(b, c), is(0));
    }

    /**
     * test for Writable.
     */
    @Test
    public void write() {
        FloatOption option = new FloatOption();
        option.modify(100);
        FloatOption restored = restore(option);
        assertThat(restored.get(), is(option.get()));
    }

    /**
     * test for Writable w/ max.
     */
    @Test
    public void write_max() {
        FloatOption option = new FloatOption();
        option.modify(Float.POSITIVE_INFINITY);
        FloatOption restored = restore(option);
        assertThat(restored.get(), is(option.get()));
    }

    /**
     * test for Writable w/ min.
     */
    @Test
    public void write_0() {
        FloatOption option = new FloatOption();
        option.modify(0);
        FloatOption restored = restore(option);
        assertThat(restored.get(), is(option.get()));
    }

    /**
     * test for Writable w/ null.
     */
    @Test
    public void writeNull() {
        FloatOption option = new FloatOption();
        FloatOption restored = restore(option);
        assertThat(restored.isNull(), is(true));
    }
}
