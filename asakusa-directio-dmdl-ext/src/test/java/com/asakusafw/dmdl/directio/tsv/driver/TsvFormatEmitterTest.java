/**
 * Copyright 2011-2013 Asakusa Framework Team.
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
package com.asakusafw.dmdl.directio.tsv.driver;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import org.apache.hadoop.io.Text;
import org.junit.Before;
import org.junit.Test;

import com.asakusafw.dmdl.java.emitter.driver.ObjectDriver;
import com.asakusafw.runtime.directio.BinaryStreamFormat;
import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.runtime.value.Date;
import com.asakusafw.runtime.value.DateTime;
import com.asakusafw.runtime.value.StringOption;

/**
 * Test for {@link TsvFormatEmitter}.
 */
public class TsvFormatEmitterTest extends GeneratorTesterRoot {

    /**
     * Initializes the test.
     * @throws Exception if some errors were occurred
     */
    @Before
    public void setUp() throws Exception {
        emitDrivers.add(new TsvFormatEmitter());
        emitDrivers.add(new ObjectDriver());
    }

    /**
     * simple testing.
     * @throws Exception if failed
     */
    @Test
    public void simple() throws Exception {
        ModelLoader loaded = generateJava("simple");
        ModelWrapper model = loaded.newModel("Simple");
        BinaryStreamFormat<?> support = (BinaryStreamFormat<?>) loaded.newObject("tsv", "SimpleTsvFormat");

        assertThat(support.getSupportedType(), is((Object) model.unwrap().getClass()));

        BinaryStreamFormat<Object> unsafe = unsafe(support);

        model.set("value", new Text("Hello, world!"));

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ModelOutput<Object> writer = unsafe.createOutput(model.unwrap().getClass(), "hello", output);
        writer.write(model.unwrap());
        writer.close();

        Object buffer = loaded.newModel("Simple").unwrap();
        ModelInput<Object> reader = unsafe.createInput(model.unwrap().getClass(), "hello", in(output),
                0, size(output));
        assertThat(reader.readTo(buffer), is(true));
        assertThat(buffer, is(model.unwrap()));
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
        BinaryStreamFormat<?> support = (BinaryStreamFormat<?>) loaded.newObject("tsv", "TypesTsvFormat");
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

        BinaryStreamFormat<Object> unsafe = unsafe(support);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ModelOutput<Object> writer = unsafe.createOutput(model.unwrap().getClass(), "hello", output);
        writer.write(empty.unwrap());
        writer.write(all.unwrap());
        writer.close();

        Object buffer = loaded.newModel("Types").unwrap();
        ModelInput<Object> reader = unsafe.createInput(model.unwrap().getClass(), "hello", in(output),
                0, size(output));
        assertThat(reader.readTo(buffer), is(true));
        assertThat(buffer, is(empty.unwrap()));
        assertThat(reader.readTo(buffer), is(true));
        assertThat(buffer, is(all.unwrap()));
        assertThat(reader.readTo(buffer), is(false));
    }

    /**
     * With compression.
     * @throws Exception if failed
     */
    @Test
    public void compression() throws Exception {
        ModelLoader loaded = generateJava("compression");
        ModelWrapper model = loaded.newModel("Compression");
        BinaryStreamFormat<?> support = (BinaryStreamFormat<?>) loaded.newObject("tsv", "CompressionTsvFormat");

        assertThat(support.getSupportedType(), is((Object) model.unwrap().getClass()));

        BinaryStreamFormat<Object> unsafe = unsafe(support);

        model.set("value", new Text("Hello, world!"));

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ModelOutput<Object> writer = unsafe.createOutput(model.unwrap().getClass(), "hello", output);
        writer.write(model.unwrap());
        writer.close();

        Object buffer = loaded.newModel("Compression").unwrap();
        ModelInput<Object> reader = unsafe.createInput(model.unwrap().getClass(), "hello", in(output),
                0, size(output));
        assertThat(reader.readTo(buffer), is(true));
        assertThat(buffer, is(model.unwrap()));
        assertThat(reader.readTo(buffer), is(false));
    }

