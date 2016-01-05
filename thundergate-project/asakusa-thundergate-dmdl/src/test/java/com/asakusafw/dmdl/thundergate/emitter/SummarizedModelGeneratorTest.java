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
package com.asakusafw.dmdl.thundergate.emitter;

import static com.asakusafw.vocabulary.model.Summarized.Aggregator.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Arrays;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;

import com.asakusafw.dmdl.java.emitter.CompositeDataModelDriver;
import com.asakusafw.dmdl.thundergate.Constants;
import com.asakusafw.dmdl.thundergate.GeneratorTesterRoot;
import com.asakusafw.dmdl.thundergate.model.Attribute;
import com.asakusafw.dmdl.thundergate.model.PropertyTypeKind;
import com.asakusafw.dmdl.thundergate.model.SummarizedModelDescription;
import com.asakusafw.dmdl.thundergate.model.TableModelDescription;
import com.asakusafw.dmdl.thundergate.util.SummarizedModelBuilder;
import com.asakusafw.dmdl.thundergate.util.TableModelBuilder;
import com.asakusafw.runtime.value.Date;
import com.asakusafw.vocabulary.model.Key;
import com.asakusafw.vocabulary.model.Summarized;
import com.asakusafw.vocabulary.model.Summarized.Aggregator;
import com.asakusafw.vocabulary.model.Summarized.Term;

/**
 * Test for {@link SummarizedModelGenerator}.
 */
public class SummarizedModelGeneratorTest extends GeneratorTesterRoot {

    /**
     * Initializes the test.
     * @throws Exception if some errors were occurred
     */
    @Before
    public void setUp() throws Exception {
        emitDrivers.add(new CompositeDataModelDriver(getClass().getClassLoader()));
    }

    /**
     * simple summarize.
     */
    @Test
    public void simple() {
        TableModelDescription target = new TableModelBuilder("TARGET")
            .add(null, "SID", PropertyTypeKind.LONG, Attribute.PRIMARY_KEY)
            .add(null, "VALUE_A", PropertyTypeKind.INT)
            .add(null, "VALUE_B", PropertyTypeKind.LONG)
            .add(null, "VALUE_C", PropertyTypeKind.DATE)
            .toDescription();
        SummarizedModelDescription summarize = new SummarizedModelBuilder("SIMPLE", target, "t")
            .groupBy("VALUE_A")
            .add("KEY", com.asakusafw.dmdl.thundergate.model.Aggregator.IDENT, "t.VALUE_A")
            .add("SUM", com.asakusafw.dmdl.thundergate.model.Aggregator.SUM, "t.VALUE_B")
            .add("COUNT", com.asakusafw.dmdl.thundergate.model.Aggregator.COUNT, "t.SID")
            .add("MAX", com.asakusafw.dmdl.thundergate.model.Aggregator.MAX, "t.VALUE_C")
            .add("MIN", com.asakusafw.dmdl.thundergate.model.Aggregator.MIN, "t.VALUE_C")
            .toDescription();

        emitDmdl(RecordModelGenerator.generate(target));
        emitDmdl(SummarizedModelGenerator.generate(summarize));

        ModelLoader loader = generateJava();
        loader.setNamespace(Constants.SOURCE_VIEW);

        ModelWrapper object = loader.newModel("Simple");

        object.set("key", 127);
        assertThat(object.get("key"), eq(127));
        object.set("sum", 256L);
        assertThat(object.get("sum"), eq(256L));
        object.set("count", 10L);
        assertThat(object.get("count"), eq(10L));
        object.set("max", new Date(2011, 12, 31));
        assertThat(object.get("max"), eq(new Date(2011, 12, 31)));
        object.set("min", new Date(2011, 4, 1));
        assertThat(object.get("min"), eq(new Date(2011, 4, 1)));

        Summarized annotation = object.unwrap().getClass().getAnnotation(Summarized.class);
        assertThat(annotation, not(nullValue()));

        loader.setNamespace(Constants.SOURCE_TABLE);

        Term term = annotation.term();
        assertThat(term.source(), eq(loader.modelType("Target")));
        assertThat(term.foldings().length, is(5));
        assertThat(term.foldings(), hasItemInArray(mapping(ANY, "valueA", "key")));
        assertThat(term.foldings(), hasItemInArray(mapping(SUM, "valueB", "sum")));
        assertThat(term.foldings(), hasItemInArray(mapping(COUNT, "sid", "count")));
        assertThat(term.foldings(), hasItemInArray(mapping(MAX, "valueC", "max")));
        assertThat(term.foldings(), hasItemInArray(mapping(MIN, "valueC", "min")));
        assertThat(term.shuffle(), is(grouping("valueA")));
    }

    /**
     * Summarize with conflict grouping key and aggregation target.
     */
    @Test
    public void conflict_key() {
        TableModelDescription target = new TableModelBuilder("TARGET")
            .add(null, "SID", PropertyTypeKind.LONG, Attribute.PRIMARY_KEY)
            .add(null, "GROUPING", PropertyTypeKind.INT)
            .toDescription();
        SummarizedModelDescription summarize = new SummarizedModelBuilder("SIMPLE", target, "t")
            .groupBy("GROUPING")
            .add("KEY", com.asakusafw.dmdl.thundergate.model.Aggregator.IDENT, "t.GROUPING")
            .add("COUNT", com.asakusafw.dmdl.thundergate.model.Aggregator.COUNT, "t.GROUPING")
            .toDescription();

        emitDmdl(RecordModelGenerator.generate(target));
        emitDmdl(SummarizedModelGenerator.generate(summarize));

        ModelLoader loader = generateJava();
        loader.setNamespace(Constants.SOURCE_VIEW);

        ModelWrapper object = loader.newModel("Simple");
        Summarized annotation = object.unwrap().getClass().getAnnotation(Summarized.class);
        assertThat(annotation, not(nullValue()));

        loader.setNamespace(Constants.SOURCE_TABLE);

        Term term = annotation.term();
        assertThat(term.source(), eq(loader.modelType("Target")));
        assertThat(term.foldings().length, is(2));
        assertThat(term.foldings(), hasItemInArray(mapping(ANY, "grouping", "key")));
        assertThat(term.foldings(), hasItemInArray(mapping(COUNT, "grouping", "count")));
        assertThat(term.shuffle(), is(grouping("grouping")));
    }

    private Matcher<Key> grouping(final String... properties) {
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

    private Matcher<Summarized.Folding> mapping(
            final Aggregator aggregator, final String src, final String dst) {
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
