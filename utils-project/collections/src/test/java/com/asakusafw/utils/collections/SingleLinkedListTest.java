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
package com.asakusafw.utils.collections;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.junit.Test;

/**
 * Test for {@link SingleLinkedList}.
 */
public class SingleLinkedListTest {

    /**
     * Test method for {@link SingleLinkedList#SingleLinkedList()}.
     */
    @Test
    public void new_empty() {
        SingleLinkedList<String> list = new SingleLinkedList<>();
        assertThat(list.size(), is(0));
    }

    /**
     * Test method for {@link SingleLinkedList#SingleLinkedList(java.util.List)}.
     */
    @Test
    public void new_list() {
        List<String> from = Arrays.asList("a", "b", "c");
        SingleLinkedList<String> list = new SingleLinkedList<>(from);
        assertThat(list.size(), is(3));
        assertThat(list.get(0), is("a"));
        assertThat(list.get(1), is("b"));
        assertThat(list.get(2), is("c"));
    }

    /**
     * Test method for {@link SingleLinkedList#SingleLinkedList(Iterable)}.
     */
    @Test
    public void new_collection() {
        List<String> from = Arrays.asList("a", "b", "c");
        SingleLinkedList<String> list = new SingleLinkedList<>(new HashSet<>(from));
        assertThat(list.size(), is(3));
        Set<String> to = new HashSet<>();
        to.add(list.get(0));
        to.add(list.get(1));
        to.add(list.get(2));
        assertThat(to, is((Object) new HashSet<>(from)));
    }

    /**
     * Test method for {@link SingleLinkedList#isEmpty()}.
     */
    @Test
    public void isEmpty() {
        {
            SingleLinkedList<String> list = new SingleLinkedList<>();
            assertTrue(list.isEmpty());
        }
        {
            List<String> from = Arrays.asList("a");
            SingleLinkedList<String> list = new SingleLinkedList<>(from);
            assertFalse(list.isEmpty());
        }
        {
            List<String> from = Arrays.asList("a", "b", "c");
            SingleLinkedList<String> list = new SingleLinkedList<>(from);
            assertFalse(list.isEmpty());
        }
    }

    /**
     * Test method for {@link SingleLinkedList#size()}.
     */
    @Test
    public void size() {
        {
            SingleLinkedList<String> list = new SingleLinkedList<>();
            assertThat(list.size(), is(0));
        }
        {
            List<String> from = Arrays.asList("a");
            SingleLinkedList<String> list = new SingleLinkedList<>(from);
            assertThat(list.size(), is(1));
        }
        {
            List<String> from = Arrays.asList("a", "b", "c");
            SingleLinkedList<String> list = new SingleLinkedList<>(from);
            assertThat(list.size(), is(3));
        }
    }

    /**
     * Test method for {@link SingleLinkedList#concat(java.lang.Object)}.
     */
    @Test
    public void concat() {
        {
            SingleLinkedList<String> list0 = new SingleLinkedList<>();
            assertThat(list0.size(), is(0));

            SingleLinkedList<String> list1 = list0.concat("a");
            assertThat(list0.size(), is(0));
            assertThat(list1.size(), is(1));
            assertThat(list1.get(0), is("a"));

            SingleLinkedList<String> list2 = list1.concat("b");
            assertThat(list0.size(), is(0));
            assertThat(list1.size(), is(1));
            assertThat(list1.get(0), is("a"));
            assertThat(list2.size(), is(2));
            assertThat(list2.get(0), is("b"));
            assertThat(list2.get(1), is("a"));

            SingleLinkedList<String> list3 = list2.concat("c");
            assertThat(list0.size(), is(0));
            assertThat(list1.size(), is(1));
            assertThat(list1.get(0), is("a"));
            assertThat(list2.size(), is(2));
            assertThat(list2.get(0), is("b"));
            assertThat(list2.get(1), is("a"));
            assertThat(list3.size(), is(3));
            assertThat(list3.get(0), is("c"));
            assertThat(list3.get(1), is("b"));
            assertThat(list3.get(2), is("a"));
        }
        {
            List<String> from = Arrays.asList("a", "b", "c");
            SingleLinkedList<String> list = new SingleLinkedList<>(from);
            assertThat(list.size(), is(3));
            assertThat(list.get(0), is("a"));
            assertThat(list.get(1), is("b"));
            assertThat(list.get(2), is("c"));

            SingleLinkedList<String> concat = list.concat("d");
            assertThat(list.size(), is(3));
            assertThat(list.get(0), is("a"));
            assertThat(list.get(1), is("b"));
            assertThat(list.get(2), is("c"));
            assertThat(concat.size(), is(4));
            assertThat(concat.get(0), is("d"));
            assertThat(concat.get(1), is("a"));
            assertThat(concat.get(2), is("b"));
            assertThat(concat.get(3), is("c"));
        }
    }

