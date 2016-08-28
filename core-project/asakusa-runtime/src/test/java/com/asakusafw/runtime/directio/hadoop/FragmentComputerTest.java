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
package com.asakusafw.runtime.directio.hadoop;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.asakusafw.runtime.directio.DirectInputFragment;

/**
 * Test for {@link FragmentComputer}.
 */
public class FragmentComputerTest {

    /**
     * simple testing.
     */
    @Test
    public void simple() {
        BlockBuilder builder = new BlockBuilder();
        builder.add(100, "a");
        List<DirectInputFragment> results = builder.compute(50, 100, true, true);
        assertThat(results.size(), is(1));
        assertThat(find(results, 0).getOwnerNodeNames(), hasItem("a"));
    }

    /**
     * empty file.
     */
    @Test
    public void empty() {
        BlockBuilder builder = new BlockBuilder();
        List<DirectInputFragment> results = builder.compute(50, 100, true, true);
        assertThat(results.size(), is(1));
    }

    /**
     * no block information.
     */
    @Test
    public void no_info() {
        BlockBuilder builder = new BlockBuilder();
        builder.seek(1000);
        List<DirectInputFragment> results = builder.compute(50, 100, true, true);
        assertThat(results.size(), is(10));
    }

    /**
     * block information is sparse.
     */
    @Test
    public void sparse() {
        BlockBuilder builder = new BlockBuilder();
        builder.seek(5);
        builder.add(10, "a");
        builder.seek(5);
        builder.add(10, "a");
        builder.seek(5);
        builder.add(10, "a");
        builder.seek(5);
        builder.add(10, "a");
        builder.seek(5);
        builder.add(10, "a");
        builder.seek(5);
        List<DirectInputFragment> results = builder.compute(50, 100, true, true);
        assertThat(results.size(), is(1));
        assertThat(find(results, 0).getOwnerNodeNames(), hasItem("a"));
    }

    /**
     * fragmentation is restricted.
     */
    @Test
    public void no_fragmentation() {
        BlockBuilder builder = new BlockBuilder();
        builder.add(10, "a");
        builder.add(10, "b");
        builder.add(10, "c");
        List<DirectInputFragment> results = builder.compute(-1, 100, true, true);
        assertThat(results.size(), is(1));
    }

    /**
     * file is too small.
     */
    @Test
    public void too_small() {
        BlockBuilder builder = new BlockBuilder();
        builder.add(10, "a");
        builder.add(10, "b");
        builder.add(10, "c");
        List<DirectInputFragment> results = builder.compute(100, 200, true, true);
        assertThat(results.size(), is(1));
    }

    /**
     * head blocks are too small.
     */
    @Test
    public void head_too_small() {
        BlockBuilder builder = new BlockBuilder();
        builder.add(10, "b");
        builder.add(10, "c");
        builder.add(10, "d");
        builder.add(100, "a");
        List<DirectInputFragment> results = builder.compute(50, 200, true, true);
        assertThat(results.size(), is(1));
        assertThat(results.get(0).getOwnerNodeNames(), hasItem("a"));
    }

    /**
     * tail blocks are too small.
     */
    @Test
    public void tail_too_small() {
        BlockBuilder builder = new BlockBuilder();
        builder.add(100, "a");
        builder.add(10, "b");
        builder.add(10, "c");
        builder.add(10, "d");
        List<DirectInputFragment> results = builder.compute(50, 200, true, true);
        assertThat(results.size(), is(1));
        assertThat(results.get(0).getOwnerNodeNames(), hasItem("a"));
    }

    /**
     * head and edge blocks are too small.
     */
    @Test
    public void edge_too_small() {
        BlockBuilder builder = new BlockBuilder();
        builder.add(10, "b");
        builder.add(10, "c");
        builder.add(400, "a");
        builder.add(10, "b");
        builder.add(10, "c");
        List<DirectInputFragment> results = builder.compute(50, 200, true, true);
        assertThat(results.size(), is(2));
        assertThat(results.get(0).getOwnerNodeNames(), hasItem("a"));
        assertThat(results.get(1).getOwnerNodeNames(), hasItem("a"));
    }

