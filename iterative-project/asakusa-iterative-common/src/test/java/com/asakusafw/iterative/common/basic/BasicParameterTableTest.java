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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.asakusafw.iterative.common.ParameterSet;
import com.asakusafw.iterative.common.ParameterTable;

/**
 * Test for {@link BasicParameterTable}.
 */
public class BasicParameterTableTest {

    /**
     * builder - simple case.
     */
    @Test
    public void builder() {
        ParameterTable table = new BasicParameterTable.BasicBuilder()
            .next()
                .put("a", "A")
            .build();

        List<ParameterSet> rows = table.getRows();
        Map<String, String> map = rows.get(0).toMap();
        assertThat(map.keySet(), containsInAnyOrder("a"));
        assertThat(map, hasEntry("a", "A"));
    }

    /**
     * builder - multiple columns.
     */
    @Test
    public void builder_columns() {
        ParameterTable table = new BasicParameterTable.BasicBuilder()
                .next()
                    .put("a", "A")
                    .put("b", "B")
                    .put("c", "C")
                .build();

        List<ParameterSet> rows = table.getRows();
        Map<String, String> map = rows.get(0).toMap();
        assertThat(map.keySet(), containsInAnyOrder("a", "b", "c"));
        assertThat(map, hasEntry("a", "A"));
        assertThat(map, hasEntry("b", "B"));
        assertThat(map, hasEntry("c", "C"));
    }

    /**
     * builder - multiple rows.
     */
    @Test
    public void builder_rows() {
        ParameterTable table = new BasicParameterTable.BasicBuilder()
                .next().put("a", "A")
                .next().put("b", "B")
                .next().put("c", "C")
                .build();

        List<ParameterSet> rows = table.getRows();
        Map<String, String> m0 = rows.get(0).toMap();
        assertThat(m0.keySet(), containsInAnyOrder("a"));
        assertThat(m0, hasEntry("a", "A"));

        Map<String, String> m1 = rows.get(1).toMap();
        assertThat(m1.keySet(), containsInAnyOrder("b"));
        assertThat(m1, hasEntry("b", "B"));

        Map<String, String> m2 = rows.get(2).toMap();
        assertThat(m2.keySet(), containsInAnyOrder("c"));
        assertThat(m2, hasEntry("c", "C"));
    }

    /**
     * builder - with map.
     */
    @Test
    public void builder_map() {
        Map<String, String> r = new LinkedHashMap<>();
        r.put("b", "B");
        r.put("c", "C");

        ParameterTable table = new BasicParameterTable.BasicBuilder()
            .next()
                .put("a", "A")
                .put(r)
            .build();

        List<ParameterSet> rows = table.getRows();
        Map<String, String> map = rows.get(0).toMap();
        assertThat(map.keySet(), containsInAnyOrder("a", "b", "c"));
        assertThat(map, hasEntry("a", "A"));
        assertThat(map, hasEntry("b", "B"));
        assertThat(map, hasEntry("c", "C"));
    }

