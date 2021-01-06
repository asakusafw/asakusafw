/**
 * Copyright 2011-2021 Asakusa Framework Team.
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
import java.util.Arrays;

import org.junit.Test;

import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.testdriver.core.MockImporterPreparator.Desc;
import com.asakusafw.vocabulary.external.ImporterDescription;

/**
 * Test for {@link SpiImporterPreparator}.
 * @since 0.2.0
 */
public class SpiImporterPreparatorTest extends SpiTestRoot {

    private static final TestContext EMPTY = new TestContext.Empty();

    /**
     * Test method for {@link SpiImporterPreparator#getDescriptionClass()}.
     */
    @Test
    public void getDescriptionClass() {
        SpiImporterPreparator target = new SpiImporterPreparator(getClass().getClassLoader());
        assertThat(target.getDescriptionClass(), equalTo(ImporterDescription.class));
    }

    /**
     * Test method for {@link SpiImporterPreparator#
     * createOutput(DataModelDefinition, ImporterDescription, TestContext)}.
     * @throws IOException if failed
     */
    @Test
    public void open() throws IOException {
        Desc desc = MockImporterPreparator.create();
        ClassLoader cl = register(ImporterPreparator.class, MockImporterPreparator.class);
        SpiImporterPreparator target = new SpiImporterPreparator(cl);
        try (ModelOutput<? super String> source = target.createOutput(ValueDefinition.of(String.class), desc, EMPTY)) {
            source.write("Hello, world!");
        }
        assertThat(desc.lines, is(Arrays.asList("Hello, world!")));
    }

    /**
     * not registered.
     * @throws IOException if failed
     */
    @Test(expected = IOException.class)
    public void open_notfound() throws IOException {
        Desc desc = MockImporterPreparator.create();
        SpiImporterPreparator target = new SpiImporterPreparator(getClass().getClassLoader());
        target.createOutput(ValueDefinition.of(String.class), desc, EMPTY);
    }
}
