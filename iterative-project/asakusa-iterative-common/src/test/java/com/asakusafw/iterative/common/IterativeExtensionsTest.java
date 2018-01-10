/**
 * Copyright 2011-2018 Asakusa Framework Team.
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
package com.asakusafw.iterative.common;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.junit.Test;

/**
 * Test for {@link IterativeExtensions}.
 */
public class IterativeExtensionsTest {

    /**
     * builder - simple case.
     */
    @Test
    public void builder() {
        ParameterTable table = IterativeExtensions.builder()
            .next()
                .put("a", "A")
            .build();

        assertThat(table.getRowCount(), is(1));

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
        ParameterTable table = IterativeExtensions.builder()
                .next()
                    .put("a", "A")
                    .put("b", "B")
                    .put("c", "C")
                .build();

        assertThat(table.getRowCount(), is(1));

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
        ParameterTable table = IterativeExtensions.builder()
                .next().put("a", "A")
                .next().put("b", "B")
                .next().put("c", "C")
                .build();

        assertThat(table.getRowCount(), is(3));

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
     * serde - simple case.
     */
    @Test
    public void serde() {
        ParameterTable table = IterativeExtensions.builder()
            .next()
                .put("a", "A")
            .build();

        ParameterTable restored = restore(table);
        assertThat(restored.getRowCount(), is(1));

        List<ParameterSet> rows = restored.getRows();
        Map<String, String> map = rows.get(0).toMap();
        assertThat(map.keySet(), containsInAnyOrder("a"));
        assertThat(map, hasEntry("a", "A"));
    }

    /**
     * serde - multiple columns.
     */
    @Test
    public void serde_columns() {
        ParameterTable table = IterativeExtensions.builder()
                .next()
                    .put("a", "A")
                    .put("b", "B")
                    .put("c", "C")
                .build();

        ParameterTable restored = restore(table);
        assertThat(restored.getRowCount(), is(1));

        List<ParameterSet> rows = restored.getRows();
        Map<String, String> map = rows.get(0).toMap();
        assertThat(map.keySet(), containsInAnyOrder("a", "b", "c"));
        assertThat(map, hasEntry("a", "A"));
        assertThat(map, hasEntry("b", "B"));
        assertThat(map, hasEntry("c", "C"));
    }

    /**
     * serde - multiple rows.
     */
    @Test
    public void serde_rows() {
        ParameterTable table = IterativeExtensions.builder()
                .next().put("a", "A")
                .next().put("b", "B")
                .next().put("c", "C")
                .build();

        ParameterTable restored = restore(table);
        assertThat(restored.getRowCount(), is(3));

        List<ParameterSet> rows = restored.getRows();
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

    private ParameterTable restore(ParameterTable table) {
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            IterativeExtensions.save(output, table);

            ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());
            return IterativeExtensions.load(input);
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }
}
