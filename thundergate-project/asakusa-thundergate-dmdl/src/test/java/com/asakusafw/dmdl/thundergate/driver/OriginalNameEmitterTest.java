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

import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.Test;

import com.asakusafw.dmdl.thundergate.GeneratorTesterRoot;
import com.asakusafw.vocabulary.bulkloader.OriginalName;

/**
 * Test for {@link OriginalNameDriver} and {@link OriginalNameEmitter}.
 */
public class OriginalNameEmitterTest extends GeneratorTesterRoot {

    /**
     * Initializes the test.
     * @throws Exception if some errors were occurred
     */
    @Before
    public void setUp() throws Exception {
        emitDrivers.add(new OriginalNameEmitter());
    }

    /**
     * explicitly defined {@code thundergate.name}.
     * @throws Exception if test was failed
     */
    @Test
    public void explicit() throws Exception {
        ModelLoader loaded = generateJava("name_explicit");
        Class<?> type = loaded.modelType("Simple");

        assertThat(type.isAnnotationPresent(OriginalName.class), is(true));
        assertThat(type.getAnnotation(OriginalName.class).value(), is("TABLE"));

        Method method = type.getMethod("getValueOption");
        assertThat(method.isAnnotationPresent(OriginalName.class), is(true));
        assertThat(method.getAnnotation(OriginalName.class).value(), is("COLUMN"));
    }

    /**
     * implicit original name.
     * @throws Exception if test was failed
     */
    @Test
    public void simple() throws Exception {
        ModelLoader loaded = generateJava("simple");
        Class<?> type = loaded.modelType("Simple");

        assertThat(type.isAnnotationPresent(OriginalName.class), is(true));
        assertThat(type.getAnnotation(OriginalName.class).value(), is("SIMPLE"));
    }

    /**
     * empty elements.
     */
    @Test
    public void invalid_name_empty() {
        shouldSemanticError("invalid_name_empty");
    }

    /**
     * extra elements.
     */
    @Test
    public void invalid_name_extra() {
        shouldSemanticError("invalid_name_extra");
    }

    /**
     * inconsistent element type.
     */
    @Test
    public void invalid_name_type() {
        shouldSemanticError("invalid_name_type");
    }
}
