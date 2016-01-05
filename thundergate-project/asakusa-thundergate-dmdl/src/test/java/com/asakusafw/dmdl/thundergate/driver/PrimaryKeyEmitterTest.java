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

import org.junit.Before;
import org.junit.Test;

import com.asakusafw.dmdl.thundergate.GeneratorTesterRoot;
import com.asakusafw.vocabulary.bulkloader.PrimaryKey;

/**
 * Test for {@link PrimaryKeyDriver} and {@link PrimaryKeyDriver}.
 */
public class PrimaryKeyEmitterTest extends GeneratorTesterRoot {

    /**
     * Initializes the test.
     * @throws Exception if some errors were occurred
     */
    @Before
    public void setUp() throws Exception {
        emitDrivers.add(new PrimaryKeyEmitter());
    }

    /**
     * simple.
     */
    @Test
    public void pk() {
        ModelLoader loaded = generateJava("pk");
        Class<?> type = loaded.modelType("Simple");

        assertThat(type.isAnnotationPresent(PrimaryKey.class), is(true));
        String[] properties = type.getAnnotation(PrimaryKey.class).value();

        assertThat(properties, is(new String[] { "value1", "value2" }));
    }

    /**
     * empty elements.
     */
    @Test
    public void invalid_empty() {
        shouldSemanticError("invalid_pk_empty");
    }

    /**
     * extra elements.
     */
    @Test
    public void invalid_extra() {
        shouldSemanticError("invalid_pk_extra");
    }

    /**
     * inconsistent element type.
     */
    @Test
    public void invalid_scalar() {
        shouldSemanticError("invalid_pk_scalar");
    }

    /**
     * inconsistent element type.
     */
    @Test
    public void invalid_type() {
        shouldSemanticError("invalid_pk_type");
    }

    /**
     * unbound property.
     */
    @Test
    public void invalid_unbound() {
        shouldSemanticError("invalid_pk_unbound");
    }

    /**
     * declared in properties.
     */
    @Test
    public void invalid_location() {
        shouldSemanticError("invalid_pk_location");
    }
}
