/**
 * Copyright 2011-2021 Asakusa Framework Team.
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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.Test;

import com.asakusafw.iterative.common.BaseCursor;

/**
 * Test for {@link CursorUtil}.
 */
public class CursorUtilTest {

    /**
     * simple case.
     */
    @Test
    public void simple() {
        assertThat(iter(new Cursor("A")), contains("A"));
    }

    /**
     * empty cursor.
     */
    @Test
    public void empty_cursor() {
        assertThat(iter(new Cursor()), hasSize(0));
    }

    /**
     * multiple objects.
     */
    @Test
    public void multiple() {
        assertThat(iter(new Cursor("A", "B", "C")), contains("A", "B", "C"));
    }

    private <T> List<T> iter(BaseCursor<T> cursor) {
        List<T> results = new ArrayList<>();
        for (Iterator<T> i = CursorUtil.toIterator(cursor); i.hasNext();) {
            results.add(i.next());
        }
        return results;
    }

    private static class Cursor implements BaseCursor<String> {

        private final LinkedList<String> rest;

        private String current;

        Cursor(String... values) {
            this.rest = new LinkedList<>();
            Collections.addAll(rest, values);
        }

        @Override
        public boolean next() {
            if (rest.isEmpty()) {
                current = null;
                return false;
            } else {
                current = rest.removeFirst();
                return true;
            }
        }

        @Override
        public String get() {
            if (current == null) {
                throw new NoSuchElementException();
            }
            return current;
        }
    }
}
