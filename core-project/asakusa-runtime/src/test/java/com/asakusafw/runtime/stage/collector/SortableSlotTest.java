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
package com.asakusafw.runtime.stage.collector;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.io.DataInputBuffer;
import org.apache.hadoop.io.DataOutputBuffer;
import org.apache.hadoop.io.RawComparator;
import org.apache.hadoop.io.Writable;
import org.junit.Test;

import com.asakusafw.runtime.value.IntOption;

/**
 * Test for {@link SortableSlot}.
 */
public class SortableSlotTest {

    /**
     * test for {@link SortableSlot#begin(int)}.
     */
    @Test
    public void begin() {
        SortableSlot slot = new SortableSlot();
        slot.begin(100);
        assertThat(slot.getSlot(), is(100));

        SortableSlot other = new SortableSlot();
        other.begin(101);
        assertThat(slot, is(not(other)));
    }

    /**
     * test for {@link SortableSlot#addByte(int)}.
     * @throws Exception if failed
     */
    @Test
    public void addByte() throws Exception {
        Map<String, SortableSlot> slots = new HashMap<>();
        slots.put("min", createWithByte(Byte.MIN_VALUE));
        slots.put("min-half", createWithByte(Byte.MIN_VALUE / 2));
        slots.put("-1", createWithByte(-1));
        slots.put("0", createWithByte(0));
        slots.put("1", createWithByte(1));
        slots.put("max-half", createWithByte(Byte.MAX_VALUE / 2));
        slots.put("max", createWithByte(Byte.MAX_VALUE));

        slots.put("0,-1", createWithByte(0));
        slots.get("0,-1").addByte(-1);

        slots.put("0,0", createWithByte(0));
        slots.get("0,0").addByte(-1);

        slots.put("0,1", createWithByte(0));
        slots.get("0,1").addByte(-1);

        List<SortableSlot> copy = new ArrayList<>(slots.values());
        Collections.sort(copy);

        assertThat(copy.get(0), is(slots.get("min")));
        assertThat(copy.get(1), is(slots.get("min-half")));
        assertThat(copy.get(2), is(slots.get("-1")));
        assertThat(copy.get(3), is(slots.get("0")));
        assertThat(copy.get(4), is(slots.get("0,-1")));
        assertThat(copy.get(5), is(slots.get("0,0")));
        assertThat(copy.get(6), is(slots.get("0,1")));
        assertThat(copy.get(7), is(slots.get("1")));
        assertThat(copy.get(8), is(slots.get("max-half")));
        assertThat(copy.get(9), is(slots.get("max")));
    }

    private SortableSlot createWithByte(int value) throws IOException {
        SortableSlot slot = new SortableSlot();
        slot.begin(0);
        slot.addByte(value - Byte.MIN_VALUE);
        return slot;
    }

    /**
     * test for {@link SortableSlot#addRandom()}.
     * @throws Exception if failed
     */
    @Test
    public void addRandom() throws Exception {
        SortableSlot slot = new SortableSlot();
        slot.begin(100);
        slot.addRandom();
        int same = 0;
        for (int i = 0; i < 100000; i++) {
            SortableSlot random = new SortableSlot();
            random.begin(100);
            random.addRandom();
            if (slot.equals(random)) {
                same++;
            }
        }
        assertThat(same, lessThan(100));
    }

    /**
     * test for {@link SortableSlot#add(Writable)}.
     * @throws Exception if failed
     */
    @Test
    public void add() throws Exception {
        Map<String, SortableSlot> slots = new HashMap<>();
        slots.put("0", createWithInt(0));
        slots.put("-1", createWithInt(-1));
        slots.put("1", createWithInt(1));
        slots.put("min", createWithInt(Integer.MIN_VALUE));
        slots.put("max", createWithInt(Integer.MAX_VALUE));
        slots.put("null", createWithInt(null));
        slots.put("0.1", createWithInt(0));
        slots.get("0.1").add(new IntOption(1));

        List<SortableSlot> copy = new ArrayList<>(slots.values());
        Collections.sort(copy);

        assertThat(copy.get(0), is(slots.get("null")));
        assertThat(copy.get(1), is(slots.get("min")));
        assertThat(copy.get(2), is(slots.get("-1")));
        assertThat(copy.get(3), is(slots.get("0")));
        assertThat(copy.get(4), is(slots.get("0.1")));
        assertThat(copy.get(5), is(slots.get("1")));
        assertThat(copy.get(6), is(slots.get("max")));
    }

