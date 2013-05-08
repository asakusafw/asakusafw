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
package com.asakusafw.testdriver.core;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

import org.junit.Test;

import com.asakusafw.testdriver.core.MockImporterPreparator.Desc;

/**
 * Test for {@link TestDataPreparator}.
 * @since 0.2.0
 */
@Deprecated
public class TestDataPreparatorTest extends SpiTestRoot {

    /**
     * simple test.
     * @throws Exception if failed
     */
    @Test
    public void simple() throws Exception {
        TestDataPreparator prep = new TestDataPreparator(
                new MockDataModelAdapter(String.class),
                new MockSourceProvider()
                    .add(uri("testing:src"), "Hello, world!"),
                new MockImporterPreparator().wrap(),
                new MockExporterRetriever().wrap());
        Desc desc = MockImporterPreparator.create();
        prep.prepare(desc.getModelType(), desc, uri("testing:src"));
        assertThat(desc.lines, is(Arrays.asList("Hello, world!")));
    }

    /**
     * with SPI.
     * @throws Exception if failed
     */
    @Test
    public void spi() throws Exception {
        register(DataModelAdapter.class, MockDataModelAdapter.class);
        register(DataModelSourceProvider.class, MockSourceProvider.class);
        ClassLoader loader = register(ImporterPreparator.class, MockImporterPreparator.class);

        TestDataPreparator prep = new TestDataPreparator(loader);
        Desc desc = MockImporterPreparator.create();
        prep.prepare(desc.getModelType(), desc, uri("default:source"));
        assertThat(desc.lines, is(Arrays.asList("MOCK")));
    }

    /**
     * invalid data model type.
     * @throws Exception if failed
     */
    @Test(expected = IOException.class)
    public void unknown_type() throws Exception {
        TestDataPreparator prep = new TestDataPreparator(
                new MockDataModelAdapter(Integer.class),
                new MockSourceProvider()
                    .add(uri("testing:src"), "Hello, world!"),
                new MockImporterPreparator().wrap(),
                new MockExporterRetriever().wrap());
        Desc desc = MockImporterPreparator.create();
        prep.prepare(desc.getModelType(), desc, uri("testing:src"));
    }

    /**
     * invalid source.
     * @throws Exception if failed
     */
    @Test(expected = IOException.class)
    public void unknown_source() throws Exception {
        TestDataPreparator prep = new TestDataPreparator(
                new MockDataModelAdapter(String.class),
                new MockSourceProvider()
                    .add(uri("testing:src"), "Hello, world!"),
                new MockImporterPreparator().wrap(),
                new MockExporterRetriever().wrap());
        Desc desc = MockImporterPreparator.create();
        prep.prepare(desc.getModelType(), desc, uri("unknown:src"));
    }

    private URI uri(String str) {
        try {
            return new URI(str);
        } catch (URISyntaxException e) {
            throw new AssertionError(e);
        }
    }
}