    /**
     * cursor - simple case.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void cursor() {
        ParameterTable.Cursor c = new BasicParameterTable.BasicBuilder()
            .next().put("a", "A")
            .build()
            .newCursor();

        assertThat(c.next(), is(true));
        ParameterSet r0 = c.get();
        assertThat(c.getDifferences(), containsInAnyOrder("a"));
        assertThat(r0.getAvailable(), containsInAnyOrder("a"));
        assertThat(r0.toMap(), allOf(hasEntry("a", "A")));

        assertThat(c.next(), is(false));
    }

    /**
     * cursor - change value.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void cursor_diff_value() {
        ParameterTable.Cursor c = new BasicParameterTable.BasicBuilder()
                .next().put("a", "A")
                .next().put("a", "B")
                .next().put("a", "C")
                .build()
                .newCursor();

        assertThat(c.next(), is(true));
        ParameterSet r0 = c.get();
        assertThat(c.getDifferences(), containsInAnyOrder("a"));
        assertThat(r0.getAvailable(), containsInAnyOrder("a"));
        assertThat(r0.toMap(), allOf(hasEntry("a", "A")));

        assertThat(c.next(), is(true));
        ParameterSet r1 = c.get();
        assertThat(c.getDifferences(), containsInAnyOrder("a"));
        assertThat(r1.getAvailable(), containsInAnyOrder("a"));
        assertThat(r1.toMap(), allOf(hasEntry("a", "B")));

        assertThat(c.next(), is(true));
        ParameterSet r2 = c.get();
        assertThat(c.getDifferences(), containsInAnyOrder("a"));
        assertThat(r2.getAvailable(), containsInAnyOrder("a"));
        assertThat(r2.toMap(), allOf(hasEntry("a", "C")));

        assertThat(c.next(), is(false));
    }

    /**
     * cursor - increment.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void cursor_increment() {
        ParameterTable.Cursor c = new BasicParameterTable.BasicBuilder()
            .next().put("a", "A")
            .next().put("a", "A").put("b", "B")
            .next().put("a", "A").put("b", "B").put("c", "C")
            .build()
            .newCursor();

        assertThat(c.next(), is(true));
        ParameterSet r0 = c.get();
        assertThat(c.getDifferences(), containsInAnyOrder("a"));
        assertThat(r0.getAvailable(), containsInAnyOrder("a"));
        assertThat(r0.toMap(), allOf(hasEntry("a", "A")));

        assertThat(c.next(), is(true));
        ParameterSet r1 = c.get();
        assertThat(c.getDifferences(), containsInAnyOrder("b"));
        assertThat(r1.getAvailable(), containsInAnyOrder("a", "b"));
        assertThat(r1.toMap(), allOf(hasEntry("a", "A")));
        assertThat(r1.toMap(), allOf(hasEntry("b", "B")));

        assertThat(c.next(), is(true));
        ParameterSet r2 = c.get();
        assertThat(c.getDifferences(), containsInAnyOrder("c"));
        assertThat(r2.getAvailable(), containsInAnyOrder("a", "b", "c"));
        assertThat(r2.toMap(), allOf(hasEntry("a", "A")));
        assertThat(r2.toMap(), allOf(hasEntry("b", "B")));
        assertThat(r2.toMap(), allOf(hasEntry("c", "C")));

        assertThat(c.next(), is(false));
    }

    /**
     * cursor - decrement.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void cursor_decrement() {
        ParameterTable.Cursor c = new BasicParameterTable.BasicBuilder()
            .next().put("a", "A").put("b", "B").put("c", "C")
            .next().put("a", "A").put("b", "B")
            .next().put("a", "A")
            .build()
            .newCursor();

        assertThat(c.next(), is(true));
        ParameterSet r0 = c.get();
        assertThat(c.getDifferences(), containsInAnyOrder("a", "b", "c"));
        assertThat(r0.getAvailable(), containsInAnyOrder("a", "b", "c"));
        assertThat(r0.toMap(), allOf(hasEntry("a", "A")));
        assertThat(r0.toMap(), allOf(hasEntry("b", "B")));
        assertThat(r0.toMap(), allOf(hasEntry("c", "C")));

        assertThat(c.next(), is(true));
        ParameterSet r1 = c.get();
        assertThat(c.getDifferences(), containsInAnyOrder("c"));
        assertThat(r1.getAvailable(), containsInAnyOrder("a", "b"));
        assertThat(r1.toMap(), allOf(hasEntry("a", "A")));
        assertThat(r1.toMap(), allOf(hasEntry("b", "B")));

        assertThat(c.next(), is(true));
        ParameterSet r2 = c.get();
        assertThat(c.getDifferences(), containsInAnyOrder("b"));
        assertThat(r2.getAvailable(), containsInAnyOrder("a"));
        assertThat(r2.toMap(), allOf(hasEntry("a", "A")));

        assertThat(c.next(), is(false));
    }

    /**
     * is empty.
     */
    @Test
    public void is_empty() {
        ParameterTable empty = new BasicParameterTable.BasicBuilder()
            .build();
        ParameterTable table = new BasicParameterTable.BasicBuilder()
            .next().put("a", "A")
            .build();

        assertThat(empty.isEmpty(), is(true));
        assertThat(table.isEmpty(), is(false));
    }

    /**
     * get row count.
     */
    @Test
    public void row_count() {
        ParameterTable r0 = new BasicParameterTable.BasicBuilder()
            .build();
        ParameterTable r1 = new BasicParameterTable.BasicBuilder()
            .next().put("a", "A")
            .build();
        ParameterTable r3 = new BasicParameterTable.BasicBuilder()
            .next().put("a", "A")
            .next().put("b", "B")
            .next().put("c", "C")
            .build();

        assertThat(r0.getRowCount(), is(0));
        assertThat(r1.getRowCount(), is(1));
        assertThat(r3.getRowCount(), is(3));
    }

    /**
     * get available.
     */
    @Test
    public void available() {
        ParameterTable t = new BasicParameterTable.BasicBuilder()
                .next().put("a", "A")
                .next().put("b", "B")
                .next().put("c", "C")
                .build();

        assertThat(t.getAvailable(), containsInAnyOrder("a", "b", "c"));
    }

    /**
     * get partial.
     */
    @Test
    public void partial() {
        ParameterTable t = new BasicParameterTable.BasicBuilder()
                .next().put("a", "A1").put("b", "B1").put("c", "C1")
                .next().put("a", "A2").put("b", "B2")
                .next().put("a", "A3").put("c", "C3")
                .build();

        assertThat(t.getPartial(), containsInAnyOrder("b", "c"));
    }
}
