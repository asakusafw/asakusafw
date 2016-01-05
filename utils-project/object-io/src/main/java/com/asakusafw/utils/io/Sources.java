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
package com.asakusafw.utils.io;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Utilities about {@link Source}.
 * @since 0.6.0
 * @version 0.7.1
 */
public final class Sources {

    private Sources() {
        return;
    }

    /**
     * Returns an empty {@link Source}.
     * @param <T> the element type
     * @return an empty source
     */
    public static <T> Source<T> empty() {
        return new Source<T>() {
            @Override
            public boolean next() throws IOException, InterruptedException {
                return false;
            }

            @Override
            public T get() throws IOException, InterruptedException {
                throw new NoSuchElementException();
            }

            @Override
            public void close() {
                return;
            }
        };
    }

    /**
     * Returns a {@link Source} for the array.
     * @param <T> the array element type
     * @param values the target array
     * @return the wrapped source
     */
    @SafeVarargs
    public static <T> Source<T> wrap(final T... values) {
        return new Source<T>() {

            private int position = -1;

            @Override
            public boolean next() throws IOException, InterruptedException {
                if (position + 1 < values.length) {
                    position++;
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            public T get() throws IOException, InterruptedException {
                if (0 <= position && position < values.length) {
                    return values[position];
                }
                throw new NoSuchElementException();
            }

            @Override
            public void close() {
                return;
            }
        };
    }

    /**
     * Wraps {@link Iterator} object.
     * @param iterator the iterator object
     * @param <T> the element type
     * @return the {@link Source} which provides elements in the iterator
     */
    public static <T> Source<T> wrap(final Iterator<? extends T> iterator) {
        return new Source<T>() {
            private T next;
            @Override
            public boolean next() throws IOException, InterruptedException {
                next = null;
                if (iterator.hasNext()) {
                    next = iterator.next();
                    return true;
                } else {
                    return false;
                }
            }
            @Override
            public T get() throws IOException, InterruptedException {
                if (next == null) {
                    throw new NoSuchElementException();
                }
                return next;
            }
            @Override
            public void close() throws IOException {
                next = null;
            }
        };
    }

    /**
     * Concatenates a list of sources.
     * @param sources the list of sources
     * @param <T> the element type
     * @return the concatenated source
     */
    public static <T> Source<T> concat(List<? extends Source<? extends T>> sources) {
        final Iterator<? extends Source<? extends T>> iter = sources.iterator();
        return new Source<T>() {

            Source<? extends T> current;

            @Override
            public boolean next() throws IOException, InterruptedException {
                while (true) {
                    if (current != null) {
                        if (current.next()) {
                            return true;
                        } else {
                            closeCurrent();
                        }
                    }
                    assert current == null;
                    if (iter.hasNext()) {
                        current = iter.next();
                    } else {
                        return false;
                    }
                }
            }

            @Override
            public T get() throws IOException, InterruptedException {
                if (current == null) {
                    throw new NoSuchElementException();
                }
                return current.get();
            }

            @Override
            public void close() throws IOException {
                closeCurrent();
                assert current == null;
                while (iter.hasNext()) {
                    current = iter.next();
                    closeCurrent();
                    assert current == null;
                }
            }

            private void closeCurrent() throws IOException {
                if (current != null) {
                    current.close();
                    current = null;
                }
            }
        };
    }

    /**
     * Merges the sorted sources.
     * @param <T> the element type
     * @param sortedSources sources which are sorted by the {@code comparator}
     * @param comparator the comparator
     * @return the merged source
     * @since 0.7.1
     */
    public static <T> Source<T> merge(
            List<? extends Source<? extends T>> sortedSources,
            Comparator<? super T> comparator) {
        if (sortedSources.isEmpty()) {
            return empty();
        } else if (sortedSources.size() == 1) {
            @SuppressWarnings("unchecked")
            Source<T> source = (Source<T>) sortedSources.get(0);
            return source;
        }
        return new HeapSource<>(sortedSources, comparator);
    }

    private static final class HeapSource<T> implements Source<T> {

        private final HeapElement<T>[] heap;

        private final Comparator<? super T> comparator;

        private boolean firstTime = true;

        @SuppressWarnings("unchecked")
        public HeapSource(
                List<? extends Source<? extends T>> sortedSources,
                Comparator<? super T> comparator) {
            assert sortedSources.isEmpty() == false;
            this.heap = new HeapElement[sortedSources.size()];
            this.comparator = comparator;
            for (int i = 0; i < heap.length; i++) {
                this.heap[i] = new HeapElement<>(sortedSources.get(i));
            }
        }

        @Override
        public boolean next() throws IOException, InterruptedException {
            HeapElement<T>[] h = heap;
            if (firstTime) {
                firstTime = false;
                for (int i = 0; i < h.length; i++) {
                    h[i].fill();
                }
                for (int i = h.length / 2; i >= 0; i--) {
                    shiftDown(i);
                }
            } else {
                h[0].fill();
                shiftDown(0);
            }
            return h[0].value != null;
        }

        private void shiftDown(int i) {
            HeapElement<T>[] h = heap;
            int length = h.length;
            int current = i;
            while (true) {
                int left = (current << 1) + 1;
                int right = left + 1;
                if (left < length && isViolate(h[current], h[left])) {
                    if (right < length && isViolate(h[left], h[right])) {
                        swap(current, right);
                        current = right;
                    } else {
                        swap(current, left);
                        current = left;
                    }
                } else {
                    if (right < length && isViolate(h[current], h[right])) {
                        swap(current, right);
                        current = right;
                    } else {
                        break;
                    }
                }
            }
        }

        private boolean isViolate(HeapElement<T> parent, HeapElement<T> node) {
            T v1 = parent.value;
            T v2 = node.value;
            if (v1 == null) {
                if (v2 == null) {
                    return false;
                }
                return true;
            } else if (v2 == null) {
                return false;
            }
            return comparator.compare(v1, v2) > 0;
        }

        private void swap(int i, int j) {
            HeapElement<T>[] h = heap;
            HeapElement<T> t = h[i];
            h[i] = h[j];
            h[j] = t;
        }

        @Override
        public T get() {
            HeapElement<T> top = heap[0];
            if (firstTime || top.value == null) {
                throw new NoSuchElementException();
            }
            return top.value;
        }

        @Override
        public void close() throws IOException {
            IOException firstException = null;
            for (HeapElement<T> element : heap) {
                try {
                    element.close();
                } catch (IOException e) {
                    if (firstException == null) {
                        firstException = e;
                    }
                }
            }
            if (firstException != null) {
                throw firstException;
            }
        }

        @Override
        public String toString() {
            return Arrays.toString(heap);
        }
    }

    private static final class HeapElement<T> implements Closeable {

        private final Source<? extends T> source;

        T value;

        public HeapElement(Source<? extends T> source) {
            this.source = source;
        }

        void fill() throws IOException, InterruptedException {
            if (source.next()) {
                value = source.get();
            } else {
                value = null;
            }
        }

        @Override
        public void close() throws IOException {
            source.close();
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }
}
