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

import org.junit.Test;

import com.asakusafw.modelgen.model.JoinedModelDescription;
import com.asakusafw.modelgen.model.PropertyTypeKind;
import com.asakusafw.modelgen.model.StringType;
import com.asakusafw.modelgen.model.TableModelDescription;
import com.asakusafw.modelgen.util.JoinedModelBuilder;
import com.asakusafw.modelgen.util.TableModelBuilder;


/**
 * Test for {@link JoinedModelEntityEmitter}.
 */
public class JoinedModelEntityEmitterTest extends EmitterTestRoot {

    /**
     * 単純なテーブル
     * @throws Throwable 例外が発生した場合
     */
    @Test
    public void simple() throws Throwable {
        TableModelDescription a = new TableModelBuilder("A")
            .add(null, "id", PropertyTypeKind.LONG)
            .add(null, "hoge", new StringType(255))
            .toDescription();
        TableModelDescription b = new TableModelBuilder("B")
            .add(null, "id", PropertyTypeKind.LONG)
            .add(null, "foo", new StringType(255))
            .toDescription();
        JoinedModelDescription j = new JoinedModelBuilder("J", a, "a", b, "b")
            .on("a.id", "b.id")
            .add("id", "a.id")
            .add("hoge", "a.hoge")
            .add("bar", "b.foo")
            .toDescription();

        new Table().emit(a);
        new Table().emit(b);
        new Joined().emit(j);
        ClassLoader loader = compile();

        // properties
        Object jObj = loader.loadClass("com.example.model.J").newInstance();
        set(jObj, "setId", 1000L);
        set(jObj, "setHogeAsString", "jhoge");
        set(jObj, "setBarAsString", "jfoo");
        assertThat(get(jObj, "getId"), is((Object) 1000L));
        assertThat(get(jObj, "getHogeAsString"), is((Object) "jhoge"));
        assertThat(get(jObj, "getBarAsString"), is((Object) "jfoo"));

        // copier
        Object copy = loader.loadClass("com.example.model.J").newInstance();
        copyFrom(copy, jObj);
        assertThat(get(copy, "getId"), is((Object) 1000L));
        assertThat(get(copy, "getHogeAsString"), is((Object) "jhoge"));
        assertThat(get(copy, "getBarAsString"), is((Object) "jfoo"));

        // joiner
        Object aObj = loader.loadClass("com.example.model.A").newInstance();
        Object bObj = loader.loadClass("com.example.model.B").newInstance();
        set(aObj, "setId", 50L);
        set(bObj, "setId", 50L);
        set(aObj, "setHogeAsString", "ahoge");
        set(bObj, "setFooAsString", "bfoo");
        joinFrom(jObj, aObj, bObj);
        assertThat(get(jObj, "getId"), is((Object) 50L));
        assertThat(get(jObj, "getHogeAsString"), is((Object) "ahoge"));
        assertThat(get(jObj, "getBarAsString"), is((Object) "bfoo"));

        // splitter
        split(copy, aObj, bObj);
        assertThat(get(aObj, "getId"), is((Object) 1000L));
        assertThat(get(aObj, "getHogeAsString"), is((Object) "jhoge"));
        assertThat(get(bObj, "getId"), is((Object) 1000L));
        assertThat(get(bObj, "getFooAsString"), is((Object) "jfoo"));
    }
}
