/**
 * Copyright 2011-2019 Asakusa Framework Team.
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
package com.asakusafw.testdriver.testing.compiler;

import com.asakusafw.testdriver.compiler.FlowPortMap;
import com.asakusafw.vocabulary.flow.In;
import com.asakusafw.vocabulary.flow.Out;
import com.asakusafw.vocabulary.flow.testing.MockIn;
import com.asakusafw.vocabulary.flow.testing.MockOut;

/**
 * Mock implementation of {@link FlowPortMap}.
 * @since 0.9.0
 */
public class MockFlowPortMap implements FlowPortMap {

    @Override
    public <T> In<T> addInput(String name, Class<T> dataType) {
        return new MockIn<>(dataType, name);
    }

    @Override
    public <T> Out<T> addOutput(String name, Class<T> dataType) {
        return new MockOut<>(dataType, name);
    }
}
