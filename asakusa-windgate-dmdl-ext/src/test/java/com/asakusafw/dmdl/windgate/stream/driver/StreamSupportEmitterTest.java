/**
 * Copyright 2011-2014 Asakusa Framework Team.
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
package com.asakusafw.dmdl.windgate.stream.driver;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;

import org.apache.hadoop.io.Text;
import org.junit.Before;
import org.junit.Test;

import com.asakusafw.dmdl.java.emitter.driver.ObjectDriver;
import com.asakusafw.runtime.value.Date;
import com.asakusafw.runtime.value.DateTime;
import com.asakusafw.windgate.core.vocabulary.DataModelStreamSupport;
import com.asakusafw.windgate.core.vocabulary.DataModelStreamSupport.DataModelReader;
import com.asakusafw.windgate.core.vocabulary.DataModelStreamSupport.DataModelWriter;

/**
 * Test for {@link StreamSupportEmitter}.
 */
public class StreamSupportEmitterTest extends GeneratorTesterRoot {

    /**
     * Initializes the test.
     * @throws Exception if some errors were occurred
     */
    @Before
    public void setUp() throws Exception {
        emitDrivers.add(new StreamSupportEmitter());
        emitDrivers.add(new ObjectDriver());
    }

    /**
     * A simple case.
     * @throws Exception if failed
     */
    @Test
    public void simple() throws Exception {
        ModelLoader loaded = generateJava("simple");
        ModelWrapper model = loaded.newModel("Simple");
        DataModelStreamSupport<?> support = (DataModelStreamSupport<?>) loaded.newObject("stream", "SimpleStreamSupport");

        assertThat(support.getSupportedType(), is((Object) model.unwrap().getClass()));

        DataModelStreamSupport<Object> unsafe = unsafe(support);

        model.set("value", new Text("Hello, world!"));

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        DataModelWriter<Object> writer = unsafe.createWriter("example", output);
        writer.write(model.unwrap());
        writer.flush();
        output.close();

        Object buffer = loaded.newModel("Simple").unwrap();
        DataModelReader<Object> reader = unsafe.createReader("example", new ByteArrayInputStream(output.toByteArray()));
        assertThat(reader.readTo(buffer), is(true));
        assertThat(buffer, is(buffer));
        assertThat(reader.readTo(buffer), is(false));
    }

    /**
     * All types.
     * @throws Exception if failed
     */
    @Test
    public void types() throws Exception {
        ModelLoader loaded = generateJava("types");
        ModelWrapper model = loaded.newModel("Types");
        DataModelStreamSupport<?> support = (DataModelStreamSupport<?>) loaded.newObject("stream", "TypesStreamSupport");
        assertThat(support.getSupportedType(), is((Object) model.unwrap().getClass()));

        ModelWrapper empty = loaded.newModel("Types");

        ModelWrapper all = loaded.newModel("Types");
        all.set("c_int", 100);
        all.set("c_text", new Text("Hello, DMDL world!"));
        all.set("c_boolean", true);
        all.set("c_byte", (byte) 64);
        all.set("c_short", (short) 1023);
        all.set("c_long", 100000L);
        all.set("c_float", 1.5f);
        all.set("c_double", 2.5f);
        all.set("c_decimal", new BigDecimal("3.1415"));
        all.set("c_date", new Date(2011, 9, 1));
        all.set("c_datetime", new DateTime(2011, 12, 31, 23, 59, 59));

        DataModelStreamSupport<Object> unsafe = unsafe(support);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        DataModelWriter<Object> writer = unsafe.createWriter("example", output);
        writer.write(empty.unwrap());
        writer.write(all.unwrap());
        writer.flush();
        output.close();

        Object buffer = loaded.newModel("Types").unwrap();
        DataModelReader<Object> reader = unsafe.createReader("example", new ByteArrayInputStream(output.toByteArray()));
        assertThat(reader.readTo(buffer), is(true));
        assertThat(buffer, is(empty.unwrap()));
        assertThat(reader.readTo(buffer), is(true));
        assertThat(buffer, is(all.unwrap()));
        assertThat(reader.readTo(buffer), is(false));
    }

    /**
     * Compile with no attributes.
     * @throws Exception if failed
     */
    @Test
    public void no_attributes() throws Exception {
        ModelLoader loaded = generateJava("no_attributes");
        assertThat(loaded.exists("stream", "NoAttributesStreamSupport"), is(false));
    }

    /**
     * Compile with invalid attribute.
     * @throws Exception if failed
     */
    @Test
    public void invalid_attribute() throws Exception {
        shouldSemanticError("invalid_attribute");
    }

    @SuppressWarnings("unchecked")
    private DataModelStreamSupport<Object> unsafe(DataModelStreamSupport<?> support) {
        return (DataModelStreamSupport<Object>) support;
    }
}
