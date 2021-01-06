/**
 * Copyright 2011-2021 Asakusa Framework Team.
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
package com.asakusafw.dmdl.java.emitter;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.io.Text;
import org.hamcrest.Matcher;
import org.junit.Test;

import com.asakusafw.dmdl.java.GeneratorTesterRoot;
import com.asakusafw.runtime.value.Date;
import com.asakusafw.runtime.value.DateTime;
import com.asakusafw.runtime.value.IntOption;
import com.asakusafw.runtime.value.ValueOptionList;
import com.asakusafw.runtime.value.ValueOptionMap;

/**
 * Test for {@link ConcreteModelEmitter}.
 */
public class ConcreteModelEmitterTest extends GeneratorTesterRoot {

    /**
     * generate a simple model class.
     */
    @Test
    public void simple() {
        ModelLoader loader = generate();
        ModelWrapper object = loader.newModel("Simple");
        object.set("value", 100);
        assertThat(object.get("value"), eq(100));
        assertThat(object.getOption("value"), eq(new IntOption(100)));
        object.setOption("value", new IntOption(200));
        assertThat(object.get("value"), eq(200));

        ModelWrapper copy = loader.newModel("Simple");
        copy.copyFrom(object);

        object.reset();
        assertThat(object.getOption("value").isNull(), eq(true));
        assertThat(copy.get("value"), eq(200));
    }

    /**
     * all primitive types.
     */
    @Test
    public void primitives() {
        ModelLoader loader = generate();
        ModelWrapper object = loader.newModel("Primitives");

        object.set("type_boolean", true);
        assertThat(object.is("type_boolean"), eq(true));

        object.set("type_byte", (byte) 64);
        assertThat(object.get("type_byte"), eq((byte) 64));

        object.set("type_short", (short) 256);
        assertThat(object.get("type_short"), eq((short) 256));

        object.set("type_int", 100);
        assertThat(object.get("type_int"), eq(100));

        object.set("type_long", 200L);
        assertThat(object.get("type_long"), eq(200L));

        object.set("type_float", 300.f);
        assertThat(object.get("type_float"), eq(300.f));

        object.set("type_double", 400.d);
        assertThat(object.get("type_double"), eq(400.d));

        object.set("type_decimal", new BigDecimal("1234.567"));
        assertThat(object.get("type_decimal"), eq(new BigDecimal("1234.567")));

        object.set("type_text", new Text("Hello, world!"));
        assertThat(object.get("type_text"), eq(new Text("Hello, world!")));

        object.set("type_date", new Date(2011, 3, 31));
        assertThat(object.get("type_date"), eq(new Date(2011, 3, 31)));

        object.set("type_datetime", new DateTime(2011, 3, 31, 23, 30, 1));
        assertThat(object.get("type_datetime"), eq(new DateTime(2011, 3, 31, 23, 30, 1)));
    }

    /**
     * change own namespace.
     */
    @Test
    public void namespace() {
        ModelLoader loader = generate();
        loader.setNamespace("other");
        ModelWrapper object = loader.newModel("Simple");

        object.set("value", 100);
        assertThat(object.get("value"), eq(100));
    }

    /**
     * change own namespace.
     */
    @Test
    public void namespace_complex() {
        ModelLoader loader = generate();
        loader.setNamespace("com.example.complexNamespace");
        ModelWrapper object = loader.newModel("Simple");

        object.set("value", 100);
        assertThat(object.get("value"), eq(100));
    }

    /**
     * w/ reference list.
     */
    @Test
    public void reference_list() {
        ModelLoader loader = generate();
        ModelWrapper object = loader.newModel("Simple");

        IntOption a = (IntOption) object.getOption("a");
        IntOption b = (IntOption) object.getOption("b");
        IntOption c = (IntOption) object.getOption("c");
        @SuppressWarnings("unchecked")
        List<IntOption> all = (List<IntOption>) object.get("all");

        assertThat(all, is(instanceOf(ValueOptionList.class)));
        assertThat(all, hasSize(3));
        assertThat(all.get(0), is(sameInstance(a)));
        assertThat(all.get(1), is(sameInstance(b)));
        assertThat(all.get(2), is(sameInstance(c)));
    }

    /**
     * w/ reference list.
     */
    @Test
    public void reference_list_empty() {
        ModelLoader loader = generate();
        ModelWrapper object = loader.newModel("Simple");

        @SuppressWarnings("unchecked")
        List<IntOption> all = (List<IntOption>) object.get("empty");
        assertThat(all, hasSize(0));
    }

    /**
     * w/ reference map.
     */
    @Test
    public void reference_map() {
        ModelLoader loader = generate();
        ModelWrapper object = loader.newModel("Simple");

        IntOption a = (IntOption) object.getOption("a");
        IntOption b = (IntOption) object.getOption("b");
        IntOption c = (IntOption) object.getOption("c");
        @SuppressWarnings("unchecked")
        Map<String, IntOption> all = (Map<String, IntOption>) object.get("all");

        assertThat(all, is(instanceOf(ValueOptionMap.class)));
        assertThat(all.keySet(), hasSize(3));
        assertThat(all, hasEntry(is("A"), sameInstance(a)));
        assertThat(all, hasEntry(is("B"), sameInstance(b)));
        assertThat(all, hasEntry(is("C"), sameInstance(c)));
    }

    /**
     * w/ reference map.
     */
    @Test
    public void reference_map_empty() {
        ModelLoader loader = generate();
        ModelWrapper object = loader.newModel("Simple");

        @SuppressWarnings("unchecked")
        Map<String, IntOption> all = (Map<String, IntOption>) object.get("empty");
        assertThat(all.keySet(), hasSize(0));
    }

    private Matcher<Object> eq(Object value) {
        return is(value);
    }
}
