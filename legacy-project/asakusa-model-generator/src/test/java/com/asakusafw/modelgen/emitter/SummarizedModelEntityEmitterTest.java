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

import com.asakusafw.modelgen.model.Aggregator;
import com.asakusafw.modelgen.model.Attribute;
import com.asakusafw.modelgen.model.PropertyTypeKind;
import com.asakusafw.modelgen.model.StringType;
import com.asakusafw.modelgen.model.SummarizedModelDescription;
import com.asakusafw.modelgen.model.TableModelDescription;
import com.asakusafw.modelgen.util.SummarizedModelBuilder;
import com.asakusafw.modelgen.util.TableModelBuilder;


/**
 * Test for {@link SummarizedModelEntityEmitter}.
 */
public class SummarizedModelEntityEmitterTest extends EmitterTestRoot {

    /**
     * 単純なテーブル
     * @throws Throwable 例外が発生した場合
     */
    @Test
    public void simple() throws Throwable {
        TableModelDescription a = new TableModelBuilder("A")
            .add(null, "id", PropertyTypeKind.LONG, Attribute.PRIMARY_KEY)
            .add(null, "word", new StringType(255))
            .toDescription();

        SummarizedModelDescription model = new SummarizedModelBuilder("S", a, "a")
            .add("word", Aggregator.IDENT, "a.word")
            .add("count", Aggregator.COUNT, "a.word")
            .groupBy("a.word")
            .toDescription();

        new Table().emit(a);
        new Summarized().emit(model);

        // properties
        ClassLoader loader = compile();
        Object sObj = loader.loadClass("com.example.model.S").newInstance();
        set(sObj, "setWordAsString", "word");
        set(sObj, "setCount", 100L);
        assertThat(get(sObj, "getWordAsString"), is((Object) "word"));
        assertThat(get(sObj, "getCount"), is((Object) 100L));

        // copier
        Object copy = loader.loadClass("com.example.model.S").newInstance();
        copyFrom(copy, sObj);
        assertThat(get(copy, "getWordAsString"), is((Object) "word"));
        assertThat(get(copy, "getCount"), is((Object) 100L));

        // start
        Object aObj = loader.loadClass("com.example.model.A").newInstance();
        set(aObj, "setId", 10L);
        set(aObj, "setWordAsString", "world");
        startSummarize(sObj, aObj);
        assertThat(get(sObj, "getWordAsString"), is((Object) "world"));
        assertThat(get(sObj, "getCount"), is((Object) 1L));

        // combine
        set(copy, "setWordAsString", "world");
        set(copy, "setCount", 10L);
        combineSummarize(sObj, copy);
        assertThat(get(sObj, "getWordAsString"), is((Object) "world"));
        assertThat(get(sObj, "getCount"), is((Object) 11L));
        combineSummarize(sObj, copy);
        assertThat(get(sObj, "getWordAsString"), is((Object) "world"));
        assertThat(get(sObj, "getCount"), is((Object) 21L));
    }

    /**
     * 集約関数の一覧。
     * @throws Throwable 例外が発生した場合
     */
    @Test
    public void aggregators() throws Throwable {
        TableModelDescription a = new TableModelBuilder("A")
            .add(null, "id", PropertyTypeKind.LONG, Attribute.PRIMARY_KEY)
            .add(null, "value", PropertyTypeKind.INT)
            .toDescription();

        SummarizedModelDescription model = new SummarizedModelBuilder("S", a, "a")
            .add("sumId", Aggregator.IDENT, "a.id")
            .add("sumSum", Aggregator.SUM, "a.value")
            .add("sumCount", Aggregator.COUNT, "a.value")
            .add("sumMax", Aggregator.MAX, "a.value")
            .add("sumMin", Aggregator.MIN, "a.value")
            .groupBy("a.id")
            .toDescription();

        new Table().emit(a);
        new Summarized().emit(model);

        // properties
        ClassLoader loader = compile();
        Object aObj = loader.loadClass("com.example.model.A").newInstance();
        Object bObj = loader.loadClass("com.example.model.A").newInstance();
        Object cObj = loader.loadClass("com.example.model.A").newInstance();
        set(aObj, "setId", 10L);
        set(aObj, "setValue", 100);
        set(bObj, "setId", 10L);
        set(bObj, "setValue", 200);
        set(cObj, "setId", 10L);
        set(cObj, "setValue", 300);

        Object sObj = loader.loadClass("com.example.model.S").newInstance();
        Object temp = loader.loadClass("com.example.model.S").newInstance();
        startSummarize(sObj, bObj);
        startSummarize(temp, aObj);
        combineSummarize(sObj, temp);
        startSummarize(temp, cObj);
        combineSummarize(sObj, temp);

        assertThat(get(sObj, "getSumId"), is((Object) 10L));
        assertThat(get(sObj, "getSumSum"), is((Object) 600L));
        assertThat(get(sObj, "getSumCount"), is((Object) 3L));
        assertThat(get(sObj, "getSumMax"), is((Object) 300));
        assertThat(get(sObj, "getSumMin"), is((Object) 100));
    }

    /**
     * 単純なテーブル
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
     * 推奨されない名前。
     * @throws Throwable 例外が発生した場合
     */
    @Test
    public void bad_name() throws Throwable {
        TableModelDescription a = new TableModelBuilder("A__a")
            .add(null, "id__a", PropertyTypeKind.LONG, Attribute.PRIMARY_KEY)
            .add(null, "word__a", new StringType(255))
            .toDescription();

        SummarizedModelDescription model = new SummarizedModelBuilder("_S__a", a, "a")
            .add("_word__a", Aggregator.IDENT, "a.word__a")
            .add("_count__a", Aggregator.COUNT, "a.word__a")
            .groupBy("a.word__a")
            .toDescription();

        new Table().emit(a);
        new Summarized().emit(model);
    }
}
