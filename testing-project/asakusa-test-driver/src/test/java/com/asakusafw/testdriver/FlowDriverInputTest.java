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
package com.asakusafw.testdriver;

import static com.asakusafw.testdriver.FlowDriverPortTestHelper.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.net.URI;

import org.apache.hadoop.io.Text;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import com.asakusafw.runtime.windows.WindowsSupport;
import com.asakusafw.testdriver.core.DataModelSourceFactory;
import com.asakusafw.testdriver.testing.dsl.SimpleStreamFormat;
import com.asakusafw.testdriver.testing.model.Simple;
import com.asakusafw.utils.io.Provider;

/**
 * Test for {@link FlowDriverInput}.
 */
public class FlowDriverInputTest {

    /**
     * Windows platform support.
     */
    @ClassRule
    public static final WindowsSupport WINDOWS_SUPPORT = new WindowsSupport();

    /**
     * Resets all Hadoop file systems.
     */
    @Rule
    public final FileSystemCleaner fsCleaner = new FileSystemCleaner();

    /**
     * simple test for {@link FlowDriverInput#prepare(java.lang.String)}.
     */
    @Test
    public void prepare_uri() {
        MockFlowDriverInput<?> mock = MockFlowDriverInput.text(getClass(), new MockTestDataToolProvider() {
            @Override
            public DataModelSourceFactory getDataModelSourceFactory(URI uri) {
                assertThat(uri.toString(), endsWith("data/dummy"));
                return factory("Hello1", "Hello2");
            }
        }).prepare("data/dummy");
        verify(mock.getSource(), DEFINITION, list("Hello1", "Hello2"));
    }

    /**
     * missing resource in {@link FlowDriverInput#prepare(java.lang.String)}.
     */
    @Test(expected = IllegalArgumentException.class)
    public void prepare_uri_missing() {
        new MockFlowDriverInput<>(getClass(), Text.class, provider()).prepare("data/__MISSING__");
    }

    /**
     * Test method for {@link FlowDriverInput#prepare(DataModelSourceFactory)}.
     */
    @Test
    public void prepare_factory() {
        MockFlowDriverInput<?> mock = MockFlowDriverInput.text(getClass(), provider())
            .prepare(factory("Hello1", "Hello2"));
        verify(mock.getSource(), DEFINITION, list("Hello1", "Hello2"));
    }

    /**
     * Test method for {@link FlowDriverInput#prepare(Iterable)}.
     */
    @Test
    public void prepare_collection() {
        MockFlowDriverInput<?> mock = MockFlowDriverInput.text(getClass(), provider())
            .prepare(list("Hello1", "Hello2"));
        verify(mock.getSource(), DEFINITION, list("Hello1", "Hello2"));
    }

    /**
     * Test method for {@link FlowDriverInput#prepare(Provider)}.
     */
    @Test
    public void prepare_iterator() {
        MockFlowDriverInput<?> mock = MockFlowDriverInput.text(getClass(), provider())
            .prepare(provider("Hello1", "Hello2"));
        verify(mock.getSource(), DEFINITION, list("Hello1", "Hello2"));
    }

    /**
     * simple test for {@link FlowDriverInput#prepare(Class, String)}.
     */
    @Test
    public void prepare_directio_path() {
        MockFlowDriverInput<?> mock = new MockFlowDriverInput<>(getClass(), Simple.class, provider())
                .prepare(SimpleStreamFormat.class, "directio/simple.txt");
        verify(mock.getSource(), DEFINITION, list("Hello, world!"));
    }

    /**
     * simple test for {@link FlowDriverInput#prepare(Class, java.io.File)}.
     */
    @Test
    public void prepare_directio_file() {
        MockFlowDriverInput<?> mock = new MockFlowDriverInput<>(getClass(), Simple.class, provider())
                .prepare(SimpleStreamFormat.class, asFile("directio/simple.txt"));
        verify(mock.getSource(), DEFINITION, list("Hello, world!"));
    }
}