    /**
     * Test method for {@link SingleLinkedList#first()}.
     */
    @Test
    public void first() {
        {
            List<String> from = Arrays.asList("a", "b", "c");
            SingleLinkedList<String> list = new SingleLinkedList<>(from);
            assertThat(list.first(), is("a"));
        }
        {
            SingleLinkedList<String> list = new SingleLinkedList<>();
            try {
                list.first();
                fail();
            } catch (NoSuchElementException e) {
                // ok.
            }
        }
    }

    /**
     * Test method for {@link SingleLinkedList#rest()}.
     */
    @Test
    public void rest() {
        {
            List<String> from = Arrays.asList("a", "b", "c");
            SingleLinkedList<String> list = new SingleLinkedList<>(from);
            SingleLinkedList<String> rest = list.rest();
            assertThat(rest.size(), is(2));
            assertThat(rest.get(0), is("b"));
            assertThat(rest.get(1), is("c"));
        }
        {
            SingleLinkedList<String> list = new SingleLinkedList<>();
            try {
                list.rest();
                fail();
            } catch (NoSuchElementException e) {
                // ok.
            }
        }
    }

    /**
     * Test method for {@link SingleLinkedList#iterator()}.
     */
    @Test
    public void iterator() {
        {
            List<String> from = Arrays.asList("a", "b", "c");
            SingleLinkedList<String> list = new SingleLinkedList<>(from);
            Iterator<String> iter = list.iterator();
            assertTrue(iter.hasNext());
            assertThat(iter.next(), is("a"));
            assertTrue(iter.hasNext());
            assertThat(iter.next(), is("b"));
            assertTrue(iter.hasNext());
            assertThat(iter.next(), is("c"));

            assertFalse(iter.hasNext());
            try {
                iter.next();
                fail();
            } catch (NoSuchElementException e) {
                // ok.
            }
        }
        {
            SingleLinkedList<String> list = new SingleLinkedList<>();
            Iterator<String> iter = list.iterator();
            assertFalse(iter.hasNext());
            try {
                iter.next();
                fail();
            } catch (NoSuchElementException e) {
                // ok.
            }
        }
    }

    /**
     * Test method for {@link SingleLinkedList#fill(java.util.Collection)}.
     */
    @Test
    public void fill() {
        {
            List<String> from = Arrays.asList("a", "b", "c");
            SingleLinkedList<String> list = new SingleLinkedList<>(from);
            List<String> to = new ArrayList<>();
            list.fill(to);
            assertThat(list.size(), is(3));
            assertThat(list.get(0), is("a"));
            assertThat(list.get(1), is("b"));
            assertThat(list.get(2), is("c"));

            assertThat(to, is(from));
        }
        {
            SingleLinkedList<String> list = new SingleLinkedList<>();
            List<String> to = new ArrayList<>();
            list.fill(to);
            assertThat(list.size(), is(0));
            assertThat(to.size(), is(0));
        }
    }

