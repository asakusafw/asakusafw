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
package com.asakusafw.compiler.fileio.flow;

import static com.asakusafw.vocabulary.flow.util.CoreOperators.*;

import com.asakusafw.compiler.fileio.external.Ex1MockExporterDescription;
import com.asakusafw.compiler.fileio.model.Ex1;
import com.asakusafw.compiler.fileio.model.Ex2;
import com.asakusafw.vocabulary.flow.Export;
import com.asakusafw.vocabulary.flow.FlowDescription;
import com.asakusafw.vocabulary.flow.Import;
import com.asakusafw.vocabulary.flow.In;
import com.asakusafw.vocabulary.flow.JobFlow;
import com.asakusafw.vocabulary.flow.Out;

/**
 * Job with mixed inputs.
 */
@JobFlow(name = "job")
public class MixedInputJob extends FlowDescription {

    private final In<Ex1> input1;

    private final In<Ex2> input2;

    private final Out<Ex1> output;

    /**
     * Creates a new instance.
     * @param input1 an input (1)
     * @param input2 an input (2)
     * @param output an output
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public MixedInputJob(
            @Import(name = "input1", description = NormalImporterDescription.class)
            In<Ex1> input1,
            @Import(name = "input2", description = TinyImporterDescription.class)
            In<Ex2> input2,
            @Export(name = "out", description = Ex1MockExporterDescription.class)
            Out<Ex1> output) {
        this.input1 = input1;
        this.input2 = input2;
        this.output = output;
    }

    @Override
    protected void describe() {
        output.add(input1);
        output.add(restructure(input2, Ex1.class));
    }
}
