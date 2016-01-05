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
package com.asakusafw.modelgen.emitter;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.apache.hadoop.io.Writable;
import org.junit.Test;

import com.asakusafw.modelgen.model.Attribute;
import com.asakusafw.modelgen.model.PropertyTypeKind;
import com.asakusafw.modelgen.model.StringType;
import com.asakusafw.modelgen.model.TableModelDescription;
import com.asakusafw.modelgen.util.TableModelBuilder;


/**
 * Test for {@link TableModelEntityEmitter}.
 */
public class TableModelEntityEmitterTest extends EmitterTestRoot {

    /**
     * 単純なテーブル
     * @throws Throwable 例外が発生した場合
     */
    @Test
    public void simple() throws Throwable {
        TableModelDescription model = new TableModelBuilder("Hello")
            .add(null, "id", PropertyTypeKind.LONG, Attribute.PRIMARY_KEY)
            .add(null, "value", new StringType(255))
            .toDescription();

        new Table().emit(model);

        ClassLoader loader = compile();
        Object hello = loader.loadClass("com.example.model.Hello").newInstance();

        set(hello, "setId", 10L);
        set(hello, "setValueAsString", "Hello, world!");

        assertThat(get(hello, "getId"), is((Object) 10L));
        assertThat(get(hello, "getValueAsString"), is((Object) "Hello, world!"));

        Object copy = loader.loadClass("com.example.model.Hello").newInstance();
        copyFrom(copy, hello);

        assertThat(get(copy, "getId"), is((Object) 10L));
        assertThat(get(copy, "getValueAsString"), is((Object) "Hello, world!"));
    }

    /**
     * booleanのgetter名に関するもの。
     * @throws Throwable 例外が発生した場合
     */
    @Test
    public void booleanGetter() throws Throwable {
        TableModelDescription model = new TableModelBuilder("Hello")
            .add(null, "id", PropertyTypeKind.LONG, Attribute.PRIMARY_KEY)
            .add(null, "frag", PropertyTypeKind.BOOLEAN)
            .toDescription();

        new Table().emit(model);

        ClassLoader loader = compile();
        Object hello = loader.loadClass("com.example.model.Hello").newInstance();

        set(hello, "setId", 10L);
        set(hello, "setFrag", true);

        assertThat(get(hello, "getId"), is((Object) 10L));
        assertThat(get(hello, "isFrag"), is((Object) true));
    }

    /**
     * Writableのテスト。
     * @throws Throwable 例外が発生した場合
     */
    @Test
    public void writable() throws Throwable {
        TableModelDescription model = new TableModelBuilder("Hello")
            .add(null, "id", PropertyTypeKind.LONG, Attribute.PRIMARY_KEY)
            .add(null, "value", new StringType(255))
            .add(null, "nothing", new StringType(255))
            .toDescription();

        new Table().emit(model);

        ClassLoader loader = compile();
        Object hello = loader.loadClass("com.example.model.Hello").newInstance();
        set(hello, "setId", 10L);
        set(hello, "setValueAsString", "Hello, world!");

        assertThat(hello, instanceOf(Writable.class));

        Object restored = restore(hello);
        assertThat(restored, not(sameInstance(hello)));
        assertThat(restored, equalTo(hello));
    }

    /**
     * 名前空間付きのモデル。
     * @throws Throwable 例外が発生した場合
     */
    @Test
    public void namespace() throws Throwable {
        TableModelDescription model = new TableModelBuilder("Hello")
            .namespace("testing", "table")
            .add(null, "id", PropertyTypeKind.LONG, Attribute.PRIMARY_KEY)
            .add(null, "value", new StringType(255))
            .toDescription();

        new Table().emit(model);

        ClassLoader loader = compile();
        Class<?> klass = loader.loadClass("com.example.testing.table.model.Hello");
        Object hello = klass.newInstance();

        set(hello, "setId", 10L);
        set(hello, "setValueAsString", "Hello, world!");

        assertThat(get(hello, "getId"), is((Object) 10L));
        assertThat(get(hello, "getValueAsString"), is((Object) "Hello, world!"));

        Object copy = klass.newInstance();
        copyFrom(copy, hello);

        assertThat(get(copy, "getId"), is((Object) 10L));
        assertThat(get(copy, "getValueAsString"), is((Object) "Hello, world!"));
    }
}
