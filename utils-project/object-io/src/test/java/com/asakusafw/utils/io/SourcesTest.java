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
package com.asakusafw.utils.io;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

import org.junit.Test;

/**
 * Test for {@link Sources}.
 */
public class SourcesTest {

    /**
     * simple test for {@link Sources#empty()}.
     * @throws Exception if failed
     */
    @Test
    public void empty() throws Exception {
        List<String> objects = dump(Sources.<String>empty());
        assertThat(objects, hasSize(0));
    }

    /**
     * simple test for {@link Sources#wrap(java.util.Iterator)}.
     * @throws Exception if failed
     */
    @Test
    public void wrap() throws Exception {
        List<String> objects = dump(Sources.wrap(iter("1", "2", "3")));
        assertThat(objects, contains("1", "2", "3"));
    }

    /**
     * empty elements for {@link Sources#wrap(java.util.Iterator)}.
     * @throws Exception if failed
     */
    @Test
    public void wrap_empty() throws Exception {
        List<String> objects = dump(Sources.wrap(this.<String>iter()));
        assertThat(objects, hasSize(0));
    }

    /**
     * Test method for {@link Sources#concat(java.util.List)}.
     * @throws Exception if failed
     */
    @Test
    public void concat() throws Exception {
        List<Source<String>> sources = new ArrayList<Source<String>>();
        sources.add(Sources.wrap(iter("1", "2")));
        sources.add(Sources.wrap(iter("3", "4")));
        sources.add(Sources.wrap(iter("5", "6")));
        List<String> objects = dump(Sources.concat(sources));

        assertThat(objects, contains("1", "2", "3", "4", "5", "6"));
    }

    /**
     * empty elements for {@link Sources#concat(java.util.List)}.
     * @throws Exception if failed
     */
    @Test
    public void concat_empty() throws Exception {
        List<Source<String>> sources = new ArrayList<Source<String>>();
        List<String> objects = dump(Sources.concat(sources));

        assertThat(objects, hasSize(0));
    }

    /**
     * Test method for {@link Sources#merge(java.util.List, java.util.Comparator)}.
     */
    @Test
    public void merge() {
        List<Source<IntBuf>> cursors = new ArrayList<Source<IntBuf>>();
        cursors.add(Sources.wrap(array(1, 4, 7)));
        cursors.add(Sources.wrap(array(2, 5, 8)));
        cursors.add(Sources.wrap(array(3, 6, 9)));
        check(Sources.merge(cursors, IntBuf.COMPARATOR), 1, 2, 3, 4, 5, 6, 7, 8, 9);
    }

    /**
     * Test method for {@link Sources#merge(java.util.List, java.util.Comparator)}.
     * @throws Exception if failed
     */
    @Test
    public void merge_large() throws Exception {
        int base = 1000000;
        List<Source<IntBuf>> cursors = new ArrayList<Source<IntBuf>>();
        cursors.add(new IntSource(random(new Random(6502 + 0), base * 3)));
        cursors.add(new IntSource(random(new Random(6502 + 1), base * 3)));
        cursors.add(new IntSource(random(new Random(6502 + 2), base * 4)));
        int last = -1;
        int count = 0;
        Source<IntBuf> c = Sources.merge(cursors, IntBuf.COMPARATOR);
        try {
            while (c.next()) {
                int next = c.get().value;
                assertTrue(last <= next);
                last = next;
                count++;
            }
        } finally {
            c.close();
        }
        assertThat(count, is(base * 10));
    }

    /**
     * Test method for {@link Sources#merge(java.util.List, java.util.Comparator)}.
     * @throws Exception if failed
     */
    @Test
    public void merge_many() throws Exception {
        int numSources = 1000;
        int base = 1000;
        List<Source<IntBuf>> cursors = new ArrayList<Source<IntBuf>>();
        for (int i = 0; i < numSources; i++) {
            cursors.add(new IntSource(random(new Random(6502 + i), base)));
        }
        int last = -1;
        int count = 0;
        Source<IntBuf> c = Sources.merge(cursors, IntBuf.COMPARATOR);
        try {
            while (c.next()) {
                int next = c.get().value;
                assertTrue(last <= next);
                last = next;
                count++;
            }
        } finally {
            c.close();
        }
        assertThat(count, is(base * numSources));
    }

    private IntBuf[] array(int... values) {
        IntBuf[] results = new IntBuf[values.length];
        for (int i = 0; i < results.length; i++) {
            results[i] = new IntBuf(values[i]);
        }
        return results;
    }

    private int[] random(Random random, int count) {
        int[] array = new int[count];
        for (int i = 0; i < array.length; i++) {
            array[i] = random.nextInt(Integer.MAX_VALUE);
        }
        Arrays.sort(array);
        return array;
    }

    private void check(Source<IntBuf> cursor, int... values) {
        try {
            for (int i = 0; i < values.length; i++) {
                assertTrue(cursor.next());
                assertEquals(values[i], cursor.get().value);
            }
            assertFalse(cursor.next());
        } catch (Exception e) {
            throw new AssertionError(e);
        } finally {
            try {
                cursor.close();
            } catch (IOException e) {
                throw new AssertionError(e);
            }
        }
    }

    private static final class IntBuf implements Comparable<IntBuf> {

        static final Comparator<IntBuf> COMPARATOR = new Comparator<IntBuf>() {
            @Override
            public int compare(IntBuf o1, IntBuf o2) {
                return o1.compareTo(o2);
            }
        };

        int value;

        IntBuf(int value) {
            this.value = value;
        }

        @Override
        public int compareTo(IntBuf o) {
            if (value < o.value) {
                return -1;
            } else if (value > o.value) {
                return +1;
            }
            return 0;
        }

        @Override
        public int hashCode() {
            return value;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            IntBuf other = (IntBuf) obj;
            if (value != other.value) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }

    private static final class IntSource implements Source<IntBuf> {

        private final int[] array;

        private int position = -1;

        private final IntBuf next = new IntBuf(-1);

        public IntSource(int[] values) {
            this.array = values;
        }

        @Override
        public boolean next() throws IOException, InterruptedException {
            if (position + 1 < array.length) {
                next.value = array[++position];
                return true;
            } else {
                next.value = -1;
                return false;
            }
        }

        @Override
        public IntBuf get() throws IOException, InterruptedException {
            if (next.value == -1) {
                throw new NoSuchElementException();
            }
            return next;
        }

        @Override
        public void close() {
            return;
        }
    }

    private <T> Iterator<T> iter(T... values) {
        return Arrays.asList(values).iterator();
    }

    private <T> List<T> dump(Source<T> source) throws IOException, InterruptedException {
        try {
            List<T> results = new ArrayList<T>();
            while (source.next()) {
                results.add(source.get());
            }
            return results;
        } finally {
            source.close();
        }
    }
}
