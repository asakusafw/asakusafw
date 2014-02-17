/**
 * Copyright 2011-2014 Asakusa Framework Team.
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

import java.util.Random;

import org.apache.hadoop.io.WritableComparator;
import org.apache.hadoop.mapreduce.Partitioner;
import org.junit.Test;

import com.asakusafw.runtime.value.StringOption;

/**
 * Test for {@link ShuffleKey}.
 */
public class ShuffleKeyTest extends WritableTestRoot {

    /**
     * serialization testing.
     * @throws Exception if failed
     */
    @Test
    public void serialize() throws Exception {
        Mock mock = new Mock("Hello", "World!");
        byte[] serialized = ser(mock);
        Mock restored = des(new Mock(), serialized);

        assertThat(restored.toString(), restored, is(mock));
        assertThat(restored.compareTo(mock), is(0));
        assertThat(restored.getGroupObject().getAsString(), is("Hello"));
        assertThat(restored.getOrderObject().getAsString(), is("World!"));
    }

    /**
     * comparison testing.
     * @throws Exception if failed
     */
    @Test
    public void compare() throws Exception {
        Mock o11 = new Mock("1", "1");
        Mock o12 = new Mock("1", "2");
        Mock o21 = new Mock("2", "1");
        Mock o22 = new Mock("2", "2");

        assertThat(cmp(o11, o11), is(0));
        assertThat(cmp(o11, o12), is(lessThan(0)));
        assertThat(cmp(o12, o11), is(greaterThan(0)));
        assertThat(cmp(o12, o21), is(lessThan(0)));
        assertThat(cmp(o21, o12), is(greaterThan(0)));
        assertThat(cmp(o21, o22), is(lessThan(0)));
        assertThat(cmp(o22, o21), is(greaterThan(0)));
    }

    /**
     * partition testing.
     * @throws Exception if failed
     */
    @SuppressWarnings("rawtypes")
    @Test
    public void partition() throws Exception {
        Partitioner<ShuffleKey, ?> part = new ShuffleKey.Partitioner();
        Mock o11 = new Mock("1", "1");
        Mock o12 = new Mock("1", "2");
        Mock o21 = new Mock("2", "1");
        Mock o22 = new Mock("2", "2");

        assertThat(part.getPartition(o11, null, 10), equalTo(part.getPartition(o11, null, 10)));
        assertThat(part.getPartition(o11, null, 10), equalTo(part.getPartition(o12, null, 10)));
        assertThat(part.getPartition(o21, null, 10), equalTo(part.getPartition(o22, null, 10)));

        Random random = new Random(12345);
        for (int i = 0; i < 100000; i++) {
            Mock mock = new Mock(String.valueOf(random.nextInt()), "left");
            int value = part.getPartition(mock, null, 10000);
            assertThat(value, is(greaterThanOrEqualTo(0)));
        }
        boolean found = false;
        for (int i = 0; i < 100000; i++) {
            Mock left = new Mock(String.valueOf(random.nextInt()), "left");
            Mock right = new Mock(String.valueOf(random.nextInt()), "right");
            if (left.getGroupObject().equals(right.getGroupObject())) {
                continue;
            }
            if (part.getPartition(left, null, 10000) != part.getPartition(right, null, 10000)) {
                found = true;
                break;
            }
        }
        assertThat(found, is(true));
    }

    /**
     * grouping testing.
     * @throws Exception if failed
     */
    @Test
    public void grouping() throws Exception {
        WritableComparator comp = new Group();
        Mock o11 = new Mock("1", "1");
        Mock o12 = new Mock("1", "2");
        Mock o21 = new Mock("2", "1");
        Mock o22 = new Mock("2", "2");

        assertThat(cmp(o11, o11, comp), is(0));

        assertThat(cmp(o11, o12, comp), is(equalTo(0)));
        assertThat(cmp(o12, o11, comp), is(equalTo(0)));
        assertThat(cmp(o21, o22, comp), is(equalTo(0)));
        assertThat(cmp(o22, o21, comp), is(equalTo(0)));

        assertThat(cmp(o12, o21, comp), is(lessThan(0)));
        assertThat(cmp(o21, o12, comp), is(greaterThan(0)));
        assertThat(cmp(o11, o22, comp), is(lessThan(0)));
        assertThat(cmp(o22, o11, comp), is(greaterThan(0)));
    }

    /**
     * ordering testing.
     * @throws Exception if failed
     */
    @Test
    public void ordering() throws Exception {
        WritableComparator comp = new Order();
        Mock o11 = new Mock("1", "1");
        Mock o12 = new Mock("1", "2");
        Mock o21 = new Mock("2", "1");
        Mock o22 = new Mock("2", "2");

        assertThat(cmp(o11, o11, comp), is(0));
        assertThat(cmp(o11, o12, comp), is(lessThan(0)));
        assertThat(cmp(o12, o11, comp), is(greaterThan(0)));
        assertThat(cmp(o12, o21, comp), is(lessThan(0)));
        assertThat(cmp(o21, o12, comp), is(greaterThan(0)));
        assertThat(cmp(o21, o22, comp), is(lessThan(0)));
        assertThat(cmp(o22, o21, comp), is(greaterThan(0)));
    }

    private static class Mock extends ShuffleKey<StringOption, StringOption> {

        Mock() {
            super(StringOption.class, StringOption.class);
        }

        Mock(String group, String order) {
            super(new StringOption(group), new StringOption(order));
        }
    }

    private static class Group extends ShuffleKey.AbstractGroupComparator {

        Group() {
            super(Mock.class);
        }
    }

    private static class Order extends ShuffleKey.AbstractOrderComparator {

        Order() {
            super(Mock.class);
        }
    }
}
