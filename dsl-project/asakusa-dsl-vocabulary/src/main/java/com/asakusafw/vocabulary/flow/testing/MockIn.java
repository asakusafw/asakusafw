/**
 * Copyright 2011-2017 Asakusa Framework Team.
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
package com.asakusafw.vocabulary.flow.testing;

import com.asakusafw.vocabulary.flow.In;
import com.asakusafw.vocabulary.flow.graph.FlowElement;
import com.asakusafw.vocabulary.flow.graph.FlowElementOutput;
import com.asakusafw.vocabulary.flow.graph.FlowElementResolver;
import com.asakusafw.vocabulary.flow.graph.InputDescription;

/**
 * Mock implementation of {@link In} (for testing).
 * @param <T> the data type
 */
public class MockIn<T> implements In<T> {

    private final FlowElementResolver resolver;

    /**
     * Creates a new instance.
     * @param type the input type
     * @param name the input name
     */
    public MockIn(Class<T> type, String name) {
        InputDescription desc = new InputDescription(name, type);
        resolver = new FlowElementResolver(desc);
    }

    /**
     * Creates a new instance.
     * @param <T> the data type
     * @param type the input type
     * @param name the input name
     * @return the created instance
     */
    public static <T> MockIn<T> of(Class<T> type, String name) {
        return new MockIn<>(type, name);
    }

    @Override
    public FlowElementOutput toOutputPort() {
        return resolver.getOutput(InputDescription.OUTPUT_PORT_NAME);
    }

    /**
     * Returns the {@link FlowElement} representation of this object.
     * @return the {@link FlowElement} representation
     */
    public FlowElement toElement() {
        return resolver.getElement();
    }
}
