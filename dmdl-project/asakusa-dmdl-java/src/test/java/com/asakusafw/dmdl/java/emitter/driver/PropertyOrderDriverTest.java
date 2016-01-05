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
package com.asakusafw.dmdl.java.emitter.driver;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.asakusafw.dmdl.java.GeneratorTesterRoot;
import com.asakusafw.runtime.model.PropertyOrder;

/**
 * Test for {@link PropertyOrderDriver}.
 */
public class PropertyOrderDriverTest extends GeneratorTesterRoot {

    /**
     * Initializes the test.
     * @throws Exception if some errors were occurred
     */
    @Before
    public void setUp() throws Exception {
        emitDrivers.add(new PropertyOrderDriver());
    }

    /**
     * Simple testing.
     * @throws Exception if test was failed
     */
    @Test
    public void single_property() throws Exception {
        ModelLoader loader = generate();
        Class<?> modelClass = loader.modelType("Model");
        PropertyOrder annotation = modelClass.getAnnotation(PropertyOrder.class);
        assertThat(annotation, is(notNullValue()));
        assertThat(annotation.value(), is(new String[] { "property" }));
    }

    /**
     * Multiple properties.
     * @throws Exception if test was failed
     */
    @Test
    public void many_properties() throws Exception {
        ModelLoader loader = generate();
        Class<?> modelClass = loader.modelType("Model");
        PropertyOrder annotation = modelClass.getAnnotation(PropertyOrder.class);
        assertThat(annotation, is(notNullValue()));
        assertThat(annotation.value(), is(new String[] {
                "first",
                "second_property",
                "a",
                "last",
        }));
    }
}
