/**
 * Copyright 2012 Asakusa Framework Team.
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
package com.asakusafw.runtime.io.util;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

import com.asakusafw.runtime.value.IntOption;
import com.asakusafw.runtime.value.LongOption;
import com.asakusafw.runtime.value.StringOption;

/**
 * Test for {@link WritableRawComparableTuple}.
 */
@SuppressWarnings("deprecation")
public class WritableRawComparableTupleTest extends WritableTestRoot {

    /**
     * Creates from classes.
     * @throws Exception if failed
     */
    @Test
    public void createFromClasses() throws Exception {
        WritableRawComparableTuple tuple = new WritableRawComparableTuple(
                IntOption.class,
                LongOption.class,
                StringOption.class);

        assertThat(tuple.size(), is(3));
        assertThat(tuple.get(0), instanceOf(IntOption.class));
        assertThat(tuple.get(1), instanceOf(LongOption.class));
        assertThat(tuple.get(2), instanceOf(StringOption.class));

        ((IntOption) tuple.get(0)).modify(100);
        ((LongOption) tuple.get(1)).modify(200L);
        ((StringOption) tuple.get(2)).modify("Hello, world!");

        assertThat(tuple.get(0), is((Object) new IntOption(100)));
        assertThat(tuple.get(1), is((Object) new LongOption(200L)));
        assertThat(tuple.get(2), is((Object) new StringOption("Hello, world!")));
    }

    /**
     * Creates from objects.
     * @throws Exception if failed
     */
    @Test
    public void createFromObjects() throws Exception {
        WritableRawComparableTuple tuple = new WritableRawComparableTuple(
                new IntOption(100),
                new LongOption(200L),
                new StringOption("Hello, world!"));

        assertThat(tuple.get(0), is((Object) new IntOption(100)));
        assertThat(tuple.get(1), is((Object) new LongOption(200L)));
        assertThat(tuple.get(2), is((Object) new StringOption("Hello, world!")));
    }

    /**
     * Serialization testing.
     * @throws Exception if failed
     */
    @Test
    public void serialize() throws Exception {
        WritableRawComparableTuple tuple = new WritableRawComparableTuple(
                new IntOption(100),
                new LongOption(200L),
                new StringOption("Hello, world!"));

        WritableRawComparableTuple restored = new WritableRawComparableTuple(
                IntOption.class,
                LongOption.class,
                StringOption.class);

        byte[] serialized = ser(tuple);
        des(restored, serialized);

        assertThat(restored.get(0), is((Object) new IntOption(100)));
        assertThat(restored.get(1), is((Object) new LongOption(200L)));
        assertThat(restored.get(2), is((Object) new StringOption("Hello, world!")));
    }

    /**
     * Comparison testing.
     * @throws Exception if failed
     */
    @Test
    public void compare() throws Exception {
        WritableRawComparableTuple a = new WritableRawComparableTuple(
                new IntOption(100),
                new IntOption(100),
                new IntOption(100));
        WritableRawComparableTuple b = new WritableRawComparableTuple(
                new IntOption(100),
                new IntOption(100),
                new IntOption(101));
        WritableRawComparableTuple c = new WritableRawComparableTuple(
                new IntOption(100),
                new IntOption(101),
                new IntOption(100));
        WritableRawComparableTuple d = new WritableRawComparableTuple(
                new IntOption(101),
                new IntOption(100),
                new IntOption(100));
        WritableRawComparableTuple e = new WritableRawComparableTuple(
                new IntOption(100),
                new IntOption(100),
                new IntOption(100));

        assertThat(cmp(a, b), is(lessThan(0)));
        assertThat(cmp(a, c), is(lessThan(0)));
        assertThat(cmp(a, d), is(lessThan(0)));
        assertThat(cmp(b, c), is(lessThan(0)));
        assertThat(cmp(b, d), is(lessThan(0)));
        assertThat(cmp(c, d), is(lessThan(0)));
        assertThat(cmp(e, a), is(equalTo(0)));
        assertThat(cmp(b, a), is(greaterThan(0)));
        assertThat(cmp(c, b), is(greaterThan(0)));
        assertThat(cmp(d, c), is(greaterThan(0)));
    }
}
