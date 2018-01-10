/**
 * Copyright 2011-2018 Asakusa Framework Team.
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

import org.apache.hadoop.io.Text;
import org.junit.Before;
import org.junit.Test;

import com.asakusafw.dmdl.java.GeneratorTesterRoot;

/**
 * Test for {@link ObjectDriver}.
 */
public class ObjectDriverTest extends GeneratorTesterRoot {

    /**
     * Initializes the test.
     * @throws Exception if some errors were occurred
     */
    @Before
    public void setUp() throws Exception {
        emitDrivers.add(new ObjectDriver());
    }

    /**
     * records.
     */
    @Test
    public void simple_record() {
        ModelLoader loader = generate();

        ModelWrapper a = loader.newModel("Simple");
        ModelWrapper b = loader.newModel("Simple");

        a.set("sid", 100L);
        assertThat(a.unwrap().equals(b.unwrap()), is(false));

        b.copyFrom(a);
        assertThat(a.unwrap().hashCode(), is(b.unwrap().hashCode()));
        assertThat(a.unwrap().equals(b.unwrap()), is(true));
        assertThat(a.unwrap().toString(), is(b.unwrap().toString()));

        b.set("value", new Text("Hello, world!"));
        assertThat(a.unwrap().equals(b.unwrap()), is(false));
    }

    /**
     * projections.
     */
    @Test
    public void simple_projection() {
        // compile checking only
        generate();
    }
}
