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
 * Test for {@link ByteOption}.
 */
@SuppressWarnings("deprecation")
public class ByteOptionTest extends ValueOptionTestRoot {

    /**
     * test for initial state.
     */
    @Test
    public void init() {
        ByteOption option = new ByteOption();
        assertThat(option.isNull(), is(true));
    }

    /**
     * test for get.
     */
    @Test
    public void get() {
        ByteOption option = new ByteOption();
        option.modify((byte) 100);
        assertThat(option.get(), is((byte) 100));
        assertThat(option.isNull(), is(false));
    }

    /**
     * test for or w/ null.
     */
    @Test
    public void or() {
        ByteOption option = new ByteOption();
        assertThat(option.or((byte) 30), is((byte) 30));
        assertThat(option.isNull(), is(true));
    }

    /**
     * test for or.
     */
    @Test
    public void orNotNull() {
        ByteOption option = new ByteOption();
        option.modify((byte) 100);
        assertThat(option.or((byte) 30), is((byte) 100));
    }

    /**
     * test for copyFrom.
     */
    @Test
    public void copy() {
        ByteOption option = new ByteOption();
        ByteOption other = new ByteOption();
        other.modify((byte) 50);
        option.copyFrom(other);
        assertThat(option.get(), is((byte) 50));

        option.modify((byte) 0);
        assertThat(other.get(), is((byte) 50));
    }

    /**
     * test for copyFrom w/ null.
     */
    @Test
    public void copyNull() {
        ByteOption option = new ByteOption();
        option.modify((byte) 100);

        ByteOption other = new ByteOption();
        option.copyFrom(other);
        assertThat(option.isNull(), is(true));
        option.modify((byte) 100);

        option.copyFrom(null);
        assertThat(option.isNull(), is(true));
    }

    /**
     * test for compare.
     */
    @Test
    public void compareTo() {
        ByteOption a = new ByteOption();
        ByteOption b = new ByteOption();
        ByteOption c = new ByteOption();
        ByteOption d = new ByteOption();

        a.modify((byte) -10);
        b.modify((byte) 0);
        c.modify((byte) 30);
        d.modify((byte) -10);

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
        ByteOption a = new ByteOption();
        ByteOption b = new ByteOption();
        ByteOption c = new ByteOption();

        a.modify((byte) 0x80);

        assertThat(compare(a, b), greaterThan(0));
        assertThat(compare(b, a), lessThan(0));
        assertThat(compare(b, c), is(0));
    }

    /**
     * test for Writable.
     */
    @Test
    public void write() {
        ByteOption option = new ByteOption();
        option.modify((byte) 100);
        ByteOption restored = restore(option);
        assertThat(restored.get(), is(option.get()));
    }

    /**
     * test for Writable.
     */
    @Test
    public void write_max() {
        ByteOption option = new ByteOption();
        option.modify(Byte.MAX_VALUE);
        ByteOption restored = restore(option);
        assertThat(restored.get(), is(option.get()));
    }

    /**
     * test for Writable.
     */
    @Test
    public void write_min() {
        ByteOption option = new ByteOption();
        option.modify(Byte.MIN_VALUE);
        ByteOption restored = restore(option);
        assertThat(restored.get(), is(option.get()));
    }

    /**
     * test for Writable w/ null.
     */
    @Test
    public void writeNull() {
        ByteOption option = new ByteOption();
        ByteOption restored = restore(option);
        assertThat(restored.isNull(), is(true));
    }
}