    private SortableSlot createWithInt(Integer value) throws IOException {
        SortableSlot slot = new SortableSlot();
        slot.begin(0);
        IntOption option;
        if (value == null) {
            option = new IntOption();
        } else {
            option = new IntOption(value);
        }
        slot.add(option);
        return slot;
    }

    /**
     * test for {@link Writable}.
     * @throws Exception if failed
     */
    @Test
    public void writable() throws Exception {
        SortableSlot slot = new SortableSlot();
        slot.begin(1);
        slot.addByte(10);
        SortableSlot s1 = read(new SortableSlot(), write(slot));

        slot.begin(2);
        slot.addByte(20);
        SortableSlot s2 = read(new SortableSlot(), write(slot));

        SortableSlot a1 = new SortableSlot();
        a1.begin(1);
        a1.addByte(10);

        SortableSlot a2 = new SortableSlot();
        a2.begin(2);
        a2.addByte(20);

        assertThat(s1, is(a1));
        assertThat(s2, is(a2));
    }

    /**
     * test for {@link SortableSlot.Comparator}.
     * @throws Exception if failed
     */
    @SuppressWarnings("unchecked")
    @Test
    public void comparator() throws Exception {
        LinkedList<SortableSlot> slots = new LinkedList<>();
        slots.add(createWithByte(Byte.MIN_VALUE));
        slots.add(createWithByte(Byte.MIN_VALUE / 2));
        slots.add(createWithByte(-1));
        slots.add(createWithByte(0));
        slots.add(createWithByte(1));
        slots.add(createWithByte(Byte.MAX_VALUE / 2));
        slots.add(createWithByte(Byte.MAX_VALUE));
        slots.add(createWithByte(0));
        slots.getLast().addByte(-1);
        slots.add(createWithByte(0));
        slots.getLast().addByte(0);
        slots.add(createWithByte(0));
        slots.getLast().addByte(1);

        LinkedList<SortableSlot> copy = new LinkedList<>(slots);
        Collections.sort(slots, new SortableSlot.Comparator());
        Collections.sort(copy, new BinaryComparator(new SortableSlot.Comparator()));

        assertThat(copy, is(slots));
    }

    /**
     * test for {@link SortableSlot.Partitioner}.
     * @throws Exception if failed
     */
    @Test
    public void partitioner() throws Exception {
        final int partitions = 10;
        final int records = 50000;

        SortableSlot.Partitioner partitioner = new SortableSlot.Partitioner();
        List<List<SortableSlot>> slotsParts = new ArrayList<>();
        for (int i = 0; i < partitions; i++) {
            slotsParts.add(new ArrayList<SortableSlot>());
        }

        int lastPartition = -1;
        int partitionChanged = -1;
        int[] partitionMemberCount = new int[partitions];
        for (int i = 0; i < partitions; i++) {
            for (int j = 0; j < records; j++) {
                SortableSlot slot = new SortableSlot();
                slot.begin(i);
                slot.add(new IntOption(j));

                int partition = partitioner.getPartition(slot, null, partitions);
                partitionMemberCount[partition]++;

                if (lastPartition != partition) {
                    partitionChanged++;
                }
                lastPartition = partition;
            }
        }

        int max = -1;
        for (int memberCount : partitionMemberCount) {
            max = Math.max(max, memberCount);
        }

        assertThat("slant members: " + Arrays.toString(partitionMemberCount),
                (double) max / records,
                lessThan(1.2));

        double sequencialReadAve = records * partitions / partitionChanged;
        assertThat("too fine partition", sequencialReadAve, greaterThan(500.0));
        assertThat("too coarse partition", sequencialReadAve, lessThan(10000.0));
    }

    static byte[] write(Writable writable) {
        DataOutputBuffer buffer = new DataOutputBuffer();
        buffer.reset();
        try {
            writable.write(buffer);
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        return Arrays.copyOf(buffer.getData(), buffer.getLength());
    }

    static <T extends Writable> T read(T writable, byte[] bytes) {
        DataInputBuffer buffer = new DataInputBuffer();
        buffer.reset(bytes, bytes.length);
        try {
            writable.readFields(buffer);
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        return writable;
    }

    static class BinaryComparator implements Comparator<Writable> {

        private final RawComparator<?> comparator;

        BinaryComparator(RawComparator<?> comparator) {
            this.comparator = comparator;
        }

        @Override
        public int compare(Writable o1, Writable o2) {
            byte[] b1 = write(o1);
            byte[] b2 = write(o2);
            return comparator.compare(b1, 0, b1.length, b2, 0, b2.length);
        }
    }
}
