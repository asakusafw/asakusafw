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
package com.asakusafw.iterative.common.basic;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

import com.asakusafw.iterative.common.BaseCursor;

/**
 * Utilities for {@link BaseCursor}.
 * @since 0.8.0
 */
public final class CursorUtil {

    private CursorUtil() {
        return;
    }

    /**
     * Returns an {@link Iterator} over the cursor.
     * @param <T> the element type
     * @param cursor the target cursor
     * @return the created iterator
     */
    public static <T> Iterator<T> toIterator(BaseCursor<T> cursor) {
        Objects.requireNonNull(cursor);
        return new Iterator<T>() {
            private boolean prepared = false;
            @Override
            public boolean hasNext() {
                return prepare();
            }
            @Override
            public T next() {
                if (prepare()) {
                    prepared = false;
                    return cursor.get();
                }
                throw new NoSuchElementException();
            }
            private boolean prepare() {
                if (prepared == false) {
                    prepared = cursor.next();
                }
                return prepared;
            }
            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}
