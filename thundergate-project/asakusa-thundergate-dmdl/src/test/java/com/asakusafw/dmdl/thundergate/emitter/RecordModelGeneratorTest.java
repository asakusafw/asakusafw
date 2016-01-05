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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.math.BigDecimal;

import org.apache.hadoop.io.Text;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;

import com.asakusafw.dmdl.java.emitter.CompositeDataModelDriver;
import com.asakusafw.dmdl.thundergate.Constants;
import com.asakusafw.dmdl.thundergate.GeneratorTesterRoot;
import com.asakusafw.dmdl.thundergate.model.Attribute;
import com.asakusafw.dmdl.thundergate.model.DecimalType;
import com.asakusafw.dmdl.thundergate.model.PropertyTypeKind;
import com.asakusafw.dmdl.thundergate.model.StringType;
import com.asakusafw.dmdl.thundergate.model.TableModelDescription;
import com.asakusafw.dmdl.thundergate.util.TableModelBuilder;
import com.asakusafw.runtime.value.Date;
import com.asakusafw.runtime.value.DateTime;
import com.asakusafw.vocabulary.bulkloader.PrimaryKey;

/**
 * Test for {@link RecordModelGenerator}.
 */
public class RecordModelGeneratorTest extends GeneratorTesterRoot {

    /**
     * Initializes the test.
     * @throws Exception if some errors were occurred
     */
    @Before
    public void setUp() throws Exception {
        emitDrivers.add(new CompositeDataModelDriver(getClass().getClassLoader()));
    }

    /**
     * simple table.
     */
    @Test
    public void simple() {
        TableModelDescription table = new TableModelBuilder("SIMPLE")
            .add("comment", "VALUE", new StringType(255))
            .toDescription();
        emitDmdl(RecordModelGenerator.generate(table));

        ModelLoader loader = generateJava();
        loader.setNamespace(Constants.SOURCE_TABLE);

        ModelWrapper object = loader.newModel("Simple");

        object.set("value", new Text("Hello, world!"));
        assertThat(object.get("value"), eq(new Text("Hello, world!")));
    }

    /**
     * all types.
     */
    @Test
    public void primitives() {
        TableModelDescription table = new TableModelBuilder("SIMPLE")
            .add(null, "TYPE_BOOLEAN", PropertyTypeKind.BOOLEAN)
            .add(null, "TYPE_BYTE", PropertyTypeKind.BYTE)
            .add(null, "TYPE_SHORT", PropertyTypeKind.SHORT)
            .add(null, "TYPE_INT", PropertyTypeKind.INT)
            .add(null, "TYPE_LONG", PropertyTypeKind.LONG)
            .add(null, "TYPE_DATE", PropertyTypeKind.DATE)
            .add(null, "TYPE_DATETIME", PropertyTypeKind.DATETIME)
            .add(null, "TYPE_TEXT", new StringType(255))
            .add(null, "TYPE_DECIMAL", new DecimalType(10, 10))
            .toDescription();
        emitDmdl(RecordModelGenerator.generate(table));

        ModelLoader loader = generateJava();
        loader.setNamespace(Constants.SOURCE_TABLE);
        ModelWrapper object = loader.newModel("Simple");

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
     * table with primary keys.
     */
    @Test
    public void primary_keys() {
        TableModelDescription table = new TableModelBuilder("SIMPLE")
            .add("SID", "SID", PropertyTypeKind.LONG, Attribute.PRIMARY_KEY)
            .add("comment", "VALUE", new StringType(255))
            .toDescription();
        emitDmdl(RecordModelGenerator.generate(table));

        ModelLoader loader = generateJava();
        loader.setNamespace(Constants.SOURCE_TABLE);
        ModelWrapper object = loader.newModel("Simple");

        object.set("sid", 257L);
        assertThat(object.get("sid"), eq(257L));
        object.set("value", new Text("Hello, DMDL!"));
        assertThat(object.get("value"), eq(new Text("Hello, DMDL!")));

        PrimaryKey pk = object.unwrap().getClass().getAnnotation(PrimaryKey.class);
        assertThat(pk, not(nullValue()));
        assertThat(pk.value(), is(new String[] { "sid" }));
    }

    private Matcher<Object> eq(final Object value) {
        return is(value);
    }
}