    /**
     * Test method for {@link SingleLinkedList#equals(java.lang.Object)}.
     */
    @Test
    public void equals() {
        {
            SingleLinkedList<?> a = new SingleLinkedList<>(Collections.emptyList());
            SingleLinkedList<?> b = new SingleLinkedList<>(Collections.emptyList());
            assertTrue(a.equals(b));
            assertTrue(b.equals(a));
        }
        {
            SingleLinkedList<?> a = new SingleLinkedList<>(Arrays.asList("a"));
            SingleLinkedList<?> b = new SingleLinkedList<>(Collections.emptyList());
            assertFalse(a.equals(b));
            assertFalse(b.equals(a));
        }
        {
            SingleLinkedList<?> a = new SingleLinkedList<>(Arrays.asList("a"));
            SingleLinkedList<?> b = new SingleLinkedList<>(Arrays.asList("a"));
            assertTrue(a.equals(b));
            assertTrue(b.equals(a));
        }
        {
            SingleLinkedList<?> a = new SingleLinkedList<>(Arrays.asList("a"));
            SingleLinkedList<?> b = new SingleLinkedList<>(Arrays.asList("b"));
            assertFalse(a.equals(b));
            assertFalse(b.equals(a));
        }
        {
            SingleLinkedList<?> a = new SingleLinkedList<>(Arrays.asList("a", "b"));
            SingleLinkedList<?> b = new SingleLinkedList<>(Arrays.asList("b"));
            assertFalse(a.equals(b));
            assertFalse(b.equals(a));
        }
        {
            SingleLinkedList<?> a = new SingleLinkedList<>(Arrays.asList("a", "b"));
            SingleLinkedList<?> b = new SingleLinkedList<>(Arrays.asList("a", "b"));
            assertTrue(a.equals(b));
            assertTrue(b.equals(a));
        }
        {
            SingleLinkedList<?> a = new SingleLinkedList<>(Arrays.asList("a", "b", "c"));
            SingleLinkedList<?> b = new SingleLinkedList<>(Arrays.asList("a", "b"));
            assertFalse(a.equals(b));
            assertFalse(b.equals(a));
        }
        {
            SingleLinkedList<?> a = new SingleLinkedList<>(Arrays.asList("a", "b", "c"));
            SingleLinkedList<?> b = new SingleLinkedList<>(Arrays.asList("a", "b", "c"));
            assertTrue(a.equals(b));
            assertTrue(b.equals(a));
        }
    }

    /**
     * Test method for {@link SingleLinkedList#equals(java.lang.Object)}.
     * @throws Exception If exception occurred
     */
    @Test
    public void serialize() throws Exception {
        {
            SingleLinkedList<String> list = new SingleLinkedList<>();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try (ObjectOutputStream oo = new ObjectOutputStream(out)) {
                oo.writeObject(list);
            }
            SingleLinkedList<?> serialized;
            try (ObjectInputStream oi = new ObjectInputStream(new ByteArrayInputStream(out.toByteArray()))) {
                serialized = (SingleLinkedList<?>) oi.readObject();
            }
            assertThat(serialized.size(), is(0));
        }
        {
            List<String> from = Arrays.asList("a", "b", "c");
            SingleLinkedList<String> list = new SingleLinkedList<>(from);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try (ObjectOutputStream oo = new ObjectOutputStream(out)) {
                oo.writeObject(list);
            }
            SingleLinkedList<?> serialized;
            try (ObjectInputStream oi = new ObjectInputStream(new ByteArrayInputStream(out.toByteArray()))) {
                serialized = (SingleLinkedList<?>) oi.readObject();
            }
            assertThat(serialized.size(), is(3));
            assertThat(serialized.get(0), is((Object) "a"));
            assertThat(serialized.get(1), is((Object) "b"));
            assertThat(serialized.get(2), is((Object) "c"));
        }
    }
}
