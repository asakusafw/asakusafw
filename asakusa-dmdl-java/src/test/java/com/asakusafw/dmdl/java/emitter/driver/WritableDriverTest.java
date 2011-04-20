/**
 * Copyright 2011 Asakusa Framework Team.
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
package com.asakusafw.dmdl.java.emitter.driver;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.math.BigDecimal;

import org.apache.hadoop.io.DataInputBuffer;
import org.apache.hadoop.io.DataOutputBuffer;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.junit.Before;
import org.junit.Test;

import com.asakusafw.dmdl.java.GeneratorTesterRoot;
import com.asakusafw.runtime.value.Date;
import com.asakusafw.runtime.value.DateTime;

/**
 * Test for {@link WritableDriver}.
 */
public class WritableDriverTest extends GeneratorTesterRoot {

    /**
     * Initializes the test.
     * @throws Exception if some errors were occurred
     */
    @Before
    public void setUp() throws Exception {
        emitDrivers.add(new WritableDriver());
        emitDrivers.add(new ObjectDriver());
    }

    /**
     * primitives.
     * @throws Exception if test was failed
     */
    @Test
    public void primitives() throws Exception {
        ModelLoader loader = generate();
        ModelWrapper object = loader.newModel("Primitives");
        assertThat(object.unwrap(), instanceOf(Writable.class));

        object.set("type_boolean", true);
        object.set("type_byte", (byte) 64);
        object.set("type_short", (short) 256);
        object.set("type_int", 100);
        object.set("type_long", 200L);
        object.set("type_decimal", new BigDecimal("1234.567"));
        object.set("type_text", new Text("Hello, world!"));
        object.set("type_date", new Date(2011, 3, 31));
        object.set("type_datetime", new DateTime(2011, 3, 31, 23, 30, 1));

        Writable writable = (Writable) object.unwrap();

        DataOutputBuffer output = new DataOutputBuffer();
        writable.write(output);

        Writable copy = (Writable) loader.newModel("Primitives").unwrap();
        DataInputBuffer input = new DataInputBuffer();
        input.reset(output.getData(), output.getLength());
        copy.readFields(input);

        assertThat(input.read(), is(-1));
        assertThat(writable, equalTo(copy));
    }
}
