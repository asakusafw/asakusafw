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
package com.asakusafw.dmdl.thundergate.util;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.asakusafw.dmdl.thundergate.model.Aggregator;
import com.asakusafw.dmdl.thundergate.model.ModelDescription;
import com.asakusafw.dmdl.thundergate.model.ModelProperty;
import com.asakusafw.dmdl.thundergate.model.PropertyTypeKind;
import com.asakusafw.dmdl.thundergate.model.Source;
import com.asakusafw.dmdl.thundergate.model.StringType;
import com.asakusafw.dmdl.thundergate.model.SummarizedModelDescription;
import com.asakusafw.dmdl.thundergate.model.TableModelDescription;
import com.asakusafw.utils.collections.Lists;
import com.asakusafw.utils.collections.Sets;


/**
 * Test for {@link SummarizedModelBuilder}.
 */
public class SummarizedModelBuilderTest {

    /**
     * 単純なワードカウント。
     */
    @Test
    public void simple() {
        TableModelDescription desc = new TableModelBuilder("A")
            .add(null, "word", new StringType(255))
            .toDescription();

        SummarizedModelDescription model = new SummarizedModelBuilder("S", desc, "a")
            .add("word", Aggregator.IDENT, "a.word")
            .add("count", Aggregator.COUNT, "a.word")
            .groupBy("a.word")
            .toDescription();

        assertThat(model.getReference().getSimpleName(), is("S"));
        assertThat(model.getGroupBy(), is(sources(desc, "word")));

        List<ModelProperty> properties = model.getProperties();
        assertThat(properties.size(), is(2));

        ModelProperty word = properties.get(0);
        assertThat(word.getName(), is("word"));
        assertThat(word.getType().getKind(), is(PropertyTypeKind.STRING));
        assertThat(word.getJoined(), is(nullValue()));
        assertThat(
                word.getFrom(),
                is(source(desc, "word", Aggregator.IDENT)));

        ModelProperty count = properties.get(1);
        assertThat(count.getName(), is("count"));
        assertThat(count.getType().getKind(), is(PropertyTypeKind.LONG));
        assertThat(count.getJoined(), is(nullValue()));
        assertThat(
                count.getFrom(),
                is(source(desc, "word", Aggregator.COUNT)));
    }

    /**
     * 全てを単一のグループにまとめる。
     */
    @Test
    public void singleGroup() {
        TableModelDescription desc = new TableModelBuilder("A")
            .add(null, "word", new StringType(255))
            .toDescription();

        SummarizedModelDescription model = new SummarizedModelBuilder("S", desc, "a")
            .add("count", Aggregator.COUNT, "a.word")
            .toDescription();

        assertThat(model.getReference().getSimpleName(), is("S"));
        assertThat(model.getGroupBy(), is(sources(desc)));

        List<ModelProperty> properties = model.getProperties();
        assertThat(properties.size(), is(1));

        ModelProperty count = properties.get(0);
        assertThat(count.getName(), is("count"));
        assertThat(count.getType().getKind(), is(PropertyTypeKind.LONG));
        assertThat(
                count.getFrom(),
                is(source(desc, "word", Aggregator.COUNT)));
    }

    /**
     * 複数のグループ化キーを指定。
     */
    @Test
    public void multiGroupKey() {
        TableModelDescription desc = new TableModelBuilder("A")
            .add(null, "sex", PropertyTypeKind.BYTE)
            .add(null, "age", PropertyTypeKind.SHORT)
            .add(null, "name", new StringType(255))
            .toDescription();

        SummarizedModelDescription model = new SummarizedModelBuilder("S", desc, "a")
            .add("sex", Aggregator.IDENT, "sex")
            .add("age", Aggregator.IDENT, "age")
            .add("count", Aggregator.COUNT, "name")
            .groupBy("sex", "age")
            .toDescription();

        assertThat(model.getReference().getSimpleName(), is("S"));
        assertThat(model.getGroupBy(), is(sources(desc, "sex", "age")));

        List<ModelProperty> properties = model.getProperties();
        assertThat(properties.size(), is(3));

        ModelProperty sex = properties.get(0);
        assertThat(sex.getName(), is("sex"));
        assertThat(sex.getType().getKind(), is(PropertyTypeKind.BYTE));
        assertThat(
                sex.getFrom(),
                is(source(desc, "sex", Aggregator.IDENT)));

        ModelProperty age = properties.get(1);
        assertThat(age.getName(), is("age"));
        assertThat(age.getType().getKind(), is(PropertyTypeKind.SHORT));
        assertThat(
                age.getFrom(),
                is(source(desc, "age", Aggregator.IDENT)));

        ModelProperty count = properties.get(2);
        assertThat(count.getName(), is("count"));
        assertThat(count.getType().getKind(), is(PropertyTypeKind.LONG));
        assertThat(
                count.getFrom(),
                is(source(desc, "name", Aggregator.COUNT)));
    }

