/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
package com.asakusafw.runtime.stage.input;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Test;

import com.asakusafw.runtime.stage.input.StageInputSplit.Source;

/**
 * Test for {@link DefaultSplitCombiner}.
 */
public class DefaultSplitCombinerTest {

    /**
     * Simple testing.
     * @throws Exception if failed
     */
    @Test
    public void simple() throws Exception {
        DefaultSplitCombiner combiner = new DefaultSplitCombiner();
        List<StageInputSplit> combined = combine(combiner, 1, list(split(1, 1, "a")));
        assertThat(combined.size(), is(1));
        assertSan(combined);
    }

    /**
     * with single slot.
     * @throws Exception if failed
     */
    @Test
    public void single_slot() throws Exception {
        DefaultSplitCombiner combiner = new DefaultSplitCombiner();
        List<StageInputSplit> combined = combine(combiner, 1, list(split(1, 1, "a"), split(2, 1, "b")));
        assertThat(combined.get(0).getLocations(), is(locations("a", "b")));
        assertThat(combined.size(), is(1));
        assertSan(combined);
    }

    /**
     * with over slot.
     * @throws Exception if failed
     */
    @Test
    public void over_slot() throws Exception {
        DefaultSplitCombiner combiner = new DefaultSplitCombiner();
        List<StageInputSplit> combined = combine(combiner, 10, list(split(1, 1, "a"), split(2, 1, "b")));
        assertThat(combined.size(), is(2));
        assertSan(combined);
    }

    /**
     * with tiny inputs.
     * @throws Exception if failed
     */
    @Test
    public void tiny() throws Exception {
        DefaultSplitCombiner combiner = new DefaultSplitCombiner();
        List<StageInputSplit> combined = combine(combiner, 10, 10L, list(
                split(0, 1, "a"),
                split(1, 1, "a"),
                split(2, 1, "a"),
                split(3, 1, "b"),
                split(4, 1, "b"),
                split(5, 1, "b"),
                split(6, 1, "b"),
                split(7, 1, "b"),
                split(8, 1, "b"),
                split(9, 1, "b")));
        assertThat(combined.size(), is(1));
        assertSan(combined);
    }

    /**
     * with non-tiny inputs.
     * @throws Exception if failed
     */
    @Test
    public void tiny_over() throws Exception {
        DefaultSplitCombiner combiner = new DefaultSplitCombiner();
        List<StageInputSplit> combined = combine(combiner, 10, 9L, list(
                split(0, 1, "a"),
                split(1, 1, "a"),
                split(2, 1, "a"),
                split(3, 1, "b"),
                split(4, 1, "b"),
                split(5, 1, "b"),
                split(6, 1, "b"),
                split(7, 1, "b"),
                split(8, 1, "b"),
                split(9, 1, "b")));
        assertThat(combined.size(), is(10));
        assertSan(combined);
    }

    /**
     * with simple GA.
     * @throws Exception if failed
     */
    @Test
    public void ga_simple() throws Exception {
        DefaultSplitCombiner combiner = new DefaultSplitCombiner();
        List<StageInputSplit> combined = combine(combiner, 2, list(
                split(1, 1, "a"),
                split(2, 1, "a"),
                split(3, 1, "b"),
                split(4, 1, "b")));
        assertThat(combined.size(), is(2));
        assertSan(combined);

        StageInputSplit tag1 = find(combined, 1);
        assertTags(tag1, 1, 2);

        StageInputSplit tag3 = find(combined, 3);
        assertTags(tag3, 3, 4);
    }

    /**
     * with simple GA.
     * @throws Exception if failed
     */
    @Test
    public void ga_nolocation() throws Exception {
        DefaultSplitCombiner combiner = new DefaultSplitCombiner();
        List<StageInputSplit> combined = combine(combiner, 2, list(
                split(1, 1, (String[]) null),
                split(2, 2, (String[]) null),
                split(3, 3, (String[]) null),
                split(4, 6, (String[]) null)));
        assertThat(combined.size(), is(2));
        assertSan(combined);

        StageInputSplit tag1 = find(combined, 1);
        assertTags(tag1, 1, 2, 3);

        StageInputSplit tag4 = find(combined, 4);
        assertTags(tag4, 4);
    }

    /**
     * with simple GA.
     * @throws Exception if failed
     */
    @Test
    public void ga_minimize() throws Exception {
        DefaultSplitCombiner combiner = new DefaultSplitCombiner();
        List<StageInputSplit> combined = combine(combiner, 2, list(
                split(1, 100, "a"),
                split(2, 100, "b"),
                split(3, 100, "c"),
                split(4, 100, "d")));
        assertThat(combined.size(), is(2));
        assertSan(combined);
        assertThat(combined.get(0).getSources().size(), is(2));
        assertThat(combined.get(1).getSources().size(), is(2));
    }

