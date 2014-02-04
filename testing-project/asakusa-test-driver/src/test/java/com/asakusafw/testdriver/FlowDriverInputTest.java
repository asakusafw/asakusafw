/**
 * Copyright 2011-2014 Asakusa Framework Team.
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

import org.junit.Test;

import com.asakusafw.testdriver.core.DataModelSourceFactory;
import com.asakusafw.utils.io.Provider;

/**
 * Test for {@link FlowDriverInput}.
 */
public class FlowDriverInputTest {

    /**
     * simple test for {@link FlowDriverInput#prepare(java.lang.String)}.
     */
    @Test
    public void prepare_uri() {
        MockFlowDriverInput mock = new MockFlowDriverInput(getClass(), new MockTestDataToolProvider() {
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
        new MockFlowDriverInput(getClass(), new MockTestDataToolProvider()).prepare("data/__MISSING__");
    }

    /**
     * Test method for {@link FlowDriverInput#prepare(DataModelSourceFactory)}.
     */
    @Test
    public void prepare_factory() {
        MockFlowDriverInput mock = new MockFlowDriverInput(getClass(), new MockTestDataToolProvider())
            .prepare(factory("Hello1", "Hello2"));
        verify(mock.getSource(), DEFINITION, list("Hello1", "Hello2"));
    }

    /**
     * Test method for {@link FlowDriverInput#prepare(Iterable)}.
     */
    @Test
    public void prepare_collection() {
        MockFlowDriverInput mock = new MockFlowDriverInput(getClass(), new MockTestDataToolProvider())
            .prepare(list("Hello1", "Hello2"));
        verify(mock.getSource(), DEFINITION, list("Hello1", "Hello2"));
    }

    /**
     * Test method for {@link FlowDriverInput#prepare(Provider)}.
     */
    @Test
    public void prepare_iterator() {
        MockFlowDriverInput mock = new MockFlowDriverInput(getClass(), new MockTestDataToolProvider())
            .prepare(provider("Hello1", "Hello2"));
        verify(mock.getSource(), DEFINITION, list("Hello1", "Hello2"));
    }
}
