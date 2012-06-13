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
package com.asakusafw.testdriver.json;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

import org.junit.Test;

import com.asakusafw.testdriver.core.DataModelDefinition;
import com.asakusafw.testdriver.core.DataModelSource;
import com.asakusafw.testdriver.core.DataModelSourceProvider;
import com.asakusafw.testdriver.core.SpiDataModelSourceProvider;
import com.asakusafw.testdriver.core.TestContext;
import com.asakusafw.testdriver.model.SimpleDataModelDefinition;

/**
 * Test for {@link JsonSourceProvider}.
 * @since 0.2.0
 */
public class JsonSourceProviderTest {

    static final DataModelDefinition<Simple> SIMPLE = new SimpleDataModelDefinition<Simple>(Simple.class);

    /**
     * simple.
     * @throws Exception if failed
     */
    @Test
    public void simple() throws Exception {
        JsonSourceProvider provider = new JsonSourceProvider();
        DataModelSource source = provider.open(SIMPLE, uri("simple.json"), new TestContext.Empty());
        assertThat(source, not(nullValue()));
        try {
            Simple s1 = SIMPLE.toObject(source.next());
            assertThat(s1.number, is(100));
            assertThat(source.next(), is(nullValue()));
        } finally {
            source.close();
        }
    }

    /**
     * via SPI.
     * @throws Exception if failed
     */
    @Test
    public void spi() throws Exception {
        DataModelSourceProvider provider = new SpiDataModelSourceProvider(JsonSourceProvider.class.getClassLoader());
        DataModelSource source = provider.open(SIMPLE, uri("simple.json"), new TestContext.Empty());
        assertThat(source, not(nullValue()));
        try {
            Simple s1 = SIMPLE.toObject(source.next());
            assertThat(s1.number, is(100));
            assertThat(source.next(), is(nullValue()));
        } finally {
            source.close();
        }
    }

    /**
     * invalid extension.
     * @throws Exception if failed
     */
    @Test
    public void invalid_extension() throws Exception {
        JsonSourceProvider provider = new JsonSourceProvider();
        DataModelSource source = provider.open(SIMPLE, uri("simple.txt"), new TestContext.Empty());
        assertThat(source, is(nullValue()));
    }

    /**
     * not found.
     * @throws Exception if failed
     */
    @Test(expected = IOException.class)
    public void not_found() throws Exception {
        URI uri;
        try {
            File file = File.createTempFile("tmp", ".json");
            file.delete();
            uri = file.toURI();
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        JsonSourceProvider provider = new JsonSourceProvider();
        provider.open(SIMPLE, uri, new TestContext.Empty());
    }

    private URI uri(String name) {
        URL resource = getClass().getResource(name);
        assertThat(name, resource, not(nullValue()));
        try {
            return resource.toURI();
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }
}
