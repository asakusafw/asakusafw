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

import org.apache.hadoop.io.Text;
import org.junit.Test;

/**
 * Test for {@link StringOption}.
 */
@SuppressWarnings("deprecation")
public class StringOptionTest extends ValueOptionTestRoot {

    static final String HELLO_JP = "\u3053\u3093\u306b\u3061\u306f\u3001\u4e16\u754c\uff01";

    /**
     * test for initial state.
     */
    @Test
    public void init() {
        StringOption option = new StringOption();
        assertThat(option.isNull(), is(true));
    }

    /**
     * test for set w/ text.
     */
    @Test
    public void text() {
        StringOption option = new StringOption();
        option.modify(new Text("Hello, world!"));
        assertThat(option.isNull(), is(false));
        assertThat(option.get().toString(), is("Hello, world!"));
    }

    /**
     * test for or w/ text.
     */
    @Test
    public void textOr() {
        StringOption option = new StringOption();
        assertThat(option.or(new Text("Hello")).toString(), is("Hello"));

        assertThat(option.isNull(), is(true));
        option.modify(new Text("World"));
        assertThat(option.or(new Text("Other")).toString(), is("World"));
    }

    /**
     * test for w/ get as string.
     */
    @Test
    public void string() {
        StringOption option = new StringOption();
        option.modify("Hello, world!");
        assertThat(option.isNull(), is(false));
        assertThat(option.getAsString(), is("Hello, world!"));
    }

    /**
     * test for or w/ string.
     */
    @Test
    public void stringOr() {
        StringOption option = new StringOption();
        assertThat(option.or("Hello"), is("Hello"));

        assertThat(option.isNull(), is(true));
        option.modify("World");
        assertThat(option.or("Other"), is("World"));
    }

    /**
     * test for modify w/ null.
     */
    @Test
    public void modifyNull() {
        StringOption option = new StringOption();
        option.modify("NotNull");
        assertThat(option.isNull(), is(false));

        option.modify((String) null);
        assertThat(option.isNull(), is(true));
        option.modify("NotNull");

        option.modify((Text) null);
        assertThat(option.isNull(), is(true));
    }

    /**
     * test for copyFrom.
     */
    @Test
    public void copy() {
        StringOption option = new StringOption();
        StringOption other = new StringOption();
        other.modify("Hello");

        option.copyFrom(other);
        assertThat(option.getAsString(), is("Hello"));

        option.modify("World");
        assertThat(other.getAsString(), is("Hello"));
    }

    /**
     * test for copyFrom w/ null.
     */
    @Test
    public void copyNull() {
        StringOption option = new StringOption();
        option.modify("Hello");
        StringOption other = new StringOption();

        option.copyFrom(other);
        assertThat(option.isNull(), is(true));

        option.modify("World");
        assertThat(option.isNull(), is(false));

        option.copyFrom(null);
        assertThat(option.isNull(), is(true));
    }

    /**
     * test for Japanese characters.
     */
    @Test
    public void japanese() {
        StringOption option = new StringOption();
        option.modify(new Text(HELLO_JP));
        assertThat(option.getAsString(), is(HELLO_JP));
    }

    /**
     * test for compare.
     */
    @Test
    public void compare() {
        StringOption a = new StringOption();
        StringOption b = new StringOption();
        StringOption c = new StringOption();
        StringOption d = new StringOption();

        a.modify("ba");
        b.modify("aa");
        c.modify("ab");
        d.modify("ba");

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
        StringOption a = new StringOption();
        StringOption b = new StringOption();
        StringOption c = new StringOption();

        a.modify("a");

        assertThat(compare(a, b), greaterThan(0));
        assertThat(compare(b, a), lessThan(0));
        assertThat(compare(b, c), is(0));
    }

    /**
     * test for max.
     */
    @Test
    public void max() {
        StringOption a = new StringOption();
        StringOption b = new StringOption();
        StringOption c = new StringOption();

        a.modify("aa");
        b.modify("ba");
        c.modify("ab");

        a.max(b);
        assertThat(a.getAsString(), is("ba"));
        assertThat(b.getAsString(), is("ba"));

        a.max(c);
        assertThat(a.getAsString(), is("ba"));
        assertThat(b.getAsString(), is("ba"));
        assertThat(c.getAsString(), is("ab"));
    }

    /**
     * test for min.
     */
    @Test
    public void min() {
        StringOption a = new StringOption();
        StringOption b = new StringOption();
        StringOption c = new StringOption();

        a.modify("cd");
        b.modify("bd");
        c.modify("ef");

        a.min(b);
        assertThat(a.getAsString(), is("bd"));
        assertThat(b.getAsString(), is("bd"));

        a.min(c);
        assertThat(a.getAsString(), is("bd"));
        assertThat(b.getAsString(), is("bd"));
        assertThat(c.getAsString(), is("ef"));
    }

    /**
     * test for Writable.
     */
    @Test
    public void writable() {
        StringOption option = new StringOption();
        option.modify("Hello");
        StringOption restored = restore(option);
        assertThat(option.getAsString(), is(restored.getAsString()));
    }

    /**
     * test for Writable w/ long text.
     */
    @Test
    public void writable_long() {
        StringBuilder buf = new StringBuilder();
        for (char c = 0x20; c < 0xfffe; c++) {
            buf.append(c);
        }

        StringOption option = new StringOption();
        option.modify(buf.toString());
        StringOption restored = restore(option);
        assertThat(option.getAsString(), is(restored.getAsString()));
    }

    /**
     * test for Writable w/ empty string.
     */
    @Test
    public void writable_empty() {
        StringOption option = new StringOption();
        option.modify("");
        StringOption restored = restore(option);
        assertThat(option.getAsString(), is(restored.getAsString()));
    }

    /**
     * test for Writable w/ null.
     */
    @Test
    public void writableOption() {
        StringOption option = new StringOption();
        StringOption restored = restore(option);
        assertThat(restored.isNull(), is(true));
    }
}
