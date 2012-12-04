/**
 * Copyright 2011-2012 Asakusa Framework Team.
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
package com.asakusafw.testdriver.core;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Test for {@link SpiDataModelAdapter}.
 * @since 0.2.0
 */
public class SpiDataModelAdapterTest extends SpiTestRoot {

    /**
     * Test method for {@link SpiDataModelAdapter#get(java.lang.Class)}.
     * @throws Exception if failed
     */
    @Test
    public void getDefinition() throws Exception {
        ClassLoader cl = register(DataModelAdapter.class, MockDataModelAdapter.class);
        DataModelAdapter adapter = new SpiDataModelAdapter(cl);
        assertThat(adapter.get(String.class), instanceOf(ValueDefinition.class));
        assertThat(adapter.get(Integer.class), is(nullValue()));
    }
}
