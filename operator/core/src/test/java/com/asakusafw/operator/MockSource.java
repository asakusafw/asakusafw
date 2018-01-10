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
package com.asakusafw.operator;

import com.asakusafw.vocabulary.flow.Source;
import com.asakusafw.vocabulary.flow.graph.FlowElementOutput;
import com.asakusafw.vocabulary.flow.util.CoreOperators;

/**
 * Mock {@link Source}.
 * @param <T> channel type
 */
public class MockSource<T> implements Source<T> {

    private final Source<T> source;

    private MockSource(Class<T> type) {
        source = CoreOperators.empty(type);
    }

    /**
     * Creates a new source object.
     * @param <T> channel type
     * @param type channel type
     * @return created object
     */
    public static <T> MockSource<T> of(Class<T> type) {
        return new MockSource<>(type);
    }

    @Override
    public FlowElementOutput toOutputPort() {
        return source.toOutputPort();
    }
}
