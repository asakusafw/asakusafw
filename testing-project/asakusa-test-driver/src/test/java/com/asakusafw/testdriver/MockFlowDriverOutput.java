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
package com.asakusafw.testdriver;

import org.apache.hadoop.io.Text;

/**
 * Mock implementation of {@link FlowDriverOutput}.
 * @param <T> the data type
 */
public class MockFlowDriverOutput<T> extends FlowDriverOutput<T, MockFlowDriverOutput<T>> {

    /**
     * Creates a new instance.
     * @param callerClass the current context class
     * @param dataType the data type
     * @param testTools the test tools
     */
    public MockFlowDriverOutput(Class<?> callerClass, Class<T> dataType, MockTestDataToolProvider testTools) {
        super(callerClass, testTools, "mock", dataType);
    }

    /**
     * Creates a new instance.
     * @param callerClass the current context class
     * @param testTools the test tools
     * @return the created instance
     */
    public static MockFlowDriverOutput<Text> text(Class<?> callerClass, MockTestDataToolProvider testTools) {
        return new MockFlowDriverOutput<>(callerClass, Text.class, testTools);
    }

    @Override
    protected MockFlowDriverOutput<T> getThis() {
        return this;
    }
}
