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

    /**
     * 初期値。
     */
    @Test
    public void init() {
        StringOption option = new StringOption();
        assertThat("初期値はnull",
                option.isNull(), is(true));
    }

    /**
     * Textの設定。
     */
    @Test
    public void text() {
        StringOption option = new StringOption();
        option.modify(new Text("Hello, world!"));
        assertThat(option.isNull(), is(false));
        assertThat(option.get().toString(), is("Hello, world!"));
    }

    /**
     * Textのor。
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
     * Stringの設定。
     */
    @Test
    public void string() {
        StringOption option = new StringOption();
        option.modify("Hello, world!");
        assertThat(option.isNull(), is(false));
        assertThat(option.getAsString(), is("Hello, world!"));
    }

    /**
     * Stringのor。
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
     * modifyにnullを設定。
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
     * copyFromでコピー。
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
     * copyFromでnullをコピー。
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
     * 日本語の利用。
     */
    @Test
    public void japanese() {
        StringOption option = new StringOption();
        option.modify(new Text("こんにちは世界"));
        assertThat(option.getAsString(), is("こんにちは世界"));
    }

    /**
     * 順序付けのテスト。
     */
    @Test
    public void compare() {
        StringOption a = new StringOption();
        StringOption b = new StringOption();
        StringOption c = new StringOption();
        StringOption d = new StringOption();

        a.modify("いあ");
        b.modify("ああ");
        c.modify("あい");
        d.modify("いあ");

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
        StringOption a = new StringOption();
        StringOption b = new StringOption();
        StringOption c = new StringOption();

        a.modify("あ");

        assertThat(compare(a, b), greaterThan(0));
        assertThat(compare(b, a), lessThan(0));
        assertThat(compare(b, c), is(0));
    }

    /**
     * 最大値のテスト。
     */
    @Test
    public void max() {
        StringOption a = new StringOption();
        StringOption b = new StringOption();
        StringOption c = new StringOption();

        a.modify("ああ");
        b.modify("いあ");
        c.modify("あい");

        a.max(b);
        assertThat(a.getAsString(), is("いあ"));
        assertThat(b.getAsString(), is("いあ"));

        a.max(c);
        assertThat(a.getAsString(), is("いあ"));
        assertThat(b.getAsString(), is("いあ"));
        assertThat(c.getAsString(), is("あい"));
    }

    /**
     * 最小値のテスト。
     */
    @Test
    public void min() {
        StringOption a = new StringOption();
        StringOption b = new StringOption();
        StringOption c = new StringOption();

        a.modify("うえ");
        b.modify("いえ");
        c.modify("おか");

        a.min(b);
        assertThat(a.getAsString(), is("いえ"));
        assertThat(b.getAsString(), is("いえ"));

        a.min(c);
        assertThat(a.getAsString(), is("いえ"));
        assertThat(b.getAsString(), is("いえ"));
        assertThat(c.getAsString(), is("おか"));
    }

    /**
     * Writable対応のテスト。
     */
    @Test
    public void writable() {
        StringOption option = new StringOption();
        option.modify("Hello");
        StringOption restored = restore(option);
        assertThat(option.getAsString(), is(restored.getAsString()));
    }

    /**
     * Writable対応のテスト。
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
     * Writable対応のテスト。
     */
    @Test
    public void writable_empty() {
        StringOption option = new StringOption();
        option.modify("");
        StringOption restored = restore(option);
        assertThat(option.getAsString(), is(restored.getAsString()));
    }

    /**
     * null-Writable対応のテスト。
     */
    @Test
    public void writableOption() {
        StringOption option = new StringOption();
        StringOption restored = restore(option);
        assertThat(restored.isNull(), is(true));
    }
}
