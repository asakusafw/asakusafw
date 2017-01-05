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
package com.asakusafw.runtime.io.util;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

import com.asakusafw.runtime.value.IntOption;
import com.asakusafw.runtime.value.LongOption;
import com.asakusafw.runtime.value.StringOption;

/**
 * Test for {@link WritableRawComparableUnion}.
 */
@SuppressWarnings("deprecation")
public class WritableRawComparableUnionTest extends WritableTestRoot {

    /**
     * Creates object from classes.
     * @throws Exception if failed
     */
    @Test
    public void createFromClasses() throws Exception {
        Union union = new WritableRawComparableUnion(IntOption.class, LongOption.class, StringOption.class);
        assertThat(union.switchObject(0), instanceOf(IntOption.class));
        assertThat(union.switchObject(1), instanceOf(LongOption.class));
        assertThat(union.switchObject(2), instanceOf(StringOption.class));

        ((IntOption) union.switchObject(0)).modify(100);
        assertThat(union.getPosition(), is(0));
        ((LongOption) union.switchObject(1)).modify(200L);
        assertThat(union.getPosition(), is(1));
        ((StringOption) union.switchObject(2)).modify("Hello, world!");
        assertThat(union.getPosition(), is(2));

        assertThat(union.switchObject(0), is((Object) new IntOption(100)));
        assertThat(union.switchObject(1), is((Object) new LongOption(200L)));
        assertThat(union.switchObject(2), is((Object) new StringOption("Hello, world!")));
    }

    /**
     * Creates object from object.
     * @throws Exception if failed
     */
    @Test
    public void createFromObject() throws Exception {
        Union union = new WritableRawComparableUnion(
                new IntOption(100),
                new LongOption(200L),
                new StringOption("Hello, world!"));
        assertThat(union.switchObject(0), is((Object) new IntOption(100)));
        assertThat(union.switchObject(1), is((Object) new LongOption(200L)));
        assertThat(union.switchObject(2), is((Object) new StringOption("Hello, world!")));
    }

    /**
     * Serialize testing
     * @throws Exception if failed
     */
    @Test
    public void serialize() throws Exception {
        WritableRawComparableUnion union = new WritableRawComparableUnion(
                new IntOption(100),
                new LongOption(200L),
                new StringOption("Hello, world!"),
                new StringOption());

        StringBuilder buf = new StringBuilder(100000);
        for (int i = 0, n = buf.capacity(); i < n; i++) {
            buf.append((char) ('A' + (i * 257 % 26)));
        }
        ((StringOption) union.switchObject(3)).modify(buf.toString());

        WritableRawComparableUnion r0 = new WritableRawComparableUnion(IntOption.class, LongOption.class, StringOption.class);
        union.switchObject(0);
        byte[] s0 = ser(union);
        des(r0, s0);
        assertThat(r0.getPosition(), is(0));
        assertThat(r0.getObject(), is((Object) new IntOption(100)));

        WritableRawComparableUnion r1 = new WritableRawComparableUnion(IntOption.class, LongOption.class, StringOption.class);
        union.switchObject(1);
        byte[] s1 = ser(union);
        des(r1, s1);
        assertThat(r1.getPosition(), is(1));
        assertThat(r1.getObject(), is((Object) new LongOption(200L)));

        WritableRawComparableUnion r2 = new WritableRawComparableUnion(IntOption.class, LongOption.class, StringOption.class);
        union.switchObject(2);
        byte[] s2 = ser(union);
        des(r2, s2);
        assertThat(r2.getPosition(), is(2));
        assertThat(r2.getObject(), is((Object) new StringOption("Hello, world!")));

        byte[] large = ser((StringOption) union.switchObject(3));
        assertThat(s0.length, lessThan(large.length));
        assertThat(s1.length, lessThan(large.length));
        assertThat(s2.length, lessThan(large.length));
    }

    /**
     * Comparison testing
     * @throws Exception if failed
     */
    @Test
    public void compare() throws Exception {
        WritableRawComparableUnion a = new WritableRawComparableUnion(
                new IntOption(100),
                new LongOption(200L));

        WritableRawComparableUnion b = new WritableRawComparableUnion(
                new IntOption(101),
                new LongOption(201L));

        WritableRawComparableUnion c = new WritableRawComparableUnion(
                new IntOption(100),
                new LongOption(200L));

        a.switchObject(0);
        b.switchObject(0);
        c.switchObject(0);
        assertThat(cmp(a, b), is(lessThan(0)));
        assertThat(cmp(b, c), is(greaterThan(0)));
        assertThat(cmp(c, a), is(equalTo(0)));

        a.switchObject(1);
        b.switchObject(1);
        c.switchObject(1);
        assertThat(cmp(a, b), is(lessThan(0)));
        assertThat(cmp(b, c), is(greaterThan(0)));
        assertThat(cmp(c, a), is(equalTo(0)));

        a.switchObject(0);
        c.switchObject(1);
        assertThat(cmp(a, c), is(not(0)));
        assertThat(cmp(c, a), is(not(0)));
    }
}