    /**
     * per prefered size.
     */
    @Test
    public void pref_size() {
        BlockBuilder builder = new BlockBuilder();
        builder.add(400, "a");
        List<DirectInputFragment> results = builder.compute(10, 80, true, true);
        assertThat(results.size(), is(5));
        assertThat(results.get(0).getOwnerNodeNames(), hasItem("a"));
        assertThat(results.get(1).getOwnerNodeNames(), hasItem("a"));
        assertThat(results.get(2).getOwnerNodeNames(), hasItem("a"));
        assertThat(results.get(3).getOwnerNodeNames(), hasItem("a"));
        assertThat(results.get(4).getOwnerNodeNames(), hasItem("a"));
    }

    /**
     * per prefered size with block join.
     */
    @Test
    public void pref_size_with_join() {
        BlockBuilder builder = new BlockBuilder();
        builder.add(100, "a");
        builder.add(100, "a");
        builder.add(100, "a");
        builder.add(100, "a");
        List<DirectInputFragment> results = builder.compute(10, 80, true, true);
        assertThat(results.size(), is(5));
        assertThat(results.get(0).getOwnerNodeNames(), hasItem("a"));
        assertThat(results.get(1).getOwnerNodeNames(), hasItem("a"));
        assertThat(results.get(2).getOwnerNodeNames(), hasItem("a"));
        assertThat(results.get(3).getOwnerNodeNames(), hasItem("a"));
        assertThat(results.get(4).getOwnerNodeNames(), hasItem("a"));
    }

    /**
     * per prefered size without block join.
     */
    @Test
    public void pref_size_without_join() {
        BlockBuilder builder = new BlockBuilder();
        builder.add(100, "a");
        builder.add(100, "b");
        builder.add(100, "c");
        builder.add(100, "d");
        List<DirectInputFragment> results = builder.compute(10, 80, true, true);
        assertThat(results.size(), is(4));
        assertThat(results.get(0).getOwnerNodeNames(), hasItem("a"));
        assertThat(results.get(0).getSize(), is(100L));
        assertThat(results.get(1).getOwnerNodeNames(), hasItem("b"));
        assertThat(results.get(1).getSize(), is(100L));
        assertThat(results.get(2).getOwnerNodeNames(), hasItem("c"));
        assertThat(results.get(2).getSize(), is(100L));
        assertThat(results.get(3).getOwnerNodeNames(), hasItem("d"));
        assertThat(results.get(3).getSize(), is(100L));
    }

    /**
     * tail blocks are too small.
     */
    @Test
    public void ignore_little_locality() {
        BlockBuilder builder = new BlockBuilder();
        builder.add(100, "a");
        builder.add(1, "b", "c", "d");
        List<DirectInputFragment> results = builder.compute(50, 200, true, true);
        assertThat(results.size(), is(1));
        assertThat(results.get(0).getOwnerNodeNames(), hasItem("a"));
        assertThat(results.get(0).getOwnerNodeNames(), not(hasItem("b")));
    }

    private DirectInputFragment find(List<DirectInputFragment> results, long position) {
        for (DirectInputFragment fragment : results) {
            long offset = fragment.getOffset();
            long size = fragment.getSize();
            if (offset <= position && position < offset + size) {
                return fragment;
            }
        }
        throw new AssertionError(position);
    }

    private static class BlockBuilder {

        long offset;

        List<BlockInfo> blocks = new ArrayList<>();

        BlockBuilder() {
            return;
        }

        void add(long size, String... hosts) {
            blocks.add(new BlockInfo(offset, offset + size, hosts));
            offset += size;
        }

        void seek(long delta) {
            offset += delta;
        }

        List<DirectInputFragment> compute(long min, long pref, boolean combine, boolean split) {
            FragmentComputer computer = new FragmentComputer(min, pref, combine, split);
            List<DirectInputFragment> results = computer.computeFragments("path", offset, blocks);
            return validate(results);
        }

        private List<DirectInputFragment> validate(List<DirectInputFragment> fragments) {
            List<DirectInputFragment> results = new ArrayList<>(fragments);
            Collections.sort(results, (o1, o2) -> Long.compare(o1.getOffset(), o2.getOffset()));
            long expectedOffset = 0;
            for (DirectInputFragment fragment : results) {
                assertThat(fragment.getOffset(), is(expectedOffset));
                expectedOffset = fragment.getOffset() + fragment.getSize();
            }
            assertThat(offset, is(expectedOffset));
            return results;
        }
    }
}
