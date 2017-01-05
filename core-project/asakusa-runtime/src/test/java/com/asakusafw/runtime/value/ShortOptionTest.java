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
package com.asakusafw.runtime.value;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Test for {@link ShortOption}.
 */
@SuppressWarnings("deprecation")
public class ShortOptionTest extends ValueOptionTestRoot {

    /**
     * test for initial state.
     */
    @Test
    public void init() {
        ShortOption option = new ShortOption();
        assertThat(option.isNull(), is(true));
    }

    /**
     * test for get.
     */
    @Test
    public void get() {
        ShortOption option = new ShortOption();
        option.modify((short) 100);
        assertThat(option.get(), is((short) 100));
        assertThat(option.isNull(), is(false));
    }

    /**
     * test for or w/ absent value.
     */
    @Test
    public void or() {
        ShortOption option = new ShortOption();
        assertThat(option.or((short) 30), is((short) 30));
        assertThat(option.isNull(), is(true));
    }

    /**
     * test for or w/ present value.
     */
    @Test
    public void orNotNull() {
        ShortOption option = new ShortOption();
        option.modify((short) 100);
        assertThat(option.or((short) 30), is((short) 100));
    }

    /**
     * test for copyFrom.
     */
    @Test
    public void copy() {
        ShortOption option = new ShortOption();
        ShortOption other = new ShortOption();
        other.modify((short) 50);
        option.copyFrom(other);
        assertThat(option.get(), is((short) 50));

        option.modify((short) 0);
        assertThat(other.get(), is((short) 50));
    }

    /**
     * test for copyFrom w/ null.
     */
    @Test
    public void copyNull() {
        ShortOption option = new ShortOption();
        option.modify((short) 100);

        ShortOption other = new ShortOption();
        option.copyFrom(other);
        assertThat(option.isNull(), is(true));
        option.modify((short) 100);

        option.copyFrom(null);
        assertThat(option.isNull(), is(true));
    }

    /**
     * test for compare.
     */
    @Test
    public void compareTo() {
        ShortOption a = new ShortOption();
        ShortOption b = new ShortOption();
        ShortOption c = new ShortOption();
        ShortOption d = new ShortOption();

        a.modify((short) -10);
        b.modify((short) 0);
        c.modify((short) 30);
        d.modify((short) -10);

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
        ShortOption a = new ShortOption();
        ShortOption b = new ShortOption();
        ShortOption c = new ShortOption();

        a.modify((short) 0x8000);

        assertThat(compare(a, b), greaterThan(0));
        assertThat(compare(b, a), lessThan(0));
        assertThat(compare(b, c), is(0));
    }

    /**
     * test for Writable.
     */
    @Test
    public void write() {
        ShortOption option = new ShortOption();
        option.modify((short) 100);
        ShortOption restored = restore(option);
        assertThat(restored.get(), is(option.get()));
    }

    /**
     * test for Writable w/ max.
     */
    @Test
    public void write_max() {
        ShortOption option = new ShortOption();
        option.modify(Short.MAX_VALUE);
        ShortOption restored = restore(option);
        assertThat(restored.get(), is(option.get()));
    }

    /**
     * test for Writable w/ min.
     */
    @Test
    public void write_min() {
        ShortOption option = new ShortOption();
        option.modify(Short.MIN_VALUE);
        ShortOption restored = restore(option);
        assertThat(restored.get(), is(option.get()));
    }

    /**
     * test for Writable w/ null.
     */
    @Test
    public void writeNull() {
        ShortOption option = new ShortOption();
        ShortOption restored = restore(option);
        assertThat(restored.isNull(), is(true));
    }
}
