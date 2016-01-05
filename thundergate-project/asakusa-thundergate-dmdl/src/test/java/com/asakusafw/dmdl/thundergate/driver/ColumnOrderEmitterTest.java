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
import com.asakusafw.vocabulary.bulkloader.ColumnOrder;

/**
 * Test for {@link ColumnOrderEmitter}.
 */
public class ColumnOrderEmitterTest extends GeneratorTesterRoot {

    /**
     * Initializes the test.
     * @throws Exception if some errors were occurred
     */
    @Before
    public void setUp() throws Exception {
        emitDrivers.add(new OriginalNameEmitter());
        emitDrivers.add(new ColumnOrderEmitter());
    }

    /**
     * explicitly defined {@code thundergate.name} for each properties.
     */
    @Test
    public void explicit() {
        ModelLoader loaded = generateJava("column_order_explicit");
        Class<?> type = loaded.modelType("Simple");

        assertThat(type.isAnnotationPresent(ColumnOrder.class), is(true));
        String[] order = type.getAnnotation(ColumnOrder.class).value();
        assertThat(order, is(new String[] { "COLUMN1", "COLUMN2", "COLUMN3" }));
    }

    /**
     * explicitly defined {@code thundergate.name} for each properties.
     */
    @Test
    public void implicit() {
        ModelLoader loaded = generateJava("column_order_implicit");
        Class<?> type = loaded.modelType("Simple");

        assertThat(type.isAnnotationPresent(ColumnOrder.class), is(true));
        String[] order = type.getAnnotation(ColumnOrder.class).value();
        assertThat(order, is(new String[] { "VALUE1", "VALUE2", "VALUE3" }));
    }
}
