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
package com.asakusafw.runtime.mapreduce.simple;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.Closeable;
import java.io.IOException;
import java.util.LinkedList;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.serializer.SerializationFactory;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExternalResource;
import org.junit.rules.TemporaryFolder;

import com.asakusafw.runtime.io.util.DataBuffer;
import com.asakusafw.runtime.util.hadoop.ConfigurationProvider;
import com.asakusafw.utils.io.Source;

/**
 * Test class for {@link KeyValueSorter}.
 */
public class KeyValueSorterTest {

    /**
     * The temporary folder.
     */
    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    final LinkedList<Object> resources = new LinkedList<>();

    /**
     * Closer.
     */
    @Rule
    public final ExternalResource closer = new ExternalResource() {
        @Override
        protected void after() {
            for (Object resource : resources) {
                if (resource instanceof Closeable) {
                    try {
                        ((Closeable) resource).close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    };

    KeyValueSorter<IntWritable, Text> sorter;

    final IntWritable keyWritable = new IntWritable();

    final Text valueWritable = new Text();

    /**
     * simple case.
     * @throws Exception if failed
     */
    @Test
    public void simple() throws Exception {
        put(100);
        put(300);
        put(200);
        put(400);

        Source<IntWritable> results = sort();
        check(results, 100);
        check(results, 200);
        check(results, 300);
        check(results, 400);
        assertThat(results.next(), is(false));
    }

    /**
     * middle.
     * @throws Exception if failed
     */
    @Test
    public void middle() throws Exception {
        int count = 2000;
        for (int i = 0; i < count; i++) {
            put(100);
            put(300);
            put(200);
            put(400);
        }
        Source<IntWritable> results = sort();
        for (int value : new int[] { 100, 200, 300, 400 }) {
            for (int i = 0; i < count; i++) {
                check(results, value);
            }
        }
        assertThat(results.next(), is(false));
    }

    /**
     * large case.
     * @throws Exception if failed
     */
    @Test
    public void large() throws Exception {
        int count = 10000;
        for (int i = 0; i < count; i++) {
            put(100);
            put(300);
            put(200);
            put(400);
        }
        Source<IntWritable> results = sort();
        for (int value : new int[] { 100, 200, 300, 400 }) {
            for (int i = 0; i < count; i++) {
                check(results, value);
            }
        }
        assertThat(results.next(), is(false));
    }

    /**
     * extra-large case.
     * @throws Exception if failed
     */
    @Test
    public void xlarge() throws Exception {
        int count = 100000;
        for (int i = 0; i < count; i++) {
            put(100);
            put(300);
            put(200);
            put(400);
        }
        Source<IntWritable> results = sort();
        for (int value : new int[] { 100, 200, 300, 400 }) {
            for (int i = 0; i < count; i++) {
                check(results, value);
            }
        }
        assertThat(results.next(), is(false));
    }

    private void check(Source<IntWritable> results, int expected) throws IOException, InterruptedException {
        assertTrue(results.next());
        assertEquals(expected, results.get().get());
    }

    private void put(int value) throws IOException, InterruptedException {
        keyWritable.set(value);
        valueWritable.set(String.valueOf(value));
        sorter().put(keyWritable, valueWritable);
    }

    private Source<IntWritable> sort() throws IOException, InterruptedException {
        return manage(new DeserSource(sorter().sort()));
    }

    private KeyValueSorter<IntWritable, Text> sorter() throws IOException {
        if (sorter == null) {
            sorter = manage(new KeyValueSorter<>(
                    new SerializationFactory(new ConfigurationProvider().newInstance()),
                    IntWritable.class,
                    Text.class,
                    new IntWritable.Comparator(),
                    new KeyValueSorter.Options()
                        .withBufferSize(0)
                        .withTemporaryDirectory(temporaryFolder.newFolder())));
        }
        return sorter;
    }

    private <T> T manage(T object) {
        resources.addFirst(object);
        return object;
    }

    private static final class DeserSource implements Source<IntWritable> {

        private final Source<KeyValueSlice> origin;

        private final IntWritable writable = new IntWritable();

        private final Text text = new Text();

        private final DataBuffer buffer = new DataBuffer();

        public DeserSource(Source<KeyValueSlice> origin) {
            this.origin = origin;
        }

        @Override
        public boolean next() throws IOException, InterruptedException {
            if (origin.next() == false) {
                return false;
            }
            KeyValueSlice slice = origin.get();

            buffer.reset(slice.getBytes(), slice.getKeyOffset(), slice.getKeyLength());
            writable.readFields(buffer);

            buffer.reset(slice.getBytes(), slice.getValueOffset(), slice.getValueLength());
            text.readFields(buffer);

            assertEquals(writable.get(), Integer.parseInt(text.toString()));
            return true;
        }

        @Override
        public IntWritable get() throws IOException, InterruptedException {
            return writable;
        }

        @Override
        public void close() throws IOException {
            origin.close();
        }
    }
}
