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

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Utilities about {@link Source}.
 * @since 0.6.0
 */
public final class Sources {

    private Sources() {
        return;
    }

    /**
     * Wraps {@link Iterator} object.
     * @param iterator the iterator object
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
     * @return the concatenated source
     */
    public static <T> Source<T> concat(final List<? extends Source<? extends T>> sources) {
        return new Source<T>() {

            final Iterator<? extends Source<? extends T>> iter = sources.iterator();

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
}
