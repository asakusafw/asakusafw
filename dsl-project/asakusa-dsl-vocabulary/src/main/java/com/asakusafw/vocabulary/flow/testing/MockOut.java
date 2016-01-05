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
package com.asakusafw.vocabulary.flow.testing;

import com.asakusafw.vocabulary.flow.Out;
import com.asakusafw.vocabulary.flow.Source;
import com.asakusafw.vocabulary.flow.graph.FlowElement;
import com.asakusafw.vocabulary.flow.graph.FlowElementInput;
import com.asakusafw.vocabulary.flow.graph.FlowElementResolver;
import com.asakusafw.vocabulary.flow.graph.OutputDescription;
import com.asakusafw.vocabulary.flow.graph.PortConnection;

/**
 * Mock implementation of {@link Out} (for testing).
 * @param <T> the data type
 */
public class MockOut<T> implements Out<T> {

    private final FlowElementResolver resolver;

    /**
     * Creates a new instance.
     * @param type the output type
     * @param name the output name
     */
    public MockOut(Class<T> type, String name) {
        OutputDescription desc = new OutputDescription(name, type);
        resolver = new FlowElementResolver(desc);
    }

    /**
     * Creates a new instance.
     * @param <T> the data type
     * @param type the output type
     * @param name the output name
     * @return the created instance
     */
    public static <T> MockOut<T> of(Class<T> type, String name) {
        return new MockOut<>(type, name);
    }

    @Override
    public void add(Source<T> source) {
        PortConnection.connect(
                source.toOutputPort(),
                this.toInputPort());
    }

    /**
     * Returns the {@link FlowElement} representation of this object.
     * @return the {@link FlowElement} representation
     */
    public FlowElement toElement() {
        return resolver.getElement();
    }

    @Override
    public FlowElementInput toInputPort() {
        return resolver.getInput(OutputDescription.INPUT_PORT_NAME);
    }
}
