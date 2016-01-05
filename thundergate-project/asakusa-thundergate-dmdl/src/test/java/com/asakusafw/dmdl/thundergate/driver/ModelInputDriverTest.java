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
package com.asakusafw.dmdl.thundergate.driver;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;

import org.apache.hadoop.io.DataInputBuffer;
import org.apache.hadoop.io.DataOutputBuffer;
import org.apache.hadoop.io.Text;
import org.junit.Before;
import org.junit.Test;

import com.asakusafw.dmdl.java.emitter.driver.ObjectDriver;
import com.asakusafw.dmdl.thundergate.GeneratorTesterRoot;
import com.asakusafw.dmdl.thundergate.driver.ModelInputDriver;
import com.asakusafw.dmdl.thundergate.driver.ModelOutputDriver;
import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.runtime.io.RecordEmitter;
import com.asakusafw.runtime.io.RecordParser;
import com.asakusafw.runtime.io.TsvEmitter;
import com.asakusafw.runtime.io.TsvParser;
import com.asakusafw.runtime.model.ModelInputLocation;
import com.asakusafw.runtime.model.ModelOutputLocation;
import com.asakusafw.runtime.value.Date;
import com.asakusafw.runtime.value.DateTime;

/**
 * Test for {@link ModelInputDriver} and {@link ModelOutputDriver}.
 */
public class ModelInputDriverTest extends GeneratorTesterRoot {

    /**
     * Initializes the test.
     * @throws Exception if some errors were occurred
     */
    @Before
    public void setUp() throws Exception {
        emitDrivers.add(new ModelInputDriver());
        emitDrivers.add(new ModelOutputDriver());
        emitDrivers.add(new ObjectDriver());
    }

    /**
     * input/output simple records.
     * @throws Exception if test was failed
     */
    @SuppressWarnings("unchecked")
    @Test
    public void simple_record() throws Exception {
        ModelLoader loader = generateJava("simple_record");

        Class<?> type = loader.modelType("Simple");
        assertThat(type.isAnnotationPresent(ModelInputLocation.class), is(true));
        assertThat(type.isAnnotationPresent(ModelOutputLocation.class), is(true));

        ModelWrapper object = loader.newModel("Simple");
        DataOutputBuffer output = new DataOutputBuffer();
        ModelOutput<Object> modelOut = (ModelOutput<Object>) type.getAnnotation(ModelOutputLocation.class)
            .value()
            .getDeclaredConstructor(RecordEmitter.class)
            .newInstance(new TsvEmitter(new OutputStreamWriter(output, "UTF-8")));

        object.set("sid", 1L);
        object.set("value", new Text("hello"));
        modelOut.write(object.unwrap());

        object.set("sid", 2L);
        object.set("value", new Text("world"));
        modelOut.write(object.unwrap());

        object.set("sid", 3L);
        object.set("value", null);
        modelOut.write(object.unwrap());
        modelOut.close();

        DataInputBuffer input = new DataInputBuffer();
        input.reset(output.getData(), output.getLength());
        ModelInput<Object> modelIn = (ModelInput<Object>) type.getAnnotation(ModelInputLocation.class)
            .value()
            .getDeclaredConstructor(RecordParser.class)
            .newInstance(new TsvParser(new InputStreamReader(input, "UTF-8")));
        ModelWrapper copy = loader.newModel("Simple");

        modelIn.readTo(copy.unwrap());
        assertThat(copy.get("sid"), is((Object) 1L));
        assertThat(copy.get("value"), is((Object) new Text("hello")));

        modelIn.readTo(copy.unwrap());
        assertThat(copy.get("sid"), is((Object) 2L));
        assertThat(copy.get("value"), is((Object) new Text("world")));

        modelIn.readTo(copy.unwrap());
        assertThat(copy.get("sid"), is((Object) 3L));
        assertThat(copy.getOption("value").isNull(), is(true));

        assertThat(input.read(), is(-1));
        modelIn.close();
    }

    /**
     * all primitive types.
     * @throws Exception if test was failed
     */
    @SuppressWarnings("unchecked")
    @Test
    public void primitives() throws Exception {
        ModelLoader loader = generateJava("primitives");

        Class<?> type = loader.modelType("Primitives");
        assertThat(type.isAnnotationPresent(ModelInputLocation.class), is(true));
        assertThat(type.isAnnotationPresent(ModelOutputLocation.class), is(true));

        ModelWrapper object = loader.newModel("Primitives");

        object.set("type_boolean", true);
        object.set("type_byte", (byte) 64);
        object.set("type_short", (short) 256);
        object.set("type_int", 100);
        object.set("type_long", 200L);
        object.set("type_float", 300.f);
        object.set("type_double", 400.d);
        object.set("type_decimal", new BigDecimal("1234.567"));
        object.set("type_text", new Text("Hello, world!"));
        object.set("type_date", new Date(2011, 3, 31));
        object.set("type_datetime", new DateTime(2011, 3, 31, 23, 30, 1));

        DataOutputBuffer output = new DataOutputBuffer();
        ModelOutput<Object> modelOut = (ModelOutput<Object>) type.getAnnotation(ModelOutputLocation.class)
            .value()
            .getDeclaredConstructor(RecordEmitter.class)
            .newInstance(new TsvEmitter(new OutputStreamWriter(output, "UTF-8")));
        modelOut.write(object.unwrap());
        modelOut.write(object.unwrap());
        modelOut.write(object.unwrap());
        modelOut.close();

        DataInputBuffer input = new DataInputBuffer();
        input.reset(output.getData(), output.getLength());
        ModelInput<Object> modelIn = (ModelInput<Object>) type.getAnnotation(ModelInputLocation.class)
            .value()
            .getDeclaredConstructor(RecordParser.class)
            .newInstance(new TsvParser(new InputStreamReader(input, "UTF-8")));
        ModelWrapper copy = loader.newModel("Primitives");
        modelIn.readTo(copy.unwrap());
        assertThat(object.unwrap(), equalTo(copy.unwrap()));
        assertThat(input.read(), is(-1));
        modelIn.close();
    }
}
