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

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.asakusafw.modelgen.model.Aggregator;
import com.asakusafw.modelgen.model.Attribute;
import com.asakusafw.modelgen.model.ModelProperty;
import com.asakusafw.modelgen.model.PropertyTypeKind;
import com.asakusafw.modelgen.model.Source;
import com.asakusafw.modelgen.model.StringType;
import com.asakusafw.modelgen.model.TableModelDescription;


/**
 * Test for {@link TableModelBuilder}.
 */
public class TableModelBuilderTest {

    /**
     * 単一のプロパティに関するテスト。
     */
    @Test
    public void simple() {
        TableModelDescription desc = new TableModelBuilder("Example")
            .add(null, "value", PropertyTypeKind.INT)
            .toDescription();

        assertThat(desc.getReference().isDefaultNameSpace(), is(true));
        assertThat(desc.getReference().getSimpleName(), is("Example"));

        List<ModelProperty> properties = desc.getProperties();
        assertThat(properties.size(), is(1));

        ModelProperty value = properties.get(0);
        assertThat(value.getName(), is("value"));
        assertThat(value.getType().getKind(), is(PropertyTypeKind.INT));
        assertThat(value.getJoined(), is(nullValue()));

        Source valueSrc = value.getFrom();
        assertThat(valueSrc.getAggregator(), is(Aggregator.IDENT));
        assertThat(valueSrc.getDeclaring(), is(desc.getReference()));
        assertThat(valueSrc.getName(), is("value"));
        assertThat(valueSrc.getType().getKind(), is(PropertyTypeKind.INT));
        assertThat(valueSrc.getAttributes().size(), is(0));
    }

    /**
     * 複数のプロパティに関するテスト。
     */
    @Test
    public void multi() {
        TableModelBuilder target = new TableModelBuilder("Example");
        target.add(null, "a", PropertyTypeKind.INT);
        target.add(null, "b", new StringType(255));
        target.add(null, "c", PropertyTypeKind.DATETIME);
        TableModelDescription desc = target.toDescription();

        assertThat(desc.getReference().getSimpleName(), is("Example"));

        List<ModelProperty> properties = desc.getProperties();
        assertThat(properties.size(), is(3));

        ModelProperty a = properties.get(0);
        assertThat(a.getName(), is("a"));
        assertThat(a.getType().getKind(), is(PropertyTypeKind.INT));

        ModelProperty b = properties.get(1);
        assertThat(b.getName(), is("b"));
        assertThat(b.getType().getKind(), is(PropertyTypeKind.STRING));

        ModelProperty c = properties.get(2);
        assertThat(c.getName(), is("c"));
        assertThat(c.getType().getKind(), is(PropertyTypeKind.DATETIME));
    }

    /**
     * 属性に関するテスト。
     */
    @Test
    public void attributes() {
        TableModelBuilder target = new TableModelBuilder("Attribute");
        target.add(null, "id", PropertyTypeKind.LONG, Attribute.PRIMARY_KEY);
        target.add(null, "str", new StringType(255), Attribute.UNIQUE, Attribute.NOT_NULL);
        TableModelDescription desc = target.toDescription();

        assertThat(desc.getReference().getSimpleName(), is("Attribute"));

        List<ModelProperty> properties = desc.getProperties();
        assertThat(properties.size(), is(2));

        ModelProperty a = properties.get(0);
        assertThat(a.getName(), is("id"));
        assertThat(a.getType().getKind(), is(PropertyTypeKind.LONG));
        Set<Attribute> aAttr = a.getFrom().getAttributes();
        assertThat(aAttr.size(), is(1));
        assertThat(aAttr.contains(Attribute.PRIMARY_KEY), is(true));

        ModelProperty b = properties.get(1);
        assertThat(b.getName(), is("str"));
        assertThat(b.getType().getKind(), is(PropertyTypeKind.STRING));
        Set<Attribute> bAttr = b.getFrom().getAttributes();
        assertThat(bAttr.size(), is(2));
        assertThat(bAttr.containsAll(list(Attribute.UNIQUE, Attribute.NOT_NULL)), is(true));
    }

    /**
     * 名前空間に関するテスト。
     */
    @Test
    public void namespace() {
        TableModelDescription desc = new TableModelBuilder("Example")
            .namespace("testing", "table")
            .add(null, "value", PropertyTypeKind.INT)
            .toDescription();

        assertThat(desc.getReference().getNamespace(), is("testing.table"));
        assertThat(desc.getReference().getSimpleName(), is("Example"));
    }

    /**
     * 標準名前空間に関するテスト。
     */
    @Test
    public void defaultNamespace() {
        TableModelDescription desc = new TableModelBuilder("Example")
            .namespace()
            .add(null, "value", PropertyTypeKind.INT)
            .toDescription();

        assertThat(desc.getReference().isDefaultNameSpace(), is(true));
        assertThat(desc.getReference().getSimpleName(), is("Example"));
    }

    private <T> List<T> list(T... values) {
        return Arrays.asList(values);
    }
}
