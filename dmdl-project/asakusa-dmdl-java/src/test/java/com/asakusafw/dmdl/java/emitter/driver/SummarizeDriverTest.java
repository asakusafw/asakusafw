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
package com.asakusafw.dmdl.java.emitter.driver;

import static com.asakusafw.vocabulary.model.Summarized.Aggregator.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Arrays;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;

import com.asakusafw.dmdl.java.GeneratorTesterRoot;
import com.asakusafw.vocabulary.model.Key;
import com.asakusafw.vocabulary.model.Summarized;
import com.asakusafw.vocabulary.model.Summarized.Aggregator;
import com.asakusafw.vocabulary.model.Summarized.Term;

/**
 * Test for {@link SummarizeDriver}.
 */
public class SummarizeDriverTest extends GeneratorTesterRoot {

    /**
     * Initializes the test.
     * @throws Exception if some errors were occurred
     */
    @Before
    public void setUp() throws Exception {
        emitDrivers.add(new SummarizeDriver());
    }

    /**
     * simple summarize.
     */
    @Test
    public void simple_summarize() {
        ModelLoader loader = generate();
        Summarized annotation = loader.modelType("Simple").getAnnotation(Summarized.class);
        assertThat(annotation, not(nullValue()));

        Term term = annotation.term();
        assertThat(term.source(), eq(loader.modelType("A")));
        assertThat(term.foldings().length, is(5));
        assertThat(term.foldings(), hasItemInArray(mapping(ANY, "valueA", "key")));
        assertThat(term.foldings(), hasItemInArray(mapping(SUM, "valueB", "sum")));
        assertThat(term.foldings(), hasItemInArray(mapping(COUNT, "valueA", "count")));
        assertThat(term.foldings(), hasItemInArray(mapping(MAX, "valueC", "max")));
        assertThat(term.foldings(), hasItemInArray(mapping(MIN, "valueC", "min")));
        assertThat(term.shuffle(), is(grouping("valueA")));
    }

    /**
     * simple record.
     */
    @Test
    public void simple_record() {
        ModelLoader loader = generate();
        Summarized annotation = loader.modelType("Simple").getAnnotation(Summarized.class);
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

    private Matcher<Summarized.Folding> mapping(Aggregator aggregator, String src, String dst) {
        return new BaseMatcher<Summarized.Folding>() {
            @Override
            public boolean matches(Object object) {
                if (object instanceof Summarized.Folding) {
                    Summarized.Folding elem = (Summarized.Folding) object;
                    if (aggregator != elem.aggregator()) {
                        return false;
                    }
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
                desc.appendText(aggregator + " " + src + " -> " + dst);
            }
        };
    }

    private Matcher<Object> eq(Object object) {
        return is(object);
    }
}
