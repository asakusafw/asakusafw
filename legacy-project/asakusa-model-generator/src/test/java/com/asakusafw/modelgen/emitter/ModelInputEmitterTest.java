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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.junit.After;
import org.junit.Test;

import com.asakusafw.modelgen.model.Attribute;
import com.asakusafw.modelgen.model.PropertyTypeKind;
import com.asakusafw.modelgen.model.StringType;
import com.asakusafw.modelgen.model.TableModelDescription;
import com.asakusafw.modelgen.util.TableModelBuilder;
import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.TsvParser;
import com.asakusafw.runtime.value.Date;
import com.asakusafw.runtime.value.DateUtil;

/**
 * Test for {@link ModelInputEmitter}.
 */
public class ModelInputEmitterTest extends EmitterTestRoot {

    private TsvParser parser;

    private void init(String fileName) throws IOException {
        InputStream in = ModelInputEmitterTest.class
            .getResourceAsStream("tsv/" + fileName);
        assertThat(fileName, in, is(not(nullValue())));
        parser = new TsvParser(new InputStreamReader(in, "UTF-8"));
    }

    /**
     * Cleans up the test.
     * @throws Exception if some errors were occurred
     */
    @Override
    @After
    public void tearDown() throws Exception {
        if (parser != null) {
            parser.close();
        }
        super.tearDown();
    }

    /**
     * 単純なモデルのテスト。
     * @throws Throwable 例外が発生した場合
     */
    @Test
    public void simple() throws Throwable {
        init("simple");

        TableModelDescription model = new TableModelBuilder("Model")
            .add(null, "id", PropertyTypeKind.LONG, Attribute.PRIMARY_KEY)
            .toDescription();

        new Table().emit(model);
        new TsvIn().emit(model);

        ClassLoader loader = compile();

        Object obj = create(loader, "model.Model");
        ModelInput<Object> input = createInput(loader, parser, "io.ModelModelInput");

        assertThat(input.readTo(obj), is(true));
        assertThat(get(obj, "getId"), is((Object) 100L));

        assertThat(input.readTo(obj), is(true));
        assertThat(get(obj, "getId"), is((Object) 300L));

        assertThat(input.readTo(obj), is(true));
        assertThat(get(obj, "getId"), is((Object) 500L));

        assertThat(input.readTo(obj), is(false));

        input.close();
    }

    /**
     * 単純なモデルのテスト。
     * @throws Throwable 例外が発生した場合
     */
    @Test
    public void complex() throws Throwable {
        init("complex");

        TableModelDescription model = new TableModelBuilder("Model")
            .add(null, "id", PropertyTypeKind.LONG, Attribute.PRIMARY_KEY)
            .add(null, "value", new StringType(255))
            .add(null, "date", PropertyTypeKind.DATE)
            .add(null, "price", PropertyTypeKind.INT)
            .add(null, "flag", PropertyTypeKind.BOOLEAN)
            .toDescription();

        new Table().emit(model);
        new TsvIn().emit(model);

        ClassLoader loader = compile();

        Object obj = create(loader, "model.Model");
        ModelInput<Object> input = createInput(loader, parser, "io.ModelModelInput");

        assertThat(input.readTo(obj), is(true));
        assertThat(get(obj, "getId"), is((Object) 100L));
        assertThat(get(obj, "getValueAsString"), is((Object) "Hello"));
        assertThat(get(obj, "getDate"), is((Object) date(1999, 12, 31)));
        assertThat(get(obj, "getPrice"), is((Object) 2000));
        assertThat(get(obj, "isFlag"), is((Object) true));

        assertThat(input.readTo(obj), is(true));
        assertThat(get(obj, "getId"), is((Object) 300L));
        assertThat(get(obj, "getValueAsString"), is((Object) "World"));
        assertThat(get(obj, "getDate"), is((Object) date(2100, 1, 1)));
        assertThat(get(obj, "getPrice"), is((Object) 9999));
        assertThat(get(obj, "isFlag"), is((Object) false));

        assertThat(input.readTo(obj), is(true));
        assertThat(get(obj, "getId"), is((Object) 500L));
        assertThat(get(obj, "getValueAsString"), is((Object) ""));
        assertThat(get(obj, "getDate"), is((Object) date(1, 1, 1)));
        assertThat(get(obj, "getPrice"), is((Object) 0));
        assertThat(get(obj, "isFlag"), is((Object) true));

        assertThat(input.readTo(obj), is(false));

        input.close();
    }

    /**
     * 空TSVのテスト。
     * @throws Throwable 例外が発生した場合
     */
    @Test
    public void empty() throws Throwable {
        init("empty");

        TableModelDescription model = new TableModelBuilder("Model")
            .add(null, "id", PropertyTypeKind.LONG, Attribute.PRIMARY_KEY)
            .toDescription();

        new Table().emit(model);
        new TsvIn().emit(model);

        ClassLoader loader = compile();

        Object obj = create(loader, "model.Model");
        ModelInput<Object> input = createInput(loader, parser, "io.ModelModelInput");

        assertThat(input.readTo(obj), is(false));

        input.close();
    }

    private Date date(int year, int month, int day) {
        Date date = new Date();
        date.setElapsedDays(DateUtil.getDayFromDate(year, month, day));
        return date;
    }
}
