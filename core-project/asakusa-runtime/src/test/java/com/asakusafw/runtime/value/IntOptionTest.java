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
package com.asakusafw.runtime.value;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Test for {@link IntOption}.
 */
@SuppressWarnings("deprecation")
public class IntOptionTest extends ValueOptionTestRoot {

    /**
     * test for initial state.
     */
    @Test
    public void init() {
        IntOption option = new IntOption();
        assertThat(option.isNull(), is(true));
    }

    /**
     * test for get.
     */
    @Test
    public void get() {
        IntOption option = new IntOption();
        option.modify(100);
        assertThat(option.get(), is(100));
        assertThat(option.isNull(), is(false));
    }

    /**
     * test for or w/ absent value.
     */
    @Test
    public void or() {
        IntOption option = new IntOption();
        assertThat(option.or(30), is(30));
        assertThat(option.isNull(), is(true));
    }

    /**
     * test for or w/ present value.
     */
    @Test
    public void orNotNull() {
        IntOption option = new IntOption();
        option.modify(100);
        assertThat(option.or(30), is(100));
    }

    /**
     * test for copyFrom.
     */
    @Test
    public void copy() {
        IntOption option = new IntOption();
        IntOption other = new IntOption();
        other.modify(50);
        option.copyFrom(other);
        assertThat(option.get(), is(50));

        option.modify(0);
        assertThat(other.get(), is(50));
    }

    /**
     * test for copyFrom w/ null.
     */
    @Test
    public void copyNull() {
        IntOption option = new IntOption();
        option.modify(100);

        IntOption other = new IntOption();
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
        IntOption a = new IntOption();
        IntOption b = new IntOption();
        IntOption c = new IntOption();
        IntOption d = new IntOption();

        a.modify(-10);
        b.modify(0);
        c.modify(30);
        d.modify(-10);

        assertThat(compare(a, b), lessThan(0));
        assertThat(compare(b, c), lessThan(0));
        assertThat(compare(c, a), greaterThan(0));
        assertThat(compare(a, c), lessThan(0));
        assertThat(compare(b, a), greaterThan(0));
        assertThat(compare(c, b), greaterThan(0));
        assertThat(compare(a, d), is(0));
    }

    /**
     * test for compare w/ null.
     */
    @Test
    public void compareNull() {
        IntOption a = new IntOption();
        IntOption b = new IntOption();
        IntOption c = new IntOption();

        a.modify(0x800000);

        assertThat(compare(a, b), greaterThan(0));
        assertThat(compare(b, a), lessThan(0));
        assertThat(compare(b, c), is(0));
    }

    /**
     * test for Writable.
     */
    @Test
    public void write() {
        IntOption option = new IntOption();
        option.modify(100);
        IntOption restored = restore(option);
        assertThat(restored.get(), is(option.get()));
    }

    /**
     * test for Writable w/ max.
     */
    @Test
    public void write_max() {
        IntOption option = new IntOption();
        option.modify(Integer.MAX_VALUE);
        IntOption restored = restore(option);
        assertThat(restored.get(), is(option.get()));
    }

    /**
     * test for Writable w/ zero.
     */
    @Test
    public void write_0() {
        IntOption option = new IntOption();
        option.modify(0);
        IntOption restored = restore(option);
        assertThat(restored.get(), is(option.get()));
    }

    /**
     * test for Writable w/ null.
     */
    @Test
    public void writeNull() {
        IntOption option = new IntOption();
        IntOption restored = restore(option);
        assertThat(restored.isNull(), is(true));
    }
}
