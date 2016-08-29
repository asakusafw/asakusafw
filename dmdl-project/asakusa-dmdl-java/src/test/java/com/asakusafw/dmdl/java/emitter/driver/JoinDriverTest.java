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
package com.asakusafw.dmdl.java.emitter.driver;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Arrays;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;

import com.asakusafw.dmdl.java.GeneratorTesterRoot;
import com.asakusafw.vocabulary.model.Joined;
import com.asakusafw.vocabulary.model.Key;

/**
 * Test for {@link JoinDriver}.
 */
public class JoinDriverTest extends GeneratorTesterRoot {

    /**
     * Initializes the test.
     * @throws Exception if some errors were occurred
     */
    @Before
    public void setUp() throws Exception {
        emitDrivers.add(new JoinDriver());
    }

    /**
     * simple join.
     */
    @Test
    public void simple_join() {
        ModelLoader loader = generate();
        Joined annotation = loader.modelType("Simple").getAnnotation(Joined.class);
        assertThat(annotation, not(nullValue()));

        assertThat(annotation.terms().length, is(2));

        Joined.Term a = annotation.terms()[0];
        assertThat(a.source(), eq(loader.modelType("A")));
        assertThat(a.mappings().length, is(2));
        assertThat(a.mappings(), hasItemInArray(mapping("sid", "sid")));
        assertThat(a.mappings(), hasItemInArray(mapping("value", "aValue")));
        assertThat(a.shuffle(), is(grouping("sid")));

        Joined.Term b = annotation.terms()[1];
        assertThat(b.source(), eq(loader.modelType("B")));
        assertThat(b.mappings().length, is(2));
        assertThat(b.mappings(), hasItemInArray(mapping("sid", "sid")));
        assertThat(b.mappings(), hasItemInArray(mapping("value", "bValue")));
        assertThat(b.shuffle(), is(grouping("sid")));
    }

    /**
     * join with renaming key.
     */
    @Test
    public void join_rename_key() {
        ModelLoader loader = generate();
        Joined annotation = loader.modelType("Simple").getAnnotation(Joined.class);
        assertThat(annotation, not(nullValue()));

        assertThat(annotation.terms().length, is(2));

        Joined.Term a = annotation.terms()[0];
        assertThat(a.source(), eq(loader.modelType("A")));
        assertThat(a.mappings().length, is(2));
        assertThat(a.mappings(), hasItemInArray(mapping("sid", "key")));
        assertThat(a.mappings(), hasItemInArray(mapping("value", "aValue")));
        assertThat(a.shuffle(), is(grouping("sid")));

        Joined.Term b = annotation.terms()[1];
        assertThat(b.source(), eq(loader.modelType("B")));
        assertThat(b.mappings().length, is(2));
        assertThat(b.mappings(), hasItemInArray(mapping("sid", "key")));
        assertThat(b.mappings(), hasItemInArray(mapping("value", "bValue")));
        assertThat(b.shuffle(), is(grouping("sid")));
    }

    /**
     * simple record.
     */
    @Test
    public void simple_record() {
        ModelLoader loader = generate();
        Joined annotation = loader.modelType("Simple").getAnnotation(Joined.class);
        assertThat(annotation, nullValue());
    }

    private Matcher<Key> grouping(String... properties) {
        return new BaseMatcher<Key>() {
            @Override
            public boolean matches(Object object) {
                if (object instanceof Key) {
                    Key elem = (Key) object;
                    if (Arrays.equals(elem.group(), properties) == false) {
                        return false;
                    }
                }
                return true;
            }
            @Override
            public void describeTo(Description desc) {
                desc.appendText(Arrays.toString(properties));
            }
        };
    }

    private Matcher<Joined.Mapping> mapping(String src, String dst) {
        return new BaseMatcher<Joined.Mapping>() {
            @Override
            public boolean matches(Object object) {
                if (object instanceof Joined.Mapping) {
                    Joined.Mapping elem = (Joined.Mapping) object;
                    if (src.equals(elem.source()) == false) {
                        return false;
                    }
                    if (dst.equals(elem.destination()) == false) {
                        return false;
                    }
                }
                return true;
            }
            @Override
            public void describeTo(Description desc) {
                desc.appendText(src + " -> " + dst);
            }
        };
    }

    private Matcher<Object> eq(Object object) {
        return is(object);
    }
}
