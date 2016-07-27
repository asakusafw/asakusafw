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
package com.asakusafw.testdriver.testing.dsl;

import com.asakusafw.testdriver.testing.model.Simple;
import com.asakusafw.vocabulary.flow.Export;
import com.asakusafw.vocabulary.flow.FlowDescription;
import com.asakusafw.vocabulary.flow.Import;
import com.asakusafw.vocabulary.flow.In;
import com.asakusafw.vocabulary.flow.JobFlow;
import com.asakusafw.vocabulary.flow.Out;

/**
 * A simple jobflow.
 * @since 0.2.0
 */
@JobFlow(name = "simple")
public class SimpleJobflow extends FlowDescription {

    In<Simple> in;

    Out<Simple> out;

    /**
     * Creates a new instance.
     * @param in source
     * @param out sink
     */
    public SimpleJobflow(
            @Import(name = "simple", description = SimpleImporter.class)
            In<Simple> in,
            @Export(name = "simple", description = SimpleExporter.class)
            Out<Simple> out) {
        this.in = in;
        this.out = out;
    }

    @Override
    protected void describe() {
        throw new AssertionError();
    }
}
