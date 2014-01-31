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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.asakusafw.testdriver.core.DataModelReflection;
import com.asakusafw.testdriver.core.DataModelSink;

/**
 * Mock implementation of {@link DataModelSink}.
 */
public class MockDataModelSink implements DataModelSink {

    private static final MockTextDefinition DEF = new MockTextDefinition();

    private final List<String> buffer = new ArrayList<String>();

    /**
     * Returns the buffer of this sink.
     * @return the buffer
     */
    public List<String> getBuffer() {
        return buffer;
    }

    @Override
    public void put(DataModelReflection model) throws IOException {
        buffer.add(DEF.toObject(model).toString());
    }

    @Override
    public void close() throws IOException {
        return;
    }
}