    /**
     * simple testing.
     * @throws Exception if failed
     */
    @Test
    public void header() throws Exception {
        ModelLoader loaded = generateJava("header");
        ModelWrapper model = loaded.newModel("Header");
        BinaryStreamFormat<?> support = (BinaryStreamFormat<?>) loaded.newObject("tsv", "HeaderTsvFormat");

        assertThat(support.getSupportedType(), is((Object) model.unwrap().getClass()));

        BinaryStreamFormat<Object> unsafe = unsafe(support);

        model.set("key", 100);
        model.set("value", new Text("Hello, world!"));

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ModelOutput<Object> writer = unsafe.createOutput(model.unwrap().getClass(), "hello", output);
        writer.write(model.unwrap());
        writer.close();

        Object buffer = loaded.newModel("Header").unwrap();
        ModelInput<Object> reader = unsafe.createInput(model.unwrap().getClass(), "hello", in(output), 0, size(output));
        assertThat(reader.readTo(buffer), is(true));
        assertThat(buffer, is(model.unwrap()));
        assertThat(reader.readTo(buffer), is(false));
    }

    /**
     * With ignoring field.
     * @throws Exception if failed
     */
    @Test
    public void ignore() throws Exception {
        ModelLoader loaded = generateJava("ignore");
        ModelWrapper model = loaded.newModel("Ignore");
        BinaryStreamFormat<?> support = (BinaryStreamFormat<?>) loaded.newObject("tsv", "IgnoreTsvFormat");

        assertThat(support.getSupportedType(), is((Object) model.unwrap().getClass()));

        BinaryStreamFormat<Object> unsafe = unsafe(support);

        model.set("value", new Text("Hello, world!"));

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ModelOutput<Object> writer = unsafe.createOutput(model.unwrap().getClass(), "hello", output);
        writer.write(model.unwrap());
        writer.close();

        Object buffer = loaded.newModel("Ignore").unwrap();
        ModelInput<Object> reader = unsafe.createInput(model.unwrap().getClass(), "hello", in(output),
                0, size(output));
        assertThat(reader.readTo(buffer), is(true));
        assertThat(buffer, is(model.unwrap()));
        assertThat(reader.readTo(buffer), is(false));
    }

    /**
     * With file name.
     * @throws Exception if failed
     */
    @Test
    public void file_name() throws Exception {
        ModelLoader loaded = generateJava("file_name");
        ModelWrapper model = loaded.newModel("FileName");
        BinaryStreamFormat<?> support = (BinaryStreamFormat<?>) loaded.newObject("tsv", "FileNameTsvFormat");

        assertThat(support.getSupportedType(), is((Object) model.unwrap().getClass()));

        BinaryStreamFormat<Object> unsafe = unsafe(support);

        model.set("value", new Text("Hello, world!"));

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ModelOutput<Object> writer = unsafe.createOutput(model.unwrap().getClass(), "hello.tsv", output);
        writer.write(model.unwrap());
        writer.close();

        ModelWrapper buffer = loaded.newModel("FileName");
        ModelInput<Object> reader = unsafe.createInput(model.unwrap().getClass(), "hello.tsv", in(output),
                0, size(output));
        assertThat(reader.readTo(buffer.unwrap()), is(true));
        assertThat(buffer.getOption("value"), is((Object) new StringOption("Hello, world!")));
        assertThat(buffer.getOption("path"), is((Object) new StringOption("hello.tsv")));
        assertThat(reader.readTo(buffer.unwrap()), is(false));
    }

    @SuppressWarnings("unchecked")
    private BinaryStreamFormat<Object> unsafe(Object support) {
        return (BinaryStreamFormat<Object>) support;
    }

    private ByteArrayInputStream in(ByteArrayOutputStream output) {
        return new ByteArrayInputStream(output.toByteArray());
    }

    private long size(ByteArrayOutputStream output) {
        return output.size();
    }
}