    /**
     * with simple GA.
     * @throws Exception if failed
     */
    @Test
    public void ga_locality() throws Exception {
        DefaultSplitCombiner combiner = new DefaultSplitCombiner();
        List<StageInputSplit> combined = combine(combiner, 2, list(
                split(1, 100, "a"),
                split(2, 100, "b"),
                split(3, 1, "c"),
                split(4, 1, "d")));
        assertThat(combined.size(), is(2));
        assertSan(combined);
        assertThat(find(combined, 1), is(not(find(combined, 2))));
    }

    /**
     * with simple GA.
     * @throws Exception if failed
     */
    @Test
    public void ga_many() throws Exception {
        String[][] locations = {
                { },
                { "a", "b" },
                { "b", "c" },
                { "a" },
                { "c" },
                { "b", "c" },
                { "d" },
                { "e", "f" },
                { "a", "g" },
        };
        List<StageInputSplit> splits = new ArrayList<>();
        long total = 0;
        for (int i = 0; i < 1000; i++) {
            long size = i * 10 + 100;
            splits.add(split(i, size, locations[i % locations.length]));
            total += size;
        }
        DefaultSplitCombiner combiner = new DefaultSplitCombiner();
        for (int i = 1; i < 12; i += 2) {
            int slots = i * 5;
            int prefSlots = i * 10 / 9;
            List<StageInputSplit> combined = combine(combiner, slots, splits);
            assertThat(combined.size(), is(greaterThan(prefSlots)));
            assertSan(combined);
            long prefMaxSize = total * 2 / prefSlots;
            for (StageInputSplit split : combined) {
                assertThat(split.getLength(), is(lessThan(prefMaxSize)));
            }
        }
    }

    private List<StageInputSplit> combine(
            DefaultSplitCombiner combiner,
            int slots,
            List<StageInputSplit> splits) throws IOException, InterruptedException {
        return combiner.combine(
                new DefaultSplitCombiner.Configuration()
                    .withSlotsPerInput(slots)
                    .withGenerations(1000)
                    .withNonLocalPenaltyRatio(10),
                splits);
    }

    private List<StageInputSplit> combine(
            DefaultSplitCombiner combiner,
            int slots,
            long limit,
            List<StageInputSplit> splits) throws IOException, InterruptedException {
        return combiner.combine(
                new DefaultSplitCombiner.Configuration()
                    .withSlotsPerInput(slots)
                    .withTinyLimit(limit),
                splits);
    }

    private void assertSan(List<StageInputSplit> splits) {
        Set<Integer> saw = new HashSet<>();
        for (StageInputSplit stage : splits) {
            for (Source source : stage.getSources()) {
                MockInputSplit split = (MockInputSplit) source.getSplit();
                assertThat(saw, not(hasItem(split.tag)));
                saw.add(split.tag);
            }
        }
    }

    private StageInputSplit find(List<StageInputSplit> list, int tag) {
        for (StageInputSplit stage : list) {
            for (Source source : stage.getSources()) {
                MockInputSplit mock = (MockInputSplit) source.getSplit();
                if (mock.tag == tag) {
                    return stage;
                }
            }
        }
        throw new AssertionError(tag);
    }

    private void assertTags(StageInputSplit split, int... tags) {
        Set<Integer> expected = new TreeSet<>();
        for (int tag : tags) {
            expected.add(tag);
        }
        Set<Integer> actual = new TreeSet<>();
        for (Source source : split.getSources()) {
            MockInputSplit mock = (MockInputSplit) source.getSplit();
            actual.add(mock.tag);
        }
        assertThat(actual, is(expected));
    }

    private Matcher<String[]> locations(String... locations) {
        Set<String> set = new TreeSet<>();
        Collections.addAll(set, locations);
        return new BaseMatcher<String[]>() {
            @Override
            public boolean matches(Object arg) {
                String[] actualArray = (String[]) arg;
                Set<String> actual = new TreeSet<>();
                if (actualArray != null) {
                    Collections.addAll(actual, actualArray);
                }
                return set.equals(actual);
            }
            @Override
            public void describeTo(Description desc) {
                desc.appendValue(set);
            }
        };
    }

    private List<StageInputSplit> list(StageInputSplit... splits) {
        return Arrays.asList(splits);
    }

    private StageInputSplit split(int tag, long length, String... locations) {
        Class<? extends Mapper<?, ?, ?, ?>> mapper = A.class;
        return split(tag, mapper, length, locations);
    }

    private StageInputSplit split(int tag, Class<? extends Mapper<?, ?, ?, ?>> mapper, long length, String... locations) {
        InputSplit split = new MockInputSplit(tag, length, locations);
        return new StageInputSplit(mapper, Collections.singletonList(new StageInputSplit.Source(split, F.class)));
    }

    private static final class A extends Mapper<Object, Object, Object, Object> {
        // nothing
    }

    private static final class F extends InputFormat<Object, Object> {

        @Override
        public List<InputSplit> getSplits(JobContext context) {
            return null;
        }

        @Override
        public RecordReader<Object, Object> createRecordReader(InputSplit split, TaskAttemptContext context) {
            return null;
        }
    }
}
