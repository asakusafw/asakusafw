/**
 * Copyright 2011-2015 Asakusa Framework Team.
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
package com.asakusafw.runtime.io.testing.io;

import java.io.IOException;

import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.runtime.io.RecordEmitter;
import com.asakusafw.runtime.io.testing.model.MockModel;

/**
 * A data model output for {@link MockModel}.
 */
public class MockModelModelOutput implements ModelOutput<MockModel> {

    private final RecordEmitter emitter;

    /**
     * Creates a new instance.
     * @param emitter the record emitter
     */
    public MockModelModelOutput(RecordEmitter emitter) {
        this.emitter = emitter;
    }

    @Override
    public void write(MockModel model) throws IOException {
        emitter.emit(model.value);
        emitter.endRecord();
    }

    @Override
    public void close() throws IOException {
        emitter.close();
    }

}