    /**
     * 空。
     */
    @Test(expected = RuntimeException.class)
    public void empty() {
        TableModelDescription desc = new TableModelBuilder("A")
            .add(null, "word", PropertyTypeKind.STRING)
            .toDescription();

        new SummarizedModelBuilder("S", desc, "a")
            .toDescription();
    }

    /**
     * 集約するカラムが見つからない。
     */
    @Test(expected = RuntimeException.class)
    public void missingAggregatingColumn() {
        TableModelDescription desc = new TableModelBuilder("A")
            .add(null, "word", PropertyTypeKind.STRING)
            .toDescription();

        new SummarizedModelBuilder("S", desc, "a")
            .add("word", Aggregator.IDENT, "a.MISSING");
    }

    /**
     * グループ化カラムが見つからない。
     */
    @Test(expected = RuntimeException.class)
    public void missingGroupingColumn() {
        TableModelDescription desc = new TableModelBuilder("A")
            .add(null, "word", PropertyTypeKind.STRING)
            .toDescription();

        new SummarizedModelBuilder("S", desc, "a")
            .groupBy("a.MISSING");
    }

    /**
     * 集約すべきカラムを集約していない。
     */
    @Test(expected = RuntimeException.class)
    public void invalidIdent() {
        TableModelDescription desc = new TableModelBuilder("A")
            .add(null, "word", PropertyTypeKind.STRING)
            .add(null, "value", PropertyTypeKind.INT)
            .toDescription();

        new SummarizedModelBuilder("S", desc, "a")
            .add("word", Aggregator.IDENT, "a.word")
            .add("value", Aggregator.IDENT, "a.value")
            .groupBy("a.word")
            .toDescription();
    }

    /**
     * 利用できない集約関数を利用している。
     */
    @Test(expected = RuntimeException.class)
    public void invalidAggregation() {
        TableModelDescription desc = new TableModelBuilder("A")
            .add(null, "word", PropertyTypeKind.STRING)
            .add(null, "name", PropertyTypeKind.STRING)
            .toDescription();

        new SummarizedModelBuilder("S", desc, "a")
            .add("name", Aggregator.SUM, "a.name");
    }

    /**
     * グループ化キーがモデルに含まれない。
     */
    @Test(expected = RuntimeException.class)
    public void noGroupColumn() {
        TableModelDescription desc = new TableModelBuilder("A")
            .add(null, "word", PropertyTypeKind.STRING)
            .toDescription();

        new SummarizedModelBuilder("S", desc, "a")
            .add("count", Aggregator.COUNT, "a.word")
            .groupBy("a.word")
            .toDescription();
    }

    private Source source(ModelDescription model, String name, Aggregator aggr) {
        for (Source s : model.getPropertiesAsSources()) {
            if (name.equals(s.getName())) {
                return new Source(
                        aggr,
                        s.getDeclaring(),
                        s.getName(),
                        s.getType(),
                        s.getAttributes());
            }
        }
        throw new AssertionError(name);
    }

    private List<Source> sources(ModelDescription model, String... names) {
        List<Source> results = Lists.create();
        Set<String> targets = Sets.create();
        Collections.addAll(targets, names);
        for (Source s : model.getPropertiesAsSources()) {
            if (targets.contains(s.getName())) {
                results.add(s);
            }
        }
        return results;
    }
}
