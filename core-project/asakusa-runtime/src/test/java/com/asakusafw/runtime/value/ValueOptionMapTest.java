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
package com.asakusafw.runtime.value;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

/**
 * Test for {@link ValueOptionMap}.
 */
public class ValueOptionMapTest {

    /**
     * simple case.
     */
    @Test
    public void simple() {
        Mock<String> map = new Mock<>("a", "b", "c");
        assertThat(map.size(), is(3));

        IntOption a = map.get("a");
        IntOption b = map.get("b");
        IntOption c = map.get("c");
        assertThat(map.get("d"), is(nullValue()));

        map.put("a", new IntOption(0));
        map.put("b", new IntOption(1));
        map.put("c", new IntOption(2));

        assertThat(map.size(), is(3));
        assertThat(a, is(new IntOption(0)));
        assertThat(b, is(new IntOption(1)));
        assertThat(c, is(new IntOption(2)));
        assertThat(map, hasEntry(is("a"), sameInstance(a)));
        assertThat(map, hasEntry(is("b"), sameInstance(b)));
        assertThat(map, hasEntry(is("c"), sameInstance(c)));
    }

    /**
     * contains key.
     */
    @Test
    public void containsKey() {
        Mock<String> map = new Mock<>("a", "b", "c");
        assertThat(map.containsKey("a"), is(true));
        assertThat(map.containsKey("A"), is(false));
    }

    /**
     * for each.
     */
    @Test
    public void entry_set() {
        Mock<String> map = new Mock<>("a", "b", "c");
        map.put("a", new IntOption(0));
        map.put("b", new IntOption(1));
        map.put("c", new IntOption(2));

        Map<String, IntOption> copy = new HashMap<>();
        for (Map.Entry<String, IntOption> entry : map.entrySet()) {
            assertThat(copy.putIfAbsent(entry.getKey(), entry.getValue()), is(nullValue()));
        }
        assertThat(copy, is(map));
    }

    /**
     * for each.
     */
    @Test
    public void forEach() {
        Mock<String> map = new Mock<>("a", "b", "c");
        map.put("a", new IntOption(0));
        map.put("b", new IntOption(1));
        map.put("c", new IntOption(2));

        Map<String, IntOption> copy = new HashMap<>();
        map.forEach((k, v) -> {
            assertThat(copy.putIfAbsent(k, v), is(nullValue()));
        });
        assertThat(copy, is(map));
    }

    /**
     * update entries.
     */
    @Test
    public void update_entries() {
        Mock<String> map = new Mock<>("a", "b", "c");
        IntOption a = map.get("a");
        IntOption b = map.get("b");
        IntOption c = map.get("c");

        int index = 0;
        for (Map.Entry<String, IntOption> entry : map.entrySet()) {
            entry.setValue(new IntOption(index++));
        }
        assertThat(map.size(), is(3));
        assertThat(a, is(new IntOption(0)));
        assertThat(b, is(new IntOption(1)));
        assertThat(c, is(new IntOption(2)));
        assertThat(map, hasEntry(is("a"), sameInstance(a)));
        assertThat(map, hasEntry(is("b"), sameInstance(b)));
        assertThat(map, hasEntry(is("c"), sameInstance(c)));
    }

    /**
     * building keys.
     */
    @Test
    public void keys() {
        Set<String> keys0 = ValueOptionMap.keys();
        assertThat(keys0, hasSize(0)); // ordered

        Set<String> keys1 = ValueOptionMap.keys("a");
        assertThat(keys1, contains("a"));

        Set<String> keysN = ValueOptionMap.keys("a", "b", "c", "d", "e");
        assertThat(keysN, contains("a", "b", "c", "d", "e")); // ordered
    }

    /**
     * put null.
     */
    @Test
    public void put_null() {
        Mock<String> map = new Mock<>("a");
        map.put("a", new IntOption(100));
        map.put("a", null);
        assertThat(map, hasEntry("a", new IntOption()));
    }

    /**
     * put unknown key.
     */
    @Test(expected = IllegalArgumentException.class)
    public void put_unknown() {
        Mock<String> map = new Mock<>("a");
        map.put("UNKNOWN", new IntOption());
    }

    private static class Mock<T> extends ValueOptionMap<T, IntOption> {

        private final Map<T, IntOption> entity = new LinkedHashMap<>();

        @SafeVarargs
        Mock(T... keys) {
            for (T key : keys) {
                entity.put(key, new IntOption());
            }
        }

        @Override
        public Set<T> keySet() {
            return entity.keySet();
        }

        @Override
        public IntOption get(Object key) {
            return entity.get(key);
        }
    }
}
