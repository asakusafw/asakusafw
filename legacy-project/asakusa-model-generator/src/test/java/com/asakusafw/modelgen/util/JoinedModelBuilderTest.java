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
package com.asakusafw.modelgen.util;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.asakusafw.modelgen.model.JoinedModelDescription;
import com.asakusafw.modelgen.model.ModelDescription;
import com.asakusafw.modelgen.model.ModelProperty;
import com.asakusafw.modelgen.model.PropertyTypeKind;
import com.asakusafw.modelgen.model.Source;
import com.asakusafw.modelgen.model.StringType;
import com.asakusafw.modelgen.model.TableModelDescription;


/**
 * Test for {@link JoinedModelBuilder}.
 */
public class JoinedModelBuilderTest {

    /**
     * 単純なテスト。
     */
    @Test
    public void simple() {
        TableModelDescription a = new TableModelBuilder("A")
            .add(null, "id", PropertyTypeKind.LONG)
            .add(null, "hoge", new StringType(255))
            .toDescription();
        TableModelDescription b = new TableModelBuilder("B")
            .add(null, "id", PropertyTypeKind.LONG)
            .add(null, "foo", new StringType(255))
            .toDescription();

        JoinedModelBuilder target = new JoinedModelBuilder(
                "J",
                a, "a",
                b, "b");
        target.on("a.id", "b.id");
        target.add("id", "a.id");
        target.add("hoge", "a.hoge");
        target.add("bar", "b.foo");

        JoinedModelDescription desc = target.toDescription();
        assertThat(desc.getFromModel().getSimpleName(), is("A"));
        assertThat(desc.getJoinModel().getSimpleName(), is("B"));

        assertThat(desc.getFromCondition(), is(sources(a, "id")));
        assertThat(desc.getJoinCondition(), is(sources(b, "id")));

        List<ModelProperty> props = desc.getProperties();
        assertThat(props.size(), is(3));

        ModelProperty id = props.get(0);
        assertThat(id.getName(), is("id"));
        assertThat(id.getType().getKind(), is(PropertyTypeKind.LONG));
        assertThat(id.getFrom(), is(source(a, "id")));
        assertThat(id.getJoined(), is(source(b, "id")));


        ModelProperty hoge = props.get(1);
        assertThat(hoge.getName(), is("hoge"));
        assertThat(hoge.getType().getKind(), is(PropertyTypeKind.STRING));
        assertThat(hoge.getFrom(), is(source(a, "hoge")));
        assertThat(hoge.getJoined(), is(nullValue()));


        ModelProperty bar = props.get(2);
        assertThat(bar.getName(), is("bar"));
        assertThat(bar.getType().getKind(), is(PropertyTypeKind.STRING));
        assertThat(bar.getFrom(), is(nullValue()));
        assertThat(bar.getJoined(), is(source(b, "foo")));
    }

    /**
     * 同名のプロパティを同一視する結合のテスト。
     */
    @Test
    public void fill() {
        TableModelDescription a = new TableModelBuilder("A")
            .add(null, "id", PropertyTypeKind.LONG)
            .add(null, "hoge", new StringType(255))
            .toDescription();
        TableModelDescription b = new TableModelBuilder("B")
            .add(null, "id", PropertyTypeKind.LONG)
            .add(null, "hoge", new StringType(255))
            .toDescription();

        JoinedModelBuilder target = new JoinedModelBuilder(
                "J",
                a, "a",
                b, "b");
        target.on("a.id", "b.id");
        target.add("id", "a.id");
        target.add("hoge", "a.hoge");

        JoinedModelDescription desc = target.toDescription();
        assertThat(desc.getFromModel().getSimpleName(), is("A"));
        assertThat(desc.getJoinModel().getSimpleName(), is("B"));

        assertThat(desc.getFromCondition(), is(sources(a, "id")));
        assertThat(desc.getJoinCondition(), is(sources(b, "id")));

        List<ModelProperty> props = desc.getProperties();
        assertThat(props.size(), is(2));

        ModelProperty id = props.get(0);
        assertThat(id.getName(), is("id"));
        assertThat(id.getType().getKind(), is(PropertyTypeKind.LONG));
        assertThat(id.getFrom(), is(source(a, "id")));
        assertThat(id.getJoined(), is(source(b, "id")));

        ModelProperty hoge = props.get(1);
        assertThat(hoge.getName(), is("hoge"));
        assertThat(hoge.getType().getKind(), is(PropertyTypeKind.STRING));
        assertThat(hoge.getFrom(), is(source(a, "hoge")));
        assertThat(hoge.getJoined(), is(source(b, "hoge")));
    }

    private Source source(ModelDescription model, String name) {
        for (Source s : model.getPropertiesAsSources()) {
            if (name.equals(s.getName())) {
                return s;
            }
        }
        throw new AssertionError(name);
    }

    private List<Source> sources(ModelDescription model, String... names) {
        List<Source> results = new ArrayList<Source>();
        Set<String> targets = new HashSet<String>();
        Collections.addAll(targets, names);
        for (Source s : model.getPropertiesAsSources()) {
            if (targets.contains(s.getName())) {
                results.add(s);
            }
        }
        return results;
    }
}
