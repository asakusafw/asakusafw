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

import java.io.IOException;
import org.junit.Test;

import com.asakusafw.testdriver.core.MockExporterRetriever.Desc;
import com.asakusafw.vocabulary.external.ExporterDescription;

/**
 * Test for {@link SpiExporterRetriever}.
 * @since 0.2.0
 */
public class SpiExporterRetrieverTest extends SpiTestRoot {

    private static final TestContext EMPTY = new TestContext.Empty();

    /**
     * Test method for {@link SpiExporterRetriever#getDescriptionClass()}.
     */
    @Test
    public void getDescriptionClass() {
        SpiExporterRetriever target = new SpiExporterRetriever(getClass().getClassLoader());
        assertThat(target.getDescriptionClass(), equalTo(ExporterDescription.class));
    }

    /**
     * Test method for {@link SpiExporterRetriever#
     * createSource(DataModelDefinition, ExporterDescription, TestContext)}.
     * @throws Exception if failed
     */
    @Test
    public void open() throws Exception {
        Desc desc = MockExporterRetriever.create("Hello, world!");
        ClassLoader cl = register(ExporterRetriever.class, MockExporterRetriever.class);

        SpiExporterRetriever target = new SpiExporterRetriever(cl);
        DataModelSource source = target.createSource(ValueDefinition.of(String.class), desc, EMPTY);
        assertThat(ValueDefinition.of(String.class).toObject(source.next()), is("Hello, world!"));
    }

    /**
     * not registered.
     * @throws Exception if failed
     */
    @Test(expected = IOException.class)
    public void open_notfound() throws Exception {
        Desc desc = MockExporterRetriever.create("Hello, world!");

        SpiExporterRetriever target = new SpiExporterRetriever(getClass().getClassLoader());
        target.createSource(ValueDefinition.of(String.class), desc, EMPTY);
    }
}
